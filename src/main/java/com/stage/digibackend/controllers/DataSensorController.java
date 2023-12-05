package com.stage.digibackend.controllers;

import com.mongodb.lang.Nullable;
import com.stage.digibackend.Collections.DataSensor;
import com.stage.digibackend.services.IDataSensorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/dataSensor")
@CrossOrigin(origins = "*")
public class DataSensorController {

    @Autowired
    IDataSensorService iDataSensorService ;

    @GetMapping("/affecteSensorDevice/{idSensor}/{idDevice}")
    public DataSensor affecteSensorDevice (@PathVariable String idSensor, @PathVariable String idDevice){
        return iDataSensorService.affecteSensorDevice(idSensor,idDevice);
    }

    @PutMapping("loadDataInSensorDevice/{idSensor}/{idDevice}")
    public DataSensor loadDataInSensorDevice(@PathVariable String idSensor,
                                             @PathVariable String idDevice,
                                             @RequestBody @Nullable DataSensor dataSensor) {
        return iDataSensorService.loadDataInSensorDevice(idSensor,idDevice,dataSensor.getData(),dataSensor.getGrowthStatus(),dataSensor.getLatestUpdate());
    }

    @GetMapping("/generateDataSensorHistoriquePdf/{dataSensorId}")
    public ResponseEntity<ByteArrayResource> generateDataSensorHistoriquePdf(@PathVariable String dataSensorId) throws IOException {

        byte[] pdfBytes = iDataSensorService.generateDataSensorHistoriquePdf(dataSensorId);

        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=historique.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(pdfBytes.length)
                .body(resource);
    }

    @GetMapping("/findAllDataSensors")
    List<DataSensor> findAllDataSensors(){
        return iDataSensorService.findAllDataSensors() ;
    }

    @GetMapping("/findDataSensorById/{idDataSensor}")
    DataSensor findDataSensorById(@PathVariable String idDataSensor){
        return iDataSensorService.findDataSensorById(idDataSensor);
    }

    @DeleteMapping("deleteDataSensorById/{idDataSensor}")
    void deleteDataSensorById(@PathVariable String idDataSensor){
        iDataSensorService.deleteDataSensorById(idDataSensor);
    }
    @GetMapping("DataId/{idSensor}/{idDevice}")
    public String findByTwoId(@PathVariable String idSensor,@PathVariable String idDevice) {
        String id= iDataSensorService.findByTwoId(idSensor,idDevice);
        System.out.println(id);
        return id;
    }

}
