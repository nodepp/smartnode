package com.nodepp.smartnode.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nodepp.smartnode.R;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.utils.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuyue on 2016/8/10.
 */
public class GroupControlAdapter extends BaseAdapter {
    private static final String TAG = GroupControlAdapter.class.getSimpleName();
    private Context context;
    private List<Device> lists;
    private List<Device> selectDevices = new ArrayList<Device>();
    private onFinishListener listener;

    public GroupControlAdapter(Context context, List<Device> lists) {
        this.context = context;
        this.lists = lists;
    }

    public interface onFinishListener {
        void onFinish(List<Device> selectDevices);
    }


    public void setOnFinishChangeListener(onFinishListener listener) {
        Log.i(TAG,"setOnFinishChangeListener");
        this.listener = listener;
    }
    @Override
    public int getCount() {
        return lists.size();

    }

    @Override
    public Device getItem(int position) {
        return lists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.listview_group_control_item, null);
            holder = new ViewHolder();
            holder.cbSelcct = (CheckBox) convertView.findViewById(R.id.cb_selcct);
            holder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
            holder.tvName = (TextView) convertView.findViewById(R.id.tv_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final Device device = getItem(position);
        switch (device.getPictureIndex()) {//根据随机数匹配相应的图片
            case 0:
                holder.ivIcon.setBackgroundResource(R.mipmap.light_1);
                break;
            case 1:
                holder.ivIcon.setBackgroundResource(R.mipmap.light_2);
                break;
            case 2:
                holder.ivIcon.setBackgroundResource(R.mipmap.light_3);
                break;
            case 3:
                holder.ivIcon.setBackgroundResource(R.mipmap.light_4);
                break;
        }
        holder.tvName.setText(device.getSocketName());
        holder.cbSelcct.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    selectDevices.add(device);
                    Log.i(TAG, "add");
                } else {
                    Log.i(TAG, "remove");
                    if (selectDevices != null && selectDevices.size() > 0) {
                        selectDevices.remove(device);
                    }
                }
                if (listener != null) {
                    Log.i(TAG, "onFinish");
                    if (selectDevices != null) {
                        listener.onFinish(selectDevices);
                    }
                }
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = holder.cbSelcct.isChecked();
                holder.cbSelcct.setChecked(!checked);
            }
        });
        return convertView;
    }

    static class ViewHolder {
        CheckBox cbSelcct;
        ImageView ivIcon;
        TextView tvName;

    }
}
