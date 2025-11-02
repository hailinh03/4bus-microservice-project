package com.mss.project.trip_service.service.impl;

import com.mss.project.trip_service.service.IWebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebSocketService implements IWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void sendMessage(String topic, long orderCode, String status, String type) {
        Object message = Map.of(
                "orderCode", orderCode,
                "status", status,
                "type", type
        );
        messagingTemplate.convertAndSend(topic, message);
    }
}
