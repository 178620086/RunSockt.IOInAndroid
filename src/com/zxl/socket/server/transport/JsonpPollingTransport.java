package com.zxl.socket.server.transport;

import com.zxl.socket.server.Transports;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class JsonpPollingTransport extends ITransport {

    public JsonpPollingTransport(HttpRequest req) {
        super(req);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.yongboy.socketio.client.ITransport#getId()
     */
    @Override
    public String getId() {
        return Transports.JSONPP0LLING.getValue();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.yongboy.socketio.client.ITransport#initNewClient(org.jboss.netty.
     * channel.ChannelHandlerContext,
     * org.jboss.netty.handler.codec.http.HttpRequest, java.lang.String)
     */
    @Override
    protected GenericIO doNewI0Client(ChannelHandlerContext ctx,
                                      HttpRequest req, String sessionId) {
        JsonpIO client = new JsonpIO(ctx, req, sessionId);
        return client;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.yongboy.socketio.com.zxl.socket.server.transport.ITransport#doPrepareAction(com.
     * yongboy.socketio.com.zxl.socket.server.transport.GenericIO)
     */
    @Override
    protected void doPrepareAction(GenericIO client, String info,
                                   String namespace) {
        client.setNamespace(namespace);
        client.prepare();
        client.connect(info);
    }
}