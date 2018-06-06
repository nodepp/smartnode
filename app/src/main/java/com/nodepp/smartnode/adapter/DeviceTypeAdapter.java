package com.nodepp.smartnode.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nodepp.smartnode.R;

/**
 * Created by yuyue on 2017/6/27.
 */
public class DeviceTypeAdapter extends BaseAdapter {
    private Context context;

    public DeviceTypeAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return 3;
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holer;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.listview_device_item, null);
            holer = new ViewHolder();
            holer.icon = (ImageView) convertView.findViewById(R.id.iv_icon);
            holer.name = (TextView) convertView.findViewById(R.id.tv_name);
            convertView.setTag(holer);
        } else {
            holer = (ViewHolder) convertView.getTag();
        }
        if (position == 0){
            holer.icon.setBackgroundResource(R.mipmap.device_socket);
            holer.name.setText("智能插座");
        }else if (position == 1){
            holer.icon.setBackgroundResource(R.mipmap.device_light);
            holer.name.setText("智能彩灯");
        }else if (position == 2){
            holer.icon.setBackgroundResource(R.mipmap.device_light);
            holer.name.setText("植物照明灯");
        }
        return convertView;
    }
    static class ViewHolder {
        ImageView icon;
        TextView name;
    }
}
