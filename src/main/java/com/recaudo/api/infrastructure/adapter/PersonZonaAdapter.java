package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.gateway.PersonZonaGateway;
import com.recaudo.api.domain.model.dto.rest_api.UpdateOrdenList;
import com.recaudo.api.domain.model.dto.rest_api.UpdateOrdenPerson;
import com.recaudo.api.infrastructure.repository.PersonZonaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class PersonZonaAdapter implements PersonZonaGateway {

    @Autowired
    private PersonZonaRepository personzonaRepository;

    @Override
    public void updateOrdenClientes(UpdateOrdenList list) {
        for (UpdateOrdenPerson cliente : list.getClientes()) {
            personzonaRepository.findByPersonId(cliente.getPersonId())
                    .ifPresent(entity -> {
                        entity.setOrden(cliente.getOrden());
                        personzonaRepository.save(entity);
                    });
        }
    }
}
