package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.ContactInfoGateway;
import com.recaudo.api.domain.mapper.ContactInfoMapper;
import com.recaudo.api.domain.model.dto.response.ContactInfoListDto;
import com.recaudo.api.domain.model.dto.rest_api.ClientDataCreditIntentionUpdateDto;
import com.recaudo.api.domain.model.dto.rest_api.ContactInfoRegisterDto;
import com.recaudo.api.domain.model.dto.rest_api.PersonRegisterDto;
import com.recaudo.api.domain.model.entity.*;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.repository.*;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class ContactInfoAdapter implements ContactInfoGateway {

    @Autowired
    private  ContactInfoRepository contactInfoRepository;

    @Autowired
    private  PersonRepository personRepository;

    @Autowired
    private GlotypesRepository glotypesRepository;

    @Autowired
    private BarrioRepository barrioRepository;

    @Autowired
    private MunicipioRepository municipioRepository;

    @Autowired
    private PaisRepository paisRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;


    @Autowired(required = false)
    ContactInfoMapper contactInfoMapper = Mappers.getMapper(ContactInfoMapper.class);


    @Override
    public List<ContactInfoListDto> getByPerson(Long personId) {
        List<ContactInfoEntity> entities = contactInfoRepository.findByPersonOrderByIdDesc(personId);

        return entities.stream().map(e -> {
            GlotypesEntity tipo = glotypesRepository.findById(e.getType())
                    .orElseThrow(() -> new RuntimeException("Tipo no encontrado"));

            // Obtener nombres desde los repositorios
            String countryName = e.getCountry() != null
                    ? paisRepository.findById(e.getCountry())
                    .map(PaisEntity::getValue)
                    .orElse(null)
                    : null;

            String departmentName = e.getDepartment() != null
                    ? departamentoRepository.findById(e.getDepartment())
                    .map(DepartamentoEntity::getValue)
                    .orElse(null)
                    : null;

            String cityName = e.getCity() != null
                    ? municipioRepository.findById(e.getCity())
                    .map(MunicipioEntity::getValue)
                    .orElse(null)
                    : null;

            String neighborhoodName = e.getNeighborhood() != null
                    ? barrioRepository.findById(e.getNeighborhood())
                    .map(BarrioEntity::getValue)
                    .orElse(null)
                    : null;


            return ContactInfoListDto.builder()
                    .id(e.getId())
                    .typeCode(tipo.getCode())
                    .typeName(tipo.getName())
                    .value(e.getValue())
                    .country(countryName)
                    .department(departmentName)
                    .city(cityName)
                    .neighborhood(neighborhoodName)
                    .description(e.getDescription())
                    .build();
        }).toList();
    }

    @Override
    public ContactInfoRegisterDto update(Long id, ContactInfoRegisterDto dto) {
        ContactInfoEntity existing = contactInfoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("La información de contacto no existe"));

        ContactInfoEntity entityToSave = buildAndValidateContactInfo(dto, existing);
        entityToSave.setId(existing.getId());
        entityToSave.setEditedAt(LocalDateTime.now());
        entityToSave.setUserEdit(getUsernameToken());

        entityToSave = contactInfoRepository.save(entityToSave);
        return contactInfoMapper.entityToDto(entityToSave);
    }

    @Override
    public void delete(Long id) {
        contactInfoRepository.deleteById(id);
    }

    @Override
    public void saveContactInfoClient(PersonRegisterDto dto, Long personId) {
        // Dirección principal
        if (dto.getAdress() != null) {
            saveContact(personId, "DIRPRIN", dto.getAdress(), dto);
        }
        // Teléfono principal
        if (dto.getTelefono() != null && !dto.getTelefono().isBlank()) {
            saveContact(personId, "TELPRIN", dto.getTelefono(), dto);
        }
        // Celular principal
        if (dto.getCelular() != null && !dto.getCelular().isBlank()) {
            saveContact(personId, "CEPRIN", dto.getCelular(), dto);
        }
        // Correo principal
        if (dto.getCorreo() != null && !dto.getCorreo().isBlank()) {
            saveContact(personId, "COPRIN", dto.getCorreo(), dto);
        }
    }

    private void saveContact(Long personId, String code, String value, PersonRegisterDto dto) {
        GlotypesEntity tipo = glotypesRepository.findByKeyAndCode("TIPUBI", code).orElse(null);
        if (tipo == null) {
            throw new BadRequestException("No existe code: " + code);
        }

        boolean exists = contactInfoRepository.existsByPersonAndType(personId, tipo.getId());
        if (exists) {
            throw new BadRequestException("Duplicado");
        }

        ContactInfoEntity.ContactInfoEntityBuilder entityBuilder = ContactInfoEntity.builder()
                .person(personId)
                .type(tipo.getId())
                .value(value.trim())
                .createdAt(LocalDateTime.now())
                .userCreate(getUsernameToken());

        if ("DIRPRIN".equalsIgnoreCase(code)) {
            entityBuilder
                    .country(dto.getCountryId())
                    .department(dto.getDepartentId())
                    .city(dto.getCityId())
                    .neighborhood(dto.getNeighborhoodId())
                    .description(dto.getDetails());
        }

        contactInfoRepository.save(entityBuilder.build());
    }


    @Override
    public ContactInfoRegisterDto save(ContactInfoRegisterDto dto) {
        ContactInfoEntity entityToSave = buildAndValidateContactInfo(dto, null);
        entityToSave.setCreatedAt(LocalDateTime.now());
        entityToSave.setUserCreate(getUsernameToken());

        entityToSave = contactInfoRepository.save(entityToSave);
        return contactInfoMapper.entityToDto(entityToSave);
    }

    private ContactInfoEntity buildAndValidateContactInfo(ContactInfoRegisterDto dto, ContactInfoEntity existing) {
        PersonEntity person = personRepository.findById(dto.getPersonId())
                .orElseThrow(() -> new RuntimeException("La persona no existe"));

        GlotypesEntity tipoHijo = glotypesRepository.findById(dto.getTypeId())
                .orElseThrow(() -> new RuntimeException("El tipo de información no existe"));

        String code = tipoHijo.getCode().toUpperCase();
        String description = tipoHijo.getName().toUpperCase();


        //PARA EXCLUIR O EVADIR CUANDO SE VAYA A EDITAR
        Long excludeId = (existing != null) ? existing.getId() : -1L;

        // DUPLICADO POR PERSONA Y TIPO (excluyendo el actual en caso de update)
        boolean existsSameType = contactInfoRepository.existsByPersonAndTypeAndIdNot(
                person.getId(),
                tipoHijo.getId(),
                excludeId
        );
        if (existsSameType) {
            throw new BadRequestException("Esta persona ya tiene un contacto de tipo " + description);
        }

        // DUPLICADO DE NÚMERO CEL (excluyendo el actual en caso de update)
        if (code.equals("CEL") || code.equals("CEPRIN") ) {
            boolean existsSameNumber = contactInfoRepository.existsByValueAndTypeAndIdNot(
                    dto.getValue(),
                    dto.getTypeId(),
                    excludeId
            );
            if (existsSameNumber) {
                throw new RuntimeException("El número de teléfono ya está registrado");
            }
        }

        // VALIDACIONES ESPECÍFICAS
        ContactInfoEntity entity = (existing != null) ? existing : new ContactInfoEntity();

        switch (code) {
            case "WHA":
            case "TEL":
            case "TELPRIN":
            case "CEL":
            case "CEPRIN":
                if (dto.getValue() == null || dto.getValue().isBlank()) {
                    throw new RuntimeException("El campo 'value' es obligatorio para tipo " + code);
                }
                if (!dto.getValue().matches("\\d+")) {
                    throw new RuntimeException("El campo 'value' para tipo " + code + " solo debe contener números.");
                }
                break;

            case "COR":
            case "COPRIN":
                if (dto.getValue() == null || dto.getValue().isBlank()) {
                    throw new RuntimeException("El campo 'value' es obligatorio para tipo " + code);
                }
                if (!dto.getValue().matches("^[\\w.%+-]+@gmail\\.com$")) {
                    throw new RuntimeException("El campo 'value' para tipo COR debe ser un correo válido con dominio @gmail.com.");
                }
                break;

            case "DIR":
            case "DIRPRIN":
                if (dto.getValue() == null || dto.getValue().isBlank()
                        || dto.getCountry() == null
                        || dto.getCity() == null
                        || dto.getNeighborhood() == null
                        || dto.getDepartment() == null) {
                    throw new RuntimeException("Todos los campos son obligatorios para tipo " + code);
                }
                entity.setCountry(dto.getCountry());
                entity.setDepartment(dto.getDepartment());
                entity.setCity(dto.getCity());
                entity.setNeighborhood(dto.getNeighborhood());
                entity.setDescription(dto.getDescription());
                break;

            default:
                throw new RuntimeException("Tipo no soportado: " + code);
        }

        entity.setPerson(person.getId());
        entity.setType(tipoHijo.getId());
        entity.setValue(dto.getValue());

        return entity;
    }

    @Override
    public void saveOrUpdateContactInfoFromIntention(Long personId, ClientDataCreditIntentionUpdateDto dto) {
        // 1. Dirección Principal (DIRPRIN)
        if (dto.getHomeAddress() != null && !dto.getHomeAddress().isBlank()) {
            upsertContact(personId, "DIRPRIN", dto.getHomeAddress(), dto, true);
        }

        // 2. Teléfono Principal (TELPRIN) - Usando el número de teléfono del DTO
        if (dto.getPhoneNumber() != null && !dto.getPhoneNumber().isBlank()) {
            upsertContact(personId, "TELPRIN", dto.getPhoneNumber(), dto, false);
        }

        // 3. Celular / Whatsapp Principal (CEPRIN o WHA dependiendo de tus códigos de glotypes)
        // Asumiendo que guardas el whatsapp como celular principal o si tienes un código 'WHA'
        if (dto.getWhatsappNumber() != null && !dto.getWhatsappNumber().isBlank()) {
            upsertContact(personId, "CEPRIN", dto.getWhatsappNumber(), dto, false);
        }

        // 4. Correo Principal (COPRIN)
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            upsertContact(personId, "COPRIN", dto.getEmail(), dto, false);
        }
    }

    private void upsertContact(Long personId, String code, String value, ClientDataCreditIntentionUpdateDto dto, boolean isAddress) {
        GlotypesEntity tipo = glotypesRepository.findByKeyAndCode("TIPUBI", code)
                .orElseThrow(() -> new BadRequestException("No existe el código de tipo ubicación: " + code));

        // Buscar si ya existe para actualizar, o crear uno nuevo
        ContactInfoEntity entity = contactInfoRepository.findByPersonAndType(personId, tipo.getId())
                .orElse(new ContactInfoEntity());

        String username = getUsernameToken();

        if (entity.getId() == null) {
            // Es nuevo registro
            entity.setPerson(personId);
            entity.setType(tipo.getId());
            entity.setCreatedAt(LocalDateTime.now());
            entity.setUserCreate(username);
        } else {
            // Es una actualización
            entity.setEditedAt(LocalDateTime.now());
            entity.setUserEdit(username);
        }

        entity.setValue(value.trim());

        // Si es dirección, mapeamos los IDs geográficos correspondientes
        if (isAddress) {
            entity.setCountry(dto.getCountryId());
            entity.setDepartment(dto.getDepartmentId());
            entity.setCity(dto.getMunicipalityId()); // Mapea tu municipalityId al campo city de la entidad
            entity.setNeighborhood(dto.getNeighborhoodId());
            entity.setDescription(dto.getDescription());
        }

        contactInfoRepository.save(entity);
    }


    private String getUsernameToken() {
        return ((UserDetailsImpl) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal())
                .getUsername();
    }
}
