package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.DailyCollectionDTO;
import com.recaudo.api.domain.model.dto.response.DailyCollectionProjection;
import com.recaudo.api.infrastructure.adapter.CollectionVisitAdapter;
import com.recaudo.api.infrastructure.adapter.DailyCollectionService;
import com.recaudo.api.infrastructure.adapter.UserDetailsImpl;
import com.recaudo.api.infrastructure.repository.RecaudoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/collection")
@Slf4j
public class CollectionVisitController {

    @Autowired
    private CollectionVisitAdapter collectionVisitService;

    @Autowired
    private DailyCollectionService dailyCollectionService;

    /**
     * Obtiene la lista diaria de clientes a cobrar
     * Incluye:
     * - vencidos
     * - los que vencen hoy
     * - pagados hoy o no
     */
    @GetMapping("/daily")
    public ResponseEntity<List<DailyCollectionDTO>> getDailyCollection(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Authentication authentication
    ) {
        String username = authentication.getName();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long personId = userDetails.getUserEntity().getPersonId();

        return ResponseEntity.ok(
                dailyCollectionService.getDailyCollection(username, personId, date)
        );
    }

    // Registra promesa de pago del cliente
    @PostMapping("/promise")
    public ResponseEntity<?> registerPromise(
            @RequestParam Long creditId,
            @RequestParam Long cuotaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate promiseDate,
            @RequestParam(required = false) String observation,
            Authentication authentication
    ) {
        try {
            String username = authentication.getName();

            collectionVisitService.registerPaymentPromise(
                    creditId,
                    cuotaId,
                    username,
                    LocalDate.now(),
                    promiseDate,
                    observation
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Promesa de pago registrada exitosamente"
            ));
        } catch (Exception e) {
            log.error("Error al registrar promesa: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Error al registrar promesa: " + e.getMessage()
                    ));
        }
    }

     //Registra que el cliente NO pagó hoy
    @PostMapping("/no-pago")
    public ResponseEntity<?> registerNoPago(
            @RequestParam Long creditId,
            @RequestParam Long cuotaId,
            @RequestParam String reason,
            @RequestParam(required = false) String observation,
            Authentication authentication
    ) {
        try {
            String username = authentication.getName();

            collectionVisitService.registerNoPago(
                    creditId,
                    cuotaId,
                    username,
                    LocalDate.now(),
                    reason,
                    observation
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Registro de 'no pago' exitoso"
            ));
        } catch (Exception e) {
            log.error("Error al registrar no pago: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Error al registrar no pago: " + e.getMessage()
                    ));
        }
    }

    //Obtiene el detalle de la visita de hoy para una cuota específica
    @GetMapping("/visit-detail")
    public ResponseEntity<?> getVisitDetail(
            @RequestParam Long cuotaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        try {
            return collectionVisitService.getVisitToday(cuotaId, date)
                    .map(visit -> ResponseEntity.ok(Map.of(
                            "success", true,
                            "data", visit
                    )))
                    .orElse(ResponseEntity.ok(Map.of(
                            "success", true,
                            "data", null,
                            "message", "No hay visita registrada para esta fecha"
                    )));
        } catch (Exception e) {
            log.error("Error al obtener detalle de visita: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of(
                            "success", false,
                            "message", "Error al obtener detalle: " + e.getMessage()
                    ));
        }
    }
}

