package com.recaudo.api.domain.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DailyCollectionDTO {

    private DailyCollectionProjection data;
    private List<DailyCollectionRespaldoProjection> recaudos;
    private CreditRatingDTO ratingCredit; // ← NUEVO


    public boolean tieneInterestMora() {
        return data.getInterestMora() != null
                && data.getInterestMora().compareTo(BigDecimal.ZERO) > 0;
    }

}
