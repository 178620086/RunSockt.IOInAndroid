package com.zxl.socket.server;

import android.content.res.Resources;
import com.zxl.printbola.R;
import com.zxl.printbola.utitl.Tools;
import org.jboss.netty.handler.codec.http.*;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author yongboy
 * @version 1.0
 * @time 2012-3-28
 */
public class SocketIOManager   {
    public static Option option = new Option();

    private static final ScheduledExecutorService scheduledExecutorService = Executors
            .newScheduledThreadPool(Runtime.getRuntime().availableProcessors() * 2 + 1);

    public static final Set<String> fobbiddenEvents = new HashSet<String>(
            Arrays.asList("message,connect,disconnect,open,close,error,retry,reconnect"
                    .split(",")));

    private static Store store = new MemoryStore();

    public static final class Option {
        public boolean heartbeat = true;
        public int heartbeat_timeout = 60;
        public int close_timeout = 60;
        public int heartbeat_interval = 25;
        public boolean flash_policy_server = true;
        public int flash_policy_port = 10843;
        public String transports = "websocket,jsonp-polling,xhr-polling";
        public String Static = "static";

        {
            //获取资源文件
              Resources res=Tools.getRes();
           //获取各种值
            heartbeat= res.getText(R.string.heartbeat).equals("true");
            heartbeat_timeout = Integer.parseInt(res.getText(R.string.heartbeat_timeout).toString());
            close_timeout = Integer.parseInt(res.getText(R.string.close_timeout).toString());
            heartbeat_interval =  Integer.parseInt(res.getText(R.string.heartbeat_interval).toString());
            flash_policy_server = res.getText(R.string.flash_policy_server).equals("true");
            flash_policy_port = Integer.parseInt(res.getText(R.string.flash_policy_port).toString());
            transports = res.getText(R.string.transports).toString();
            Static =  res.getText(R.string.stic).toString();
        }
    }

    /**
     * @return
     * @author yongboy
     * @time 2012-3-29
     */
    public static Store getDefaultStore() {
        return store;
    }

    /**
     * @param runnable
     * @author yongboy
     * @time 2012-3-28
     */
    public static void schedule(Runnable runnable) {
        scheduledExecutorService.schedule(runnable, option.heartbeat_interval,
                TimeUnit.SECONDS);
    }

    /**
     * @return
     * @author yongboy
     * @time 2012-3-28
     */
    public static String getHandshakeResult() {
        return "%s:"
                + (option.heartbeat ? Integer
                .toString(option.heartbeat_timeout) : "") + ":"
                + option.close_timeout + ":" + option.transports;
    }

    /**
     * 统一控制是否跨域请求等
     *
     * @param req
     * @return
     * @author yongboy
     * @time 2012-3-28
     */
    public static HttpResponse getInitResponse(HttpRequest req) {
        return getInitResponse(req, HttpResponseStatus.OK);
    }

    /**
     * @param req
     * @param status
     * @return
     * @author yongboy
     * @time 2012-3-28
     */
    public static HttpResponse getInitResponse(HttpRequest req,
                                               HttpResponseStatus status) {
        HttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                status);

        if (req != null && req.getHeader("Origin") != null) {
            resp.addHeader("Access-Control-Allow-Origin",
                    req.getHeader("Origin"));
            resp.addHeader("Access-Control-Allow-Credentials", "true");
        }

        return resp;
    }

    /**
     * @param runnable
     * @return
     * @author yongboy
     * @time 2012-4-3
     */
    @Deprecated
    public static ScheduledFuture<?> scheduleClearTask(Runnable runnable) {
        return scheduledExecutorService.schedule(runnable,
                option.heartbeat_timeout, TimeUnit.SECONDS);
    }

    public static ScheduledFuture<?> scheduleClearTask(Runnable runnable,
                                                       long delay, TimeUnit unit) {
        return scheduledExecutorService.schedule(runnable, delay, unit);
    }

    /**
     * 得到文件名
     *
     * @param filename
     * @return
     * @author yongboy
     * @time 2012-4-5
     */
    public static String getFileName(String filename) {
        if (filename == null) {
            return null;
        }
        int index = indexOfLastSeparator(filename);
        return filename.substring(index + 1);
    }

    private static int indexOfLastSeparator(String filename) {
        if (filename == null) {
            return -1;
        }
        int lastUnixPos = filename.lastIndexOf('/');
        int lastWindowsPos = filename.lastIndexOf('\\');
        return Math.max(lastUnixPos, lastWindowsPos);
    }
}