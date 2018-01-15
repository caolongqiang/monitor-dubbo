package com.jimu.common.jmonitor.dubbo.filter;

import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcException;
import com.jimu.commom.jmonitor.customization.DubboThreadPoolMonitorTask;
import com.jimu.common.jmonitor.JMonitor;

public class DubboMonitorFilter implements Filter {
    public static final String TAG = "_";
    private final String metric;

    public DubboMonitorFilter(String metric) {
        this.metric = metric;
    }

    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        String clazz = invoker.getInterface().getName();
        String method = invocation.getMethodName();
        String monitorKey = this.metric + "_" + clazz + "_" + method + "_";
        long start = System.currentTimeMillis();

        try {
            Result e = invoker.invoke(invocation);
            JMonitor.recordOne(monitorKey + "success", System.currentTimeMillis() - start);
            return e;
        } catch (RpcException var9) {
            JMonitor.recordOne(monitorKey + "failed", System.currentTimeMillis() - start);
            throw var9;
        }
    }

    @Activate(
        group = {"consumer"}
    )
    public static class Consumer extends DubboMonitorFilter {
        public Consumer() {
            super("dubbo_consumer");
            JMonitor.addMonitorTask(DubboThreadPoolMonitorTask.INSTANCE);
        }
    }

    @Activate(
        group = {"provider"}
    )
    public static class Provider extends DubboMonitorFilter {
        public Provider() {
            super("dubbo_provider");
            JMonitor.addMonitorTask(DubboThreadPoolMonitorTask.INSTANCE);
        }
    }
}
