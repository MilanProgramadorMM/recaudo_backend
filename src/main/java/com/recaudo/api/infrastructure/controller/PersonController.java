package com.recaudo.api.infrastructure.controller;

import com.recaudo.api.domain.model.dto.response.DefaultResponseDto;
import com.recaudo.api.domain.model.dto.response.LoginResponseDto;
import com.recaudo.api.domain.model.dto.response.PersonInterfaceResponseDto;
import com.recaudo.api.domain.model.dto.response.PersonResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.LoginDto;
import com.recaudo.api.domain.model.dto.rest_api.PersonRegisterDto;
import com.recaudo.api.domain.model.entity.PersonEntity;
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
    public ResponseEntity<DefaultResponseDto<PersonResponseDto>> register(
            @RequestBody @Valid PersonRegisterDto data, BindingResult
                    bindingResult) throws Exception {
        if (bindingResult.hasErrors())
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());

        return ResponseEntity.ok(
                DefaultResponseDto.<PersonResponseDto>builder()
                        .message("Registro completado")
                        .status(HttpStatus.OK)
                        .details("Registro completado exitosamente")
                        .data(personUseCase.register(data))
                        .build()
        );
    }

    @PutMapping("/update")
    public ResponseEntity<DefaultResponseDto<PersonResponseDto>> updatePerson(
            @RequestBody @Valid PersonRegisterDto data,
            BindingResult bindingResult) throws Exception {

        if (bindingResult.hasErrors())
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());

        return ResponseEntity.ok(
                DefaultResponseDto.<PersonResponseDto>builder()
                        .message("Persona actualizada correctamente")
                        .status(HttpStatus.OK)
                        .details("Los datos fueron modificados")
                        .data(personUseCase.edit(data))
                        .build());
    }

    @GetMapping("/get/{id}")
    public ResponseEntity<DefaultResponseDto<PersonResponseDto>> getByUserId(@PathVariable("id") Long id) throws org.apache.coyote.BadRequestException {

        PersonResponseDto dto = personUseCase.getById(id);

        return ResponseEntity.ok(
                DefaultResponseDto.<PersonResponseDto>builder()
                        .message("Información encontrada")
                        .status(HttpStatus.OK)
                        .details("Información encontrada")
                        .data(dto)
                        .build()
        );
    }

    @GetMapping("/get-all")
    public ResponseEntity<DefaultResponseDto<List<PersonResponseDto>>> getAllPersons() {
        List<PersonResponseDto> persons = personUseCase.getAll();

        return ResponseEntity.ok(
                DefaultResponseDto.<List<PersonResponseDto>>builder()
                        .message("Personas encontradas")
                        .status(HttpStatus.OK)
                        .details("Listado completo")
                        .data(persons)
                        .build()
        );
    }

    @GetMapping("/get-by-type/{type}")
    public ResponseEntity<DefaultResponseDto<List<PersonInterfaceResponseDto>>> getPersonsByType(
            @PathVariable("type") String type) {

        List<PersonInterfaceResponseDto> persons = personUseCase.getByType(type);

        return ResponseEntity.ok(
                DefaultResponseDto.<List<PersonInterfaceResponseDto>>builder()
                        .message("Personas encontradas por tipo")
                        .status(HttpStatus.OK)
                        .details("Listado filtrado por tipo: " + type)
                        .data(persons)
                        .build()
        );
    }

    @GetMapping("/get-by-zona/{type}/{zona}")
    public ResponseEntity<DefaultResponseDto<List<PersonInterfaceResponseDto>>> getPersonsByZona(
            @PathVariable("type") String type, @PathVariable("zona") String zona) {

        List<PersonInterfaceResponseDto> persons = personUseCase.getByZona(type,zona);

        return ResponseEntity.ok(
                DefaultResponseDto.<List<PersonInterfaceResponseDto>>builder()
                        .message("Personas encontradas por zona")
                        .status(HttpStatus.OK)
                        .details("Listado filtrado por zona: " + zona)
                        .data(persons)
                        .build()
        );
    }


    @PutMapping("/delete/{id}")
    public ResponseEntity<DefaultResponseDto<String>> deletePerson(
            @PathVariable Long id) {

        personUseCase.delete(id);

        return ResponseEntity.ok(
                DefaultResponseDto.<String>builder()
                        .message("Persona inactivada")
                        .status(HttpStatus.OK)
                        .details("La persona y el usuario fueron marcados como inactivos")
                        .data("OK")
                        .build()
        );
    }

    @PutMapping("/person/reactivate/{id}")
    public ResponseEntity<DefaultResponseDto<PersonResponseDto>> reactivatePerson(@PathVariable Long id) {
        PersonResponseDto dto = personUseCase.reactivate(id);

        return ResponseEntity.ok(
                DefaultResponseDto.<PersonResponseDto>builder()
                        .message("Persona reactivada correctamente")
                        .status(HttpStatus.OK)
                        .details("Se reactivó la persona y su usuario asociado")
                        .data(dto)
                        .build()
        );
    }
    @PutMapping("/status/{id}")
    public ResponseEntity<DefaultResponseDto<PersonResponseDto>> toggleStatus(
            @PathVariable Long id,
            @RequestParam boolean status) {

        PersonResponseDto dto = personUseCase.toggleStatus(id, status);

        return ResponseEntity.ok(
                DefaultResponseDto.<PersonResponseDto>builder()
                        .message("Estado actualizado correctamente")
                        .status(HttpStatus.OK)
                        .details("El estado de la persona fue actualizado a " + (status ? "Activo" : "Inactivo"))
                        .data(dto)
                        .build()
        );
    }


}
