package com.stage.digibackend.Collections;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import javax.validation.constraints.NotBlank;
import java.util.List;
@Document(collection = "devices")
@AllArgsConstructor
@Data
@Builder
public class Device {
    @Id
    private String deviceId ;
    @NotBlank
    private String macAdress ;
    @NotBlank
    private String nom ;
    @NotBlank
    private String Description ;
    @NotBlank
    private Double lat;
    @NotBlank
    private Double lng;
    private List<String> sensorList ;
    private String DeviceCode;
    private String Location;
    private Boolean active;
    private String idAdmin;
    private String idClient;


}
