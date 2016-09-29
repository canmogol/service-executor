package com.fererlab.service;

import com.fererlab.event.Event;

public interface Service {

    Object handle(Event event);

}
