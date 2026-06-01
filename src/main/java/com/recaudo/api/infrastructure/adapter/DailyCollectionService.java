package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.model.dto.response.CreditRatingDTO;
import com.recaudo.api.domain.model.dto.response.DailyCollectionDTO;
import com.recaudo.api.domain.model.dto.response.DailyCollectionProjection;
import com.recaudo.api.domain.model.dto.response.DailyCollectionRespaldoProjection;
import com.recaudo.api.infrastructure.repository.CreditRatingRangeRepository;
import com.recaudo.api.infrastructure.repository.DailyCollectionRepository;
import com.recaudo.api.infrastructure.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DailyCollectionService {

    @Autowired
    private DailyCollectionRepository dailyCollectionRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private CreditRatingRangeRepository creditRatingRangeRepository;


    public List<DailyCollectionDTO> getDailyCollection(
            String username,
            Long personId,
            LocalDate date
    ) {
        List<Long> zonas = personRepository.getZonasIdByAsesor(personId);
        List<DailyCollectionProjection> dailyData = dailyCollectionRepository.findDailyCollection(zonas, date);

        List<Long> creditIds = dailyData.stream()
                .map(DailyCollectionProjection::getCreditId)
                .distinct()
                .toList();

        List<DailyCollectionRespaldoProjection> recaudos = dailyCollectionRepository.finDailyCollectionRespaldo(creditIds);

        Map<Long, List<DailyCollectionRespaldoProjection>> mapa =
                recaudos.stream()
                        .collect(Collectors.groupingBy(
                                DailyCollectionRespaldoProjection::getCreditId
                        ));

        return dailyData.stream()
                .map(b -> DailyCollectionDTO.builder()
                        .data(b)
                        .recaudos(mapa.getOrDefault(b.getCreditId(), List.of()))
                        .ratingCredit(calcularCalificacion(b.getDiasMora()))
                        .build()
                )
                .toList();
    }

    private CreditRatingDTO calcularCalificacion(Integer diasMora) {
        if (diasMora == null || diasMora < 0) diasMora = 0;

        return creditRatingRangeRepository.findByDiasMora(diasMora)
                .map(r -> new CreditRatingDTO(r.getRatingValue(), r.getStart(), r.getEnd()))
                .orElse(new CreditRatingDTO("N/A", null, null));
    }
}
