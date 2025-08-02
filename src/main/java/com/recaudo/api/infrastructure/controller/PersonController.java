package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.response.LoginResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.LoginDto;
import com.recaudo.api.domain.model.dto.rest_api.PersonRegisterDto;
import com.recaudo.api.domain.usecase.RegisterPersonUseCase;
import com.recaudo.api.domain.usecase.RegisterUseCase;
import com.recaudo.api.exception.BadRequestException;
import com.recaudo.api.infrastructure.adapter.UserDetailsImpl;
import com.recaudo.api.infrastructure.helper.security.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/person")
@Tag(name = "Person Controller", description = "Register persons")
@AllArgsConstructor
public class PersonController {

    private final RegisterPersonUseCase personUseCase;

    @PostMapping("/register")
    public ResponseEntity<DefaultResponseDto<PersonRegisterDto>> register(
            @RequestBody @Valid PersonRegisterDto data, BindingResult
                    bindingResult) throws Exception {
        if (bindingResult.hasErrors())
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());

        return ResponseEntity.ok(
                DefaultResponseDto.<PersonRegisterDto>builder()
                        .message("Registro completado")
                        .status(HttpStatus.OK)
                        .details("Registro completado exitosamente")
                        .data(personUseCase.register(data))
                        .build()
        );
    }

    @PutMapping("/update")
    public ResponseEntity<DefaultResponseDto<PersonRegisterDto>> updatePerson(
            @RequestBody @Valid PersonRegisterDto data,
            BindingResult bindingResult) throws Exception {

        if (bindingResult.hasErrors())
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());

        return ResponseEntity.ok(
                DefaultResponseDto.<PersonRegisterDto>builder()
                        .message("Persona actualizada correctamente")
                        .status(HttpStatus.OK)
                        .details("Los datos fueron modificados")
                        .data(personUseCase.edit(data))
                        .build());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<DefaultResponseDto<PersonRegisterDto>> getByUserId(@PathVariable("id") Long id) throws org.apache.coyote.BadRequestException {

        PersonRegisterDto dto = personUseCase.getById(id);

        return ResponseEntity.ok(
                DefaultResponseDto.<PersonRegisterDto>builder()
                        .message("Información encontrada")
                        .status(HttpStatus.OK)
                        .details("Información encontrada")
                        .data(dto)
                        .build()
        );
    }

    @GetMapping("/get-all")
    public ResponseEntity<DefaultResponseDto<List<PersonRegisterDto>>> getAllPersons() {
        List<PersonRegisterDto> persons = personUseCase.getAll();

        return ResponseEntity.ok(
                DefaultResponseDto.<List<PersonRegisterDto>>builder()
                        .message("Personas encontradas")
                        .status(HttpStatus.OK)
                        .details("Listado completo")
                        .data(persons)
                        .build()
        );
    }

    @PutMapping("/delete/{id}")
    public ResponseEntity<DefaultResponseDto<String>> deletePerson(
            @PathVariable Long id,
            @RequestParam String userDelete) {

        personUseCase.delete(id, userDelete);

        return ResponseEntity.ok(
                DefaultResponseDto.<String>builder()
                        .message("Persona inactivada")
                        .status(HttpStatus.OK)
                        .details("La persona y el usuario fueron marcados como inactivos")
                        .data("OK")
                        .build()
        );
    }


}
