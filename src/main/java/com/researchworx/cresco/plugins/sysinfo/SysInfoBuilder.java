package com.researchworx.cresco.plugins.sysinfo;

import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSFileStore;
import oshi.util.FormatUtil;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class SysInfoBuilder {
    public SysInfoBuilder() {
        System.setProperty("java.net.preferIPv4Stack", "true");
    }

    public Map<String, String> getSysInfoMap() {
        Map<String, String> ret = new HashMap<>();
        try {
            SystemInfo systemInfo = new SystemInfo();
            HardwareAbstractionLayer hardwareAbstractionLayer = systemInfo.getHardware();

            ret.put("sys-os", systemInfo.getOperatingSystem().toString());
            ret.put("cpu-core-count", String.valueOf(hardwareAbstractionLayer.getProcessors().length));
            ret.put("cpu-sn", hardwareAbstractionLayer.getProcessors()[0].getSystemSerialNumber());
            ret.put("cpu-summary", hardwareAbstractionLayer.getProcessors()[0].toString());
            String cpuIdent = hardwareAbstractionLayer.getProcessors()[0].getIdentifier();
            if(cpuIdent != null) {
                ret.put("cpu-ident", cpuIdent );
            }
            else {
                ret.put("cpu-ident", "unknown");
            }
            String cpuSNIdent = hardwareAbstractionLayer.getProcessors()[0].getIdentifier();
            if(cpuSNIdent != null) {
                ret.put("cpu-sn-ident",cpuSNIdent);
            }
            else {
                ret.put("cpu-sn-ident","unknown");
            }

            ret.put("sys-uptime", FormatUtil.formatElapsedSecs(hardwareAbstractionLayer.getProcessors()[0].getSystemUptime()));

            ret.put("memory-available", String.valueOf(hardwareAbstractionLayer.getMemory().getAvailable()));
            ret.put("memory-total", String.valueOf(hardwareAbstractionLayer.getMemory().getTotal()));

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

            ret.put("cpu-user-load", String.valueOf(100d * user / totalCpu));
            ret.put("cpu-nice-load", String.valueOf(100d * nice / totalCpu));
            ret.put("cpu-sys-load", String.valueOf(100d * sys / totalCpu));
            ret.put("cpu-idle-load", String.valueOf(100d * idle / totalCpu));
            ret.put("cpu-per-cpu-load", procCpu.toString());

            int fsCount = 0;
            StringBuilder fsStringBuilder = new StringBuilder();
            OSFileStore[] fsArray = hardwareAbstractionLayer.getFileStores();
            for (OSFileStore fs : fsArray) {
                fsStringBuilder.append(String.valueOf(fsCount)).append(":").append(fs.getName()).append(",");
                long usable = fs.getUsableSpace();
                long total = fs.getTotalSpace();
                ret.put("fs-" + String.valueOf(fsCount) + "-available", String.valueOf(usable));
                ret.put("fs-" + String.valueOf(fsCount) + "-total", String.valueOf(total));
                fsCount++;
            }
            fsStringBuilder.deleteCharAt(fsStringBuilder.lastIndexOf(","));
            ret.put("fs-map", fsStringBuilder.toString());

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
                            System.out.println("SysInfoBuilder : getSysInfoMap : nicLoop : addrLoop : Error : " + e.getMessage());
                        }
                    }
                    if (interfaceAddressStringBuilder.length() == 0)
                        continue;
                    interfaceAddressStringBuilder.deleteCharAt(interfaceAddressStringBuilder.lastIndexOf(","));
                    ret.put("nic-" + String.valueOf(nicCount) + "-ip", interfaceAddressStringBuilder.toString());
                }
                nicStringBuilder.deleteCharAt(nicStringBuilder.lastIndexOf(","));
                ret.put("nic-map", nicStringBuilder.toString());
            } catch (Exception e) {
                System.out.println("SysInfoBuilder : getSysInfoMap : nicLoop : Error : " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("SysInfoBuilder : getSysInfoMap : Error : " + e.getMessage());
            e.printStackTrace();
        }
        return ret;
    }
}
