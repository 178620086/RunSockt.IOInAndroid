package com.zxl.socket.server.transport;

import android.util.Log;
import com.zxl.socket.MainServer;
import com.zxl.socket.server.SocketIOManager;
import com.zxl.socket.server.Transports;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.websocketx.*;

public class WebSocketTransport extends ITransport {
    private WebSocketServerHandshaker handshaker;

    public WebSocketTransport(HttpRequest req) {
        super(req);
    }

    @Override
    public String getId() {
        return Transports.WEBSOCKET.getValue();
    }

    @Override
    protected GenericIO doNewI0Client(ChannelHandlerContext ctx,
                                      HttpRequest req, String sessionId) {
        WebSocketIO client = new WebSocketIO(ctx, req, sessionId);
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
        client.connect(info);
        client.heartbeat(MainServer.getIOHandler(client));
    }

    @Override
    public void doHandle(ChannelHandlerContext ctx, HttpRequest req,
                         MessageEvent e) {
        Log.d("提示", "websocket handls the request ...");
        // 需要调用父级的，否则将会发生异常
        String sessionId = super.getSessionId();
        Log.d("提示", "session id " + sessionId);

        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                this.getTargetLocation(req, sessionId), null, false);
        this.handshaker = wsFactory.newHandshaker(req);
        if (this.handshaker == null) {
            wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
            return;
        }

        this.handshaker.handshake(ctx.getChannel(), req);

        doPrepareClient(ctx, req, sessionId);
    }

    /**
     * @param ctx
     * @param req
     * @param sessionId
     * @author yongboy
     * @time 2012-5-4
     */
    private void doPrepareClient(ChannelHandlerContext ctx, HttpRequest req,
                                 String sessionId) {
        GenericIO client = null;
        try {
            client = (GenericIO) SocketIOManager.getDefaultStore().get(
                    sessionId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (client != null) {
            return;
        }

        Log.d("提示", "the client is null now ...");
        client = doNewI0Client(ctx, req, sessionId);
        client.connect(null);
        SocketIOManager.getDefaultStore().add(sessionId, client);
        // this.handler.OnConnect(client);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.yongboy.socketio.client.ITransport#doHandle(org.jboss.netty.channel
     * .ChannelHandlerContext,
     * org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame,
     * org.jboss.netty.channel.MessageEvent)
     */
    @Override
    public void doHandle(ChannelHandlerContext ctx, WebSocketFrame frame,
                         MessageEvent e) {
        Log.d("提示", "frame " + frame + " with instance " + frame.getClass());
        if (frame instanceof CloseWebSocketFrame) {
            this.handshaker
                    .close(ctx.getChannel(), (CloseWebSocketFrame) frame);
            return;
        } else if (frame instanceof PingWebSocketFrame) {
            ctx.getChannel().write(
                    new PongWebSocketFrame(frame.getBinaryData()));
            return;
        } else if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format(
                    "%s frame types not supported", frame.getClass().getName()));
        }

        TextWebSocketFrame textFrame = ((TextWebSocketFrame) frame);
        String content = textFrame.getText();
        Log.d("提示", "websocket received data packet " + content);

        GenericIO client = initGenericClient(ctx, null);
        if (client == null) {
            return;
        }

        String respContent = handleContent(client, content);
        Log.d("提示", "respContent " + respContent);

        client.send(respContent);
        // client.sendEncoded(respContent);
    }

    /**
     * @return
     * @author yongboy
     * @time 2012-3-30
     */
    @Override
    protected String getSessionId(HttpRequest req) {
        String webSocketUrl = this.handshaker.getWebSocketUrl();
        Log.d("提示", "webSocketUrl " + webSocketUrl);
        Log.d("提示", "webSocketUrl sessionid "
                + webSocketUrl.substring(webSocketUrl.lastIndexOf('/') + 1));

        return webSocketUrl.substring(webSocketUrl.lastIndexOf('/') + 1);
    }

    /**
     * @param req
     * @param sessionId
     * @return
     * @author yongboy
     * @time 2012-3-30
     */
    private String getTargetLocation(HttpRequest req, String sessionId) {
        return "ws://" + req.getHeader(HttpHeaders.Names.HOST)
                + "/socket.io/1/" + getId() + "/" + sessionId;
    }
}