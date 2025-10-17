package com.recaudo.api.domain.model.dto.rest_api;

import lombok.Data;

@Data
public class UpdateOrdenPerson {
    private Long personId;
    private Long zonaId;
    private Long orden;
    private boolean status;
}
