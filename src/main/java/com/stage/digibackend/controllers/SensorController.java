package com.stage.digibackend.controllers;

import com.stage.digibackend.Collections.Device;
import com.stage.digibackend.Collections.Sensor;
import com.stage.digibackend.services.ISensorService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sensor")
@CrossOrigin(origins = "*")
public class SensorController {

    @Autowired
    ISensorService iSensorService ;


    @PostMapping( "/addSensor")
    String addSensor(@RequestBody Sensor sensor){

        return iSensorService.addSensor(sensor);
    }


/*
    @PostMapping("/addSensors")
    String addSensors(@RequestBody List<Sensor> sensorList){
return iSensorService.addSensors(sensorList);
    }
*/
    @PutMapping("/updateSensor/{idSensor}")
    String updateSensor(@RequestBody Sensor sensor, @PathVariable String idSensor)
    {
        return iSensorService.updateSensor(sensor,idSensor);
    }

    @DeleteMapping("/deleteSensor/{sensorId}")
    String deleteSensor(@PathVariable String sensorId){
return iSensorService.deleteSensor(sensorId);
    }

    @GetMapping("/getSensorById/{sensorId}")
    Sensor getSensor(@PathVariable String sensorId)
    {
        return iSensorService.getSensor(sensorId);
    }

    @GetMapping("/getAllSensorsById")
    String getAllSensors(@RequestBody List<String> sensorIds)
    {
        return iSensorService.getAllSensors(sensorIds);
    }

    @GetMapping("/getAllSensors")
    List<Sensor> getAllSensors(){
        return iSensorService.getAllSensors();
}

    @GetMapping("/getAllSensorsSignal420")
    public List<Sensor> getAllSensorsSignal420() {
        return iSensorService.getAllSensorsSignal420();
    }

}
