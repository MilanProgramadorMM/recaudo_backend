package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.ContactInfoGateway;
import com.recaudo.api.domain.gateway.PersonGateway;
import com.recaudo.api.domain.gateway.PersonZonaGateway;
import com.recaudo.api.domain.gateway.UserGateway;
import com.recaudo.api.domain.mapper.PersonMapper;
import com.recaudo.api.domain.model.dto.response.PersonInterfaceForRecaudoResponseDto;
import com.recaudo.api.domain.model.dto.response.PersonInterfaceResponseDto;
import com.recaudo.api.domain.model.dto.response.PersonResponseDto;
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
import org.springframework.security.core.context.SecurityContextHolder;
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

    TypePersonRepository typePersonRepository;

    PersonZonaRepository personZonaRepository;

    ZonaRepository zonaRepository;

    ContactInfoGateway contactInfoGateway;
    PersonZonaGateway personZonaGateway;

    PaisRepository paisRepository;
    MunicipioRepository municipioRepository;
    BarrioRepository barrioRepository;
    DepartamentoRepository departamentoRepository;


    @Autowired(required = false)
    PersonMapper personMapper = Mappers.getMapper(PersonMapper.class);

    private RegisterUseCase registerUseCase;


    @Override
    public PersonEntity getByUserName(String username) {
        return null;
    }

    @Override
    public PersonResponseDto getById(Long id){
        Optional<PersonEntity> optional = personRepository.findById(id);
        if (optional.isEmpty()) {
            throw new BadRequestException("No existe registro asociado al id " + id);
        }
        PersonEntity entity = optional.get();
        return personMapper.entityToDto(entity);
    }

    @Override
    public List<PersonInterfaceResponseDto> getAll() {
        return personRepository.getAllPerson();
    }

    @Override
    public List<PersonInterfaceResponseDto> getByType(String type) {
        return personRepository.getByTypePerson(type);
    }

    @Override
    public PersonInterfaceResponseDto getByDocument(String document) {
        return personRepository.getByDocument(document);
         }

    @Override
    public List<PersonInterfaceForRecaudoResponseDto> getByZonaforRecaudo(String type, String zona) {
        return personRepository.getByZonaforRecaudo(type, zona);
    }

    @Override
    public List<PersonInterfaceResponseDto> getByZona(String type, String zona) {
        return personRepository.getByZona(type, zona);
    }

    @Override
    public List<String> getZonaByAsesor(Long asesorId) {

        return userRepository.findById(asesorId)
                .map(user -> personRepository.getZonasByAsesor(user.getPersonId()))
                .orElseThrow(() ->
                        new RuntimeException("No existe un asesor con id: " + asesorId)
                );
    }


    @Override
    public PersonResponseDto save(PersonRegisterDto person) {
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

        normalizePersonDto(person);

        String uniqueCode = setUniqueCode(person);

        PersonEntity personEntity = personMapper.dtoToEntity(person);
        personEntity.setUniqueCode(uniqueCode);
        personEntity.setUserCreate(getUsernameToken());
        TypePersonEntity typeEntity = typePersonRepository.findByValue(person.getTypePerson())
                .orElseThrow(() -> new RuntimeException("Tipo de persona no encontrado"));
        personEntity.setTypePersonId(typeEntity.getId());

        personEntity = personRepository.save(personEntity);

        // Solo si es ASESOR se crea usuario
        if ("ASESOR".equalsIgnoreCase(person.getTypePerson())) {
            userGateway.saveUserToPerson(personEntity);

            // Asignar múltiples zonas al asesor
            if (person.getZonas() != null && !person.getZonas().isEmpty()) {
                personZonaGateway.assignZonasToAsesor(personEntity.getId(), person.getZonas());
            } else {
                throw new BadRequestException("Debe asignar al menos una zona al asesor");
            }
        }

        // Si es CLIENTE, asignar una sola zona con orden
        if ("CLIENTE".equalsIgnoreCase(person.getTypePerson())) {
            if (person.getZona() == null) {
                throw new BadRequestException("Debe asignar una zona al cliente");
            }

            Long zonaId = person.getZona();

            // Obtenemos los clientes actuales de la zona
            List<PersonZonaEntity> clientesExistentes = personZonaRepository
                    .findAllByZonaIdOrderByOrdenAsc(zonaId);

            int nuevoOrden;

            if (clientesExistentes.isEmpty()) {
                nuevoOrden = 1; // Primer cliente en la zona
            } else {
                // Buscamos el último orden y sumamos 1
                // Usamos el último elemento de la lista ya que viene ordenada por el repositorio
                long ultimoOrden = clientesExistentes.get(clientesExistentes.size() - 1).getOrden();
                nuevoOrden = (int) ultimoOrden + 1;
            }

            // Pasamos el nuevoOrden calculado
            saveToPersonZona(personEntity.getId(), zonaId, nuevoOrden);
        }

        // Guardar información de contacto
        contactInfoGateway.saveContactInfoClient(person, personEntity.getId());

        return personMapper.entityToDto(personEntity);
    }

    private String setUniqueCode(PersonRegisterDto person) {
        //Obtener el documento
        String document = (person.getDocument() != null) ? person.getDocument().trim() : "";

        // Obtener las 2 primeras letras del primer nombre
        String firstPart = "";
        if (person.getFirstName() != null && person.getFirstName().length() >= 2) {
            firstPart = person.getFirstName().substring(0, 2);
        } else if (person.getFirstName() != null) {
            firstPart = person.getFirstName();
        }

        // Obtener las 2 primeras letras del segundo apellido
        String secondPart = "";
        if (person.getLastName() != null && person.getLastName().length() >= 2) {
            secondPart = person.getLastName().substring(0, 2);
        } else if (person.getLastName() != null) {
            secondPart = person.getLastName();
        }

        // Concatenar y convertir a mayúsculas
        return (document + firstPart + secondPart).toUpperCase();
    }


    private void saveToPersonZona(Long id, Long zonaId, int orden){
        PersonZonaEntity personZona = PersonZonaEntity.builder()
                .personId(id)
                .zonaId(zonaId)
                .orden(orden)
                .status(true)
                .createdAt(LocalDateTime.now())
                .build();

        personZonaRepository.save(personZona);
    }

    @Override
    @Transactional
    public PersonResponseDto reactivate(Long id) {
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
    public PersonResponseDto edit(PersonRegisterDto dto) {

        PersonInterfaceResponseDto personByDocument =
                personRepository.getByDocument(dto.getDocument());

        if (personByDocument == null) {
            throw new BadRequestException("No existe una persona con el documento " + dto.getDocument());
        }

        Long personId = personByDocument.getId();

        PersonEntity entity = personRepository.findById(personId)
                .orElseThrow(() -> new BadRequestException("La persona a actualizar no existe"));

        String uniqueCode = setUniqueCode(dto);
        entity = personMapper.dtoToEntity(dto);
        entity.setId(personId);


        TypePersonEntity typeEntity = typePersonRepository
                .findByValue(dto.getTypePerson())
                .orElseThrow(() -> new BadRequestException("Tipo de persona no encontrado"));
        entity.setEditedAt(LocalDateTime.now());
        entity.setUniqueCode(uniqueCode);
        entity.setUserEdit(getUsernameToken());
        entity.setTypePersonId(typeEntity.getId());


        // Actualizar zonas según el tipo de persona
        if ("ASESOR".equalsIgnoreCase(dto.getTypePerson())) {
            // Para asesores: actualizar múltiples zonas
            if (dto.getZonas() != null && !dto.getZonas().isEmpty()) {
                personZonaGateway.assignZonasToAsesor(personId, dto.getZonas());
            } else {
                throw new BadRequestException("Debe asignar al menos una zona al asesor");
            }
        } else if ("CLIENTE".equalsIgnoreCase(dto.getTypePerson())) {
            // Para clientes: actualizar una sola zona
            if (dto.getZona() == null) {
                throw new BadRequestException("Debe asignar una zona al cliente");
            }
            personZonaGateway.updateClientToZone(personId, dto.getZona());
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

    @Override
    @Transactional
    public PersonResponseDto toggleStatus(Long id, boolean status) {
        PersonEntity person = personRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Persona no encontrada"));



        person.setStatus(status);
        person.setEditedAt(LocalDateTime.now());
        person.setUserEdit(getUsernameToken());

        // Obtener tipo de persona
        TypePersonEntity typeEntity = typePersonRepository.findById(person.getTypePersonId())
                .orElseThrow(() -> new BadRequestException("Tipo de persona no encontrado"));

        if ("ASESOR".equalsIgnoreCase(typeEntity.getValue())) {
            if (!status) {
                // Inactivar asesor y su usuario
                person.setDeletedAt(LocalDateTime.now());
                userGateway.inactivateUserByPersonId(id);
            } else {
                // Reactivar asesor y usuario
                person.setDeletedAt(null);
                userGateway.reactivateUserFromPerson(person);
            }
        } else if ("CLIENTE".equalsIgnoreCase(typeEntity.getValue())) {
            // Para cliente solo se actualiza el estado y las fechas
            if (!status) {
                person.setDeletedAt(LocalDateTime.now());
            } else {
                person.setDeletedAt(null);
            }
        }

        PersonEntity updated = personRepository.saveAndFlush(person);
        return personMapper.entityToDto(updated);
    }


    private String getUsernameToken() {
        return ((UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();
    }


    private void normalizePersonDto(PersonRegisterDto dto) {
        if (dto.getDocument() != null) dto.setDocument(dto.getDocument().trim().toUpperCase());
        if (dto.getFirstName() != null) dto.setFirstName(dto.getFirstName().trim().toUpperCase());
        if (dto.getMiddleName() != null) dto.setMiddleName(dto.getMiddleName().trim().toUpperCase());
        if (dto.getLastName() != null) dto.setLastName(dto.getLastName().trim().toUpperCase());
        if (dto.getMaternalLastname() != null) dto.setMaternalLastname(dto.getMaternalLastname().trim().toUpperCase());
        if (dto.getFullName() != null) dto.setFullName(dto.getFullName().trim().toUpperCase());
        if (dto.getOccupation() != null) dto.setOccupation(dto.getOccupation().trim().toUpperCase());
        if (dto.getDescription() != null) dto.setDescription(dto.getDescription().trim().toUpperCase());
        if (dto.getTypePerson() != null) dto.setTypePerson(dto.getTypePerson().trim().toUpperCase());
        if (dto.getAdress() != null) dto.setAdress(dto.getAdress().trim().toUpperCase());
        if (dto.getDetails() != null) dto.setDetails(dto.getDetails().trim().toUpperCase());

        if (dto.getCorreo() != null) dto.setCorreo(dto.getCorreo().trim().toLowerCase());
        if (dto.getCelular() != null) dto.setCelular(dto.getCelular().trim());
        if (dto.getTelefono() != null) dto.setTelefono(dto.getTelefono().trim());
    }


}
