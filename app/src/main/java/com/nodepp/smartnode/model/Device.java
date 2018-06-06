package com.nodepp.smartnode.model;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;

/**
 * Created by yuyue on 2016/8/15.
 */
@Table(name = "tb_device")  // 建议加上注解， 混淆后表名不受影响
public class Device extends EntityBase {
    @Column(column = "pictureIndex")
    private int pictureIndex;//随机数，对应4张图片其中的一种

    @Column(column = "socketName")
    private String socketName;//设备的名称

    @Column(column = "userName")
    private String userName ;//当前用户名

    @Column(column = "did")
    private long did;//设备唯一的id

    @Column(column = "tid")
    private long tid;//设备临时id

    @Column(column = "deviceType")
    private int deviceType = 0;//设备类型，0表示不确定什么类型，1表示普通1路控制灯，2表示普通6路控制灯，3表示彩光灯,4表示4路，5表示8路,6表示白灯，7pwm彩灯，8 pwm白灯,9 串口通讯,10 二路控制器

    @Column(column = "connetedMode")
    private int connetedMode = 0;//设备连接类型，0表示互联网连接控制，1表示近场局域网控制

    @Column(column = "isGroup")
    private int isGroup;//0代表不是，1代表是群组控制

    @Column(column = "ip")
    private String ip;//记录近场通讯的设备ip

    @Column(column = "isOnline")
    private boolean isOnline = false;//设备是否在线

    @Column(column = "deviceGroupTids")
    private String deviceGroupTids = "";

    @Column(column = "deviceGroupDids")
    private String deviceGroupDids = "";

    @Column(column = "deviceIps")
    private String deviceIps = "";

    @Column(column = "routerMac")
    private String routerMac = "";

    @Column(column = "special")
    private int special = 0;

    @Column(column = "clientKey")
    private String clientKey = "1234567890";


    @Column(column = "firmwareLevel")
    private int firmwareLevel = 0;

    @Column(column = "firmwareVersion")
    private int  firmwareVersion= 10000;

    @Column(column = "deviceMode")
    private int  deviceMode= 0;//设备的模式 0表示自锁模式 ，1表示点动模式,2表示互锁模式

    public int getPictureIndex() {
        return pictureIndex;
    }

    public void setPictureIndex(int pictureIndex) {
        this.pictureIndex = pictureIndex;
    }

    public String getSocketName() {
        return socketName;
    }

    public void setSocketName(String socketName) {
        this.socketName = socketName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public long getDid() {
        return did;
    }

    public void setDid(long did) {
        this.did = did;
    }

    public long getTid() {
        return tid;
    }

    public void setTid(long tid) {
        this.tid = tid;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public int getConnetedMode() {
        return connetedMode;
    }

    public void setConnetedMode(int connetedMode) {
        this.connetedMode = connetedMode;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public String getDeviceGroupTids() {
        return deviceGroupTids;
    }

    public void setDeviceGroupTids(String deviceGroupTids) {
        this.deviceGroupTids = deviceGroupTids;
    }

    public String getDeviceGroupDids() {
        return deviceGroupDids;
    }

    public void setDeviceGroupDids(String deviceGroupDids) {
        this.deviceGroupDids = deviceGroupDids;
    }

    public int getIsGroup() {
        return isGroup;
    }

    public void setIsGroup(int isGroup) {
        this.isGroup = isGroup;
    }

    public int getSpecial() {
        return special;
    }

    public void setSpecial(int special) {
        this.special = special;
    }

    public String getDeviceIps() {
        return deviceIps;
    }

    public void setDeviceIps(String deviceIps) {
        this.deviceIps = deviceIps;
    }

    public String getRouterMac() {
        return routerMac;
    }

    public void setRouterMac(String routerMac) {
        this.routerMac = routerMac;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public int getFirmwareLevel() {
        return firmwareLevel;
    }

    public void setFirmwareLevel(int firmwareLevel) {
        this.firmwareLevel = firmwareLevel;
    }

    public int getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(int firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    @Override
    public int getId() {
        return super.getId();
    }

    @Override
    public void setId(int id) {
        super.setId(id);
    }

    public int getDeviceMode() {
        return deviceMode;
    }

    public void setDeviceMode(int deviceMode) {
        this.deviceMode = deviceMode;
    }

    @Override
    public String toString() {
        return "Device{" +
                "pictureIndex=" + pictureIndex +
                ", socketName='" + socketName + '\'' +
                ", userName='" + userName + '\'' +
                ", did=" + did +
                ", tid=" + tid +
                ", deviceType=" + deviceType +
                ", connetedMode=" + connetedMode +
                ", isGroup=" + isGroup +
                ", ip='" + ip + '\'' +
                ", isOnline=" + isOnline +
                ", deviceGroupTids='" + deviceGroupTids + '\'' +
                ", deviceGroupDids='" + deviceGroupDids + '\'' +
                ", deviceIps='" + deviceIps + '\'' +
                ", routerMac='" + routerMac + '\'' +
                ", special=" + special +
                ", clientKey='" + clientKey + '\'' +
                ", firmwareLevel=" + firmwareLevel +
                ", firmwareVersion=" + firmwareVersion +
                ", deviceMode=" + deviceMode +
                '}';
    }
}
