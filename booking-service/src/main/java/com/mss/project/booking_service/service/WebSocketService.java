package com.mss.project.booking_service.service;

public interface WebSocketService {
    void sendMessage(String topic, long orderCode, String status, String type);
}
