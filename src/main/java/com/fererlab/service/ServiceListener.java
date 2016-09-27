package com.fererlab.service;

import com.fererlab.event.Event;

public interface ServiceListener {

    void onCreate();

    void onDeploy();

    void onReload();

    void onDestroy();

    void onHandle(Event event);

}
