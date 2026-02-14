package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.AsesorZonaDto;
import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.UpdateOrdenList;
import com.recaudo.api.infrastructure.adapter.PersonZonaAdapter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/person-zona")
@AllArgsConstructor
public class PersonZonaController {

     PersonZonaAdapter personZonaAdapter;

    @PostMapping("/update-orden")
    public ResponseEntity<DefaultResponseDto<Void>> updateOrdenClientes(
            @RequestBody UpdateOrdenList data
    ) {
        personZonaAdapter.updateOrdenClientes(data);

        return ResponseEntity.ok(
                DefaultResponseDto.<Void>builder()
                        .message("Orden de clientes actualizado correctamente")
                        .status(HttpStatus.OK)
                        .details("Se actualizaron los órdenes de " + data.getClientes().size() + " cliente(s)")
                        .build()
        );
    }

    /**
     * Asignar múltiples zonas a un asesor
     */
    @PostMapping("/asesor/{personId}/assign-zonas")
    public ResponseEntity<DefaultResponseDto<Void>> assignZonasToAsesor(
            @PathVariable Long personId,
            @RequestBody List<Long> zonasIds
    ) {
        if (zonasIds == null || zonasIds.isEmpty()) {
            return ResponseEntity.badRequest().body(
                    DefaultResponseDto.<Void>builder()
                            .message("Error")
                            .status(HttpStatus.BAD_REQUEST)
                            .details("Debe proporcionar al menos una zona")
                            .build()
            );
        }

        personZonaAdapter.assignZonasToAsesor(personId, zonasIds);

        return ResponseEntity.ok(
                DefaultResponseDto.<Void>builder()
                        .message("Zonas asignadas correctamente")
                        .status(HttpStatus.OK)
                        .details("Se asignaron " + zonasIds.size() + " zona(s) al asesor")
                        .build()
        );
    }

    /**
     * Obtener las zonas asignadas a un asesor
     */
    @GetMapping("/asesor/{personId}/zonas")
    public ResponseEntity<DefaultResponseDto<List<AsesorZonaDto>>> getZonasByAsesor(
            @PathVariable Long personId
    ) {
        List<AsesorZonaDto> zonas = personZonaAdapter.getZonasByAsesor(personId);

        return ResponseEntity.ok(
                DefaultResponseDto.<List<AsesorZonaDto>>builder()
                        .message("Zonas del asesor obtenidas correctamente")
                        .status(HttpStatus.OK)
                        .details("El asesor tiene " + zonas.size() + " zona(s) asignada(s)")
                        .data(zonas)
                        .build()
        );
    }
}

