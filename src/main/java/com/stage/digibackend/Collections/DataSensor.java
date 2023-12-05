package com.stage.digibackend.Collections;

import com.stage.digibackend.Enumeration.GrowthStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.joda.time.DateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "dataSensor")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class DataSensor {

    @Id
    private String dataSensorId ;
    @DBRef
    private Sensor sensor ;
    @DBRef
    private Device device ;
    private LocalDateTime latestUpdate ;
    private GrowthStatus growthStatus ;
    private Double data ;
    private Double total ;




}
