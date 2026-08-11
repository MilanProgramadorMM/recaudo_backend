package com.recaudo.api.domain.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DailyCollectionResultDTO {
    private List<DailyCollectionDTO> cobroHoy;
    private List<DailyCollectionDTO> carteraZona;   // ← unificado
    private List<DailyCollectionDTO> enMora;         // ← unificado
}