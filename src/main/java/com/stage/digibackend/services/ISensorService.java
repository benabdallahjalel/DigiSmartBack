package com.stage.digibackend.services;

import com.stage.digibackend.Collections.Device;
import com.stage.digibackend.Collections.Sensor;

import java.util.List;

public interface ISensorService {
    String addSensor(Sensor sensor);
    String updateSensor(Sensor sensor,String idSensor);
    String deleteSensor(String sensorId);
    Sensor getSensor(String sensorId);
    String getAllSensors( List<String> sensorIds);
    List<Sensor> getAllSensors();
    List<Sensor> getAllSensorsSignal420();



}
