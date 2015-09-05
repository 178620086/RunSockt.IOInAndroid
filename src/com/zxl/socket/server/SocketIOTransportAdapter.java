package com.zxl.socket.server;

import android.util.Log;
import com.zxl.printbola.utitl.Tools;
import com.zxl.socket.MainServer;
import com.zxl.socket.server.transport.BlankIO;
import com.zxl.socket.server.transport.GenericIO;
import com.zxl.socket.server.transport.IOClient;
import com.zxl.socket.server.transport.ITransport;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.stream.ChunkedFile;
import org.jboss.netty.util.CharsetUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.UUID;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.*;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class SocketIOTransportAdapter extends SimpleChannelUpstreamHandler {

    // private IOHandlerAbs handler;
    private ITransport currentTransport = null;

    public SocketIOTransportAdapter() {
        super();
        // this.handler = handler;
    }

    private String getUniqueID() {
        return UUID.randomUUID().toString();
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx,
                                    org.jboss.netty.channel.ChannelStateEvent e) throws Exception {
        if (this.currentTransport == null) {
            return;
        }

        if ("websocket,flashsocket,htmlfile".contains(this.currentTransport
                .getId())) {
            Store store = SocketIOManager.getDefaultStore();
            String sessionId = this.currentTransport.getSessionId();
            IOClient client = store.get(sessionId);
            if (client == null) {
                return;
            }

            if (client instanceof GenericIO) {
                GenericIO genericIO = (GenericIO) client;
                genericIO
                        .scheduleRemoveTask(MainServer.getIOHandler(genericIO));
            }
        }

    }

    public void disconnect(IOClient client) {
        client.disconnect();
        SocketIOManager.getDefaultStore().remove(client.getSessionID());

        MainServer.getIOHandler(client).OnDisconnect(client);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {
        Object msg = e.getMessage();
        if (msg instanceof HttpRequest) {
            handleHttpRequest(ctx, (HttpRequest) msg, e);
            return;
        }

        if (msg instanceof WebSocketFrame) {
            if (currentTransport != null) {
                this.currentTransport.doHandle(ctx, (WebSocketFrame) msg, e);
            } else {
                Log.w("注意","currentTransport is null, do nothing ...");
            }
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req,
                                   MessageEvent e) throws Exception {
        String reqURI = req.getUri();
        Log.d("请求：",req.getMethod().getName() + " request uri " + reqURI);

        if (reqURI.equals("/") || reqURI.indexOf("/socket.io/1/") == -1) {
            handleStaticRequest(req, e, reqURI);
            return;
        }

        // eg:http://localhost/socket.io/1/?t=1332308953338
        if (reqURI.matches("/.*/\\d{1}/([^/]*)?")) {
            handleHandshake(req, e, reqURI);
            return;
        }

        if (currentTransport == null) {
            currentTransport = Transports.getTransportByReq(req);
        }

        if (currentTransport != null) {
            currentTransport.doHandle(ctx, req, e);
            return;
        }

        // if (currentTransport.getId() != Transports.WEBSOCKET.getValue()) {
        sendHttpResponse(ctx, req,
                SocketIOManager.getInitResponse(req, FORBIDDEN));
    }

    private void handleHandshake(HttpRequest req, MessageEvent e, String reqURI) {
        HttpResponse resp = SocketIOManager.getInitResponse(req);
        resp.addHeader(CONTENT_TYPE, "text/plain; charset=UTF-8");

        final String uID = getUniqueID();
        String contentString = String.format(
                SocketIOManager.getHandshakeResult(), uID);

        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(reqURI);

        String jsonpValue = getParameter(queryStringDecoder, "jsonp");
        // io.j[1]("9135478181958205332:60:60:websocket,flashsocket");
        if (jsonpValue != null) {
            resp.addHeader(CONTENT_TYPE, "application/javascript");
        }

        ChannelBuffer content = ChannelBuffers.copiedBuffer(contentString,
                CharsetUtil.UTF_8);

        resp.addHeader(HttpHeaders.Names.CONNECTION,
                HttpHeaders.Values.KEEP_ALIVE);
        resp.setContent(content);

        e.getChannel().write(resp).addListener(ChannelFutureListener.CLOSE);

        Store store = SocketIOManager.getDefaultStore();
        store.add(uID, BlankIO.getInstance());

        SocketIOManager.schedule(new Runnable() {
            @Override
            public void run() {
                Store store = SocketIOManager.getDefaultStore();
                IOClient client = store.get(uID);
                if (client == null)
                    store.remove(uID);
            }
        });
    }

    private static String getParameter(QueryStringDecoder queryStringDecoder,
                                       String parameterName) {
        if (queryStringDecoder == null || parameterName == null)
            return null;

        List<String> values = queryStringDecoder.getParameters().get(
                parameterName);

        if (values == null || values.isEmpty())
            return null;

        return values.get(0);
    }

    /**
     * @param req
     * @param e
     * @param reqURI
     * @throws IOException
     * @author yongboy
     * @time 2012-3-28
     */
    private void handleStaticRequest(HttpRequest req, MessageEvent e,
                                     String reqURI) throws IOException {
        String fileName = null;
        if (req.getUri().indexOf("/socket.io/") != -1) {
            fileName = SocketIOManager.getFileName(req.getUri());
        } else {
            fileName = req.getUri();
        }

        if (fileName == null || fileName.trim().equals("/")
                || fileName.trim().equals("")) {
            fileName = "index.html";
        }

        StringBuilder sb = new StringBuilder();
        /**
         * 修改这个地方
         */
        String resPath = Tools.getContext().getExternalFilesDir("static").getAbsolutePath();
        if (resPath.startsWith("rsrc:") || resPath.startsWith("jar:")) {
            sb.append(System.getProperty("user.dir")).append("/");
        } else {
            sb.append(resPath);
        }

        if (!fileName.startsWith("/")) {
            sb.append("/");
        }
        sb.append(fileName);
        if (sb.indexOf("file:/") != -1) {
            sb.delete(0, 6);
        }

        if (sb.indexOf("/") != 0) {
            sb.insert(0, "/");
        }

        File file = new File(sb.toString());
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException fnfe) {
            Log.d("提示","文件丢失：" + fnfe.getMessage());
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, NOT_FOUND);
            e.getChannel().write(response)
                    .addListener(ChannelFutureListener.CLOSE);
            return;
        }
        long fileLength = raf.length();

        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
        setContentLength(response, fileLength);
        response.addHeader("Content-Type", getContentType(file));

        Channel ch = e.getChannel();
        ch.write(response);

        ChannelFuture writeFuture;
        if (ch.getPipeline().get(SslHandler.class) != null) {
            // Cannot use zero-copy with HTTPS.
            writeFuture = ch.write(new ChunkedFile(raf, 0, fileLength, 8192));
        } else {
            // No encryption - use zero-copy.
            final FileRegion region = new DefaultFileRegion(raf.getChannel(),
                    0, fileLength);
            writeFuture = ch.write(region);
            writeFuture.addListener(new ChannelFutureProgressListener() {
                public void operationComplete(ChannelFuture future) {
                    region.releaseExternalResources();
                }

                public void operationProgressed(ChannelFuture future,
                                                long amount, long current, long total) {
                }
            });
        }

        if (!isKeepAlive(req)) {
            writeFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private String getContentType(File file) {
        if (file == null) {
            return "application/octet-stream";
        }

        String fileName = file.getName();
        if (fileName.toLowerCase().endsWith(".js")) {
            return "application/x-javascript";
        } else if (fileName.toLowerCase().endsWith(".css")) {
            return "text/css";
        } else if (fileName.toLowerCase().endsWith(".swf")) {
            return "application/x-shockwave-com.zxl.socket.flash";
        } else if (fileName.toLowerCase().endsWith(".htm")
                || fileName.toLowerCase().endsWith(".html")) {
            return "text/html";
        } else if (fileName.toLowerCase().endsWith(".jpg")
                || fileName.toLowerCase().endsWith(".png")
                || fileName.toLowerCase().endsWith(".gif")) {
            return "image/*";
        }

        return "application/octet-stream";
    }

    private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req,
                                  HttpResponse res) {
        if (res.getStatus().getCode() != 200) {
            res.setContent(ChannelBuffers.copiedBuffer(res.getStatus()
                    .toString(), CharsetUtil.UTF_8));
            setContentLength(res, res.getContent().readableBytes());
        }

        ChannelFuture f = ctx.getChannel().write(res);
        if (!isKeepAlive(req) || res.getStatus().getCode() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        Log.d("提示","exceptionCaught now ...");
        e.getCause().printStackTrace();
        e.getChannel().close();

        if (this.currentTransport == null) {
            return;
        }

        // 清理资源
        Store store = SocketIOManager.getDefaultStore();
        String sessionId = this.currentTransport.getSessionId();
        IOClient client = store.get(sessionId);
        if (client == null) {
            Log.d("提示","client had been removed by session id " + sessionId);
            return;
        }

        if (client instanceof GenericIO) {
            GenericIO genericIO = (GenericIO) client;
            genericIO.scheduleRemoveTask(MainServer.getIOHandler(genericIO));
        }
    }
}