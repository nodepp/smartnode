package com.nodepp.smartnode.model;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;

/**
 * Created by yuyue on 2017/10/10.
 */
@Table(name = "tb_multiple_remark_name")  // 建议加上注解， 混淆后表名不受影响
public class MultipleRemarkName extends EntityBase{

    @Column(column = "userName")
    private String userName;//当前用户名

    @Column(column = "did")
    private long did;//设备唯一的id

    @Column(column = "tid")
    private long tid;//设备临时id

    @Column(column = "ChannelOneName")
    private String ChannelOneName ="通道一";//通道1名称

    @Column(column = "ChannelTwoName")
    private String ChannelTwoName ="通道二";//通道2名称

    @Column(column = "ChannelThreeName")
    private String ChannelThreeName ="通道三";//通道3名称

    @Column(column = "ChannelFourName")
    private String ChannelFourName ="通道四";//通道4名称

    @Column(column = "ChannelFiveName")
    private String ChannelFiveName ="通道五";//通道5名称

    @Column(column = "ChannelSixName")
    private String ChannelSixName ="通道六";//通道6名称

    @Column(column = "ChannelSevenName")
    private String ChannelSevenName ="通道七";//通道7名称

    @Column(column = "ChannelEightName")
    private String ChannelEightName ="通道八";//通道8名称

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

    public String getChannelOneName() {
        return ChannelOneName;
    }

    public void setChannelOneName(String channelOneName) {
        ChannelOneName = channelOneName;
    }

    public String getChannelTwoName() {
        return ChannelTwoName;
    }

    public void setChannelTwoName(String channelTwoName) {
        ChannelTwoName = channelTwoName;
    }

    public String getChannelThreeName() {
        return ChannelThreeName;
    }

    public void setChannelThreeName(String channelThreeName) {
        ChannelThreeName = channelThreeName;
    }

    public String getChannelFourName() {
        return ChannelFourName;
    }

    public void setChannelFourName(String channelFourName) {
        ChannelFourName = channelFourName;
    }

    public String getChannelFiveName() {
        return ChannelFiveName;
    }

    public void setChannelFiveName(String channelFiveName) {
        ChannelFiveName = channelFiveName;
    }

    public String getChannelSixName() {
        return ChannelSixName;
    }

    public void setChannelSixName(String channelSixName) {
        ChannelSixName = channelSixName;
    }

    public String getChannelSevenName() {
        return ChannelSevenName;
    }

    public void setChannelSevenName(String channelSevenName) {
        ChannelSevenName = channelSevenName;
    }

    public String getChannelEightName() {
        return ChannelEightName;
    }

    public void setChannelEightName(String channelEightName) {
        ChannelEightName = channelEightName;
    }

    @Override
    public String toString() {
        return "MultipleRemarkName{" +
                "userName='" + userName + '\'' +
                ", did=" + did +
                ", tid=" + tid +
                ", ChannelOneName='" + ChannelOneName + '\'' +
                ", ChannelTwoName='" + ChannelTwoName + '\'' +
                ", ChannelThreeName='" + ChannelThreeName + '\'' +
                ", ChannelFourName='" + ChannelFourName + '\'' +
                ", ChannelFiveName='" + ChannelFiveName + '\'' +
                ", ChannelSixName='" + ChannelSixName + '\'' +
                ", ChannelSevenName='" + ChannelSevenName + '\'' +
                ", ChannelEightName='" + ChannelEightName + '\'' +
                '}';
    }
}
