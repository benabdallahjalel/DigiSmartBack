package com.stage.digibackend.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CsvData {
    private String historyId;
    private String startDate;
    private String endDate;
    private String deviceName;
    private String sensorName;
    private double data;
    private String growthStatus;

}
