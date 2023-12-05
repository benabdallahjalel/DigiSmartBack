package com.stage.digibackend.Collections;

import com.stage.digibackend.Enumeration.EUnite;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import javax.validation.constraints.NotBlank;

@Document(collection = "sensors")
@AllArgsConstructor
@Data
@Builder
public class Sensor {
    @Id
    private String sensorId ;
    @NotBlank
    private String sensorName ;
    private Double rangeMin ;
    private Double rangeMax ;
    private EUnite unit ;
    private String symboleUnite ;
    private Boolean signal ;
    private Double a ;
    private Double b ;
    private Boolean isPulse ;
    private Double pulseValue ;
    private Double pulseInit ;
}
