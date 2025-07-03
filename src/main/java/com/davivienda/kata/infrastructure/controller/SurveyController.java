package com.davivienda.kata.infrastructure.controller;

import com.davivienda.kata.domain.model.dto.response.DefaultResponseDto;
import com.davivienda.kata.domain.model.dto.response.LoginResponseDto;
import com.davivienda.kata.domain.model.dto.rest_api.CreateSurveyDto;
import com.davivienda.kata.domain.model.dto.rest_api.LoginDto;
import com.davivienda.kata.domain.model.dto.rest_api.RegisterDto;
import com.davivienda.kata.domain.usecase.RegisterUseCase;
import com.davivienda.kata.domain.usecase.SurveyUseCase;
import com.davivienda.kata.exception.BadRequestException;
import com.davivienda.kata.infrastructure.helper.security.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/survey")
@Tag(name = "Auth Controller", description = "Authentication and register users")
@AllArgsConstructor
public class SurveyController {

    private final SurveyUseCase surveyUseCase;

    @Operation(
            summary = "Get Surveys",
            description = "Return User Created Surveys",
            responses = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "failure")
            }
    )
    @GetMapping("/get-all")
    public ResponseEntity<DefaultResponseDto<?>> getAll(Authentication authentication){
        Long userId = (Long) authentication.getDetails();
        return ResponseEntity.ok(
                DefaultResponseDto.builder()
                        .message("Surveys")
                        .status(HttpStatus.OK)
                        .details("Surveys.")
                        .data(surveyUseCase.getAll(userId))
                        .build()
                );
    }

    @Operation(
            summary = "Get Surveys",
            description = "Return User Created Surveys",
            responses = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "failure")
            }
    )
    @GetMapping("/get/{id}")
    public ResponseEntity<DefaultResponseDto<?>> get(@PathVariable("id") Long surveyId, Authentication authentication){
        Long userId = (Long) authentication.getDetails();
        return ResponseEntity.ok(
                DefaultResponseDto.builder()
                        .message("Surveys")
                        .status(HttpStatus.OK)
                        .details("Surveys.")
                        .data(surveyUseCase.get(surveyId))
                        .build()
        );
    }

    @Operation(
            summary = "Save Surveys",
            description = "Return User Created Surveys",
            responses = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "failure")
            }
    )
    @PostMapping("/save")
    public ResponseEntity<DefaultResponseDto<?>> save(@RequestBody CreateSurveyDto survey, Authentication authentication){
        Long userId = (Long) authentication.getDetails();
        return ResponseEntity.ok(
                DefaultResponseDto.builder()
                        .message("Surveys")
                        .status(HttpStatus.OK)
                        .details("Surveys.")
                        .data(surveyUseCase.save(userId, survey))
                        .build()
        );
    }

    @Operation(
            summary = "Delete Surveys",
            description = "Delete Surveys",
            responses = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "failure")
            }
    )
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<DefaultResponseDto<?>> save(@PathVariable("id") Long surveyId, Authentication authentication){
        Long userId = (Long) authentication.getDetails();
        return ResponseEntity.ok(
                DefaultResponseDto.builder()
                        .message("Surveys")
                        .status(HttpStatus.OK)
                        .details("Surveys.")
                        .data(surveyUseCase.delete(surveyId))
                        .build()
        );
    }

    @Operation(
            summary = "Save Surveys",
            description = "Return User Created Surveys",
            responses = {
                    @ApiResponse(responseCode = "200", description = "success"),
                    @ApiResponse(responseCode = "404", description = "failure")
            }
    )
    @PostMapping("/answer")
    public ResponseEntity<DefaultResponseDto<?>> answer(@RequestBody CreateSurveyDto survey, Authentication authentication){
        Long userId = (Long) authentication.getDetails();
        return ResponseEntity.ok(
                DefaultResponseDto.builder()
                        .message("Surveys")
                        .status(HttpStatus.OK)
                        .details("Surveys.")
                        .data(surveyUseCase.saveAnswer(survey))
                        .build()
        );
    }

}
