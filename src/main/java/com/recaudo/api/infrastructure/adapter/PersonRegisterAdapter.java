package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.PersonGateway;
import com.recaudo.api.domain.gateway.UserGateway;
import com.recaudo.api.domain.mapper.PersonMapper;
import com.recaudo.api.domain.model.dto.rest_api.PersonRegisterDto;
import com.recaudo.api.domain.model.entity.PersonEntity;
import com.recaudo.api.domain.model.entity.RoleEntity;
import com.recaudo.api.domain.model.entity.UserEntity;
import com.recaudo.api.domain.model.entity.UserRoleEntity;
import com.recaudo.api.domain.usecase.RegisterUseCase;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.repository.PersonRepository;
import com.recaudo.api.infrastructure.repository.RoleRepository;
import com.recaudo.api.infrastructure.repository.UserRepository;
import com.recaudo.api.infrastructure.repository.UserRoleRepository;
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
public class PersonRegisterAdapter implements PersonGateway {

    PersonRepository personRepository;
    UserRepository userRepository;

    RoleRepository roleRepository;

    UserRoleRepository userRoleRepository;
    UserGateway userGateway;


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
        List<PersonEntity> entities = personRepository.findByStatusTrue();
        return entities.stream()
                .map(personMapper::entityToDto)
                .toList();
    }


    @Override
    public PersonRegisterDto save(PersonRegisterDto person) {

        //VALIDAMOS EXISTENCIA DE NUMERO DE DOCUMENTO EN LA BD
        Optional<PersonEntity> document = personRepository.findByDocument(person.getDocument());
        if(document.isPresent())
            throw new BadRequestException("Ya existe este documento de Identificacion");

        PersonEntity personEntity = personMapper.dtoToEntity(person);
        personEntity = personRepository.save(personEntity);
        userGateway.saveUserToPerson(personEntity);
        return personMapper.entityToDto(personEntity);
    }

    @Override
    public PersonRegisterDto edit(PersonRegisterDto dto) {
        PersonEntity entity = personMapper.dtoToEntity(dto);
        if (entity.getId() != null && personRepository.existsById(entity.getId())) {
            entity.setEditedAt(LocalDateTime.now());
            entity.setUserEdit(dto.getUserEdit());
        }
        return personMapper.entityToDto(personRepository.save(entity));
    }

    @Override
    public void delete(Long id, String userDelete) {

        //CONSULTAMOS SI EXISTE LA PERSONA Y EL USUARIO DE ESA PERSONA
        Optional<PersonEntity> optionalPerson = personRepository.findById(id);
        Optional<UserEntity> optionalUser = userRepository.findByPersonId(id);

        //VALIDAMOS QUE EXISTAN AMBOS REGISTROS
        if (optionalPerson.isPresent() && optionalUser.isPresent()) {
            PersonEntity person = optionalPerson.get();
            person.setStatus(false);
            person.setDeletedAt(LocalDateTime.now());
            person.setUserDelete(userDelete);
            personRepository.save(person);

                UserEntity user = optionalUser.get();
                user.setStatus(false);
                user.setDeletedAt(LocalDateTime.now());
                user.setUserDelete(userDelete);
                userRepository.save(user);
            }else {
            throw new BadRequestException("No se pudo eliminar a esta persona");
        }
        }

}
