package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.PersonGateway;
import com.recaudo.api.domain.model.dto.response.PersonInterfaceResponseDto;
import com.recaudo.api.domain.model.dto.response.PersonResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.PersonRegisterDto;
import lombok.AllArgsConstructor;

import java.util.List;

@UseCase
@AllArgsConstructor
public class RegisterPersonUseCase {

    private PersonGateway personGateway;

    public PersonResponseDto register(PersonRegisterDto data) {
        return personGateway.save(data);
    }

    public PersonResponseDto edit(PersonRegisterDto data) {
        return personGateway.edit(data);
    }

    public PersonResponseDto getById(Long id)  {
        return personGateway.getById(id);
    }

    public List<PersonResponseDto> getAll() {
        return personGateway.getAll();
    }
    public List<PersonInterfaceResponseDto> getByType(String type) {
        return personGateway.getByType(type);
    }

    public List<PersonInterfaceResponseDto> getByZona(String type, String zona) {
        return personGateway.getByZona(type,zona);
    }

    public void delete(Long id){
        personGateway.delete(id);
    }

    public PersonResponseDto reactivate(Long id) {
        return personGateway.reactivate(id);
    }

    public PersonResponseDto toggleStatus(Long id, boolean status) {
        return personGateway.toggleStatus(id, status);
    }



}
