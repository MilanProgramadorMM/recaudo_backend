package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.ContactInfoGateway;
import com.recaudo.api.domain.gateway.UserGateway;
import com.recaudo.api.domain.model.dto.response.ContactInfoListDto;
import com.recaudo.api.domain.model.dto.response.UserDto;
import com.recaudo.api.domain.model.dto.rest_api.ContactInfoRegisterDto;
import com.recaudo.api.domain.model.dto.rest_api.UpdateUserDto;
import com.recaudo.api.domain.model.dto.rest_api.UpdateUserPasswordDto;
import com.recaudo.api.domain.model.dto.rest_api.UserCreateDto;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@UseCase
@AllArgsConstructor
public class RegisterContactInfoUseCase {


    private ContactInfoGateway contactInfoGateway;

    public ContactInfoRegisterDto register(ContactInfoRegisterDto data) {
        return contactInfoGateway.save(data);
    }
    public ContactInfoRegisterDto update(Long id,ContactInfoRegisterDto data) {
        return contactInfoGateway.update(id,data);
    }

    public void delete(Long id) {
        contactInfoGateway.delete(id);
    }
    public List<ContactInfoListDto> getContactInfoByPerson(Long personId) {
        return contactInfoGateway.getByPerson(personId);
    }
}
