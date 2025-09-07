package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.ContactInfoListDto;
import com.recaudo.api.domain.model.dto.rest_api.ContactInfoRegisterDto;
import com.recaudo.api.domain.model.dto.rest_api.PersonRegisterDto;
import com.recaudo.api.domain.model.entity.PersonEntity;

import java.util.List;

public interface ContactInfoGateway {

    public ContactInfoRegisterDto save(ContactInfoRegisterDto person);
    List<ContactInfoListDto> getByPerson(Long personId);
    ContactInfoRegisterDto update(Long id, ContactInfoRegisterDto dto);
    void delete(Long id);


}
