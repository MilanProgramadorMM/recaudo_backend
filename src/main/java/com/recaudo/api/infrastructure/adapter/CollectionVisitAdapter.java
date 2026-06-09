package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.model.dto.response.QuotaPendingValueProjection;
import com.recaudo.api.domain.model.entity.CollectionVisitEntity;
import com.recaudo.api.domain.model.entity.DashboardNoPagoEntity;
import com.recaudo.api.infrastructure.repository.CollectionVisitRepository;
import com.recaudo.api.infrastructure.repository.DailyCollectionRepository;
import com.recaudo.api.infrastructure.repository.DashboardNoPagoRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class CollectionVisitAdapter {

    @Autowired
    private CollectionVisitRepository collectionVisitRepository;

    @Autowired
    private DashboardNoPagoRepository dashboardNoPagoRepository;

    @Autowired
    private DailyCollectionRepository dailyCollectionRepository;

    @Transactional
    public void registerDailyVisitIfNotExists(
            Long creditId,
            Long cuotaId,
            String advisorUsername,
            LocalDate date
    ) {

        // Si ya existe una visita hoy, NO hacemos nada
        collectionVisitRepository
                .findByCuotaIdAndVisitDate(cuotaId, date)
                .orElseGet(() -> {

                    // Se crea la visita del día
                    CollectionVisitEntity visit = CollectionVisitEntity.builder()
                            .creditId(creditId)
                            .cuotaId(cuotaId)
                            .visitDate(date)
                            .advisorUsername(advisorUsername)
                            .paid(false) // Por defecto NO pagó
                            .noPago(false) // Por defecto NO se ha registrado "no pago"
                            .createdAt(LocalDateTime.now())
                            .build();

                    log.info("Creando visita diaria para cuota {}", cuotaId);
                    return collectionVisitRepository.save(visit);
                });
    }

    // Marca que HOY la cuota fue pagada, Se llama desde processPayment()
    @Transactional
    public void markAsPaidToday(Long cuotaId, LocalDate date) {

        CollectionVisitEntity visit =
                collectionVisitRepository
                        .findByCuotaIdAndVisitDate(cuotaId, date)
                        .orElseThrow(() ->
                                new RuntimeException("No existe visita para marcar como pagada"));

        visit.setPaid(true);
        visit.setNoPago(false); // Si pagó, entonces NO es "no pago"
        visit.setPaymentPromiseDate(null); // Si pagó, ya no hay promesa pendiente

        collectionVisitRepository.save(visit);
        log.info("Cuota {} marcada como pagada hoy", cuotaId);
    }

     //Registra promesa de pago, Crea la visita si no existe
    @Transactional
    public void registerPaymentPromise(
            Long creditId,
            Long cuotaId,
            String advisorUsername,
            LocalDate visitDate,
            LocalDate promiseDate,
            String observation
    ) {
        // Buscar o crear la visita del día
        CollectionVisitEntity visit = collectionVisitRepository
                .findByCuotaIdAndVisitDate(cuotaId, visitDate)
                .orElseGet(() -> {
                    // Si no existe, la creamos
                    CollectionVisitEntity newVisit = CollectionVisitEntity.builder()
                            .creditId(creditId)
                            .cuotaId(cuotaId)
                            .visitDate(visitDate)
                            .advisorUsername(advisorUsername)
                            .paid(false)
                            .noPago(false)
                            .createdAt(LocalDateTime.now())
                            .build();

                    log.info("Creando visita para registrar promesa en cuota {}", cuotaId);
                    return collectionVisitRepository.save(newVisit);
                });

        // Actualizar la promesa
        visit.setPaymentPromiseDate(promiseDate);
        visit.setObservation(observation);
        visit.setNoPago(false);
        collectionVisitRepository.save(visit);

        QuotaPendingValueProjection quotaData = dailyCollectionRepository
                .findPendingValueAndZoneByCuotaId(cuotaId)
                .orElseThrow(() -> new EntityNotFoundException("No se encontró la información financiera para la cuota: " + cuotaId));

        BigDecimal saldoPendiente = quotaData.getSaldoPendienteCuota();
        Long zonaId = quotaData.getZonaId();

        DashboardNoPagoEntity dashboardNoPagoEntity = DashboardNoPagoEntity.builder()
                .value(saldoPendiente)
                .zonaId(zonaId)
                .userCreate(advisorUsername)
                .createdAt(LocalDateTime.now())
                .cant(1)
                .build();

        dashboardNoPagoRepository.save(dashboardNoPagoEntity);

        log.info("Promesa registrada para cuota {} para el día {}", cuotaId, promiseDate);
    }


     //Registra que el cliente NO pagó, Crea la visita si no existe
    @Transactional
    public void registerNoPago(
            Long creditId,
            Long cuotaId,
            String advisorUsername,
            LocalDate visitDate,
            String reason,
            String observation
    ) {
        // Buscar o crear la visita del día
        CollectionVisitEntity visit = collectionVisitRepository
                .findByCuotaIdAndVisitDate(cuotaId, visitDate)
                .orElseGet(() -> {
                    // Si no existe, la creamos
                    CollectionVisitEntity newVisit = CollectionVisitEntity.builder()
                            .creditId(creditId)
                            .cuotaId(cuotaId)
                            .visitDate(visitDate)
                            .advisorUsername(advisorUsername)
                            .paid(false)
                            .noPago(false)
                            .createdAt(LocalDateTime.now())
                            .build();

                    log.info("Creando visita para registrar 'no pago' en cuota {}", cuotaId);
                    return collectionVisitRepository.save(newVisit);
                });

        // Actualizar el no pago
        visit.setNoPago(true);
        visit.setNoPagoReason(reason);
        visit.setObservation(observation);
        visit.setPaid(false);
        visit.setPaymentPromiseDate(null);

        collectionVisitRepository.save(visit);

        log.info("Registrado 'no pago' para cuota {} con motivo: {}", cuotaId, reason);
    }

     //Obtiene el estado de la visita de hoy para una cuota
    @Transactional(readOnly = true)
    public Optional<CollectionVisitEntity> getVisitToday(Long cuotaId, LocalDate date) {
        return collectionVisitRepository.findByCuotaIdAndVisitDate(cuotaId, date);
    }
}
