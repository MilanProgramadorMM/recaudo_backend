package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.PersonGateway;
import com.recaudo.api.domain.gateway.UserGateway;
import com.recaudo.api.domain.mapper.PersonMapper;
import com.recaudo.api.domain.model.dto.rest_api.PersonRegisterDto;
import com.recaudo.api.domain.model.entity.*;
import com.recaudo.api.domain.usecase.RegisterUseCase;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.exception.InactivePersonException;
import com.recaudo.api.infrastructure.repository.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.webjars.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PersonRegisterAdapter implements PersonGateway {

    PersonRepository personRepository;
    UserRepository userRepository;

    RoleRepository roleRepository;

    UserRoleRepository userRoleRepository;
    UserGateway userGateway;

    TypePersonRepository typePersonRepository;


    @Autowired(required = false)
    PersonMapper personMapper = Mappers.getMapper(PersonMapper.class);

    private RegisterUseCase registerUseCase;


    @Override
    public PersonEntity getByUserName(String username) {
        return null;
    }

    @Override
    public PersonRegisterDto getById(Long id){
        Optional<PersonEntity> optional = personRepository.findById(id);
        if (optional.isEmpty()) {
            throw new BadRequestException("No existe registro asociado al id " + id);
        }
        PersonEntity entity = optional.get();
        return personMapper.entityToDto(entity);
    }

    @Override
    public List<PersonRegisterDto> getAll() {
        List<PersonEntity> entities = personRepository.findByStatusTrue(Sort.by(Sort.Direction.DESC, "id"));
        return entities.stream()
                .map(personMapper::entityToDto)
                .toList();
    }

    @Override
    public List<PersonRegisterDto> getByType(String type) {
        TypePersonEntity typePerson = typePersonRepository.findByValue(type)
                .orElseThrow(() -> new RuntimeException("Tipo de persona no encontrado: " + type));
        Long typePersonId = typePerson.getId();

        List<PersonEntity> entities = personRepository.findByTypePersonIdAndStatusTrue(typePersonId);

        return entities.stream()
                .map(personMapper::entityToDto)
                .toList();
    }

    @Override
    public PersonRegisterDto save(PersonRegisterDto person) {
        Optional<PersonEntity> documentOpt = personRepository.findByDocument(person.getDocument());

        if (documentOpt.isPresent()) {
            PersonEntity existing = documentOpt.get();

            if (existing.isStatus()) {
                // Ya existe y está activa
                throw new BadRequestException("Ya existe este documento de Identificación");
            } else {
                // Existe pero está inactiva
                throw new InactivePersonException(
                        "La persona con este documento está inactiva",
                        existing.getId()
                );
            }
        }


        PersonEntity personEntity = personMapper.dtoToEntity(person);
        personEntity.setUserCreate(getUsernameToken());
        TypePersonEntity typeEntity = typePersonRepository.findByValue(person.getTypePerson())
                .orElseThrow(() -> new RuntimeException("Tipo de persona no encontrado"));
        personEntity.setTypePersonId(typeEntity.getId());

        personEntity = personRepository.save(personEntity);

        // Solo si es ASESOR se crea usuario
        if ("ASESOR".equalsIgnoreCase(person.getTypePerson())) {
            userGateway.saveUserToPerson(personEntity);
        }

        return personMapper.entityToDto(personEntity);
    }

    @Override
    @Transactional
    public PersonRegisterDto reactivate(Long id) {
        PersonEntity person = personRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Persona no encontrada"));

        person.setStatus(true);
        person.setDeletedAt(null);
        person.setEditedAt(LocalDateTime.now());
        person.setUserEdit(getUsernameToken());

        // Reactivar usuario asociado
        userGateway.reactivateUserFromPerson(person);

        PersonEntity updated = personRepository.saveAndFlush(person);
        return personMapper.entityToDto(updated);
    }


    @Override
    public PersonRegisterDto edit(PersonRegisterDto dto) {
        PersonEntity entity = personMapper.dtoToEntity(dto);

        if (entity.getId() != null && personRepository.existsById(entity.getId())) {
            TypePersonEntity typeEntity = typePersonRepository.findByValue(dto.getTypePerson())
                    .orElseThrow(() -> new BadRequestException("Tipo de persona no encontrado"));
            entity.setEditedAt(LocalDateTime.now());
            entity.setUserEdit(getUsernameToken());
            entity.setTypePersonId(typeEntity.getId());

        }
        return personMapper.entityToDto(personRepository.save(entity));
    }

    @Override
    public void delete(Long id) {
        PersonEntity person = personRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Persona no encontrada con id " + id));

        TypePersonEntity typeEntity = typePersonRepository.findById(person.getTypePersonId())
                .orElseThrow(() -> new BadRequestException("Tipo de persona no encontrado"));

        person.setStatus(false);
        person.setDeletedAt(LocalDateTime.now());
        person.setUserDelete(getUsernameToken());
        personRepository.save(person);

        // 4. Si es ASESOR, también inactivar usuario
        if ("ASESOR".equalsIgnoreCase(typeEntity.getValue())) {
            userGateway.inactivateUserByPersonId(id);

        }
    }

    private String getUsernameToken() {
        return ((UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();
    }

}
