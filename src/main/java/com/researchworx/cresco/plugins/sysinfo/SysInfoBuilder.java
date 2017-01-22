package com.researchworx.cresco.plugins.sysinfo;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSFileStore;
import oshi.util.FormatUtil;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class SysInfoBuilder {
    private static ConcurrentHashMap<String, String> info = new ConcurrentHashMap<>();

    SysInfoBuilder() {
        System.setProperty("java.net.preferIPv4Stack", "true");
        try {
            SystemInfo systemInfo = new SystemInfo();
            HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware();

            info.put("sys-os", systemInfo.getOperatingSystem().toString());
            info.put("cpu-core-count", String.valueOf(hardwareAbstractionLayer.getProcessors().length));
            info.put("cpu-sn", hardwareAbstractionLayer.getProcessors()[0].getSystemSerialNumber());
            info.put("cpu-summary", hardwareAbstractionLayer.getProcessors()[0].toString());

            try {
                info.put("cpu-ident", hardwareAbstractionLayer.getProcessors()[0].getIdentifier());
            }
            catch (Exception ex) {
                info.put("cpu-ident", "unknown");
            }
            try {
                info.put("cpu-sn-ident",hardwareAbstractionLayer.getProcessors()[0].getIdentifier());
            }
            catch(Exception ex) {
                info.put("cpu-sn-ident","unknown");
            }
            info.put("memory-total", String.valueOf(hardwareAbstractionLayer.getMemory().getTotal()));
            int nicCount = 0;
            StringBuilder nicStringBuilder = new StringBuilder();
            try {
                Enumeration<NetworkInterface> nicEnum = NetworkInterface.getNetworkInterfaces();
                while (nicEnum.hasMoreElements()) {
                    NetworkInterface nic = nicEnum.nextElement();
                    if (nic.isLoopback())
                        continue;
                    nicStringBuilder.append(String.valueOf(nicCount)).append(":").append(nic.getName()).append(",");
                    StringBuilder interfaceAddressStringBuilder = new StringBuilder();
                    for (InterfaceAddress interfaceAddress : nic.getInterfaceAddresses()) {
                        if (interfaceAddress == null)
                            continue;
                        try {
                            InetAddress address = interfaceAddress.getAddress();
                            interfaceAddressStringBuilder.append(address.getHostAddress()).append(",");
                        } catch (Exception e) {
                            System.out.println("SysInfoBuilder : Constructor : nicLoop : addrLoop : Error : " + e.getMessage());
                        }
                    }
                    if (interfaceAddressStringBuilder.length() == 0)
                        continue;
                    interfaceAddressStringBuilder.deleteCharAt(interfaceAddressStringBuilder.lastIndexOf(","));
                    info.put("nic-" + String.valueOf(nicCount) + "-ip", interfaceAddressStringBuilder.toString());
                }
                nicStringBuilder.deleteCharAt(nicStringBuilder.lastIndexOf(","));
                info.put("nic-map", nicStringBuilder.toString());
            } catch (Exception e) {
                System.out.println("SysInfoBuilder : Constructor : nicLoop : Error : " + e.getMessage());
            }
            getSysInfoMap();
        } catch (Exception e) {
            System.out.println("SysInfoBuilder : Constructor : Error : " + e.getMessage());
            e.printStackTrace();
        }
    }

    Map<String, String> getSysInfoMap() {
        try {
            SystemInfo systemInfo = new SystemInfo();
            HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware();

            info.put("sys-uptime", FormatUtil.formatElapsedSecs(hardwareAbstractionLayer.getProcessors()[0].getSystemUptime()));
            info.put("memory-available", String.valueOf(hardwareAbstractionLayer.getMemory().getAvailable()));

            long[] prevTicks = hardwareAbstractionLayer.getProcessors()[0].getSystemCpuLoadTicks();
            Thread.sleep(1000);
            long[] ticks = hardwareAbstractionLayer.getProcessors()[0].getSystemCpuLoadTicks();

            long user = ticks[0] - prevTicks[0];
            long nice = ticks[1] - prevTicks[1];
            long sys = ticks[2] - prevTicks[2];
            long idle = ticks[3] - prevTicks[3];
            long totalCpu = user + nice + sys + idle;

            StringBuilder procCpu = new StringBuilder("CPU Load per processor:");
            for (int cpu = 0; cpu < hardwareAbstractionLayer.getProcessors().length; cpu++)
                procCpu.append(String.format(" %.1f%%", hardwareAbstractionLayer.getProcessors()[cpu].getProcessorCpuLoadBetweenTicks() * 100));

            info.put("cpu-total-ticks", String.valueOf(totalCpu/100));
            info.put("cpu-idle-ticks", String.valueOf(idle/100));


            info.put("cpu-user-load", String.valueOf(100d * user / totalCpu));
            info.put("cpu-nice-load", String.valueOf(100d * nice / totalCpu));
            info.put("cpu-sys-load", String.valueOf(100d * sys / totalCpu));
            info.put("cpu-idle-load", String.valueOf(100d * idle / totalCpu));
            info.put("cpu-per-cpu-load", procCpu.toString());

            int fsCount = 0;
            StringBuilder fsStringBuilder = new StringBuilder();
            OSFileStore[] fsArray = hardwareAbstractionLayer.getFileStores();
            for (OSFileStore fs : fsArray) {
                fsStringBuilder.append(String.valueOf(fsCount)).append(":").append(fs.getName()).append(",");
                long usable = fs.getUsableSpace();
                long total = fs.getTotalSpace();
                info.put("fs-" + String.valueOf(fsCount) + "-available", String.valueOf(usable));
                info.put("fs-" + String.valueOf(fsCount) + "-total", String.valueOf(total));
                fsCount++;
            }
            fsStringBuilder.deleteCharAt(fsStringBuilder.lastIndexOf(","));
            info.put("fs-map", fsStringBuilder.toString());
        } catch (Exception e) {
            System.out.println("SysInfoBuilder : getSysInfoMap : Error : " + e.getMessage());
            e.printStackTrace();
        }
        return info;
    }

    static ConcurrentHashMap<String, String> getInfo() {
        return info;
    }
}
