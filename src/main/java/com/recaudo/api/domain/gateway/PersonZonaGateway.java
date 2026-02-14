package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.AsesorZonaDto;
import com.recaudo.api.domain.model.dto.rest_api.UpdateOrdenList;

import java.util.List;

public interface PersonZonaGateway {

    void updateOrdenClientes(UpdateOrdenList list);
    void updateClientToZone(Long personId, Long zoneId);
    void assignZonasToAsesor(Long personId, List<Long> zonasIds);
    List<AsesorZonaDto> getZonasByAsesor(Long personId);
}
