package com.fererlab.event;

import com.owlike.genson.Genson;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;
import java.util.Map;

@XmlRootElement
public class Event {

    private Head head = new Head();
    private Map<String, Object> body = new HashMap<>();

    public Head getHead() {
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }

    public Map<String, Object> getBody() {
        return body;
    }

    public void setBody(Map<String, Object> body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return new Genson().serialize(this);
    }
}
