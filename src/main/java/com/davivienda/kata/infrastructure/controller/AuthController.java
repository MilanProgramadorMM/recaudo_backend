package com.davivienda.kata.infrastructure.controller;

import com.davivienda.kata.domain.model.dto.response.DefaultResponseDto;
import com.davivienda.kata.domain.model.dto.response.LoginResponseDto;
import com.davivienda.kata.domain.model.dto.rest_api.LoginDto;
import com.davivienda.kata.domain.model.dto.rest_api.RegisterDto;
import com.davivienda.kata.domain.model.entity.UserEntity;
import com.davivienda.kata.domain.usecase.RegisterUseCase;
import com.davivienda.kata.exception.BadRequestException;
import com.davivienda.kata.infrastructure.adapter.UserDetailsImpl;
import com.davivienda.kata.infrastructure.helper.security.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth Controller", description = "Authentication and register users")
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtUtil jwtService;
    private final RegisterUseCase registerUseCase;

    @Operation(
            summary = "Authenticate User",
            description = "Return JWT Token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authentication success"),
                    @ApiResponse(responseCode = "404", description = "Authentication failure")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<DefaultResponseDto> login(
            @RequestBody @Valid LoginDto userData, BindingResult
            bindingResult) throws Exception {

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(userData.getUsername(), userData.getPassword())
        );
        UserDetailsImpl user = (UserDetailsImpl) auth.getPrincipal();
        String token = jwtService.generateToken(user.getUserEntity());
        if (bindingResult.hasErrors())
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());

        return ResponseEntity.ok(
                DefaultResponseDto.builder()
                        .message("Usuario logueado")
                        .status(HttpStatus.OK)
                        .details("Nos alegra tenerte de nuevo.")
                        .data(LoginResponseDto.builder().user(userData.getUsername()).token(token).build())
                        .build()
                );
    }

    @Operation(
            summary = "Authenticate User",
            description = "Return JWT Token",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authentication success"),
                    @ApiResponse(responseCode = "404", description = "Authentication failure")
            }
    )
    @PostMapping("/register")
    public ResponseEntity<DefaultResponseDto<?>> register(
            @RequestBody @Valid RegisterDto registerDto, BindingResult
                    bindingResult) throws Exception {
        if (bindingResult.hasErrors())
            throw new BadRequestException(bindingResult.getAllErrors().get(0).getDefaultMessage());

        return registerUseCase.register(registerDto);
    }

}
