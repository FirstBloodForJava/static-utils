package com.oycm.rmi;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author ouyangcm
 * create 2024/12/11 15:55
 */
public class HelloServiceImpl extends UnicastRemoteObject implements HelloService {
    protected HelloServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public String sayHello(String name) throws RemoteException {
        return "Hello, " + name + "!";
    }
}
