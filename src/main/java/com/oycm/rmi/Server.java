package com.oycm.rmi;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.rmi.RemoteException;

/**
 * @author ouyangcm
 * create 2024/12/11 15:56
 */
public class Server {

    /**
     * rmiregistry 1099 & class下执行
     * @param args
     */
    public static void main(String[] args) {
        try {
            // 创建远程对象
            HelloService helloService = new HelloServiceImpl();

            // 设置 JNDI 环境
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.rmi.registry.RegistryContextFactory");
            System.setProperty(Context.PROVIDER_URL, "rmi://localhost:1099");

            // 获取 JNDI 初始上下文
            Context context = new InitialContext();

            // 将远程对象绑定到 JNDI
            context.bind("java:comp/env/HelloService", helloService);
//            context.bind("rmi://localhost:1099/HelloService", helloService);
            System.out.println(Thread.activeCount());

            System.out.println("RMI service started and bound to JNDI!");
        } catch (RemoteException | NamingException e) {
            e.printStackTrace();
        }
    }
}
