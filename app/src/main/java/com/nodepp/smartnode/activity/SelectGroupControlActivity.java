package com.nodepp.smartnode.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ListView;

import com.lidroid.xutils.db.sqlite.Selector;
import com.lidroid.xutils.db.sqlite.WhereBuilder;
import com.lidroid.xutils.exception.DbException;
import com.nodepp.smartnode.R;
import com.nodepp.smartnode.adapter.GroupControlAdapter;
import com.nodepp.smartnode.model.Device;
import com.nodepp.smartnode.utils.DBUtil;
import com.nodepp.smartnode.utils.JDJToast;
import com.nodepp.smartnode.utils.Log;
import com.nodepp.smartnode.utils.SharedPreferencesUtils;
import com.nodepp.smartnode.view.TitleBar;

import java.util.List;
import java.util.Random;

public class SelectGroupControlActivity extends BaseActivity {

    private static final String TAG = SelectGroupControlActivity.class.getSimpleName();
    private ListView listView;
    private long socketId;
    private long socketTid;
    private int connetedMode;
    private int deviceType;
    private List<Device> devices;
    private GroupControlAdapter groupControlAdapter;
    private StringBuffer tIds;
    private StringBuffer dIds;
    private StringBuffer Ips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_group_control);
        socketId = getIntent().getLongExtra("socket_id", 0);
        socketTid = getIntent().getLongExtra("socket_tid", 0);
        connetedMode = getIntent().getIntExtra("connetedMode", 0);
        deviceType = getIntent().getIntExtra("deviceType", 0);
        initView();
        initData();
    }

    private void initData() {
        String username = SharedPreferencesUtils.getString(this, "username", "");
        try {
            devices = DBUtil.getInstance(this).findAll(Selector.from(Device.class).where("userName", "=", username).and((WhereBuilder.b("connetedMode", "=", connetedMode).and("isGroup", "=", 0))));
        } catch (DbException e) {
            e.printStackTrace();
        }
        if (devices != null) {
            groupControlAdapter = new GroupControlAdapter(this, devices);
            listView.setAdapter(groupControlAdapter);
            if (groupControlAdapter != null) {
                Log.i(TAG,"finish in");
                groupControlAdapter.setOnFinishChangeListener(new GroupControlAdapter.onFinishListener() {
                    @Override
                    public void onFinish(List<Device> selectDevices) {
                        Log.i(TAG,"onFinish");
                        tIds = new StringBuffer();
                        dIds = new StringBuffer();
                        Ips = new StringBuffer();
                        tIds.append("");
                        dIds.append("");
                        for (Device device : selectDevices){
                            tIds.append(device.getTid());
                            tIds.append(";");
                            dIds.append(device.getDid());
                            dIds.append(";");
                            Ips.append(device.getIp());
                            Ips.append(";");
                        }
                    }
                });
            }
        }
    }

    private void initView() {
        listView = (ListView) findViewById(R.id.list_view);
        initTitleBar();
    }

    //初始化titlebar
    private void initTitleBar() {
        TitleBar titleBar = (TitleBar) findViewById(R.id.title_bar);
        titleBar.setRightVisible(TitleBar.BUTTON);
        titleBar.setRightClickListener(new TitleBar.RightClickListener() {
            @Override
            public void onClick() {
                Log.i(TAG, "finish");
                if (tIds != null && dIds != null) {
                    Log.i(TAG, "tIds==" + tIds.toString());
                    Log.i(TAG, "dIds==" + dIds.toString());
                    if (!tIds.toString().equals("") || !dIds.toString().equals("")) {
                        showAddDialog(tIds.toString(), dIds.toString(), Ips.toString());
                    } else {
                        JDJToast.showMessage(SelectGroupControlActivity.this, "请选择后再点击完成");
                    }
                } else {
                    JDJToast.showMessage(SelectGroupControlActivity.this, "请选择后再点击完成");
                }
            }
        });
    }

    private void showAddDialog(final String tIds,final String dIds,final String Ips) {
        Log.i(TAG,"showDialog");
        final String username = SharedPreferencesUtils.getString(this, "username", "");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.input_device_group));
        final EditText editText = new EditText(this);
        editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
        builder.setView(editText);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Device device = new Device();
                String name = editText.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    JDJToast.showMessage(SelectGroupControlActivity.this, getString(R.string.device_name));
                } else {
                    device.setSocketName(name);
                    Random ra = new Random();//产生0，1,2,3随机数，匹配图片
                    device.setPictureIndex(ra.nextInt(4));
                    device.setUserName(username);
                    device.setDid(0);
                    device.setTid(0);
                    device.setDeviceType(deviceType);
                    device.setIsGroup(1);
                    device.setDeviceGroupTids(tIds);
                    device.setIsOnline(true);
                    device.setConnetedMode(connetedMode);
                    device.setDeviceGroupDids(dIds);
                    device.setDeviceIps(Ips);
                    try {
                        DBUtil.getInstance(SelectGroupControlActivity.this).saveBindingId(device);
                    } catch (DbException e) {
                        e.printStackTrace();
                    }
                    ActivityManager.getAppManager().goToActivity(MainActivity.class);
                    dialog.dismiss();
                }
            }
        });
        builder.setNegativeButton(getString(R.string.cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
}
