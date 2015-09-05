package com.zxl.socket.server;

import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.zxl.socket.server.transport.IOClient;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * @author yongboy
 * @version 1.0
 * @time 2012-3-30
 */
public abstract class IOHandlerAbs implements IOHandler {

    /**
     * @return
     * @author yongboy
     * @time 2012-3-31
     */
    protected Collection<IOClient> getClients() {
        Store store = SocketIOManager.getDefaultStore();

        return store.getClients();
    }

    /**
     * 广播方式发送到各个连接断点
     *
     * @param message
     */
    protected void broadcast(String message) {
        broadcast(null, message);
    }

    /**
     * @param current 若不为null，则广播剩余节点，否则广播到所有
     * @param message
     * @author yongboy
     * @time 2012-3-31
     */
    protected void broadcast(IOClient current, String message) {
        for (IOClient client : getClients()) {
            if (current != null
                    && current.getSessionID().equals(client.getSessionID()))
                continue;

            client.send(message);
        }
    }

    /**
     * @author yongboy
     * @time 2012-3-31
     */
    protected void ackNotify(IOClient client, String messageIdPlusStr,
                             Object obj) {
        StringBuilder builder = new StringBuilder("6::");
        builder.append(client.getNamespace()).append(":");

        String formateJson = JSON.toJSONString(obj);
        if (formateJson.startsWith("[") && formateJson.endsWith("]")) {
            formateJson = formateJson.substring(1, formateJson.length() - 1);
        }

        builder.append(messageIdPlusStr).append("[").append(formateJson)
                .append("]");

        Log.d("提示", "ack message " + builder);
        client.send(builder.toString());
    }
}