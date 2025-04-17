package com.oycm.trace;

/**
 * @author ouyangcm
 * create 2024/11/28 13:56
 */

import com.oycm.system.IpUtils;
import com.oycm.system.ProgramUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TraceIdGenerator {

    private static Logger log = LoggerFactory.getLogger(TraceIdGenerator.class);
    private static String IP_16 = "00000000";
    private static AtomicInteger count = new AtomicInteger(1000);

    public static String P_ID_CACHE = null;

    static {

        String ipAddress = IpUtils.getInetAddress();
        if (ipAddress != null) {
            IP_16 = getIP_16(ipAddress);
        }

        P_ID_CACHE = ProgramUtils.getPID();

        if (P_ID_CACHE.length() < 7) {
            // todo 不足7位的操作?
        }

    }

    /**
     * traceId结构: 8(ip_16进制) + 13(当前系统时间戳) + 4(当前机器的处理数) + pid(不固定,一般4-6位) = 25 -> 32?
     * 是否固定32,不足后面补?
     * @return
     */
    public static String getTraceId() {

        // 根据需要指定长度,避免扩容
        StringBuilder traceId = new StringBuilder(34);

        // ip对应16进制的值
        traceId.append(IP_16);

        // 当前系统的时间(距离1970的毫秒数) 13位
        traceId.append(System.currentTimeMillis());

        // 4位当前机器处理数
        traceId.append(getNextId());

        traceId.append(P_ID_CACHE);


        return traceId.toString();
    }

    /**
     * ipv4的范围是0-255，转换成16进制最长为2为
     * 把ipv4地址数字转换成4对16进制的字符串，不足2为前面补0
     * @param ip
     * @return
     */
    private static String getIP_16(String ip) {
        String[] ips = ip.split("\\.");
        StringBuilder sb = new StringBuilder();
        for (String column : ips) {
            String hex = Integer.toHexString(Integer.parseInt(column));
            if (hex.length() == 1) {
                sb.append('0').append(hex);
            } else {
                sb.append(hex);
            }

        }
        return sb.toString();
    }

    private static int getNextId() {
        for (; ; ) {
            int current = count.get();
            int next = (current > 9000) ? 1000 : current + 1;
            if (count.compareAndSet(current, next)) {
                return next;
            }
        }
    }

    /**
     * 解析traceId的结果格式: ipv4 + 日期(yyyy-MM-dd HH:mm:ss) + 计数器 + pid
     * @param traceId
     * @return
     */

    public static String parseTraceId(String traceId) {

        // 服务所在的ip
        char[] ipChars = traceId.substring(0, 8).toCharArray();
        // (?<=\\G.{2}) 可以使用正则
        List<String> ips = new ArrayList<>();

        for (int i = 0; i < ipChars.length; i+=2) {
            ips.add(new String(ipChars, i, 2));
        }
        String ipString = ips.stream().map(s -> Integer.valueOf(s, 16).toString()).collect(Collectors.joining("."));



        // 时间戳解析成日期
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeMills = traceId.substring(8, 21);
        String timeString = sdf.format(new Date(Long.parseLong(timeMills)));

        // count 计数器
        String count = traceId.substring(21, 25);

        // 进程pid
        String pid = traceId.substring(25);


        List<String> resultList = new ArrayList<>();
        resultList.add("pid=" + pid);
        resultList.add("date=" + timeString);
        resultList.add("ip=" + ipString);
        resultList.add("count=" + count);
        log.info("ip: {}, date: {}, pid: {}, count: {}", ipString, timeString, pid, count);
        return resultList.stream().collect(Collectors.joining(", "));

    }
}
