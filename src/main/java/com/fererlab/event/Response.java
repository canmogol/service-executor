package com.fererlab.event;

public class Response {

    private Object content;
    private String status = Status.SUCCESS.getMessage();

    public Response() {
    }

    public Response(Object content, String status) {
        this.content = content;
        this.status = status;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
