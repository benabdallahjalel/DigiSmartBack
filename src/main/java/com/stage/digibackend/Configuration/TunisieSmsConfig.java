package com.stage.digibackend.Configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "tunisiesms")
@Data
public class TunisieSmsConfig {
    private String sender;
    private String key;
//comment

}
