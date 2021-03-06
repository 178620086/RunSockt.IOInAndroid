package com.zxl.socket.server.transport;

import com.zxl.socket.MainServer;
import com.zxl.socket.server.SocketIOManager;
import com.zxl.socket.server.Store;
import org.apache.log4j.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.util.CharsetUtil;

import java.util.ArrayList;
import java.util.List;

import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;

/**
 * 定义抽象Transport
 *
 * @author yongboy
 * @version 1.0
 * @time 2012-3-26
 */
public abstract class ITransport {
    private static final Logger log = Logger.getLogger(ITransport.class);
    protected Store store;
    protected HttpRequest req;

    public ITransport(HttpRequest req) {
        this.req = req;

        this.store = SocketIOManager.getDefaultStore();
    }

    /**
     * @param uri
     * @return
     * @author yongboy
     * @time 2012-3-26
     */
    public boolean check(String uri) {
        return uri.contains("/" + getId() + "/");
    }

    /**
     * @return
     * @author yongboy
     * @time 2012-3-26
     */
    public abstract String getId();

    /**
     * 仅仅构造一个GenericIOClient实例，并返回
     *
     * @param ctx
     * @param req
     * @param sessionId
     * @return
     * @author yongboy
     * @time 2012-3-27
     */
    protected abstract GenericIO doNewI0Client(ChannelHandlerContext ctx,
                                               HttpRequest req, String sessionId);

    /**
     * 初始化连接动作
     *
     * @param client
     * @param info      TODO
     * @param namespace TODO
     * @author yongboy
     * @time 2012-5-28
     */
    protected abstract void doPrepareAction(GenericIO client, String info,
                                            String namespace);

    /**
     * @param ctx
     * @param frame
     * @param e
     * @author yongboy
     * @time 2012-3-27
     */
    public void doHandle(ChannelHandlerContext ctx, WebSocketFrame frame,
                         MessageEvent e) {
        // TO DO NOTHING ...
    }

    /**
     * @param ctx
     * @param req
     * @param e
     */
    public void doHandle(ChannelHandlerContext ctx, HttpRequest req,
                         MessageEvent e) {
        String sessionId = getSessionId(req);

        if (!this.store.checkExist(sessionId)) {
            handleInvalidRequest(req, e);
            return;
        }

        GenericIO client = null;
        try {
            client = (GenericIO) this.store.get(sessionId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        String reqURI = req.getUri();
        // 主动要求断开
        if (reqURI.contains("?disconnect") || reqURI.contains("&disconnect")) {
            log.debug("the request uri contains the 'disconnect' paremeter");
            if (client != null)
                client.disconnect();

            return;
        }

        boolean isNew = false;
        if (client == null) {
            log.debug("the client is null with id : " + sessionId);
            client = doNewI0Client(ctx, req, sessionId);

            store.add(sessionId, client);
            isNew = true;
        }

        if (!reqURI.contains("/" + client.getId() + "/")) {
            store.remove(sessionId);
            // 避免在if (!this.store.checkExist(sessionId)) 处陷入循环
            store.add(sessionId, BlankIO.getInstance());
            doHandle(ctx, req, e);
            return;
        }

        // 需要切换到每一个具体的transport中
        if (req.getMethod() == HttpMethod.GET) { // 非第一次请求时
            if (!isNew) {
                client.reconnect(ctx, req);
                client.heartbeat(MainServer.getIOHandler(client));
            } else {
                client.connect(null);
            }

            return;
        }

        if (req.getMethod() != HttpMethod.POST) {
            log.debug("the request method " + req.getMethod());
            return;
        }

        // 增加判断是否存在连接已经关闭情况
        Channel channel = e.getChannel();
        if (channel == null || !channel.isOpen()) {
            client.disconnect();
            return;
        }

        // 判断POST提交值
        ChannelBuffer buffer = req.getContent();
        String content = buffer.toString(CharsetUtil.UTF_8);

        String respContent = handleContent(client, content);

        HttpResponse resp = SocketIOManager.getInitResponse(req);
        resp.setContent(ChannelBuffers.copiedBuffer(respContent,
                CharsetUtil.UTF_8));
        resp.addHeader(HttpHeaders.Names.CONNECTION,
                HttpHeaders.Values.KEEP_ALIVE);
        setContentLength(resp, resp.getContent().readableBytes());

        e.getChannel().write(resp).addListener(ChannelFutureListener.CLOSE);
    }

    private void handleInvalidRequest(HttpRequest req, MessageEvent e) {
        // QueryStringDecoder queryStringDecoder = new QueryStringDecoder(
        // req.getUri());
        HttpResponse resp = SocketIOManager.getInitResponse(req,
                HttpResponseStatus.FORBIDDEN);
        // String respContent = "7:::[\"invalide request\"]";
        //
        // String jsonpValue = getParameter(queryStringDecoder, "jsonp");
        // // io.j[1]("9135478181958205332:60:60:websocket,flashsocket");
        // if (jsonpValue != null) {
        // log.debug("request uri with parameter jsonp = " + jsonpValue);
        // respContent = "io.j[" + jsonpValue + "]('" + respContent
        // + "');";
        // resp.addHeader(CONTENT_TYPE, "application/javascript");
        // }
        //
        // resp.addHeader(HttpHeaders.Names.CONNECTION,
        // HttpHeaders.Values.KEEP_ALIVE);
        // resp.setContent(ChannelBuffers.copiedBuffer(respContent,
        // CharsetUtil.UTF_8));
        // setContentLength(resp, resp.getContent().readableBytes());

        e.getChannel().write(resp).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * @param ctx
     * @param req
     * @return
     * @author yongboy
     * @time 2012-4-1
     */
    protected GenericIO initGenericClient(ChannelHandlerContext ctx,
                                          HttpRequest req) {
        String sessionId = getSessionId(req);

        GenericIO client = null;
        try {
            client = (GenericIO) this.store.get(sessionId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (client == null) {
            log.debug("initGenericClient the client is null with id : "
                    + sessionId);
            client = doNewI0Client(ctx, req, sessionId);

            store.add(sessionId, client);
            // this.handler.OnConnect(client);
        }

        return client;
    }

    /**
     * @param client
     * @param content
     * @return
     * @author yongboy
     * @time 2012-3-30
     */
    protected String handleContent(GenericIO client, String content) {
        log.debug("received content " + content);

        if (content.startsWith("d=")) {
            log.debug("post with parameter d");
            // 用于解决application/x-www-form-urlencoded提交表单
            QueryStringDecoder queryStringDecoder = new QueryStringDecoder(
                    content, CharsetUtil.UTF_8, false, 2);

            // TODO 这里当遇到 & 符号时，将无法正常解析; 有待修补
            // fixit it: http://www.x2x1.com/show/8659522.aspx
            /**
             * HttpRequest request = (HttpRequest) e.getMessage();
             * HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new
             * DefaultHttpDataFactory(false), request);
             *
             * InterfaceHttpData data = decoder.getBodyHttpData("fromField1");
             * if (data.getHttpDataType() == HttpDataType.Attribute) { Attribute
             * attribute = (Attribute) data; String value = attribute.getValue()
             * System.out.println("fromField1 :" + value); }
             */
            content = queryStringDecoder.getParameters().get("d").get(0);
            if (content.startsWith("\"")) {
                content = content.substring(1);
            }

            if (content.endsWith("\"")) {
                content = content.substring(0, content.length() - 1);
            }
        }

        if (content == null) {
            content = "";
        } else {
            content = content.trim();
        }

        List<String> contentList = null;
        if (content.startsWith(SPLIT_CHAR)) {
            contentList = getSplitResults(content);
        } else {
            contentList = new ArrayList<String>(1);
            contentList.add(content);
        }

        String respContent = null;

        for (String subContent : contentList) {
            int messageType = -1;

            try {
                messageType = Integer.parseInt(subContent.substring(0, 1));
            } catch (Exception e) {
            }

            switch (messageType) {
                case 5:
                case 4:
                case 3: {
                    MainServer.getIOHandler(client).OnMessage(client, subContent);
                    respContent = "1";
                }
                break;
                case 2: {
                    log.debug("got heartbeat packets");
                    client.heartbeat(MainServer.getIOHandler(client));
                    respContent = "1";
                }
                break;
                case 1: {
                    if (subContent.length() > 3) {
                        String namespace = null;
                        int endIndex = subContent.lastIndexOf('?');
                        if (endIndex == -1)
                            endIndex = subContent.length();
                        int startIndex = 3;
                        namespace = subContent.substring(startIndex, endIndex);
                        doPrepareAction(client, subContent, namespace);
                    } else {
                        doPrepareAction(client, subContent, null);
                    }

                    MainServer.getIOHandler(client).OnConnect(client);

                    respContent = "1";
                }
                break;
                case 0: {
                    MainServer.getIOHandler(client).OnDisconnect(client);
                    respContent = subContent;
                }
                break;
            }

            if (respContent == null) {
                respContent = "1";
            }

            if (respContent.equals("11")) {
                respContent = "1";
            }
        }

        return respContent;
    }

    private static String SPLIT_CHAR = String.valueOf('\ufffd');

    /**
     * 处理多个数据包`\ufffd` [message lenth] `\ufffd`的情况
     *
     * @param ori
     * @return
     * @author yongboy
     * @time 2012-5-2
     */
    private static List<String> getSplitResults(String ori) {
        List<String> list = new ArrayList<String>();
        String[] results = ori.split(SPLIT_CHAR + "\\d{1,}" + SPLIT_CHAR);

        for (String d : results) {
            if (d.equals(""))
                continue;

            list.add(d);
        }

        return list;
    }

    /**
     * @param req
     * @return
     * @author yongboy
     * @time 2012-3-26
     */
    protected String getSessionId(HttpRequest req) {
        String reqURI = req.getUri();
        String[] parts = reqURI.substring(1).split("/");
        String sessionId = parts.length > 3 ? parts[3] : "";
        if (sessionId.indexOf("?") != -1) {
            sessionId = sessionId.replaceAll("\\?.*", "");
        }

        return sessionId;
    }

    public String getSessionId() {
        String reqURI = req.getUri();
        String[] parts = reqURI.substring(1).split("/");
        String sessionId = parts.length > 3 ? parts[3] : "";
        if (sessionId.indexOf("?") != -1) {
            sessionId = sessionId.replaceAll("\\?.*", "");
        }

        return sessionId;
    }

    /**
     * @param ctx
     * @param req
     * @param res
     */
    protected void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req,
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
}