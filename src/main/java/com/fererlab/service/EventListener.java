package com.fererlab.service;

import com.fererlab.event.Event;

public interface EventListener {

    void onNotify(Event event);

    void onHandle(Event event);

    void onSend(Event event);

}
