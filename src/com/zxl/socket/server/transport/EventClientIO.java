package com.zxl.socket.server.transport;


import android.util.Log;
import com.zxl.socket.server.IOHandler;
import com.zxl.socket.server.SocketIOManager;
import com.zxl.socket.server.Store;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;

/**
 * @author yongboy
 * @version 1.0
 * @time 2012-4-1
 */
abstract class EventClientIO implements IOClient {

    public Map<String, Object> attr = null;
    protected final BlockingQueue<String> queue;
    protected ScheduledFuture<?> scheduledFuture;

    public EventClientIO() {
        attr = new HashMap<String, Object>();
        queue = new LinkedBlockingQueue<String>();
    }

    /**
     * @author yongboy
     * @version 1.0
     * @time 2012-4-4
     */
    @Deprecated
    protected static class ClearTask implements Runnable {
        protected String sessionId;
        private boolean clearSession = false;
        protected IOHandler handler = null;

        public ClearTask(String sessionId, final IOHandler handler) {
            this.sessionId = sessionId;
            this.handler = handler;
        }

        public ClearTask(String sessionId, final IOHandler handler,
                         boolean clearSession) {
            this(sessionId, handler);

            this.clearSession = clearSession;
        }

        @Override
        public void run() {
            Log.d("提示-", "entry ClearTask run method clearSession is "
                    + clearSession + " and sessionId is " + sessionId);
            Store store = SocketIOManager.getDefaultStore();
            IOClient client = store.get(sessionId);
            if (client == null) {
                Log.d("提示-", "the client is null");
                return;
            }

            if (!clearSession && client.isOpen()) {
                client.setOpen(false);
                // maybe you need to save it into database
                // some update method here
            }

            if (!clearSession) {
                Log.d("提示-", "add new task ~");
                SocketIOManager.scheduleClearTask(new ClearTask(sessionId,
                        handler, true));
                return;
            }

            // start new task to clear the client object
            // 若被其它线程激活，则意味着当前client为有效状态
            if (client.isOpen()) {
                Log.d("提示-", "the client's open is " + client.isOpen());
                return;
            }

            Log.i("提示-","now remove the clients from store with sessionid "
                    + sessionId);

            if (handler != null) {
                handler.OnDisconnect(client);
            } else {
                Log.i("提示-","ioHandler is null");
            }

            client.disconnect();
            store.remove(sessionId);
        }
    }

    /**
     * close the client right now
     *
     * @author yongboy
     * @version 1.0
     * @time 2012-7-12
     */
    protected static class ClearTaskSpeed extends ClearTask {
        public ClearTaskSpeed(String sessionId, IOHandler handler) {
            super(sessionId, handler);
        }



        public void run() {
            Log.d("提示-", "entry ClearTask run method clearSession is and sessionId is " + sessionId);
            Store store = SocketIOManager.getDefaultStore();
            IOClient client = store.get(sessionId);
            if (client == null) {
                Log.d("提示-", "the client is null");
                return;
            }

            if (client.isOpen()) {
                client.setOpen(false);
            }

            Log.i("提示-","now remove the clients from store with sessionid "
                    + sessionId);

            if (handler != null) {
                handler.OnDisconnect(client);
            } else {
                Log.i("提示-","ioHandler is null");
            }

            client.disconnect();
            store.remove(sessionId);
        }
    }
}