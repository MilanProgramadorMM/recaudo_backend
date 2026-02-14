package com.recaudo.api.domain.gateway;

import com.recaudo.api.domain.model.entity.CreditIntentionDocumentEntity;
import com.recaudo.api.infrastructure.helper.util.DocumentMetadata;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CreditIntentionDocumentGateway {

    List<CreditIntentionDocumentEntity> saveDocuments( Long intentionId,
                                                       List<MultipartFile> files,
                                                       List<DocumentMetadata> metadata) throws IOException;

    List<CreditIntentionDocumentEntity> getDocumentsByIntentionId(Long intentionId);

    CreditIntentionDocumentEntity getCedulaByIntentionId(Long intentionId);


}
