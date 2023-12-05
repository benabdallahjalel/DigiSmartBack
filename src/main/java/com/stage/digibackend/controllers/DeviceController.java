package com.stage.digibackend.controllers;

import com.stage.digibackend.Collections.Device;

import com.stage.digibackend.Collections.Sensor;
import com.stage.digibackend.Collections.User;
import com.stage.digibackend.dto.deviceResponse;
import com.stage.digibackend.services.IDeviceService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.zip.DataFormatException;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@RequestMapping("/devices")
public class DeviceController {
    @Autowired
    IDeviceService ideviceService;
  //  @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")

    @PostMapping("/addDevice")
    public String addDevice(@RequestBody Device device)
    {
        return ideviceService.addDevice(device);
    }
    @GetMapping("/getAllDevices")
    public List<Device> getAllDevice() {
        return ideviceService.getAllDevices();
    }
    @GetMapping("getDeviceId/{deviceId}")
    public Device getDevice(@PathVariable String deviceId)
    {
        return ideviceService.getDeviceById(deviceId);
    }
    @GetMapping("getDeviceMac/{macadd}")
    public Device getDeviceByMac(@PathVariable String macadd)
    {
        return ideviceService.getDeviceByMacAdd(macadd);
    }
   // @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    @PutMapping("/updateDevice/{deviceId}")
    public deviceResponse updateDevice(@PathVariable String deviceId,@RequestBody Device device){return ideviceService.updateDevice(deviceId,device);}
   // @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")

    @DeleteMapping("/deleteDevice/{deviceId}")
    public String deleteDevice(@PathVariable String deviceId)
{
    return ideviceService.deleteDevice(deviceId);
}
   // @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")

    @PutMapping("/affectToAdmin/{deviceId}")
    public String affectToAdmin(@PathVariable String deviceId, @RequestBody String adminId) throws MessagingException, UnsupportedEncodingException {
        return ideviceService.affectDeviceToAdmin(deviceId,adminId);}
    @PutMapping("/affectToClient/{deviceId}")
    public String affectToClient(@PathVariable String deviceId, @RequestBody String clientId) {
        return ideviceService.affectDeviceToClient(deviceId,clientId);}
    @GetMapping("/getSensorList/{deviceId}")
    List<Sensor> getAllSensors(@PathVariable String deviceId){
        return ideviceService.getSensorsList(deviceId);
    }

    @GetMapping("/getAdminDevices/{adminId}")
    List<Device> getAdminDevices(@PathVariable String adminId){
        return ideviceService.getAdminDevices(adminId);
    }
    @GetMapping("/getClientDevices/{clientId}")
    List<Device> getClientDevices(@PathVariable String clientId){
        return ideviceService.getClientDevices(clientId);
    }
    @PutMapping("/setDeviceState/{deviceId}")
    public String setDeviceState(@PathVariable String deviceId){return ideviceService.setDeviceState(deviceId);}
}
