package com.jimu.commom.jmonitor.customization;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.store.DataStore;
import com.google.common.collect.Maps;
import com.jimu.common.jmonitor.MonitorTask;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DubboThreadPoolMonitorTask implements MonitorTask {
    protected static Logger logger = LoggerFactory.getLogger("datalogger.JMonitor");
    private static String MONITOR_PREFFIX = "Dubbo_ThreadPool_";
    public static final DubboThreadPoolMonitorTask INSTANCE = new DubboThreadPoolMonitorTask();
    private static final Map<String, Long> taskCountMap = Maps.newConcurrentMap();

    private DubboThreadPoolMonitorTask() {
    }

    public Map<String, Number> execute() {
        HashMap ret = Maps.newHashMap();

        try {
            DataStore t = (DataStore)ExtensionLoader.getExtensionLoader(DataStore.class).getDefaultExtension();
            Map executors = t.get(Constants.EXECUTOR_SERVICE_COMPONENT_KEY);
            Iterator var4 = executors.entrySet().iterator();

            while(var4.hasNext()) {
                Map.Entry entry = (Map.Entry)var4.next();
                String port = (String)entry.getKey();
                ExecutorService executor = (ExecutorService)entry.getValue();
                if(executor != null && executor instanceof ThreadPoolExecutor) {
                    ThreadPoolExecutor tp = (ThreadPoolExecutor)executor;
                    String preffix = MONITOR_PREFFIX + port + "_";
                    ret.put(preffix + "activeCount", Integer.valueOf(tp.getActiveCount()));
                    ret.put(preffix + "poolSize", Integer.valueOf(tp.getPoolSize()));
                    ret.put(preffix + "currentQueueSize", Integer.valueOf(tp.getQueue().size()));
                    String taskCountKey = preffix + "taskCount";
                    long lastTaskCount = taskCountMap.containsKey(taskCountKey)?((Long)taskCountMap.get(taskCountKey)).longValue():0L;
                    ret.put(preffix + "taskCount", Long.valueOf(Math.max(tp.getTaskCount() - lastTaskCount, 0L)));
                    taskCountMap.put(taskCountKey, Long.valueOf(tp.getTaskCount()));
                    String completeCountKey = preffix + "completedTaskCount";
                    long lastCompleteCount = taskCountMap.containsKey(completeCountKey)?((Long)taskCountMap.get(completeCountKey)).longValue():0L;
                    ret.put(preffix + "completedTaskCount", Long.valueOf(Math.max(tp.getCompletedTaskCount() - lastCompleteCount, 0L)));
                    taskCountMap.put(completeCountKey, Long.valueOf(tp.getCompletedTaskCount()));
                }
            }
        } catch (Throwable var16) {
            logger.error("record dubbo thread pool exception", var16);
            ret.put(MONITOR_PREFFIX + "ERROR", Integer.valueOf(1));
        }

        return ret;
    }

    interface ValueNames {
        String ACTIVE_COUNT = "activeCount";
        String COMPLETED_TASK_COUNT = "completedTaskCount";
        String CURRENT_QUEUE_SIZE = "currentQueueSize";
        String POOL_SIZE = "poolSize";
        String TASK_COUNT = "taskCount";
    }
}
