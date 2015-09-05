package com.zxl.socket.server;

import com.zxl.socket.server.transport.IOClient;
import org.jboss.netty.channel.ChannelHandlerContext;

import java.util.Collection;

/**
 * @author yongboy
 * @version 1.0
 * @time 2012-3-29
 */
public interface Store {

    /**
     * @param sessionId
     * @author yongboy
     * @time 2012-3-29
     */
    void remove(String sessionId);

    /**
     * @param sessionId
     * @param client
     * @author yongboy
     * @time 2012-3-29
     */
    void add(String sessionId, IOClient client);

    /**
     * @return
     * @author yongboy
     * @time 2012-3-29
     */
    Collection<IOClient> getClients();

    /**
     * @param sessionId
     * @return
     * @author yongboy
     * @time 2012-3-29
     */
    IOClient get(String sessionId);

    /**
     * @param sessionId
     * @return
     * @author yongboy
     * @time 2012-4-3
     */
    boolean checkExist(String sessionId);

    /**
     * @param ctx
     * @return
     * @author yongboy
     * @time 2012-3-31
     */
    IOClient getByCtx(ChannelHandlerContext ctx);
}
