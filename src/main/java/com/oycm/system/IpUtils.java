package com.oycm.system;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * @author ouyangcm
 * create 2024/11/27 13:59
 */
public class IpUtils {


    // ip addr会显示网络接口信息，inet 后面跟的是ip地址信息
    // ifconfig inet后面跟的是ip地址信息
    /**
     * 获取第一个非回环、非IPv6的IPv4地址(多个ip会随机获取1个)
     * @return
     */
    public static String getInetAddress() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            InetAddress address;
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = interfaces.nextElement();
                Enumeration<InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    address = addresses.nextElement();

                    // 非回环地址 && 非IPv6地址
                    if (!address.isLoopbackAddress() && !address.getHostAddress().contains(":")) {
                        return address.getHostAddress();
                    }
                }
            }
            return null;
        } catch (Throwable var4) {
            return null;
        }
    }
}
