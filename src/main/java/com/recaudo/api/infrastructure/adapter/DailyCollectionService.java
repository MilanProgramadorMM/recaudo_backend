package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.model.dto.response.DailyCollectionDTO;
import com.recaudo.api.domain.model.dto.response.DailyCollectionProjection;
import com.recaudo.api.domain.model.dto.response.DailyCollectionRespaldoProjection;
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

    public List<DailyCollectionDTO> getDailyCollection(
            String username,
            Long personId,
            LocalDate date
    ) {
        Long zona = personRepository.getZonasIdByAsesor(personId).stream().findFirst().orElse(null);
        List<DailyCollectionProjection> dailyData = dailyCollectionRepository.findDailyCollection(zona, date);
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
                .map(b -> new DailyCollectionDTO(
                        b,
                        mapa.getOrDefault(b.getCreditId(), List.of())
                ))
                .toList();
    }
}
