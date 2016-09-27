package com.fererlab.event;

public enum Status {

    SUCCESS("SUCCESS"),
    ERROR("ERROR");

    private final String message;

    Status(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
