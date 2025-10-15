package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.UpdateOrdenList;
import com.recaudo.api.infrastructure.adapter.PersonZonaAdapter;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
