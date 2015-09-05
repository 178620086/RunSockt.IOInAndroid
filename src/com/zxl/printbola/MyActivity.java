package com.zxl.printbola;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.zxl.printbola.service.SocketService;
import com.zxl.printbola.utitl.Tools;

import java.io.File;
import java.io.IOException;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if(getIntent().getAction().equals(SocketService.StopService)){ //如果是启动的service
            Log.e("pok", "程序退出" + getIntent().getAction());
            stopService(new Intent(SocketService.StopService));
            finish();
            System.exit(0);
        }  else {
            setup();       //启动时初始化
            Intent intent = new Intent(SocketService.LaunchService);
            startService(intent);
            finish();
        }

    }

    private void setup() {
        final Tools tools = new Tools(this);
        Tools.setContext(this.getApplicationContext()); //给tools上弹药
        new Thread(new Runnable() {
            @Override
            public void run() {
                File files = getExternalFilesDir("static");
                String[] fileList = files.list();
                if (files.exists() && fileList.length < 5) {
                    toastInUI("第一次使用开始初始化，请稍等");
                    try {
                        boolean isCopy = tools.copy();
                        if (isCopy) {
                            toastInUI("初始化完成。程序启动中...");
                        } else {
                            toastInUI("初始化失败...");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    toastInUI("程序启动中...");
                }
            }
        }).start();
    }

    private void toastInUI(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }

}
