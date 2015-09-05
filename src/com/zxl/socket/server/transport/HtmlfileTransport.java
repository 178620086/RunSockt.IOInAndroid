package com.zxl.socket.server.transport;

import com.zxl.socket.MainServer;
import com.zxl.socket.server.Transports;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class HtmlfileTransport extends ITransport {

    public HtmlfileTransport(HttpRequest req) {
        super(req);
    }

    @Override
    public String getId() {
        return Transports.HTMLFILE.getValue();
    }

    /**
     * 25秒为默认触发值，但触发之后，客户端会发起新的一个心跳检测连接
     *
     * @param htmlfile
     */

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
        HtmlfileIO client = new HtmlfileIO(ctx, req, sessionId);
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

        client.heartbeat(MainServer.getIOHandler(client));
    }
}