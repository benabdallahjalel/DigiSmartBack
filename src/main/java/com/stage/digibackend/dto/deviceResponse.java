package com.stage.digibackend.dto;

import com.stage.digibackend.Collections.Device;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class deviceResponse {
    private OtpStatus status;
    private Device device;

}
