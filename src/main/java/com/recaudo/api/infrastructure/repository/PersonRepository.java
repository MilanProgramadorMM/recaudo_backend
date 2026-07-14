package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.dto.response.PersonInterfaceForRecaudoResponseDto;
import com.recaudo.api.domain.model.dto.response.PersonInterfaceResponseDto;
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
                	                u.username AS username,
                	                pz.orden AS orden,
                	                ci_cel.value AS celular,
                	                ci_cor.value AS correo,
                	                ci_tel.value AS whatsApp,
                	                ci_dir.value AS adress,
                	                pai.id   AS countryId,
                	                pai.value AS country,
                	                d.id     AS departentId,
                	                d.value  AS departent,
                	                m.id     AS cityId,
                	                m.value  AS city,
                	                b.id     AS neighborhoodId,
                	                b.value  AS neighborhood,
                	                ci_dir.description AS description,
                	                GROUP_CONCAT(DISTINCT z.id ORDER BY z.id SEPARATOR '-') AS zid,
                	                GROUP_CONCAT(DISTINCT z.value ORDER BY z.value SEPARATOR ', ') AS zona
                	            FROM person p
                	            INNER JOIN type_person tp
                	                ON p.type_person_id = tp.id
                	               AND tp.value = :type
                	                    -- .split("-") array donde cada elemento es el ID de una zona
                	            LEFT JOIN user u
                	                ON u.person_id = p.id
                	               AND u.status = true
                	            LEFT JOIN person_zona pz
                	                ON p.id = pz.person_id
                	               AND pz.status = true AND pz.is_asesor = :isAsesor
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
                	               AND ci_tel.`type` = (SELECT id FROM glotypes WHERE code = 'WHA')
                	            LEFT JOIN contact_info ci_dir
                	                ON ci_dir.person = p.id
                	               AND ci_dir.`type` = (SELECT id FROM glotypes WHERE code = 'DIRPRIN')
                	            LEFT JOIN pais pai
                	                ON pai.id = ci_dir.country
                	            LEFT JOIN departamento d
                	                ON d.id = ci_dir.department
                	            LEFT JOIN municipio m
                	                ON m.id = ci_dir.city
                	            LEFT JOIN barrio b
                	                ON b.id = ci_dir.neighborhood
                	            GROUP BY p.id,
                	                p.document_type,
                	                p.document,
                	                p.firstname,
                	                p.middlename,
                	                p.lastname,
                	                p.maternal_lastname,
                	                p.fullname,
                	                p.gender,
                	                p.occupation,
                	                p.description,
                	                p.status,
                	                u.username,
                	                pz.orden,
                	                ci_cel.value,
                	                ci_cor.value,
                	                ci_tel.value,
                	                ci_dir.value,
                	                pai.id,
                	                pai.value,
                	                d.id,
                	                d.value,
                	                m.id,
                	                m.value,
                	                b.id,
                	                b.value,
                	                ci_dir.description
                	            ORDER BY p.id DESC;
        """, nativeQuery = true)
    List<PersonInterfaceResponseDto> getByTypePerson (@Param("type") String type, @Param("isAsesor") Boolean isAsesor);

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
                                        ci_cel.value AS celular,
                                        ci_cor.value AS correo,
                                        ci_tel.value AS whatsApp,
                                        ci_dir.value AS adress,
                                        pai.id   AS countryId,
                                        pai.value AS country,
                                        d.id     AS departentId,
                                        d.value  AS departent,
                                        m.id     AS cityId,
                                        m.value  AS city,
                                        b.id     AS neighborhoodId,
                                        b.value  AS neighborhood,
                                        ci_dir.description AS description,
                                        GROUP_CONCAT(DISTINCT z.id ORDER BY z.id SEPARATOR '-') AS zid,
                                	                GROUP_CONCAT(DISTINCT z.value ORDER BY z.value SEPARATOR ', ') AS zona,
 COALESCE((
        SELECT MAX(DATEDIFF(CURDATE(), a.expiration_date))
        FROM credit c
        JOIN credit_amortization a ON a.credit_id = c.id
        WHERE c.person_id = p.id
          AND c.deleted_at IS NULL
          AND a.paid_full = 'N'
          AND a.expiration_date < CURDATE()
    ), 0) AS diasMora,
    -- Calificación directa desde la tabla parametrizable
    COALESCE((
        SELECT crr.value
        FROM credit_rating_range crr
        WHERE crr.start <= COALESCE((
                SELECT MAX(DATEDIFF(CURDATE(), a.expiration_date))
                FROM credit c
                JOIN credit_amortization a ON a.credit_id = c.id
                WHERE c.person_id = p.id
                  AND c.deleted_at IS NULL
                  AND a.paid_full = 'N'
                  AND a.expiration_date < CURDATE()
            ), 0)
          AND (crr.end IS NULL OR crr.end >= COALESCE((
                SELECT MAX(DATEDIFF(CURDATE(), a.expiration_date))
                FROM credit c
                JOIN credit_amortization a ON a.credit_id = c.id
                WHERE c.person_id = p.id
                  AND c.deleted_at IS NULL
                  AND a.paid_full = 'N'
                  AND a.expiration_date < CURDATE()
            ), 0))
        LIMIT 1
    ), 'N/A') AS ratingCredit
                                    FROM person p
                                    INNER JOIN type_person tp
                                        ON p.type_person_id = tp.id AND p.document = :document
                                    LEFT JOIN person_zona pz
                                        ON p.id = pz.person_id AND pz.status = true AND pz.is_asesor = false
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
    PersonInterfaceResponseDto getByDocument (@Param("document") String document);

    @Query(value = """
             SELECT DISTINCT
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
                            c.id AS creditId,
                            c.quota_value AS creditAmount,
                            c.total_capital_value AS creditBalance,
                            c.total_financed_value AS creditStatus
                        FROM person p
                        INNER JOIN type_person tp
                            ON p.type_person_id = tp.id
                            AND tp.value = :type
                        INNER JOIN person_zona pz
                            ON p.id = pz.person_id
                        INNER JOIN zona z
                            ON pz.zona_id = z.id
                            AND z.value = :zona
                        INNER JOIN credit c
                            ON c.person_id = p.id
                            AND c.deleted_at IS NULL
                        ORDER BY pz.orden ASC;
            """, nativeQuery = true)
    List<PersonInterfaceForRecaudoResponseDto> getByZonaforRecaudo
            (@Param("type") String type, @Param("zona") String zona);

    @Query(value = """
             SELECT DISTINCT
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
                                     INNER JOIN person_zona pz
                                         ON p.id = pz.person_id AND pz.is_asesor = :isAsesor
                                     INNER JOIN zona z
                                         ON pz.zona_id = z.id
                                         AND z.value = :zona
                                     WHERE pz.status = 1    
                                     ORDER BY pz.orden ASC;
            """, nativeQuery = true)
    List<PersonInterfaceResponseDto> getByZona(@Param("type") String type, @Param("zona") String zona, @Param("isAsesor") Boolean isAsesor);

    @Query(value = """
            SELECT z.value
                FROM person p
                INNER JOIN type_person tp ON p.type_person_id = tp.id
                    AND tp.value = 'asesor'
                INNER JOIN person_zona pz ON p.id = pz.person_id AND pz.is_asesor = true
                INNER JOIN zona z ON pz.zona_id = z.id
                WHERE p.id = :asesorId AND pz.status = true
                ORDER BY z.value
        """, nativeQuery = true)
    List<String> getZonasByAsesor(@Param("asesorId") Long asesorId);

    @Query(value = """
            SELECT z.id
                FROM person p
                INNER JOIN type_person tp ON p.type_person_id = tp.id
                    AND tp.value = 'asesor'
                INNER JOIN person_zona pz ON p.id = pz.person_id AND pz.is_asesor = true
                INNER JOIN zona z ON pz.zona_id = z.id
                WHERE p.id = :asesorId AND pz.status = true
                ORDER BY z.value
        """, nativeQuery = true)
    List<Long> getZonasIdByAsesor(@Param("asesorId") Long asesorId);

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
            ci_tel.value AS whatsApp,
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
        List<PersonInterfaceResponseDto> getAllPerson();


    }

