package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.ContactInfoListDto;
import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.response.UserDto;
import com.recaudo.api.domain.model.dto.rest_api.ContactInfoRegisterDto;
import com.recaudo.api.domain.model.dto.rest_api.UpdateUserDto;
import com.recaudo.api.domain.model.dto.rest_api.UpdateUserPasswordDto;
import com.recaudo.api.domain.model.dto.rest_api.UserCreateDto;
import com.recaudo.api.domain.usecase.RegisterContactInfoUseCase;
import com.recaudo.api.domain.usecase.RegisterUseCase;
import com.recaudo.api.exception.BadRequestException;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contact")
@AllArgsConstructor
public class ContactInfoController {

    private final RegisterContactInfoUseCase registerContactInfoUseCase;

    @PostMapping("/info")
    public ResponseEntity<DefaultResponseDto<ContactInfoRegisterDto>> registerUser(
            @RequestBody @Valid ContactInfoRegisterDto data,
            BindingResult bindingResult) throws Exception {

        ContactInfoRegisterDto newRegister = registerContactInfoUseCase.register(data);
        if (bindingResult.hasErrors())
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());

        return ResponseEntity.ok(
            DefaultResponseDto.<ContactInfoRegisterDto>builder()
                .message("Usuario creado correctamente")
                .status(HttpStatus.OK)
                .details("Los datos fueron creados")
                .data(newRegister)
                .build());
    }

    @GetMapping("/info/{personId}")
    public ResponseEntity<DefaultResponseDto<List<ContactInfoListDto>>> getContactInfoByPerson(
            @PathVariable Long personId) {

        List<ContactInfoListDto> data = registerContactInfoUseCase.getContactInfoByPerson(personId);

        return ResponseEntity.ok(
                DefaultResponseDto.<List<ContactInfoListDto>>builder()
                        .message("Información de contacto obtenida correctamente")
                        .status(HttpStatus.OK)
                        .details("Se listó la información de contacto de la persona")
                        .data(data)
                        .build()
        );
    }

    @PutMapping("/info/{contactInfoId}")
    public ResponseEntity<DefaultResponseDto<ContactInfoRegisterDto>> updateContactInfo(
            @PathVariable Long contactInfoId,
            @RequestBody @Valid ContactInfoRegisterDto data,
            BindingResult bindingResult) throws Exception {

        if (bindingResult.hasErrors()) {
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        ContactInfoRegisterDto updatedContactInfo = registerContactInfoUseCase.update(contactInfoId, data);

        return ResponseEntity.ok(
                DefaultResponseDto.<ContactInfoRegisterDto>builder()
                        .message("Información de contacto actualizada correctamente")
                        .status(HttpStatus.OK)
                        .details("Los datos fueron actualizados")
                        .data(updatedContactInfo)
                        .build()
        );
    }



}
