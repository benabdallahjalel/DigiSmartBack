package com.stage.digibackend.Collections;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Document(collection = "historique")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Historique {
    @Id
    private String id;
    @NotBlank
    private LocalDateTime date;
    @NotBlank
    private String action;
    @NotBlank
    //@DBRef
    private DataSensor dataSensor;


}
