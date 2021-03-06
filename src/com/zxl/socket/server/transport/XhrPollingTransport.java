package com.zxl.socket.server.transport;

import com.zxl.socket.MainServer;
import com.zxl.socket.server.Transports;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class XhrPollingTransport extends ITransport {

    public XhrPollingTransport(HttpRequest req) {
        super(req);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.yongboy.socketio.client.ITransport#getId()
     */
    @Override
    public String getId() {
        return Transports.XHRPOLLING.getValue();
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.yongboy.socketio.com.zxl.socket.server.transport.ITransport#initGenericClient(org
     * .jboss.netty.channel.ChannelHandlerContext,
     * org.jboss.netty.handler.codec.http.HttpRequest)
     */
    @Override
    protected GenericIO initGenericClient(ChannelHandlerContext ctx,
                                          HttpRequest req) {
        GenericIO client = super.initGenericClient(ctx, req);

        if (!(client instanceof XhrIO)) {
            String sessionId = super.getSessionId();
            super.store.remove(sessionId);

            return initGenericClient(ctx, req);
        }

        // 需要切换到每一个具体的transport中
        if (req.getMethod() == HttpMethod.GET) { // 非第一次请求时
            client.reconnect(ctx, req);

            client.heartbeat(MainServer.getIOHandler(client));
        }

        return client;
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
        XhrIO client = new XhrIO(ctx, req, sessionId);
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