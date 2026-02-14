package com.recaudo.api.infrastructure.repository;

import com.recaudo.api.domain.model.entity.ClosingSpendEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ClosingSpendRepository extends JpaRepository<ClosingSpendEntity, Long> {

    /**
     * Obtiene todos los gastos activos de un cierre
     */
    List<ClosingSpendEntity> findByClosingIdAndStatusTrue(Long closingId);

    /**
     * Obtiene un gasto específico por cierre y tipo de gasto
     * Ej: BASE, MOTO, GASOLINA
     */
    Optional<ClosingSpendEntity> findByClosingIdAndSpendTypeIdAndStatusTrue(
            Long closingId,
            Long spendTypeId
    );

    /**
     * Obtiene todos los gastos de un tipo específico en un cierre
     * (útil si permites varios gastos del mismo tipo)
     */
    List<ClosingSpendEntity> findAllByClosingIdAndSpendTypeIdAndStatusTrue(
            Long closingId,
            Long spendTypeId
    );

    /**
     * Verifica si ya existe un gasto de un tipo específico en el cierre
     */
    boolean existsByClosingIdAndSpendTypeIdAndStatusTrue(
            Long closingId,
            Long spendTypeId
    );

    ClosingSpendEntity findByClosingIdAndSpendTypeId(Long closingId, Long type);
}
