package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.rest_api.PersonRegisterDto;
import com.recaudo.api.domain.model.entity.PersonEntity;

import java.util.List;

public interface PersonGateway {

    public PersonEntity getByUserName(String username);
    public PersonRegisterDto getById(Long id);
    List<PersonRegisterDto> getAll();
    List<PersonRegisterDto> getByType(String type);
    public PersonRegisterDto save(PersonRegisterDto person);
    public PersonRegisterDto edit(PersonRegisterDto person);
    public void delete(Long id);
    PersonRegisterDto reactivate(Long id);



}
