package com.stage.digibackend.services;

import com.stage.digibackend.Collections.Device;
import com.stage.digibackend.Collections.Sensor;
import com.stage.digibackend.dto.deviceResponse;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.List;

public interface IDeviceService {
    String addDevice(Device device);
    List<Device> getAllDevices();
    Device getDeviceById(String deviceId);
    Device getDeviceByMacAdd(String add_mac);
    deviceResponse updateDevice(String deviceId, Device deviceRequest);
    String deleteDevice(String deviceId);
    String affectDeviceToAdmin(String deviceId,String adminId) throws MessagingException, UnsupportedEncodingException;
    String affectDeviceToClient(String deviceId, String clientId);
List<Sensor> getSensorsList(String deviceId);
List<Device> getAdminDevices(String adminId);
    List<Device> getClientDevices(String clientId);
String setDeviceState(String deviceId);
void checkAndSendNotification(String deviceId);
}
