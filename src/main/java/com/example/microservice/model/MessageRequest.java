package com.example.microservice.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

public class MessageRequest {

    @NotNull(message = "ID cannot be null")
    private String id;

    @NotEmpty(message = "Payload cannot be empty")
    @Size(min = 1, max = 255, message = "Payload must be between 1 and 255 characters")
    private String payload;

    private Map<String, String> headers;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public String toString() {
        return "MessageRequest{" +
                "id='" + id + '\'' +
                ", payload='" + payload + '\'' +
                ", headers=" + headers +
                '}';
    }
}