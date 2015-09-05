package com.zxl.socket.server.transport;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * @author yongboy
 * @version 1.0
 * @time 2012-3-28
 */
public class FlashSocketIO extends WebSocketIO {
    public FlashSocketIO(ChannelHandlerContext ctx, HttpRequest req, String uID) {
        super(ctx, req, uID);
    }
}