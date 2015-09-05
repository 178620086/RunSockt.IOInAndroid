package com.zxl.socket.server.transport;

import android.util.Log;
import com.zxl.socket.server.IOHandler;
import com.zxl.socket.server.SocketIOManager;
import com.zxl.socket.server.Transports;
import org.apache.log4j.Logger;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author yongboy
 * @version 1.0
 * @time 2012-5-29
 */
public class WebSocketIO extends GenericIO {
    private static final Logger log = Logger.getLogger(WebSocketIO.class);

    public WebSocketIO(ChannelHandlerContext ctx, HttpRequest req, String uID) {
        super(ctx, req, uID);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.yongboy.socketio.client.GenericIOClient#heartbeat()
     */
    @Override
    public void heartbeat(final IOHandler handler) {
        prepareHeartbeat();
        scheduleClearTask(handler);

        // 25秒为默认触发值，但触发之后，客户端会发起新的一个心跳检测连接
        SocketIOManager.schedule(new Runnable() {
            @Override
            public void run() {
                Channel chan = ctx.getChannel();
                if (chan.isOpen()) {
                    chan.write(new TextWebSocketFrame("2::"));
                }

                Log.d("提示", "emitting heartbeat for client " + getSessionID());
            }
        });
    }

    @Override
    public void sendEncoded(String message) {
        this.queue.offer(message);
        if (!this.open)
            return;

        while (true) {
            String msg = this.queue.poll();
            if (msg == null)
                break;

            Log.d("提示", "websocket writing " + msg + " for client "
                    + getSessionID());
            Channel chan = ctx.getChannel();
            if (chan.isOpen()) {
                chan.write(new TextWebSocketFrame(msg));
            }
        }
    }

    public void sendDirect(String message) {
        if (!this.open) {
            Log.d("提示", "this.open is false");
            return;
        }

        Log.d("提示", "websocket writing " + message + " for client "
                + getSessionID());
        Channel chan = ctx.getChannel();
        if (chan.isOpen()) {
            chan.write(new TextWebSocketFrame(message));
        } else {
            Log.d("提示", "chan.isOpen() is false");
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.yongboy.socketio.com.zxl.socket.server.transport.IOClient#getId()
     */
    @Override
    public String getId() {
        return Transports.WEBSOCKET.getValue();
    }
}