package com.zxl.socket.handlers;

import android.util.Log;
import com.zxl.socket.server.IOHandlerAbs;
import com.zxl.socket.server.transport.IOClient;
import org.apache.log4j.Logger;


/**
 * Created by admin on 2015/8/7 0007.
 */
public class ServiceHandler extends IOHandlerAbs {
    @Override
    public void OnConnect(IOClient client) {
        Log.e("有客户端连接",client.getSessionID())  ;

    }
public ServiceHandler(){}
    @Override
    public void OnMessage(IOClient ioClient, String s) {
        //忠实转发即可
        super.broadcast(s);
    }

    @Override
    public void OnDisconnect(IOClient ioClient) {
       Log.d("客户断开连接",ioClient.getSessionID());
    }

    @Override
    public void OnShutdown() {
      Log.d("服务器关闭","shutdown");
    }
}
