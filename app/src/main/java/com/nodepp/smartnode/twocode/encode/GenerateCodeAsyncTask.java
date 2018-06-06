package com.nodepp.smartnode.twocode.encode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.Toast;

import com.nodepp.smartnode.R;
import com.nodepp.smartnode.utils.Utils;

/**
 * Created by yuyue on 2017/1/6.
 */
public class GenerateCodeAsyncTask extends AsyncTask<String, Void, Bitmap>{
    private Context context;
    private GenerateListener listener;

    public GenerateCodeAsyncTask(Context context){
        this.context = context;
    }
    @Override
    protected Bitmap doInBackground(String... params) {
        Bitmap logoBitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.app_logo);
        return QRCodeEncoder.syncEncodeQRCode(params[0], Utils.Dp2Px(context, 150), Color.BLACK, Color.WHITE, logoBitmap);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (bitmap != null) {
            if (listener != null){
                listener.onShow(bitmap);
            }
        } else {
            Toast.makeText(context, "生成二维码失败", Toast.LENGTH_SHORT).show();
        }
    }
    public void setOnGenerateListener(GenerateListener listener){
        this.listener = listener;
    }

    public interface GenerateListener {
        void onShow(Bitmap bitmap);
    }
}
