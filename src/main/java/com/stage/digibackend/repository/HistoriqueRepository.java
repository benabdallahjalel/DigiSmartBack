package com.stage.digibackend.repository;

import com.stage.digibackend.Collections.Device;
import com.stage.digibackend.Collections.Historique;
import com.stage.digibackend.Collections.Sensor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface HistoriqueRepository extends MongoRepository<Historique, String> {

    List<Historique> findAllByDataSensorDevice(Device device);
    List<Historique> findAllByDataSensorDeviceAndDataSensorSensor(Device device, Sensor sensor);


    List<Historique> findAllByDataSensorDevice(Device device, PageRequest of);

    List<Historique> findAllByDataSensorDeviceAndDateBetween(Device device, LocalDate startDate, LocalDate endDate);
    Page<Historique> findAllByDataSensorDevice(String idDevice, Pageable pageable);

    @Query(value="{'dataSensor.device': ?0}")
    List<Historique> findHistoriqueForLastMonth(Device device, LocalDateTime lastMonthDate);


}
