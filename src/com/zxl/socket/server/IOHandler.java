package com.zxl.socket.server;


import com.zxl.socket.server.transport.IOClient;

/**
 * @author yongboy
 * @version 1.0
 * @time 2012-3-23
 */
public interface IOHandler {
    void OnConnect(IOClient client);

    void OnMessage(IOClient client, String oriMessage);

    void OnDisconnect(IOClient client);

    void OnShutdown();
}
