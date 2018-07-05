package com.nodepp.smartnode.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.lidroid.xutils.DbUtils;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.model.TimeTask;
import com.nodepp.smartnode.utils.DBUtil;
import com.nodepp.smartnode.utils.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yuyue on 2016/8/10.
 */
public class TimeTaskAdapter extends BaseAdapter {
    private static final String TAG = TimeTaskAdapter.class.getSimpleName();
    private Context context;
    private List<TimeTask> lists;
    private StateChangeListener stateChangeListener;
    private TimerListener timerListener;
    private boolean isClickChange = true;
    private ArrayList<String> nameLists;

    public void setNameLists(ArrayList<String> nameLists) {
        this.nameLists = nameLists;
    }

    public interface StateChangeListener {
        void onChange();
    }

    public interface TimerListener {
        void open();

        void close();
    }

    public TimeTaskAdapter(Context context, List<TimeTask> lists) {
        this.context = context;
        this.lists = lists;
    }

    public void refresh(List<TimeTask> lists) {
        if (lists != null){
            this.lists = lists;
            Log.i(TAG, "refresh");
            notifyDataSetChanged();
        }
    }

    public void setOnTimerListener(TimerListener listener) {
        this.timerListener = listener;
    }

    public void setOnStateChangeListener(StateChangeListener listener) {

        this.stateChangeListener = listener;
    }

    @Override
    public int getCount() {
        if (lists == null) {
            return 0;
        } else {
            return lists.size();
        }
    }

    @Override
    public TimeTask getItem(int position) {
        if (lists != null && position < lists.size()){
            return lists.get(position);
        }else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.listview_time_task_item, null);
            holder = new ViewHolder();
            holder.tvTime = (TextView) convertView.findViewById(R.id.tv_time);
            holder.tvRepeatTime = (TextView) convertView.findViewById(R.id.tv_repeat_time);
            holder.tvStartTime = (TextView) convertView.findViewById(R.id.tv_start_time);
            holder.tbSwitch = (ToggleButton) convertView.findViewById(R.id.tb_switch);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        TimeTask timeTask = getItem(position);
        if (timeTask != null){
            String time = timeTask.getTime();
            holder.tvTime.setText(time);
            Log.i(TAG, "isUse==" + timeTask.isUse());
            isClickChange =false;//是否时手动改变tbSwitch的标志
            holder.tbSwitch.setChecked(timeTask.isUse());
            isClickChange =true;
            boolean isMonday = timeTask.isMonday();
            boolean isTuesday = timeTask.isTuesday();
            boolean isWednesday = timeTask.isWednesday();
            boolean isThursday = timeTask.isThursday();
            boolean isFriday = timeTask.isFriday();
            boolean isSaturday = timeTask.isSaturday();
            boolean isSunday = timeTask.isSunday();
            StringBuffer buffer = new StringBuffer();
            buffer.append("周");
            if (isMonday)
                buffer.append("一、");
            if (isTuesday)
                buffer.append("二、");
            if (isWednesday)
                buffer.append("三、");
            if (isThursday)
                buffer.append("四、");
            if (isFriday)
                buffer.append("五、");
            if (isSaturday)
                buffer.append("六、");
            if (isSunday)
                buffer.append("日、");
            if (!isMonday && !isTuesday && !isWednesday && !isThursday && !isFriday && !isSaturday && !isSunday) {
//            holder.tvRepeatTime.setText(context.getString(R.string.today));
            } else {
                String s = buffer.toString().substring(0, buffer.length() - 1);//去掉最后一个"、"
                holder.tvRepeatTime.setText(s);
            }
            holder.tbSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ToggleButton tvSwitch = (ToggleButton) v;
                    boolean checked = tvSwitch.isChecked();

                }
            });
            holder.tbSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    DbUtils dbUtils = DBUtil.getInstance(context);
                    if (isClickChange){
                        if (position >lists.size()){
                            return;
                        }
                        TimeTask task = lists.get(position);
                        task.setIsUse(isChecked);
                        try {
                            dbUtils.update(task, WhereBuilder.b("id", "=", lists.get(position).getId()));
                            if (stateChangeListener != null) {
                                stateChangeListener.onChange();//开关状态改变时重新发送定时任务
                            }
                        } catch (DbException e) {
                            e.printStackTrace();
                        }
                        refresh(lists);//状态改变，重新刷新定时任务列表
                    }

                }
            });
            StringBuilder builder = new StringBuilder();
            boolean isOpen = timeTask.isOpen();
            int operateIndex = timeTask.getOperateIndex();
            Log.i("operateIndex","operateIndex---"+operateIndex);
            if (nameLists != null){//多路的时候才显示
                for (int i = 0;i < nameLists.size();i++){
                    builder.append((operateIndex & 1<<i)!=0?nameLists.get(i)+"、":"");
                }
                if (builder.length() > 0){
                    builder.deleteCharAt(builder.length()-1);
                }
                Log.i("aa","time name:"+builder.toString());
                builder.append(isOpen?"开启":"关闭");
            }else {
                builder.append(isOpen?"定时开启":"定时关闭");
            }

            holder.tvStartTime.setText(builder.toString());

//        holder.tvStartTime.setText(getCurrentTime2(timeTask));
        }
        return convertView;
    }

    private String getCurrentTime(String s, TimeTask task) {
        int now = 0;
        int time = 0;
        int sum = 0;
        String[] ss = s.split(":");
        if (ss != null && ss.length > 1) {
            int hour = Integer.parseInt(ss[0]);
            int minute = Integer.parseInt(ss[1]);
            time = hour * 60 + minute;
        }
        Date date = new Date();
        //获取今天的时和分
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        String currentTime = dateFormat.format(date);
        String[] ss1 = currentTime.split(":");
        if (ss != null && ss.length > 1) {
            int hour = Integer.parseInt(ss1[0]);
            int minute = Integer.parseInt(ss1[1]);
            now = hour * 60 + minute;
        }
        if (time < now) {
            time = time + 24 * 60;
        }
        sum = time - now;
        int h = sum / 60;
        int m = sum % 60;
        return h + context.getString(R.string.hour) + m + (task.isOpen() ? context.getString(R.string.open_time) : context.getString(R.string.close_time));
    }

    private String getCurrentTime2(TimeTask task) {
        LinkedList<Integer> timmeList = new LinkedList<Integer>();
        timmeList.add(task.getTimeStamp0());
        timmeList.add(task.getTimeStamp1());
        timmeList.add(task.getTimeStamp2());
        timmeList.add(task.getTimeStamp3());
        timmeList.add(task.getTimeStamp4());
        timmeList.add(task.getTimeStamp5());
        timmeList.add(task.getTimeStamp6());
        int currentTime = (int) (System.currentTimeMillis() / 1000);
        int whichDay = getWhichDay(task, timmeList);
        int timeTime = timmeList.get(whichDay);
        if (timeTime > currentTime) {
            timeTime = timeTime - currentTime;
        } else {
            if (task.isRepeat()) {
                timeTime = (timeTime + 7 * 24 * 60 * 60) - currentTime;//每周重复才用
            } else {
                return "已经启用";
            }
        }
        Log.i(TAG, "timeTime==" + timeTime);
        int day = timeTime / (60 * 60 * 24);
        int h = (timeTime % (60 * 60 * 24)) / (60 * 60);
        int m = ((timeTime) % (60 * 60)) / (60);
        //定时完成
        Log.i(TAG, "isOpen==" + task.isOpen());
        if (day <= 0 && h <= 0 && m <= 0) {
            if (timerListener != null) {
                if (task.isOpen()) {
                    timerListener.open();
                } else {
                    timerListener.close();
                }
            }
        }
        return (day > 0 ? day + "天" : "") + (h > 0 ? h + context.getString(R.string.hour) : "") + (m > 0 ? m + "分钟后" : "即将") + (task.isOpen() ? context.getString(R.string.open_time) : context.getString(R.string.close_time));
    }

    static class ViewHolder {
        TextView tvTime;
        TextView tvRepeatTime;
        TextView tvStartTime;
        ToggleButton tbSwitch;
    }

    private int getWhichDay(TimeTask timeTask, LinkedList<Integer> timmeList) {//获取离今天最近的定时任务是哪一天
        int weekDay = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK) - 1;//减1之后周日是0，周一是1
        int whichDay = weekDay;
        int temp = 0;
        LinkedList<Boolean> weekLists = new LinkedList<Boolean>();
        weekLists.add(0, timeTask.isSunday() ? true : false);
        weekLists.add(1, timeTask.isMonday() ? true : false);
        weekLists.add(2, timeTask.isTuesday() ? true : false);
        weekLists.add(3, timeTask.isWednesday() ? true : false);
        weekLists.add(4, timeTask.isThursday() ? true : false);
        weekLists.add(5, timeTask.isFriday() ? true : false);
        weekLists.add(6, timeTask.isSaturday() ? true : false);
        for (int i = 0 + weekDay; i < 7 + weekDay; i++) {
            int currentTime = (int) (System.currentTimeMillis() / 1000);
            if (timmeList.get(i % 7) > (currentTime + 6 * 24 * 60 * 60)) {//若日期是今天但是时间戳刚好小于当前，先存着，跳过今天，寻找下一个定时时间
                continue;
            }
            if (weekLists.get(i % 7)) {
                whichDay = i % 7;
                break;
            }
        }
        return whichDay;
    }

}
