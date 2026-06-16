package com.recaudo.api.domain.model.version;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppVersionConfig {

    @Value("${app.version}")
    private String version;

    public String getVersion() {
        return version;
    }

}