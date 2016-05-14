package com.researchworx.cresco.plugins.sysinfo;

import com.google.auto.service.AutoService;
import com.researchworx.cresco.library.core.WatchDog;
import com.researchworx.cresco.library.plugin.core.CPlugin;

@AutoService(CPlugin.class)
public class Plugin extends CPlugin {
    private PerfMonitor perfMonitor;
    private WatchDog watchMonitor;

    public void setExecutor() {
        this.exec = new Executor(this);
    }

    public void start() {
        perfMonitor = new PerfMonitor(this);
        perfMonitor.start();
        startWatchDog();



    }
}
