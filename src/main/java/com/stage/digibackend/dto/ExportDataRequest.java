package com.stage.digibackend.dto;

import lombok.Data;

import java.time.LocalDateTime;
@Data
public class ExportDataRequest {
    LocalDateTime startDate;
    LocalDateTime endDate;
}
