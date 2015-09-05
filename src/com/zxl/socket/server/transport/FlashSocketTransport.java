package com.zxl.socket.server.transport;

import com.zxl.socket.server.Transports;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequest;

/**
 * @author yongboy
 * @version 1.0
 * @time 2012-3-29
 */
public class FlashSocketTransport extends WebSocketTransport {

    public FlashSocketTransport(HttpRequest req) {
        super(req);
    }

    @Override
    public String getId() {
        return Transports.FLASHSOCKET.getValue();
    }

    @Override
    protected GenericIO doNewI0Client(ChannelHandlerContext ctx,
                                      HttpRequest req, String sessionId) {
        FlashSocketIO client = new FlashSocketIO(ctx, req, sessionId);
        return client;
    }
}