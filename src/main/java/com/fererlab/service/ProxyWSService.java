package com.fererlab.service;

public interface ProxyWSService extends WSService {

    Object execute(String methodName, Object param);

}
