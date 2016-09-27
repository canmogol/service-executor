package com.fererlab.event;

import com.owlike.genson.Genson;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Head {

    private String id;
    private String type;
    private Long createdAt;
    private Origin origin = new Origin();

    public Origin getOrigin() {
        return origin;
    }

    public void setOrigin(Origin origin) {
        this.origin = origin;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return new Genson().serialize(this);
    }
}
