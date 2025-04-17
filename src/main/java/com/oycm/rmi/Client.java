package com.oycm.rmi;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.rmi.RemoteException;

/**
 * @author ouyangcm
 * create 2024/12/11 16:03
 */
public class Client {
    public static void main(String[] args) {
        try {
            // 设置 JNDI 环境
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
            System.setProperty(Context.PROVIDER_URL, "rmi://localhost:1099");

            // 获取 JNDI 初始上下文
            Context context = new InitialContext();

            // 查找远程对象
            HelloService helloService = (HelloService) context.lookup("java:comp/env/HelloService");
            //HelloService helloService = (HelloService) context.lookup("rmi://localhost:1099/HelloService");

            // 调用远程方法
            String response = helloService.sayHello("World");
            System.out.println(response);
            System.out.println(Thread.activeCount());
        } catch (NamingException | RemoteException e) {
            e.printStackTrace();
        }
    }
}
