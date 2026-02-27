package com.recaudo.api.domain.usecase;

import com.recaudo.api.domain.gateway.CreditIGateway;
import com.recaudo.api.domain.model.dto.response.CreditFullResponseDto;
import com.recaudo.api.domain.model.dto.response.CreditResponseDto;
import com.recaudo.api.domain.model.dto.rest_api.CreditRegisterDto;
import com.recaudo.api.domain.model.entity.CreditEntity;
import com.recaudo.api.infrastructure.helper.security.jwt.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CreditUseCase {

    @Autowired
    private CreditIGateway creditGateway;

    @Autowired
    JwtUtil jwtUtil;

    public List<CreditFullResponseDto> getAll() {
        return creditGateway.getAll();
    }

    public List<CreditFullResponseDto> getCredits(String token) {
        try {
            String role = jwtUtil.getClaimFromToken(token, "role", String.class);
            if ("Asesor".equals(role)) {
                String username = jwtUtil.getUsernameFromToken(token);
                return creditGateway.getByUsername(username);
            }
            return creditGateway.getAll();
        } catch (Exception e) {
            log.error("Error al obtener los créditos", e);
            throw new RuntimeException("Error al obtener los créditos", e);
        }
    }

    public CreditResponseDto getById(Long id) {
        return creditGateway.getById(id);
    }

    public CreditResponseDto getByPersonId(Long personId) {
        return creditGateway.getByPersonId(personId);
    }

    public CreditResponseDto create(CreditRegisterDto dto) {
        return creditGateway.create(dto);
    }
}