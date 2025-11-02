package com.mss.project.user_service.dto.response;

import lombok.Data;

@Data
public class ErrorResponse {
    private boolean success;
    private String message;
    private String errorCode;
    private Object data;
    private Object errors;
}
