package com.mss.project.booking_service.service.impl;
import com.mss.project.booking_service.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebSocketServiceImpl implements WebSocketService {

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