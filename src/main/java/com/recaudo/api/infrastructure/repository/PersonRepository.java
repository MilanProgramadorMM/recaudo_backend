package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.dto.response.PersonInterfaceResponseDto;
import com.recaudo.api.domain.model.dto.response.PersonResponseDto;
import com.recaudo.api.domain.model.entity.PersonEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface PersonRepository extends JpaRepository<PersonEntity, Long> {

    List<PersonEntity> findByStatusTrue(Sort id);
    Optional<PersonEntity> findByDocument(String document);
    List<PersonEntity> findByTypePersonIdAndStatusTrue(Long typePersonId);
    List<PersonEntity> findByTypePersonId(Long typePersonId, Sort id);


    @Query(value = """
        SELECT 
            p.id AS id,
            p.document_type AS documentType,
            p.document AS document,
            p.firstname AS firstName,
            p.middlename AS middleName,
            p.lastname AS lastName,
            p.maternal_lastname AS maternalLastname,
            p.fullname AS fullName,
            p.gender AS gender,
            p.occupation AS occupation,
            p.description AS description,
            p.status AS status,
            pz.orden AS orden,
            z.id AS zonaId,
            z.value AS zona,
            ci_cel.value AS celular,
            ci_cor.value AS correo,
            ci_tel.value AS telefono,
            ci_dir.value AS adress,
            pai.id   AS countryId,
            pai.value AS country,
            d.id     AS departentId,
            d.value  AS departent,
            m.id     AS cityId,
            m.value  AS city,
            b.id     AS neighborhoodId,
            b.value  AS neighborhood,
            ci_dir.description AS descriptionD    
        FROM person p
        INNER JOIN type_person tp 
            ON p.type_person_id = tp.id AND tp.value = :type
        LEFT JOIN person_zona pz 
            ON p.id = pz.person_id AND pz.status = true
        LEFT JOIN zona z 
            ON pz.zona_id = z.id
        LEFT JOIN contact_info ci_cel 
            ON ci_cel.person = p.id 
            AND ci_cel.`type` = (SELECT id FROM glotypes WHERE code = 'CEPRIN')
        LEFT JOIN contact_info ci_cor 
            ON ci_cor.person = p.id 
            AND ci_cor.`type` = (SELECT id FROM glotypes WHERE code = 'COPRIN')
        LEFT JOIN contact_info ci_tel 
            ON ci_tel.person = p.id 
            AND ci_tel.`type` = (SELECT id FROM glotypes WHERE code = 'TELPRIN')
        LEFT JOIN contact_info ci_dir 
            ON ci_dir.person = p.id 
            AND ci_dir.`type` = (SELECT id FROM glotypes WHERE code = 'DIRPRIN')
        LEFT JOIN pais pai ON pai.id = ci_dir.country
        LEFT JOIN departamento d ON d.id = ci_dir.department
        LEFT JOIN municipio m ON m.id = ci_dir.city
        LEFT JOIN barrio b ON b.id = ci_dir.neighborhood
        """, nativeQuery = true)
    List<PersonInterfaceResponseDto> getByTypePerson (@Param("type") String type);

    @Query(value = """
            SELECT 
                p.id AS id,
                p.document_type AS documentType,
                p.document AS document,
                p.firstname AS firstName,
                p.middlename AS middleName,
                p.lastname AS lastName,
                p.maternal_lastname AS maternalLastname,
                p.fullname AS fullName,
                p.gender AS gender,
                p.occupation AS occupation,
                p.description AS description,
                p.status AS status,
                pz.orden AS orden,
                z.id AS zonaId,
                z.value AS zona
                    FROM person p
                    INNER JOIN type_person tp
                        ON p.type_person_id = tp.id AND tp.value = :type
                    INNER JOIN person_zona pz
                        ON p.id = pz.person_id
                   INNER JOIN zona z
                        ON pz.zona_id = z.id AND z.value = :zona
                        """, nativeQuery = true)
    List<PersonInterfaceResponseDto> getByZona (@Param("type") String type, @Param("zona") String zona);


    @Query(value = """
        SELECT 
            p.id AS id,
            p.document_type AS documentType,
            p.document AS document,
            p.firstname AS firstName,
            p.middlename AS middleName,
            p.lastname AS lastName,
            p.maternal_lastname AS maternalLastname,
            p.fullname AS fullName,
            p.gender AS gender,
            p.occupation AS occupation,
            p.description AS description,
            p.status AS estado,
            pz.orden AS orden,
            z.value AS zona,
            ci_cel.value AS celular,
            ci_cor.value AS correo,
            ci_tel.value AS telefono,
            ci_dir.value AS adress,
            pai.value AS countryId,
            d.value AS departentId,
            m.value AS cityId,
            b.value AS neighborhoodId
        FROM person p
        INNER JOIN type_person tp ON p.type_person_id = tp.id
        LEFT JOIN person_zona pz ON p.id = pz.person_id
        LEFT JOIN zona z ON pz.zona_id = z.id
        INNER JOIN contact_info ci_cel ON ci_cel.person = p.id 
            AND ci_cel.`type` = (SELECT id FROM glotypes WHERE code = 'CEPRIN')
        INNER JOIN contact_info ci_cor ON ci_cor.person = p.id 
            AND ci_cor.`type` = (SELECT id FROM glotypes WHERE code = 'COPRIN')
        INNER JOIN contact_info ci_tel ON ci_tel.person = p.id 
            AND ci_tel.`type` = (SELECT id FROM glotypes WHERE code = 'TELPRIN')
        INNER JOIN contact_info ci_dir ON ci_dir.person = p.id 
            AND ci_dir.`type` = (SELECT id FROM glotypes WHERE code = 'DIRPRIN')
        INNER JOIN pais pai ON pai.id = ci_dir.country
        INNER JOIN departamento d ON d.id = ci_dir.department
        INNER JOIN municipio m ON m.id = ci_dir.city
        INNER JOIN barrio b ON b.id = ci_dir.neighborhood
        WHERE p.status = true
        ORDER BY p.id DESC
        """, nativeQuery = true)
        List<PersonResponseDto> getAllPerson();


    }

