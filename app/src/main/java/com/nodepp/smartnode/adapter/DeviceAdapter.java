package com.nodepp.smartnode.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.activity.AddDeviceActivity;
import com.nodepp.smartnode.activity.BathHeaterActivity;
import com.nodepp.smartnode.activity.ColorControlActivity;
import com.nodepp.smartnode.activity.MultichannelControlActivity;
import com.nodepp.smartnode.activity.SendMessageActivity;
import com.nodepp.smartnode.activity.SwitchActivity;
import com.nodepp.smartnode.activity.WhiteLightActivity;
import com.nodepp.smartnode.model.ColorsSelect;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.model.TimeTask;
import com.nodepp.smartnode.twocode.encode.GenerateCodeAsyncTask;
import com.nodepp.smartnode.udp.ResponseListener;
import com.nodepp.smartnode.udp.UDPSocketA2S;
import com.nodepp.smartnode.utils.DBUtil;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.PbDataUtils;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;
import com.nodepp.smartnode.utils.Utils;
import com.nodepp.smartnode.view.PopupWindow.ClientInfoPopup;
import com.nodepp.smartnode.view.SelectDialog;
import com.nodepp.smartnode.view.loadingdialog.LoadingDialog;

import java.util.List;

import nodepp.Nodepp;

/**
 * Created by yuyue on 2016/8/9.
 */
public class DeviceAdapter extends BaseAdapter {

    private static final String TAG = DeviceAdapter.class.getSimpleName();
    private Context context;
    private List<Device> devices;

    //    public interface shareListener {
//        void OnShare(Device device);
//    }
//    public void setShareListener(shareListener shareListener){
//
//        this.shareListener = shareListener;
//    }
    public DeviceAdapter(Context context, List<Device> devices) {
        this.context = context;
        this.devices = devices;
    }

    public void refresh(List<Device> devices) {
        this.devices = devices;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holer;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.listview_devices_item, null);
            holer = new ViewHolder();
            holer.ivDeviceLogo = (ImageView) convertView.findViewById(R.id.iv_device_logo);
            holer.ivDeviceUninleTag = (ImageView) convertView.findViewById(R.id.iv_device_unline_tag);
            holer.tvDeviceDame = (TextView) convertView.findViewById(R.id.tv_device_name);
            holer.tvDeviceTypeDame = (TextView) convertView.findViewById(R.id.tv_device_type_name);
            convertView.setTag(holer);
        } else {
            holer = (ViewHolder) convertView.getTag();
        }
        Device device = devices.get(position);
        holer.tvDeviceDame.setText(device.getSocketName());
        switch (device.getDeviceType()) {//1表示普通1路控制灯，2表示普通6路控制灯，3表示彩光灯,4表示4路，5表示8路,6表示白灯
            case 1:
                //holer.tvDeviceTypeDame.setText("单路控制器");
                holer.ivDeviceLogo.setBackgroundResource(device.isOnline()?R.mipmap.ic_control_one_online:R.mipmap.ic_control_one_unline);
                break;
            case 2:
                //holer.tvDeviceTypeDame.setText("六路控制器");
                holer.ivDeviceLogo.setBackgroundResource(device.isOnline()?R.mipmap.ic_control_six_online:R.mipmap.ic_control_six_unline);
                break;
            case 3:
            case 7:
                //holer.tvDeviceTypeDame.setText("彩色灯");
                holer.ivDeviceLogo.setBackgroundResource(device.isOnline()?R.mipmap.ic_control_colorlight_online:R.mipmap.ic_control_colorlight_unline);
                break;
            case 4:
                //holer.tvDeviceTypeDame.setText("四路控制器");
                holer.ivDeviceLogo.setBackgroundResource(device.isOnline()?R.mipmap.ic_control_four_online:R.mipmap.ic_control_four_unline);
                break;
            case 6:
            case 8:
                //holer.tvDeviceTypeDame.setText("白色灯");
                holer.ivDeviceLogo.setBackgroundResource(device.isOnline()?R.mipmap.ic_control_whitelight_online:R.mipmap.ic_control_whitelight_unline);
                break;
            case 9:
                //holer.tvDeviceTypeDame.setText("串口通讯");
                holer.ivDeviceLogo.setBackgroundResource(device.isOnline()?R.mipmap.bath_nor:R.mipmap.bath_un);
                break;
            case 10:
                //holer.tvDeviceTypeDame.setText("二路控制器");
                holer.ivDeviceLogo.setBackgroundResource(device.isOnline()?R.mipmap.ic_control_two_online:R.mipmap.ic_control_two_unline);
                break;
            case 12:
                //holer.tvDeviceTypeDame.setText("浴霸通讯");
                holer.ivDeviceLogo.setBackgroundResource(device.isOnline()?R.mipmap.bath_nor:R.mipmap.bath_un);
                break;
        }
        if (device.isOnline()){
            holer.tvDeviceTypeDame.setTextColor(context.getResources().getColor(R.color.text_color2));
            holer.tvDeviceDame.setTextColor(context.getResources().getColor(R.color.text_color1));
            holer.ivDeviceUninleTag.setVisibility(View.INVISIBLE);
        }else {
            holer.tvDeviceTypeDame.setTextColor(context.getResources().getColor(R.color.edit_hint));
            holer.tvDeviceDame.setTextColor(context.getResources().getColor(R.color.edit_hint));
            holer.ivDeviceUninleTag.setVisibility(View.VISIBLE);
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = null;
                Device device = devices.get(position);
                if (device.isOnline()) {
                    int deviceType = device.getDeviceType();
                    if (deviceType == 1) {
                        intent = new Intent(context, SwitchActivity.class);
                    } else if (deviceType == 3 || deviceType == 7) {//彩灯
                        SharedPreferencesUtils.saveBoolean(context, device.getTid() + "isClickWhite", false);
                        intent = new Intent(context, ColorControlActivity.class);
                    } else if (deviceType == 2 || deviceType == 4 || deviceType == 10 ) {//6,4,2路控制器
                        intent = new Intent(context, MultichannelControlActivity.class);
                    } else if (deviceType == 6 || deviceType == 8) {//白灯
                        intent = new Intent(context, WhiteLightActivity.class);
                    }else if (deviceType == 12){
                        intent = new Intent(context, BathHeaterActivity.class);
                    }else {
                        JDJToast.showMessage(context, context.getString(R.string.unknow_device));
                        return;
                    }
                    intent.putExtra("device", device);
                    context.startActivity(intent);
                } else {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {//部分手机上报没有在主线程
                            JDJToast.showMessage(context, "设备不在线，无法操作");
                        }
                    });
                }
            }
        });
        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //长按分享和删除
                SelectDialog selectDialog = new SelectDialog(context);
                selectDialog.setCallBackListener(new SelectDialog.MyDialogListener() {
                    @Override
                    public void share() {
                        final LoadingDialog loadingDialog = new LoadingDialog(context, "正在生成分享二维码...");
                        loadingDialog.show();
                        long did = devices.get(position).getDid();
                        final long tid = devices.get(position).getTid();
                        long uid = Long.parseLong(Constant.userName);
                        final Nodepp.Msg msg = PbDataUtils.requestShareSig(uid, did, Constant.usig);
                        Log.i("kk", "requestShareSig=send=msg=" + msg.toString());
                        UDPSocketA2S.send(context, msg, new ResponseListener() {
                            @Override
                            public void onSuccess(Nodepp.Msg msg) {
                                Log.i("kk", "receive=sharesig=msg=" + msg.toString());
                                int result = msg.getHead().getResult();
                                if (result == 404) {
                                    Log.i("kk", "==========1============");
                                    JDJToast.showMessage(context, "分享失败了");
                                    loadingDialog.dismiss();
                                    return;
                                } else if (result == 0) {
                                    String shareSig = PbDataUtils.byteString2String(msg.getShareVerification());
                                    if (tid != 0) {
                                        GenerateCodeAsyncTask task = new GenerateCodeAsyncTask(context);
                                        String createTwoCodeString = Utils.getCreateTwoCodeString(devices.get(position), msg.getShareVerification());
                                        task.execute(createTwoCodeString);
                                        task.setOnGenerateListener(new GenerateCodeAsyncTask.GenerateListener() {
                                            @Override
                                            public void onShow(Bitmap bitmap) {
                                                View view = View.inflate(context, R.layout.two_code, null);
                                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                                builder.setView(view);
                                                ImageView imageView = (ImageView) view.findViewById(R.id.iv_two_code);
                                                imageView.setImageBitmap(bitmap);
                                                AlertDialog dialog = builder.create();
                                                dialog.setCancelable(true);
                                                dialog.setCanceledOnTouchOutside(true);
                                                dialog.show();
                                                loadingDialog.dismiss();
                                            }
                                        });
                                    } else {
                                        loadingDialog.dismiss();
                                        JDJToast.showMessage(context, "群组没有分享功能");
                                    }
                                }
                            }

                            @Override
                            public void onTimeout(Nodepp.Msg msg) {

                            }

                            @Override
                            public void onFaile() {
                                loadingDialog.dismiss();
                                JDJToast.showMessage(context, "分享失败请重试");
                            }
                        });
                    }

                    @Override
                    public void delect() {
                        showDialog(context, DeviceAdapter.this, devices.get(position));
                    }

                    @Override
                    public void exit() {

                    }
                });
                selectDialog.show();
                return false;

            }
        });

        return convertView;
    }

    private void showDialog(final Context context, final DeviceAdapter socketAdapter, final Device device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(context.getString(R.string.delect_device_or_not));
        builder.setPositiveButton(context.getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                DbUtils dbUtils = DBUtil.getInstance(context);
                try {
                    devices.remove(device);
                    dbUtils.delete(device);
                    dbUtils.delete(TimeTask.class, WhereBuilder.b("deviceId", "=", device.getId()));
                    dbUtils.delete(ColorsSelect.class, WhereBuilder.b("deviceId", "=", device.getId()));
                    //删除记录彩色值
                    SharedPreferencesUtils.remove(context, "white" + device.getTid() + "lightDark");
                    SharedPreferencesUtils.remove(context, "white" + device.getTid() + "addColor");
                    socketAdapter.notifyDataSetChanged();

                } catch (DbException e) {
                    e.printStackTrace();
                }
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(context.getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    static class ViewHolder {
        ImageView ivDeviceLogo;
        ImageView ivDeviceUninleTag;
        TextView  tvDeviceDame;
        TextView tvDeviceTypeDame;
    }
}
