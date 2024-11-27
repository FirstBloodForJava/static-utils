package com.oycm.system;

import java.lang.management.ManagementFactory;

/**
 * @author ouyangcm
 * create 2024/11/27 14:02
 */
public class ProgramUtils {

    /**
     * 获取当前java程序的pid
     * @return
     */
    public static String getPID() {

        // 获取当前运行的 Java 虚拟机的名称,通常是<pid>@<hostname>格式
        String processName = ManagementFactory.getRuntimeMXBean().getName();
        System.out.println(processName);
        if (processName == null || "".equals(processName)) {
            return "";
        } else {
            String[] processSplitName = processName.split("@");
            if (processSplitName.length == 0) {
                return "";
            } else {
                String pid = processSplitName[0];
                if (pid == null || "".equals(pid)) {
                    return "";
                } else {
                    return pid;
                }
            }
        }
    }

}
