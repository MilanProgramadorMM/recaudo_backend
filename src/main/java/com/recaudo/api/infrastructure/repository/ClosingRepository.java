package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.dto.response.TodayClosingProjection;
import com.recaudo.api.domain.model.entity.ClosingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClosingRepository extends JpaRepository<ClosingEntity, Long> {

    //DEVUELVE UN CIERRE CON SU ESTADO
    @Query(value = """
    SELECT 
        cs.code        AS closingStatus,
        c.closing_date AS closingDate,
        c.person_id    AS personId,
        c.id AS closingId
    FROM closing c
    JOIN closing_status cs
        ON cs.closing_id = c.id
    WHERE c.person_id = :personId
      AND c.closing_date = :date
      AND c.status = 1
      AND cs.status = 1
""", nativeQuery = true)
    Optional<TodayClosingProjection> findTodayClosing(
            @Param("personId") Long personId,
            @Param("date") LocalDate date
    );

    //VALIDA SI YA EXISTE UN CIERRE PARA UN ASESRO EN DETERMINADA ZONA PARA DETERMINADA FECHA
    boolean existsByPersonIdAndZonaIdAndClosingDate(
            Long personId,
            Long zonaId,
            LocalDate closingDate
    );

    // Devuelve el cierre del día de un asesor para una zona específica
    @Query(value = """
        SELECT 
            cs.code        AS closingStatus,
            c.closing_date AS closingDate,
            c.person_id    AS personId,
            c.zona_id      AS zonaId,
            c.id           AS closingId
        FROM closing c
        JOIN closing_status cs ON cs.closing_id = c.id
        WHERE c.person_id = :personId
          AND c.zona_id = :zonaId
          AND c.closing_date = :date
          AND c.status = 1
          AND cs.status = 1
""", nativeQuery = true)
    Optional<TodayClosingProjection> findTodayClosingByPersonAndZona(
            @Param("personId") Long personId,
            @Param("zonaId") Long zonaId,
            @Param("date") LocalDate date
    );


    //RESUMEN DE CIERRES POR ASESOR
    @Query(value = """
            SELECT
                c.id               AS id,
                c.closing_date     AS closingDate,
                c.observation      AS observation,
                c.user_create      AS userCreate,
                p.fullname         AS namePerson,
                c.created_at       AS createdAt,
                cs.code            AS closingStatus,
                z.value 		   AS zona,
                z.id               AS zonaId
            FROM closing c
            INNER JOIN person p ON p.id = c.person_id
            INNER JOIN zona z ON c.zona_id = z.id
            INNER JOIN closing_status cs ON cs.closing_id = c.id
            WHERE c.person_id = :personId
            AND c.status = 1
            AND cs.status = 1
            AND z.status = 1
            ORDER BY c.closing_date DESC
        """,
            nativeQuery = true)
    List<Object[]> findClosingResume(Long personId);

    @Query(value = """
            SELECT
                c.id               AS id,
                c.closing_date     AS closingDate,
                c.observation      AS observation,
                c.user_create      AS userCreate,
                p.fullname         AS namePerson,
                c.created_at       AS createdAt,
                cs.code            AS closingStatus,
                z.value 		   AS zona,
                z.id               AS zonaId
            FROM closing c
            INNER JOIN person p ON p.id = c.person_id
            INNER JOIN zona z ON c.zona_id = z.id
            INNER JOIN closing_status cs ON cs.closing_id = c.id
            WHERE c.status = 1
            AND cs.status = 1
            AND z.status = 1
            ORDER BY c.closing_date DESC
        """,
            nativeQuery = true)
    List<Object[]> findClosingAllResume();

    @Query(nativeQuery = true, value = """
        SELECT c.amount_asesor 
        FROM closing AS c 
        INNER JOIN closing_status AS cs ON cs.closing_id = c.id
        WHERE cs.code = 'APPROVED'
        AND c.person_id = :personId
        AND c.closing_date = :date
    """)
    List<Double> findByPersonIdAndClosingDate(@Param("personId")  Long personId, @Param("date")  LocalDate date);

}