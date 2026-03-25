package com.recaudo.api.domain.usecase;

import com.recaudo.api.config.UseCase;
import com.recaudo.api.domain.gateway.CreditIntentionGateway;
import com.recaudo.api.domain.model.dto.response.*;
import com.recaudo.api.domain.model.dto.rest_api.*;
import com.recaudo.api.infrastructure.helper.security.jwt.JwtUtil;
import com.recaudo.api.infrastructure.helper.util.DocumentMetadata;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@AllArgsConstructor
@UseCase
public class CreditIntentionUseCase {

    private CreditIntentionGateway creditIntentionGateway;
    private CreditIntentionDocumentUseCaseJ documentUseCase;
    private JwtUtil jwtUtil;

    public List<SimulationResponseDto> simulationIntention(CalculateCreditIntentionDto data) {
        return creditIntentionGateway.simulate(data);
    }

    public CreditIntentionResponseDto create(CreditIntentionDto creditIntentionDto, String token, Long personId){
        return creditIntentionGateway.create(creditIntentionDto, token, personId);
    }

    public List<IntentionCreditResponseAllDto> getAll(){
        return creditIntentionGateway.getAll();
    }

    public List<IntentionCreditResponseAllDto> getAllIncludingClosed(String token) {
        try {
            String role = jwtUtil.getClaimFromToken(token, "role", String.class);

            if ("Asesor".equals(role)) {
                String username = jwtUtil.getUsernameFromToken(token);
                return creditIntentionGateway.getAllIncludingClosedByUsername(username); // ← usa el nuevo método
            }

            return creditIntentionGateway.getAllIncludingClosed(); // Admin / Backoffice ven todo
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener las intenciones", e);
        }
    }

    public List<IntentionCreditResponseAllDto> getById(Long id){
        return creditIntentionGateway.getById(id);
    }

    public CreditIntentionResponseDto updateDataCreditIntention(Long id, CreditIntentionUpdateDto dto){
        return creditIntentionGateway.updateDataCreditIntention(id,dto);
    }

    public CreditIntentionResponseDto updateDataClient(Long id, ClientDataCreditIntentionUpdateDto dto){
        return creditIntentionGateway.updateDataClient(id,dto);
    }

    public CreditIntentionResponseDto updateFechaTentativaClient(Long id, UpdateFechaTentativaCreditIntentionDto dto){
        return creditIntentionGateway.updateFechaTentativaCreditIntention(id,dto);
    }

    public CreditIntentionResponseDto createWithDocuments(
            CreditIntentionDto intention,
            List<MultipartFile> files,
            List<DocumentMetadata> metadata,
            String token,
            Long personId
    ) throws IOException {

        CreditIntentionResponseDto saved =
                creditIntentionGateway.create(intention, token, personId);

        if (files != null && !files.isEmpty()) {
            documentUseCase.saveDocument(
                    saved.getId(),
                    files,
                    metadata
            );
        }
        return saved;
    }

}
