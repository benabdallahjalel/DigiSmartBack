package com.stage.digibackend.services;

import com.stage.digibackend.Collections.DataSensor;
import com.stage.digibackend.Enumeration.GrowthStatus;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

public interface IDataSensorService {

    List<DataSensor> findAllDataSensors();
    DataSensor findDataSensorById(String idDataSensor) ;
    void deleteDataSensorById(String idDataSensor) ;
    DataSensor affecteSensorDevice (String idSensor, String idDevice);
    DataSensor loadDataInSensorDevice (String idSensor, String idDevice, Double data, GrowthStatus growthStatus, LocalDateTime latestUpdate);
    byte[] generateDataSensorHistoriquePdf(String dataSensorId) throws IOException ;
    String findByTwoId (String idSensor, String idDevice);
}
