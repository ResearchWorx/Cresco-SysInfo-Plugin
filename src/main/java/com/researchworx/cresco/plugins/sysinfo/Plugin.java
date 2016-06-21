package com.researchworx.cresco.plugins.sysinfo;

import com.google.auto.service.AutoService;
import com.researchworx.cresco.library.plugin.core.CPlugin;

@AutoService(CPlugin.class)
public class Plugin extends CPlugin {
    private PerfMonitor perfMonitor;

    public void start() {
        logger.info("Performance monitoring plugin initialized");
        perfMonitor = new PerfMonitor(this);
        perfMonitor.start();
        setExec(new Executor(this));
    }

    @Override
    public void cleanUp() {
        perfMonitor.stop();
    }
}
