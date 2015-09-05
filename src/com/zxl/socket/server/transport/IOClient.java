package com.zxl.socket.server.transport;

import com.zxl.socket.server.IOHandler;
import org.jboss.netty.channel.ChannelHandlerContext;

/**
 * @author yongboy
 * @version 1.0
 * @time 2012-3-27
 */
public interface IOClient {

    /**
     * @param message
     * @author yongboy
     * @time 2012-3-27
     */
    void send(String message);

    /**
     * @param message
     * @author yongboy
     * @time 2012-3-27
     */
    void sendEncoded(String message);

    /**
     *
     * @author yongboy
     * @time 2012-3-27
     *
     * @param beat
     * @return
     */

    /**
     * @author yongboy
     * @time 2012-3-27
     */
    void heartbeat(final IOHandler ioHandler);

    /**
     * @author yongboy
     * @time 2012-3-27
     */
    void disconnect();

    /**
     * @return
     * @author yongboy
     * @time 2012-3-27
     */
    String getSessionID();

    /**
     * @return
     * @author yongboy
     * @time 2012-3-27
     */
    ChannelHandlerContext getCTX();

    /**
     * return the self's description id ,eg :
     * xhr-polling/jsonp-polling/websocket
     *
     * @return
     * @author yongboy
     * @time 2012-4-1
     */
    String getId();

    /**
     * @return
     * @author yongboy
     * @time 2012-4-3
     */
    boolean isOpen();

    /**
     * @param open
     * @author yongboy
     * @time 2012-4-3
     */
    void setOpen(boolean open);

    /**
     * 获取命名空间 namespaces (ie: multiple sockets)
     *
     * @author yongboy
     * @time 2012-5-28
     */
    String getNamespace();
}
