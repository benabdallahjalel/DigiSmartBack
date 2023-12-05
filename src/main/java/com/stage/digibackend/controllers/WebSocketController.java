package com.stage.digibackend.controllers;

import com.stage.digibackend.services.IDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

@Controller
@CrossOrigin(origins = "*")

public class WebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    @Autowired
    IDeviceService ideviceService;
    @Autowired
    public WebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    @MessageMapping("/sendNotification/{deviceId}")
    public void sendNotification(@DestinationVariable String deviceId) {
        ideviceService.checkAndSendNotification(deviceId);
    }
}
