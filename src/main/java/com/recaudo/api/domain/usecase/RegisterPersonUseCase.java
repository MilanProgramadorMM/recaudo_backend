package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.PersonGateway;
import com.recaudo.api.domain.model.dto.rest_api.PersonRegisterDto;
import lombok.AllArgsConstructor;

import java.util.List;

@UseCase
@AllArgsConstructor
public class RegisterPersonUseCase {


    private PersonGateway personGateway;


    public PersonRegisterDto register(PersonRegisterDto data) {
        return personGateway.save(data);
    }

    public PersonRegisterDto edit(PersonRegisterDto data) {
        return personGateway.edit(data);
    }

    public PersonRegisterDto getById(Long id)  {
        return personGateway.getById(id);
    }

    public List<PersonRegisterDto> getAll() {
        return personGateway.getAll();
    }

    public void delete(Long id, String userDelete){
        personGateway.delete(id,userDelete);
    }

}
