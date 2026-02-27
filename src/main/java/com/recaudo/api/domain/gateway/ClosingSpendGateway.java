package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.dto.response.ClosingSpendFileDto;
import com.recaudo.api.domain.model.dto.response.ClosingSpendResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ClosingSpendGateway {

    ClosingSpendResponseDto saveSpend(
            Long closingId,
            Long spendTypeId,
            Long zonaId,
            Double amount,
            MultipartFile file,
            String description
    ) throws IOException;

    List<ClosingSpendResponseDto> getSpendsByClosingId(Long closingId);

    public ClosingSpendResponseDto getSpendsByClosingAndType(Long closingId, Long typeId);

    void deactivateSpend(Long spendId);
    ClosingSpendResponseDto updateSpend(
            Long spendId,
            Long spendTypeId,
            Double amount,
            MultipartFile file,
            String description
    ) throws IOException;

    ClosingSpendResponseDto getSpendById(Long spendId);

    ClosingSpendFileDto getFileBySpendId(Long spendId);
}
