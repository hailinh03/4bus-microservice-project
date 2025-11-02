package com.mss.project.trip_service.service;

public interface IWebSocketService {
    void sendMessage(String topic, long orderCode, String status, String type);
}
