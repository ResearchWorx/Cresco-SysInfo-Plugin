package com.researchworx.cresco.plugins.sysinfo;

import com.researchworx.cresco.library.messaging.MsgEvent;
import com.researchworx.cresco.library.plugin.core.CExecutor;
import com.researchworx.cresco.library.utilities.CLogger;

import java.util.Map;

public class Executor extends CExecutor {
    private final CLogger logger;

    public Executor (Plugin plugin) {
        super(plugin);
        this.logger = new CLogger(plugin.getMsgOutQueue(), plugin.getRegion(), plugin.getAgent(), plugin.getPluginID(), CLogger.Level.Info);
    }

    @Override
    public MsgEvent processExec(MsgEvent msg) {
        logger.debug("Processing EXEC message {}", msg.getParams());
        SysInfoBuilder sb = new SysInfoBuilder();
        for (Map.Entry<String, String> param : sb.getInfo().entrySet()) {
            msg.setParam(param.getKey(), param.getValue());
        }
        return msg;
    }
}
