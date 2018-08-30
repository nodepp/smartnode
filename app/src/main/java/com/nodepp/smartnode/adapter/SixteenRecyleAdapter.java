package com.nodepp.smartnode.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;

import com.nodepp.smartnode.R;
import com.nodepp.smartnode.activity.CusSixteenchannelControlActivity;

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
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        viewHolder.sixteen_channen_name.setText("通道"+mDatas.get(i));
        if(CusSixteenchannelControlActivity.f1 == 1){

        }
        switch (i){
            case 0:
                if(CusSixteenchannelControlActivity.f1 == 1){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shapeblue_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f1 == 2){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shapeblue_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f1 == 3) {
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_blue_rightcorner);
                }
                viewHolder.battle_tv.setText(CusSixteenchannelControlActivity.b1+"");
            case 1:
                if(CusSixteenchannelControlActivity.f2 == 1){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shapeblue_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f2 == 2){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shapeblue_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f2 == 3) {
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_blue_rightcorner);
                }
                viewHolder.battle_tv.setText(CusSixteenchannelControlActivity.b2+"");
            case 2:
                if(CusSixteenchannelControlActivity.f3 == 1){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shapeblue_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f3 == 2){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shapeblue_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f3 == 3) {
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_blue_rightcorner);
                }
                viewHolder.battle_tv.setText(CusSixteenchannelControlActivity.b3+"");
            case 3:
                if(CusSixteenchannelControlActivity.f4 == 1){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shapeblue_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f4 == 2){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shapeblue_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f4 == 3) {
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_blue_rightcorner);
                }
                viewHolder.battle_tv.setText(CusSixteenchannelControlActivity.b4+"");
            case 4:
                if(CusSixteenchannelControlActivity.f5 == 1){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shapeblue_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f5 == 2){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shapeblue_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f5 == 3) {
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_blue_rightcorner);
                }
                viewHolder.battle_tv.setText(CusSixteenchannelControlActivity.b5+"");
            case 5:
                if(CusSixteenchannelControlActivity.f6 == 1){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shapeblue_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f6 == 2){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shapeblue_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f6 == 3) {
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_blue_rightcorner);
                }
                viewHolder.battle_tv.setText(CusSixteenchannelControlActivity.b6+"");
            case 6:
                if(CusSixteenchannelControlActivity.f7 == 1){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shapeblue_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f7 == 2){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shapeblue_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f7 == 3) {
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_blue_rightcorner);
                }
                viewHolder.battle_tv.setText(CusSixteenchannelControlActivity.b2+"");
            case 7:
                if(CusSixteenchannelControlActivity.f8 == 1){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shapeblue_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f8 == 2){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shapeblue_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f8 == 3) {
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_blue_rightcorner);
                }
                viewHolder.battle_tv.setText(CusSixteenchannelControlActivity.b2+"");
            case 8:
                if(CusSixteenchannelControlActivity.f9 == 1){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shapeblue_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f9 == 2){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shapeblue_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f9 == 3) {
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_blue_rightcorner);
                }
                viewHolder.battle_tv.setText(CusSixteenchannelControlActivity.b9+"");
            case 9:
                if(CusSixteenchannelControlActivity.f10 == 1){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shapeblue_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f10 == 2){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shapeblue_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f10 == 3) {
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_blue_rightcorner);
                }
                viewHolder.battle_tv.setText(CusSixteenchannelControlActivity.b10+"");
            case 10:
                if(CusSixteenchannelControlActivity.f11 == 1){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shapeblue_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f11 == 2){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shapeblue_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f11 == 3) {
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_blue_rightcorner);
                }
                viewHolder.battle_tv.setText(CusSixteenchannelControlActivity.b11+"");
            case 11:
                if(CusSixteenchannelControlActivity.f12 == 1){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shapeblue_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f12 == 2){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shapeblue_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f12 == 3) {
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_blue_rightcorner);
                }
                viewHolder.battle_tv.setText(CusSixteenchannelControlActivity.b12+"");
            case 12:
                if(CusSixteenchannelControlActivity.f13 == 1){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shapeblue_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f13 == 2){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shapeblue_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f13 == 3) {
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_blue_rightcorner);
                }
                viewHolder.battle_tv.setText(CusSixteenchannelControlActivity.b13+"");
            case 13:
                if(CusSixteenchannelControlActivity.f14 == 1){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shapeblue_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f14 == 2){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shapeblue_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f14 == 3) {
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_blue_rightcorner);
                }
                viewHolder.battle_tv.setText(CusSixteenchannelControlActivity.b13+"");
            case 14:
                if(CusSixteenchannelControlActivity.f15 == 1){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shapeblue_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f15 == 2){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shapeblue_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f15 == 3) {
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_blue_rightcorner);
                }
                viewHolder.battle_tv.setText(CusSixteenchannelControlActivity.b15+"");
            case 15:
                if(CusSixteenchannelControlActivity.f16 == 1){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shapeblue_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f16 == 2){
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shapeblue_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
                }else if(CusSixteenchannelControlActivity.f16 == 3) {
                    viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                    viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                    viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_blue_rightcorner);
                }
                viewHolder.battle_tv.setText(CusSixteenchannelControlActivity.b16+"");
        }
        viewHolder.turn_on.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.contronturnon(i);
                viewHolder.turn_on.setBackgroundResource(R.drawable.shapeblue_sixteen_leftcorner);
                viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
            }
        });
        viewHolder.turn_off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.contronturnoff(i);
                viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                viewHolder.turn_off.setBackgroundResource(R.drawable.shapeblue_sixteen_centercorner);
                viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_sixteen_rightcorner);
            }
        });
        viewHolder.turn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.contronturnstop(i);
                viewHolder.turn_on.setBackgroundResource(R.drawable.shape_sixteen_leftcorner);
                viewHolder.turn_off.setBackgroundResource(R.drawable.shape_sixteen_centercorner);
                viewHolder.turn_stop.setBackgroundResource(R.drawable.shape_blue_rightcorner);
            }
        });
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
