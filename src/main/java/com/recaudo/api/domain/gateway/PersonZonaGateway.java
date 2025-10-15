package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.rest_api.UpdateOrdenList;

public interface PersonZonaGateway {

    void updateOrdenClientes(UpdateOrdenList list);
}
