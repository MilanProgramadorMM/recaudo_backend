package com.recaudo.api.infrastructure.adapter;

import com.recaudo.api.domain.model.dto.response.DailyCollectionProjection;
import com.recaudo.api.infrastructure.repository.DailyCollectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class DailyCollectionService {

    @Autowired
    private DailyCollectionRepository dailyCollectionRepository;

    public List<DailyCollectionProjection> getDailyCollection(
            String username,
            LocalDate date
    ) {
        return dailyCollectionRepository.findDailyCollection(username, date);
    }
}
