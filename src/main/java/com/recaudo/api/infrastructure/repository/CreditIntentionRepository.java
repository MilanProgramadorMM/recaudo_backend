package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.dto.response.IntentionCreditResponseAllDto;
import com.recaudo.api.domain.model.dto.response.ProyeccionAmortizacionDto;
import com.recaudo.api.domain.model.entity.CreditIntentionEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditIntentionRepository extends JpaRepository<CreditIntentionEntity, Long> {

    @Transactional
    @Query(value = "CALL PROC_ORQUESTADOR_PROYECCIONES(:json)", nativeQuery = true)
    List<ProyeccionAmortizacionDto> ejecutarProyeccion(
            @Param("json") String json
    );

    boolean existsById(Long id);

    List<CreditIntentionEntity> findTop5ByOrderByCreatedAtDesc();

    @Query(value = """
    	SELECT
                	 ci.id AS id,
                	 ci.zone_id AS zoneId,
                	 z.value AS zoneName,
                	 z.description AS zoneDescription,
                	 ci.document_type AS documentType,
                	 ci.document AS document,
                	 ci.firstname AS firstname,
                	 ci.middlename AS middlename,
                	 ci.lastname AS lastname,
                	 ci.maternal_lastname AS maternalLastname,
                	 ci.fullname AS fullname,
                	 ci.gender,
                	 g.name AS genero,
                	 ci.occupation AS occupation,
                	 ci.description AS description,
                	 ci.email AS email,
                	 ci.phone_number AS phoneNumber,
                	 ci.whatsapp_number AS whatsappNumber,
                	 ci.credit_line_id AS creditLineId,
                	 cl.name AS creditLineName,
                	 ci.quota_value AS quotaValue,
                	 ci.period_id AS periodId,
                	 p.name AS periodName,
                	 p.cod AS periodCode,
                	 ci.period_quantity AS periodQuantity,
                	 ci.tax_type_id AS taxTypeId,
                	 tt.name AS taxTypeName,
                	 ci.tax_value AS taxValue,
                	 ci.total_intention_value AS totalIntentionValue,
                	 ci.total_interest_value AS totalInterestValue,
                	 ci.total_capital_value AS totalCapitalValue,
                	 ci.item_value AS itemValue,
                	 ci.initial_value_payment AS initialValuePayment,
                	 ci.total_financed_value AS totalFinancedValue,
                	 ci.stationery AS stationery,
                	 ci.home_address AS homeAddress,
                	 ci.country_id AS countryId,
                	 c.value AS countryName,
                	 ci.department_id AS departmentId,
                	 d.value AS departmentName,
                	 ci.municipality_id AS municipalityId,
                	 m.value AS municipalityName,
                	 ci.neighborhood_id AS neighborhoodId,
                	 b.value AS neighborhoodName,
                	 ci.created_at AS createdAt,
                	 ci.edited_at AS editedAt,
                	 ci.initial_quincena,
                	 ci.end_quincena,
                	 ci.referido,
                	 ci.call_success,
                	 ci.approval_link AS approvalLink,
                	 ci.approval_token AS approvalToken,
                	 ci.approval_status AS approvalStatus,
                	 ci.approved_at AS approvedAt,
                	 ci.approval_ip AS approvalIp,
                	 ci.token_expires_at AS tokenExpiresAt,
                	 ci.date_start AS fechaInicio,
                	 cis.code AS estado_actual,
                	 CASE
                	        WHEN EXISTS (
                	            SELECT 1
                	            FROM person p
                	            WHERE p.document = ci.document
                	        )
                	        THEN 1
                	        ELSE 0
                	    END AS client_exists
              FROM credit_intention ci
              LEFT JOIN credit_intention_status cis
              ON cis.credit_intention_id = ci.id
              AND cis.status = 1
              LEFT JOIN zona z ON z.id = ci.zone_id
              LEFT JOIN glotypes g ON g.id = ci.gender
              LEFT JOIN credit_line cl ON cl.id = ci.credit_line_id
              LEFT JOIN period p ON p.id = ci.period_id
              LEFT JOIN tax_type tt ON tt.id = ci.tax_type_id
              LEFT JOIN pais c ON c.id = ci.country_id
              LEFT JOIN departamento d ON d.id = ci.department_id
              LEFT JOIN municipio m ON m.id = ci.municipality_id
              LEFT JOIN barrio b ON b.id = ci.neighborhood_id
              WHERE cis.code NOT IN ('TERMINATED', 'RECHAZED')
              OR cis.code IS NULL
              ORDER BY ci.created_at DESC
""", nativeQuery = true)
    List<IntentionCreditResponseAllDto> findAllCreditIntentions();

    @Query(value = """
        SELECT
            ci.id AS id,
            ci.zone_id AS zoneId,
            z.value AS zoneName,
            z.description AS zoneDescription,
            ci.document_type AS documentType,
            ci.document AS document,
            ci.firstname AS firstname,
            ci.middlename AS middlename,
            ci.lastname AS lastname,
            ci.maternal_lastname AS maternalLastname,
            ci.fullname AS fullname,
            ci.gender,
            g.name AS genero,
            ci.occupation AS occupation,
            ci.description AS description,
            ci.email AS email,
            ci.phone_number AS phoneNumber,
            ci.whatsapp_number AS whatsappNumber,
            ci.credit_line_id AS creditLineId,
            cl.name AS creditLineName,
            ci.quota_value AS quotaValue,
            ci.period_id AS periodId,
            p.name AS periodName,
            p.cod AS periodCode,
            ci.period_quantity AS periodQuantity,
            ci.tax_type_id AS taxTypeId,
            tt.name AS taxTypeName,
            ci.tax_value AS taxValue,
            ci.total_intention_value AS totalIntentionValue,
            ci.total_interest_value AS totalInterestValue,
            ci.total_capital_value AS totalCapitalValue,
            ci.item_value AS itemValue,
            ci.initial_value_payment AS initialValuePayment,
            ci.total_financed_value AS totalFinancedValue,
            ci.stationery AS stationery,
            ci.home_address AS homeAddress,
            ci.country_id AS countryId,
            c.value AS countryName,
            ci.department_id AS departmentId,
            d.value AS departmentName,
            ci.municipality_id AS municipalityId,
            m.value AS municipalityName,
            ci.neighborhood_id AS neighborhoodId,
            b.value AS neighborhoodName,
            ci.created_at AS createdAt,
            ci.edited_at AS editedAt,
            ci.initial_quincena,
            ci.end_quincena,
            ci.referido,
            ci.call_success,
            ci.approval_link AS approvalLink,
            ci.approval_token AS approvalToken,
            ci.approval_status AS approvalStatus,
            ci.approved_at AS approvedAt,
            ci.approval_ip AS approvalIp,
            ci.token_expires_at AS tokenExpiresAt,
            ci.date_start AS fechaInicio,
            cis.code AS estado_actual,
            CASE
                   WHEN EXISTS (
                       SELECT 1
                       FROM person p
                       WHERE p.document = ci.document
                   )
                   THEN 1
                   ELSE 0
               END AS client_exists
        FROM credit_intention ci
        LEFT JOIN credit_intention_status cis
        ON cis.credit_intention_id = ci.id
        AND cis.status = 1
        LEFT JOIN zona z ON z.id = ci.zone_id
        LEFT JOIN glotypes g ON g.id = ci.gender
        LEFT JOIN credit_line cl ON cl.id = ci.credit_line_id
        LEFT JOIN period p ON p.id = ci.period_id
        LEFT JOIN tax_type tt ON tt.id = ci.tax_type_id
        LEFT JOIN pais c ON c.id = ci.country_id
        LEFT JOIN departamento d ON d.id = ci.department_id
        LEFT JOIN municipio m ON m.id = ci.municipality_id
        LEFT JOIN barrio b ON b.id = ci.neighborhood_id
        WHERE ci.id = :creditIntentionId
        ORDER BY ci.created_at DESC
    """, nativeQuery = true)
    List<IntentionCreditResponseAllDto> findByIdProjection(
            @Param("creditIntentionId") Long creditIntentionId
    );

    Optional<CreditIntentionEntity> findByApprovalToken(String approvalToken);


}
