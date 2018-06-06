package com.nodepp.smartnode.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.nodepp.smartnode.utils.JDJToast;

import org.apache.http.conn.ssl.SSLSocketFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * 下载皮肤
 * Created by yuyue on 2017/8/26.
 */
public class DownloadSkinTask extends AsyncTask<String, Integer, File> {
    private Context context;
    private ProgressDialog pd; //进度条对话框

    public DownloadSkinTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        pd = new ProgressDialog(context);
        pd.setCanceledOnTouchOutside(false);
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setMessage("正在下载皮肤");
        pd.setProgressNumberFormat("%1d kb/%2d kb");//进度和总数
        pd.setCancelable(false);
        pd.show();
        super.onPreExecute();
    }

    @Override
    protected File doInBackground(String... params) {
        String path = params[0];
        String skinDir = params[1];
        String skinName = params[2];
        HttpURLConnection conn = null;
        File file = null;
        //如果相等的话表示当前的sdcard挂载在手机上并且是可用的
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            URL url = null;
            try {
                url = new URL(path);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            try {
                if (url.getProtocol().toLowerCase().equals("https")) {
                    HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
                    https.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
                    conn = https;
                } else {
                    conn = (HttpURLConnection) url.openConnection();
                }
                conn.setConnectTimeout(10000);
                //获取到文件的大小
                int count = conn.getContentLength();
                InputStream is = conn.getInputStream();
                File dirFirstFolder = new File(skinDir);//方法二：通过变量文件来获取需要创建的文件夹名字
                if(!dirFirstFolder.exists())
                { //如果该文件夹不存在，则进行创建
                    dirFirstFolder.mkdirs();//创建文件夹

                }
                file = new File(dirFirstFolder,skinName);
                FileOutputStream fos = new FileOutputStream(file);
                BufferedInputStream bis = new BufferedInputStream(is);
                byte[] buffer = new byte[1024];
                int len;
                int total = 0;
                while ((len = bis.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    total += len;
                    //获取当前下载量
                    publishProgress(total, count);
                }
                fos.close();
                bis.close();
                is.close();
                return file;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return null;
        }
        return file;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        pd.setProgress(values[0] / 1024);
        pd.setMax(values[1] / 1024);
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(File file) {
        if (file != null) {
            JDJToast.showMessage(context,"下载成功");
            pd.dismiss();
        } else {
            JDJToast.showMessage(context,"下载失败");
            pd.dismiss();
        }
    }
}
