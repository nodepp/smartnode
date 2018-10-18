package com.nodepp.smartnode.adapter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.activity.BaseVoiceActivity;
import com.nodepp.smartnode.activity.CusSixteenchannelControlActivity;
import com.nodepp.smartnode.utils.DBUtil;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;

import java.util.List;

/**
 * Created by yuyue on 2016/8/9.
 */
public class SixteenRecyleAdapter extends RecyclerView.Adapter<SixteenRecyleAdapter.ViewHolder> {

    private static final String TAG = SixteenRecyleAdapter.class.getSimpleName();
    private List<String> mDatas;
    private AdapterView.OnItemClickListener onItemClickListener;
    CusSixteenchannelControlActivity context;

    public SixteenRecyleAdapter(CusSixteenchannelControlActivity context, List<String> mDatas) {
        this.context = context;
        this.mDatas = mDatas;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.listview_sixteen_item, parent, false);
        //创建一个viewholder
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        viewHolder.sixteen_channen_name.setText("通道" + mDatas.get(position));
        updateData(viewHolder, position);
        viewHolder.turn_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.contronturnon(position);
                viewHolder.turn_on.setBackgroundResource(R.drawable.shapeblue_sixteen_leftcorner);
                viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
            }
        });
        viewHolder.turn_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.contronturnoff(position);
                viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                viewHolder.turn_off.setBackgroundResource(R.drawable.shapeblue_sixteen_centercorner);
                viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
            }
        });
        viewHolder.turn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.contronturnstop(position);
                viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_blue_rightcorner);
            }
        });
    }

    private void updateData(final ViewHolder viewHolder, final int position) {
        if (CusSixteenchannelControlActivity.arrayF[position] == 1) {
            viewHolder.turn_on.setBackgroundResource(R.drawable.shapeblue_sixteen_leftcorner);
            viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
            viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
        } else if (CusSixteenchannelControlActivity.arrayF[position] == 2) {
            viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
            viewHolder.turn_off.setBackgroundResource(R.drawable.shapeblue_sixteen_centercorner);
            viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
        } else if (CusSixteenchannelControlActivity.arrayF[position] == 3) {
            viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
            viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
            viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_blue_rightcorner);
        }
        viewHolder.battle_tv.setText(CusSixteenchannelControlActivity.arrayB[position] + "");
        android.util.Log.e(TAG, "b: -->" + CusSixteenchannelControlActivity.arrayB[position]);
    }


    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView sixteen_channen_name;
        Button turn_on;
        Button turn_off;
        Button turn_stop;
        TextView battle_tv;

        public ViewHolder(View view) {
            super(view);
            sixteen_channen_name = view.findViewById(R.id.tv_sixteen_channel_name);
            turn_on = view.findViewById(R.id.sixteen_turn_on);
            turn_off = view.findViewById(R.id.sixteen_turn_off);
            turn_stop = view.findViewById(R.id.sixteen_turn_stop);
            battle_tv = view.findViewById(R.id.battle_tv);
        }
    }

}
