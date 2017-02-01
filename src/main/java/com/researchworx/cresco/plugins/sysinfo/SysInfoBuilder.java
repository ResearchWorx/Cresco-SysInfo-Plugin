package com.researchworx.cresco.plugins.sysinfo;

import oshi.SystemInfo;
import oshi.hardware.HWDiskStore;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class SysInfoBuilder {
    private ConcurrentHashMap<String, String> info = new ConcurrentHashMap<>();
    private SystemInfo systemInfo;
    private HardwareAbstractionLayer hardwareAbstractionLayer;
    private OperatingSystem os;

    public Map<String, String> getSysInfoMap() {

        try {
            systemInfo = new SystemInfo();
            hardwareAbstractionLayer = systemInfo.getHardware();
            os = systemInfo.getOperatingSystem();


            getOSInfo();
            getFSInfo();
            //getSensorInfo();
            //getDiskInfo();
            getCPUInfo();
            getMemoryInfo();
            getNetworkInfo();


        } catch (Exception e) {
            System.out.println("SysInfoBuilder : getSysInfoMap : Error : " + e.getMessage());
            e.printStackTrace();
        }
        return info;
    }

    public SysInfoBuilder() {
        /*
        System.setProperty("java.net.preferIPv4Stack", "true");
        try {

            } catch (Exception e) {
                System.out.println("SysInfoBuilder : Constructor : nicLoop : Error : " + e.getMessage());
            }
            getSysInfoMap();
        } catch (Exception e) {
            System.out.println("SysInfoBuilder : Constructor : Error : " + e.getMessage());
            e.printStackTrace();
        }
        */
    }

    public void getFSInfo() {
        try{
            int fsCount = 0;
            StringBuilder fsStringBuilder = new StringBuilder();
            OSFileStore[] fsArray = hardwareAbstractionLayer.getFileSystem().getFileStores();
            for (OSFileStore fs : fsArray) {
                fsStringBuilder.append(String.valueOf(fsCount)).append(":").append(fs.getName()).append(",");
                long usable = fs.getUsableSpace();
                long total = fs.getTotalSpace();
                info.put("fs-" + String.valueOf(fsCount) + "-available", String.valueOf(usable));
                info.put("fs-" + String.valueOf(fsCount) + "-total", String.valueOf(total));
                fsCount++;
            }
            int index = fsStringBuilder.lastIndexOf(",");
            if(index != -1) {
                fsStringBuilder.deleteCharAt(index);
            }
            info.put("fs-map", fsStringBuilder.toString());
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getOSInfo() {
        try{

            info.put("sys-os", os.getVersion().toString());
            info.put("sys-family", os.getFamily());
            //info.put("sys-threadcount", String.valueOf(os.getThreadCount()));
            info.put("sys-manufacturer", os.getManufacturer());
            //info.put("sys-uptime", FormatUtil.formatElapsedSecs(hardwareAbstractionLayer.getProcessor().getSystemUptime()));
            info.put("sys-uptime", String.valueOf(hardwareAbstractionLayer.getProcessor().getSystemUptime()));
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getSensorInfo() {
        try{

            info.put("sys-temperature", String.format("%.1f", hardwareAbstractionLayer.getSensors().getCpuTemperature()));
            info.put("sys-voltage", String.format("%.1f",hardwareAbstractionLayer.getSensors().getCpuVoltage()));
            StringBuilder fanStringBuilder = new StringBuilder();
            for(int fanspeed : hardwareAbstractionLayer.getSensors().getFanSpeeds()) {
                fanStringBuilder.append(String.valueOf(fanspeed)).append(",");
            }

            int index = fanStringBuilder.lastIndexOf(",");
            if(index != -1) {
                fanStringBuilder.deleteCharAt(index);
            }

            info.put("sys-fanspeeds", fanStringBuilder.toString());

        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getDiskInfo() {
        try{

            /*
            int fsCount = 0;
            StringBuilder fsStringBuilder = new StringBuilder();
            HWDiskStore[] fsArray = hardwareAbstractionLayer.getDiskStores();
            for (HWDiskStore fs : fsArray) {
                fsStringBuilder.append(String.valueOf(fsCount)).append(":").append(fs.getName()).append(",");
                long usable = fs.get .getSize()fs.getUsableSpace();
                long total = fs.getTotalSpace();
                info.put("fs-" + String.valueOf(fsCount) + "-available", String.valueOf(usable));
                info.put("fs-" + String.valueOf(fsCount) + "-total", String.valueOf(total));
                fsCount++;
            }
            fsStringBuilder.deleteCharAt(fsStringBuilder.lastIndexOf(","));
            info.put("fs-map", fsStringBuilder.toString());
            */
            int diskCount = 0;
            StringBuilder diskStringBuilder = new StringBuilder();
            HWDiskStore[] diskArray = hardwareAbstractionLayer.getDiskStores();
            for (HWDiskStore disk : diskArray) {
                diskStringBuilder.append(String.valueOf(diskCount)).append(":").append(disk.getName()).append(",");
                /*
                int partCount = 0;

                for(HWPartition part : disk.getPartitions()) {
                    info.put("disk-" + String.valueOf(diskCount) + "-part-" + String.valueOf(partCount) + "-CODY", String.valueOf(part.getIdentification()));
                    info.put("disk-" + String.valueOf(diskCount) + "-part-" + String.valueOf(partCount) + "-CODY", String.valueOf(part.getName()));
                    info.put("disk-" + String.valueOf(diskCount) + "-part-" + String.valueOf(partCount) + "-CODY", String.valueOf(part.getSize()));
                    info.put("disk-" + String.valueOf(diskCount) + "-part-" + String.valueOf(partCount) + "-CODY", String.valueOf(part.getMountPoint()));

                    partCount++;
                }
                */
                info.put("disk-" + String.valueOf(diskCount) + "-size", String.valueOf(disk.getSize()));
                info.put("disk-" + String.valueOf(diskCount) + "-model", String.valueOf(disk.getModel()));
                info.put("disk-" + String.valueOf(diskCount) + "-name", String.valueOf(disk.getName()));
                //info.put("disk-" + String.valueOf(diskCount) + "-readbytes", String.valueOf(disk.getReadBytes()));
                //info.put("disk-" + String.valueOf(diskCount) + "-writebytes", String.valueOf(disk.getWriteBytes()));

                //long usable = fs.get .getSize()fs.getUsableSpace();
                //long total = fs.getTotalSpace();
                //info.put("fs-" + String.valueOf(fsCount) + "-available", String.valueOf(usable));
                //info.put("fs-" + String.valueOf(fsCount) + "-total", String.valueOf(total));
                diskCount++;
            }

            int index = diskStringBuilder.lastIndexOf(",");
            if(index != -1) {
                diskStringBuilder.deleteCharAt(index);
            }

            info.put("disk-map", diskStringBuilder.toString());

        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getMemoryInfo() {
        try{
            info.put("memory-total", String.valueOf(hardwareAbstractionLayer.getMemory().getTotal()));
            info.put("memory-available", String.valueOf(hardwareAbstractionLayer.getMemory().getAvailable()));
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getNetworkInfo() {
        try{
            int nicCount = 0;
            StringBuilder nicStringBuilder = new StringBuilder();
            //try {
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
                info.put("nic-" + String.valueOf(nicCount) + "-mtu", String.valueOf(nic.getMTU()));

            }
            int index = nicStringBuilder.lastIndexOf(",");
            if(index != -1) {
                nicStringBuilder.deleteCharAt(index);
            }

            info.put("nic-map", nicStringBuilder.toString());
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void getCPUInfo() {
        try{

            info.put("is64bit", String.valueOf(hardwareAbstractionLayer.getProcessor().isCpu64bit()));
            //removing do to error
            //info.put("cpu-physical-count", String.valueOf(hardwareAbstractionLayer.getProcessor().getPhysicalProcessorCount()));

            info.put("cpu-logical-count", String.valueOf(hardwareAbstractionLayer.getProcessor().getLogicalProcessorCount()));
            info.put("cpu-sn", hardwareAbstractionLayer.getProcessor().getSystemSerialNumber());
            info.put("cpu-summary", hardwareAbstractionLayer.getProcessor().toString());

            try {
                info.put("cpu-ident", hardwareAbstractionLayer.getProcessor().getIdentifier());
            }
            catch (Exception ex) {
                info.put("cpu-ident", "unknown");
            }
            try {
                info.put("cpu-sn-ident",hardwareAbstractionLayer.getProcessor().getIdentifier());
            }
            catch(Exception ex) {
                info.put("cpu-sn-ident","unknown");
            }

            //performance
            long[] prevTicks = hardwareAbstractionLayer.getProcessor().getSystemCpuLoadTicks();
            Thread.sleep(1000);
            long[] ticks = hardwareAbstractionLayer.getProcessor().getSystemCpuLoadTicks();

            long user = ticks[0] - prevTicks[0];
            long nice = ticks[1] - prevTicks[1];
            long sys = ticks[2] - prevTicks[2];
            long idle = ticks[3] - prevTicks[3];
            long totalCpu = user + nice + sys + idle;

            info.put("cpu-user-load", String.format("%.1f", (100d * user / totalCpu)));
            info.put("cpu-nice-load", String.format("%.1f", (100d * nice / totalCpu)));
            info.put("cpu-sys-load", String.format("%.1f", (100d * sys / totalCpu)));
            info.put("cpu-idle-load", String.format("%.1f", (100d * idle / totalCpu)));


        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public ConcurrentHashMap<String, String> getInfo() {
        return info;
    }
}
