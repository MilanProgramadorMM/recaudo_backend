package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.UserGateway;
import com.recaudo.api.domain.mapper.UserMapper;
import com.recaudo.api.domain.model.dto.response.RoleDto;
import com.recaudo.api.domain.model.dto.response.UserDto;
import com.recaudo.api.domain.model.dto.rest_api.UpdateUserDto;
import com.recaudo.api.domain.model.dto.rest_api.UpdateUserPasswordDto;
import com.recaudo.api.domain.model.dto.rest_api.UserCreateDto;
import com.recaudo.api.domain.model.entity.*;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.repository.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserAdapter implements UserGateway {

    @Autowired
    UserRepository userRepository;

    @Autowired(required = false)
    UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Autowired
    PersonRepository personRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    UserRoleRepository userRoleRepository;

    @Autowired
    UserPermissionRepository userPermissionRepository;

    @Autowired
    RolePermissionRepository rolePermissionRepository;

    private PasswordEncoder passwordEncoder;


    @Override
    public UserDto getById(Long id) {
        Optional<UserEntity> optional = userRepository.findById(id);
        if (optional.isEmpty()) {
            throw new BadRequestException("No existe registro asociado al id " + id);
        }

        UserEntity user = optional.get();
        UserDto dto = new UserDto();

        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setUserCreate(user.getUserCreate());
        dto.setCreatedAt(user.getCreatedAt().toString());

        // Obtener nombre completo de la persona
        if (user.getPersonId() != null) {
            dto.setPersonFullName(
                personRepository.findById(user.getPersonId())
                    .map(PersonEntity::getFullName)
                    .orElse(null)
            );
        } else {            
            dto.setPersonFullName("Usuario sin persona asociada");
        }

        // Obtener rol asociado al usuario
        List<UserRoleEntity> userRole = userRoleRepository.findByUserId(user.getId());
        // OBTENEMOS TODOS LOS ROLES ASOCIADOS AL USUARIO
        List<UserRoleEntity> userRoles = userRoleRepository.findByUserId(user.getId());
        if (userRoles != null && !userRoles.isEmpty()) {
            List<RoleDto> roles = userRoles.stream()
                    .map(ur -> roleRepository.findById(ur.getRoleId())
                            .map(role -> RoleDto.builder()
                                    .id(role.getId())
                                    .name(role.getName())
                                    .description(role.getDescription())
                                    .build()
                            ).orElse(null)
                    )
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            dto.setRol(roles);
        } else {
            dto.setRol(Collections.emptyList());
        }
        return dto;
    }


    @Override
    public List<UserDto> getAll() {
        List<UserEntity> entities = userRepository.findByStatusTrue(Sort.by(Sort.Direction.DESC, "id"));

        return entities.stream().map(user -> {
            UserDto dto = new UserDto();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setUserCreate(user.getUserCreate());
            dto.setCreatedAt(user.getCreatedAt().toString());

            // OBTENEMOS EL NOMBRE COMPLETO DE LA PERSONA
            if (user.getPersonId() != null)
                dto.setPersonFullName(
                        personRepository.findById(user.getPersonId())
                                .map(PersonEntity::getFullName)
                                .orElse(null)
                );
            else
                dto.setPersonFullName("Usuario sin persona asociada");

            // OBTENEMOS TODOS LOS ROLES ASOCIADOS AL USUARIO
            List<UserRoleEntity> userRoles = userRoleRepository.findByUserId(user.getId());
            if (userRoles != null && !userRoles.isEmpty()) {
                List<RoleDto> roles = userRoles.stream()
                        .map(ur -> roleRepository.findById(ur.getRoleId())
                                .map(role -> RoleDto.builder()
                                        .id(role.getId())
                                        .name(role.getName())
                                        .description(role.getDescription())
                                        .build()
                                ).orElse(null)
                        )
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

                dto.setRol(roles);
            } else {
                dto.setRol(Collections.emptyList());
            }

            return dto;
        }).toList();
    }


    @Override
    public UserEntity saveUserToPerson(PersonEntity personEntity) {

        //CREAMOS EL USUARIO ASOCIADO A LA NUEVA PERSONA
        String hashedPassword = passwordEncoder.encode(personEntity.getDocument());
        String username = asignUsername(personEntity.getFirstName(), personEntity.getLastName());

        UserEntity user = UserEntity.builder()
                .username(username)
                .password(hashedPassword)
                .personId(personEntity.getId())
                .userCreate(getUsernameToken())
                .createdAt(LocalDateTime.now())
                .build();
        UserEntity userCreated = userRepository.save(user);

        // Asignamos rol al usuario
        RoleEntity rol = roleRepository.findByName("Asesor");
        if (rol == null)
            throw new BadRequestException("Rol asesor no encontrado");

        UserRoleEntity userRoleEntity = UserRoleEntity.builder()
                .roleId(rol.getId())
                .userId(userCreated.getId())
                .createdAt(LocalDateTime.now())
                .build();
        userRoleRepository.saveAndFlush(userRoleEntity);

        return userCreated;
    }

    @Override
    @Transactional
    public UserDto saveUser(UserCreateDto dto) {

        Optional<UserEntity> existUsername = userRepository.findByUsername(dto.getUsername());
        if (existUsername.isPresent())
            throw new BadRequestException("Ya existe este username");

        String hashedPassword = passwordEncoder.encode(dto.getPassword());

        // Guardar el usuario sin roles primero (para tener el ID)
        UserEntity user = new UserEntity();
        user.setUsername(dto.getUsername().toUpperCase());
        user.setPassword(hashedPassword);
        user.setUserCreate(getUsernameToken());
        user.setCreatedAt(LocalDateTime.now());
        UserEntity savedUser = userRepository.saveAndFlush(user);

        assignRolesToUser(savedUser.getId(), dto.getRoles());

        return userMapper.entityToDto(savedUser);
    }

    public String asignUsername(String primerNombre, String primerApellido) {
        primerNombre = primerNombre.trim().toUpperCase();
        primerApellido = primerApellido.trim().toUpperCase();

        int letras = 1;
        String username;

        do {
            // Tomar las primeras 'letras' del nombre + apellido
            String prefijo = primerNombre.substring(0, Math.min(letras, primerNombre.length()));
            username = prefijo + primerApellido;

            // Si no existe en la base de datos, lo retornamos
            if (!userRepository.findByUsername(username).isPresent()) {
                return username;
            }

            letras++;
        } while (letras <= primerNombre.length());

        // En caso extremo, añadir un sufijo numérico
        int contador = 1;
        String base = primerNombre + primerApellido;
        username = base;
        while (userRepository.findByUsername(username).isPresent()) {
            username = base + contador;
            contador++;
        }

        return username;
    }

    void assignRolesToUser(Long userId, List<Long> roles) {
        roles.forEach(rol -> {
                if (roleRepository.existsById(rol)) {
                    userRoleRepository.saveAndFlush(
                        UserRoleEntity.builder()
                            .userId(userId)
                            .roleId(rol)
                            .createdAt(LocalDateTime.now())
                            .build()
                    );
                }
            }
        );
    }

    private String getUsernameToken() {
        return ((UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();
    }

    private Long getUserIdToken() {
        return ((UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getId();
    }


    @Override
    @Transactional
    public void updateUsername(UpdateUserDto userDto) {
        Optional<UserEntity> optionalUser = userRepository.findById(userDto.getUserId());
        if (optionalUser.isEmpty())
            throw new BadRequestException("Usuario no encontrado con ID: " + userDto.getUserId());

        // Validar si el nuevo username ya existe
        Optional<UserEntity> exist = userRepository.findByUsername(userDto.getValue());
        if (exist.isPresent())
            throw new BadRequestException("El username ya está en uso");

        UserEntity user = optionalUser.get();
        user.setUsername(userDto.getValue());
        user.setEditedAt(LocalDateTime.now());

        userRepository.saveAndFlush(user);
    }

    @Override
    @Transactional
    public void updatePassword(UpdateUserPasswordDto userDto) {
        Optional<UserEntity> optionalUser = userRepository.findById(getUserIdToken());

        if (optionalUser.isEmpty())
            throw new BadRequestException("Usuario no encontrado con ID: " + getUserIdToken());

        UserEntity user = optionalUser.get();

        if (!passwordEncoder.matches(userDto.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("La contraseña actual es incorrecta");
        }

        // Validar nueva contraseña
        String password = userDto.getNewPassword();
        if (password == null || !password.matches("^(?=.*[A-Za-z])(?=.*\\d).+$")) {
            throw new BadRequestException("La contraseña debe contener letras y números");
        }

        // Guardar nueva contraseña
        String hashedPassword = passwordEncoder.encode(password);
        user.setPassword(hashedPassword);
        user.setPasswordChange(LocalDateTime.now());
        user.setEditedAt(LocalDateTime.now());

        userRepository.saveAndFlush(user);
    }

    @Transactional
    @Override
    public void deleteUser(Long userId) {
        Optional<UserEntity> optionalUser = userRepository.findById(userId);
        if (optionalUser.isEmpty())
            throw new BadRequestException("Usuario no encontrado con ID: " + userId);

        // Validar si el usuario a eliminar tiene una persona asociada
        if(optionalUser.get().getPersonId() != null)
            throw new BadRequestException("El usuario tiene una persona asociada, elimine primero a la persona");


        UserEntity user = optionalUser.get();
        user.setUserDelete(getUsernameToken());
        user.setDeletedAt(LocalDateTime.now());
        user.setStatus(false);
        userRepository.saveAndFlush(user);
    }

    @Override
    @Transactional
    public void reactivateUserFromPerson(PersonEntity personEntity) {
        Optional<UserEntity> optionalUser = userRepository.findByPersonId(personEntity.getId());

        if (optionalUser.isEmpty()) {
            throw new BadRequestException("No se encontró un usuario asociado a esta persona");
        }

        UserEntity user = optionalUser.get();

        // Reactivar usuario
        user.setStatus(true);
        user.setDeletedAt(null);
        user.setEditedAt(LocalDateTime.now());

        userRepository.saveAndFlush(user);
    }

    @Override
    public void inactivateUserByPersonId(Long personId) {
        UserEntity user = userRepository.findByPersonId(personId).orElse(null);
        if (user != null) {
            user.setStatus(false);
            user.setDeletedAt(LocalDateTime.now());
            user.setUserDelete(getUsernameToken());
            userRepository.save(user);
        }
    }


}
