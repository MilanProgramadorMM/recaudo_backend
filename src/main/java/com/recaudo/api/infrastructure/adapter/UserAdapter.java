package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.UserGateway;
import com.recaudo.api.domain.mapper.PersonMapper;
import com.recaudo.api.domain.mapper.UserMapper;
import com.recaudo.api.domain.model.dto.response.RoleDto;
import com.recaudo.api.domain.model.dto.response.RolePermissionDto;
import com.recaudo.api.domain.model.dto.response.UserPermissionDto;
import com.recaudo.api.domain.model.dto.rest_api.PersonRegisterDto;
import com.recaudo.api.domain.model.dto.rest_api.UserDto;
import com.recaudo.api.domain.model.entity.*;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.repository.*;
import lombok.AllArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
        dto.setPersonFullName(
                personRepository.findById(user.getPersonId())
                        .map(PersonEntity::getFullName)
                        .orElse(null)
        );

        // Obtener rol asociado al usuario
        UserRoleEntity userRole = userRoleRepository.findByUserId(user.getId());
        if (userRole != null) {
            roleRepository.findById(userRole.getRoleId()).ifPresent(role -> {
                RoleDto roleDto = RoleDto.builder()
                        .id(role.getId())
                        .name(role.getName())
                        .build();
                dto.setRol(roleDto);
            });
        }

        return dto;
    }


    @Override
    public List<UserDto> getAll() {
        List<UserEntity> entities = userRepository.findByStatusTrue();

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


            // OBTENEMOS EL ROL Y ID ASOCIADO A LA PERSONA
            UserRoleEntity userRole = userRoleRepository.findByUserId(user.getId());
            if (userRole != null) {
                roleRepository.findById(userRole.getRoleId()).ifPresent(role -> {
                    RoleDto roleDto = RoleDto.builder()
                            .id(role.getId())
                            .name(role.getName())
                            .build();
                    dto.setRol(roleDto);
                });
            }

            return dto;
        }).toList();
    }

    @Override
    public UserEntity saveUserToPerson(PersonEntity personEntity) {

        //CREAMOS EL USUARIO ASOCIADO A LA NUEVA PERSONA
        String hashedPassword = passwordEncoder.encode(personEntity.getDocument());
        UserEntity user = UserEntity.builder()
                .username(personEntity.getDocument())
                .password(hashedPassword)
                .personId(personEntity.getId())
                .userCreate(personEntity.getUserCreate())
                .createdAt(LocalDateTime.now())
                .build();
        UserEntity userCreated = userRepository.save(user);

        // Asignamos rol al usuario
        RoleEntity rol = roleRepository.findByName("ASESOR");
        if (rol == null)
            throw new BadRequestException("Rol ASESOR no encontrado");

        UserRoleEntity userRoleEntity = UserRoleEntity.builder()
                .roleId(rol.getId())
                .userId(userCreated.getId())
                .createdAt(LocalDateTime.now())
                .build();
        userRoleRepository.saveAndFlush(userRoleEntity);

        // Obtenemos los permisos del rol
        List<RolePermissionDto> permisosRol = rolePermissionRepository.findPermissionsByRoleId(rol.getId());

        // Mapeamos y guardar permisos para el usuario
        List<UserPermissionEntity> userPermissions = permisosRol.stream()
                .map(permiso -> UserPermissionEntity.builder()
                        .userId(userCreated.getId())
                        .actionId(permiso.getActionId())
                        .moduleId(permiso.getModuleId())
                        .allow(permiso.getPermiso())
                        .createdAt(LocalDateTime.now())
                        .build())
                .toList();

        userPermissionRepository.saveAllAndFlush(userPermissions);

        return userCreated;
    }
}
