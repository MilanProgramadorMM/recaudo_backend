package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.PersonInterfaceResponseDto;
import com.recaudo.api.domain.model.dto.response.PersonResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.PersonRegisterDto;
import com.recaudo.api.domain.model.entity.PersonEntity;

import java.util.List;

public interface PersonGateway {

    public PersonEntity getByUserName(String username);
    public PersonResponseDto getById(Long id);
    List<PersonResponseDto> getAll();
    List<PersonInterfaceResponseDto> getByType(String type);

    List<PersonInterfaceResponseDto> getByZona(String type,String zona );
    public PersonResponseDto save(PersonRegisterDto person);
    public PersonResponseDto edit(PersonRegisterDto person);
    public void delete(Long id);
    PersonResponseDto reactivate(Long id);
    PersonResponseDto toggleStatus(Long id, boolean status);




}
