package com.stage.digibackend.repository;

import com.stage.digibackend.Collections.Device;
import com.stage.digibackend.Collections.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeviceRepository extends MongoRepository<Device, String> {

    Device findBymacAdress(String mac_add);

}
