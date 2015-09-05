package com.zxl.printbola.utitl;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by admin on 2015/8/10 0010.
 */
public class Tools {
    private static Resources res = null;
    private static Context context=null;
    public static Resources getRes() {
        return res;
    }
    public static Context getContext() {
        return context;
    }
    public static void setContext(Context ctx) {
       res=ctx.getResources();
        context=ctx;
    }
    private final Context mContext;
    private final AssetManager mAssetManager;
    private File mAppDirectory;

    public Tools(Context context) {
        mContext = context;
        mAssetManager = context.getAssets();
    }

    /**
     * 将assets目录下指定的文件拷贝到sdcard中
     *
     * @return 是否拷贝成功, true 成功；false 失败
     * @throws IOException
     */
    public boolean copy() throws IOException {
        //获取系统在SDCard中为app分配的目录，eg:/sdcard/Android/data/$(app's package)
        //该目录存放app相关的各种文件(如cache，配置文件等)，unstall app后该目录也会随之删除
        mAppDirectory = mContext.getExternalFilesDir(null);
        if (null == mAppDirectory) {
            return false;
        }

        //读取assets/$(subDirectory)目录下的assets.lst文件，得到需要copy的文件列表
        String[] assets = getAssetsList();
        for (String asset : assets) {
            //如果不存在，则添加到copy列表
            if (!new File(mAppDirectory, asset).exists()) {
                diguiCopy("static/"+asset);
            }
        }

        return true;
    }

    /**
     * 递归拷贝文件
     * @param files
     */
    private  void diguiCopy(String files){
        try {
            String[] fL=mAssetManager.list(files);
                  if(fL.length>0){//如果是文件夹就继续向下拷贝
                      for(String i:fL){
                          diguiCopy(files+"/"+i);
                      }
                  }else{ //否则就复制这个文件
                      copy(files);
                      return;
                  }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取需要拷贝的文件列表（记录在assets/assets.lst文件中）
     *
     * @return 文件列表
     * @throws IOException
     */
    protected String[] getAssetsList() throws IOException {
        return mContext.getAssets().list("static");
    }

    /**
     * 执行拷贝任务
     *
     * @param asset 需要拷贝的assets文件路径
     * @return 拷贝成功后的目标文件句柄
     * @throws IOException
     */
    protected File copy(String asset) throws IOException {
        InputStream source = mAssetManager.open(asset);
        File destinationFile = new File(mAppDirectory, asset);
        destinationFile.getParentFile().mkdirs();
        OutputStream destination = new FileOutputStream(destinationFile);
        byte[] buffer = new byte[1024];
        int nread;

        while ((nread = source.read(buffer)) != -1) {
            if (nread == 0) {
                nread = source.read();
                if (nread < 0)
                    break;
                destination.write(nread);
                continue;
            }
            destination.write(buffer, 0, nread);
        }
        destination.close();

        return destinationFile;
    }


}
