package com.fererlab.service;

import com.fererlab.event.Request;

public interface ScriptingService {

    Object execute(String methodName, Request request);

}
