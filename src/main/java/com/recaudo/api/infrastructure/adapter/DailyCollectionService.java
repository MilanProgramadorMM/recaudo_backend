package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.model.dto.response.DailyCollectionProjection;
import com.recaudo.api.infrastructure.repository.DailyCollectionRepository;
import com.recaudo.api.infrastructure.repository.PersonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DailyCollectionService {

    @Autowired
    private DailyCollectionRepository dailyCollectionRepository;

    @Autowired
    private PersonRepository personRepository;

    public List<DailyCollectionProjection> getDailyCollection(
            String username,
            Long personId,
            LocalDate date
    ) {
        Long zona = personRepository.getZonasIdByAsesor(personId).stream().findFirst().orElse(null);
        return dailyCollectionRepository.findDailyCollection(zona, date);
    }
}
