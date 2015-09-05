package com.zxl.printbola.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;
import com.zxl.printbola.R;
import com.zxl.socket.MainServer;
import com.zxl.socket.handlers.ServiceHandler;

/**
 * Created by admin on 2015/8/8 0008.
 */
public class SocketService extends Service {
    private MainServer server;
    public static String LaunchService = "com.zxl.pt.Start";
    public static String StopService = "com.zxl.pt.ShutDown";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public SocketService() {
        super();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.e("啊哈", "服务被结束");
        server.stop();
        stopForeground(true);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("哈哈", "服务启动！！！！！");
        server.start();
        return START_NOT_STICKY;
    }


    @Override
    public void onCreate() {
        Log.e("哈哈", "服务创建");
        //启动socket.io服务
        server = new MainServer(new ServiceHandler(), 8000);
        //启动通知栏
        Notification notification = new Notification();
        notification.icon = R.drawable.icon;
        RemoteViews rv = new RemoteViews(getPackageName(), R.layout.notif);
        Intent notifIntent = new Intent(StopService);
        //设置退出按钮的事件
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifIntent, 0);
        rv.setOnClickPendingIntent(R.id.exit, pendingIntent);
        notification.contentView = rv;
        //设置打开浏览器
        Intent notiIntent = new Intent(Intent.ACTION_VIEW);
        if (isPkgInstalled("com.UCMobile")) {
            notiIntent.setClassName("com.UCMobile", "com.UCMobile.main.UCMobile");
        }
        notiIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        notiIntent.setData(Uri.parse("http://" + getLocalIpAddress() + ":" + 8000));
        //设置当点击通知时有响应
        pendingIntent = PendingIntent.getActivity(this, 0, notiIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        notification.contentIntent = pendingIntent;
        startForeground(1, notification);     //启动一个前台不可取消的通知栏
        startActivity(notiIntent); //打开网页
        super.onCreate();
    }

    public String getLocalIpAddress() {
        String localip = null;
        WifiManager manager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (!manager.isWifiEnabled()) {
            Toast.makeText(this.getApplicationContext(),"wifi没有启动请注意！请开启后再次启动程序",Toast.LENGTH_LONG);
        }
        WifiInfo info = manager.getConnectionInfo();
        if (info != null) {
            int address = info.getIpAddress();
            localip = String.format("%d.%d.%d.%d", (address & 0xff), (address & 0xff00) >>> 8, (address & 0xff0000) >>> 16, address >>> 24);
            Log.e("ip","ip+"+localip);
        }

        return localip;
    }

    private boolean isPkgInstalled(String pkgName) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = this.getPackageManager().getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
        }
        if (packageInfo == null) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }
}
