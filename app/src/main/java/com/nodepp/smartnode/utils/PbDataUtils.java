package com.nodepp.smartnode.utils;

import android.content.Context;

import com.google.protobuf.ByteString;
import com.nodepp.smartnode.Constant;
import com.nodepp.smartnode.model.ColorsSelect;
import com.nodepp.smartnode.model.MultipleTimeTask;
import com.nodepp.smartnode.model.TimeTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import nodepp.Nodepp;
import outnodepp.Outnodepp;

public class PbDataUtils {
    private static String TAG = "PbDataUtils";
    public static int seq = 0;
    private static byte[] buff = new byte[4096];
    public static int getCurrentSeq(){
        synchronized (PbDataUtils.class){
            seq ++;
            if (seq > 999999999){
                seq = 0;
            }
        }
        return seq;
    }
    public static int getSeq(){
        return seq;
    }
    public static Nodepp.Msg createTwoCode(long did, long tid,int deviceType,String ip,ByteString shareSig) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        msgBuilder.setDid(did);
        msgBuilder.setTid(tid);
        msgBuilder.setDeviceType(deviceType);
        msgBuilder.setWifiName(PbDataUtils.string2ByteString(ip));
        msgBuilder.setShareVerification(shareSig);
        msgBuilder.setAppProtocol(1);
        return msgBuilder.build();
    }
    /**
     * 设置获取uid请求参数
     *
     * @param userType
     * @param appId
     * @param user
     * @param key
     * @return
     */
    public static Nodepp.Msg setRequestParamToGetUid(int userType, long appId, String user, String key) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setCmd(0x12);
        msgBuilder.setHead(headBuilder);
        msgBuilder.setUserType(userType);
        msgBuilder.setAppid(appId);
        msgBuilder.setUser(PbDataUtils.string2ByteString(user));
        msgBuilder.setKey(PbDataUtils.string2ByteString(key));
        return msgBuilder.build();
    }
    public static Nodepp.Msg getSDKUid(Context context,String user, long appId, String appKey) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setDeviceIdentification(string2ByteString(Utils.getPhoneSign(context)));
        headBuilder.setCmd(0x12);
        msgBuilder.setHead(headBuilder);
        msgBuilder.setUserType(4);
        msgBuilder.setAppid(appId);
        msgBuilder.setUser(PbDataUtils.string2ByteString(user));
        msgBuilder.setKey(PbDataUtils.string2ByteString(appKey));
        return msgBuilder.build();
    }
    /**
     * 设置查询插座状态的请求参数
     *
     * @param uid
     * @param did
     * @param uidSig
     * @return
     */
    public static Nodepp.Msg setQueryStateRequestParam(long uid, long did,long tid, String uidSig) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setCmd(0x10);
        headBuilder.setUid(uid);
        headBuilder.setDid(did);
        headBuilder.setTid(tid);
        headBuilder.setUsig(PbDataUtils.string2ByteString(uidSig));
        headBuilder.setSubCmd(1);
        msgBuilder.setAppProtocol(1);
        msgBuilder.setHead(headBuilder);
        return msgBuilder.build();
    }

    //设置检查更新的参数
    public static Nodepp.Msg setCheckUpdateRequestParam(int versionCurrent) {
        long uid = Long.parseLong(Constant.userName);
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setUid(uid);
        headBuilder.setCmd(0x13);
        msgBuilder.setPlatform(0);//0代表android端
        msgBuilder.setHead(headBuilder);
        msgBuilder.setAppProtocol(1);
        msgBuilder.setVerCur(versionCurrent);
        return msgBuilder.build();
    }
    //设置检查固件的参数
    public static Nodepp.Msg setCheckFirmwareVersion(long uid, long did,String uidSig,int deviceType) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setCmd(0x20);
        headBuilder.setUid(uid);
        headBuilder.setDid(did);
        msgBuilder.setDeviceType(deviceType);
//        msgBuilder.setAppProtocol(1);
        headBuilder.setUsig(PbDataUtils.string2ByteString(uidSig));
        msgBuilder.setHead(headBuilder);
        return msgBuilder.build();
    }
    //设置请求更新固件的参数
    public static Nodepp.Msg setUpdateFirmwareParm(long uid,long tid , long did,String uidSig) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setCmd(0x10);
        headBuilder.setSubCmd(2);
        headBuilder.setUid(uid);
        headBuilder.setDid(did);
        headBuilder.setTid(tid);
        msgBuilder.setAppProtocol(1);
        headBuilder.setUsig(PbDataUtils.string2ByteString(uidSig));
        msgBuilder.setHead(headBuilder);
        return msgBuilder.build();
    }

    //查询当前设备的电平
    public static Nodepp.Msg queryFirmwareLevel(long uid, long did,String uidSig) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setCmd(0x22);
        headBuilder.setSubCmd(1);
        headBuilder.setUid(uid);
        headBuilder.setDid(did);
        headBuilder.setUsig(PbDataUtils.string2ByteString(uidSig));
        msgBuilder.setHead(headBuilder);
        msgBuilder.setAppProtocol(1);
        return msgBuilder.build();
    }
    //修改当前设备的电平，默认0 ，1进行反转
    public static Nodepp.Msg changeFirmwareLevel(long uid, long did,String uidSig,int level) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setCmd(0x22);
        headBuilder.setSubCmd(0);
        headBuilder.setUid(uid);
        headBuilder.setDid(did);
        headBuilder.setUsig(PbDataUtils.string2ByteString(uidSig));
        msgBuilder.setHead(headBuilder);
        msgBuilder.setAppProtocol(1);
        msgBuilder.setState(level);
        return msgBuilder.build();
    }
    //改变白灯的亮暗
    public static Nodepp.Msg changeWhiteLightBrightDark(long uid, long did,String uidSig,int brightDark,int operate) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setCmd(0x23);
        headBuilder.setSubCmd(0);
        headBuilder.setUid(uid);
        headBuilder.setDid(did);
        headBuilder.setUsig(PbDataUtils.string2ByteString(uidSig));
        msgBuilder.setHead(headBuilder);
        msgBuilder.setOperate(operate);
        msgBuilder.setAppProtocol(1);
        msgBuilder.setBrightDark(brightDark);
        return msgBuilder.build();
    }
    //查询白灯的状态
    public static Nodepp.Msg queryWhiteLightState(long uid, long did,String uidSig) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setCmd(0x23);
        headBuilder.setSubCmd(1);
        headBuilder.setUid(uid);
        headBuilder.setDid(did);
        headBuilder.setUsig(PbDataUtils.string2ByteString(uidSig));
        msgBuilder.setHead(headBuilder);
        msgBuilder.setAppProtocol(1);
        return msgBuilder.build();
    }
    //修改设备工作模式
    public static Nodepp.Msg changeDeviceMode(long uid, long did,String uidSig,int mode) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setCmd(0x24);
        headBuilder.setSubCmd(0);
        headBuilder.setUid(uid);
        headBuilder.setDid(did);
        headBuilder.setUsig(PbDataUtils.string2ByteString(uidSig));
        msgBuilder.setHead(headBuilder);
        msgBuilder.setDeviceMode(mode);
        msgBuilder.setAppProtocol(1);
        return msgBuilder.build();
    }
    //查询设备工作模式
    public static Nodepp.Msg queryDeviceMode(long uid, long did,String uidSig) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setCmd(0x24);
        headBuilder.setSubCmd(1);
        headBuilder.setUid(uid);
        headBuilder.setDid(did);
        headBuilder.setUsig(PbDataUtils.string2ByteString(uidSig));
        msgBuilder.setHead(headBuilder);
        msgBuilder.setAppProtocol(1);
        return msgBuilder.build();
    }
    //请求分享的sig
    public static Nodepp.Msg requestShareSig(long uid, long did,String uidSig) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setCmd(0x1b);
        headBuilder.setSubCmd(2);
        headBuilder.setUid(uid);
        headBuilder.setDid(did);
        headBuilder.setUsig(PbDataUtils.string2ByteString(uidSig));
        msgBuilder.setHead(headBuilder);
        msgBuilder.setAppProtocol(1);
        return msgBuilder.build();
    }
    //设置检查分享的sig
    public static Nodepp.Msg checkShareSig(long uid, long did,String uidSig,ByteString shareSig) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setCmd(0x1b);
        headBuilder.setSubCmd(3);
        headBuilder.setUid(uid);
        headBuilder.setDid(did);
        headBuilder.setUsig(PbDataUtils.string2ByteString(uidSig));
        msgBuilder.setHead(headBuilder);
        msgBuilder.setAppProtocol(1);
        msgBuilder.setShareVerification(shareSig);
        return msgBuilder.build();
    }

    //设置请求参数
    public static Nodepp.Msg setRequestParam(int cmd, int version, long uid, long did,long tid, int Operate, String uidSig) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setCmd(cmd);
        headBuilder.setUid(uid);
        headBuilder.setVersion(version);
        headBuilder.setSubCmd(0);
        headBuilder.setDid(did);
        headBuilder.setTid(tid);
        headBuilder.setUsig(PbDataUtils.string2ByteString(uidSig));
        msgBuilder.setHead(headBuilder);
        msgBuilder.setOperate(Operate);
        msgBuilder.setAppProtocol(1);
        return msgBuilder.build();
    }

    /**
     * 设置查询彩灯状态参数
     *
     * @param uid
     * @param did
     * @param uidSig
     * @return
     */
    public static Nodepp.Msg setQueryCorlorLightStateParam(long uid, long did, long tid, String uidSig) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setCmd(0x17);
        headBuilder.setUid(uid);
        headBuilder.setDid(did);
        headBuilder.setTid(tid);
        headBuilder.setUsig(PbDataUtils.string2ByteString(uidSig));
        headBuilder.setSubCmd(1);
        msgBuilder.setHead(headBuilder);
        msgBuilder.setAppProtocol(1);
        return msgBuilder.build();
    }

    //设置控制彩灯的请求参数
    public static Nodepp.Msg setControlColorLightParam(long uid, long did, long tid, String uidSig, Nodepp.Rgb.Builder color,int operate,int sence,int BrightDark,int suYan) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setCmd(0x17);
        headBuilder.setSubCmd(0);
        headBuilder.setUid(uid);
        headBuilder.setDid(did);
        headBuilder.setTid(tid);
        headBuilder.setUsig(PbDataUtils.string2ByteString(uidSig));
        msgBuilder.setHead(headBuilder);
        Nodepp.Rgb.Builder colors = Nodepp.Rgb.newBuilder();
        colors.setW(color.getW());
        colors.setR(color.getR());
        colors.setG(color.getG());
        colors.setB(color.getB());
        msgBuilder.setOperate(operate);
        msgBuilder.addColors(colors);
        msgBuilder.setAppProtocol(1);
        //开灯时候才设置
        if (operate == 1){
            msgBuilder.setPlatform(sence);
            msgBuilder.setBrightDark(BrightDark);
            msgBuilder.setSuYan(suYan);
        }
        return msgBuilder.build();
    }

    //设置彩灯场景模式切换的参数
    public static Nodepp.Msg setControlColorLightParam(long uid, long did, long tid, String uidSig, ColorsSelect colorsSelect) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        int sence = colorsSelect.getScene();
        headBuilder.setSeq(getCurrentSeq());//head设置数据包的序列号seq
        headBuilder.setCmd(0x19);//head设置发送的命令
        headBuilder.setSubCmd(sence - 2);//发送的子命令
        headBuilder.setUid(uid);//head设置用户id
        headBuilder.setDid(did);//head设置设备id
        headBuilder.setTid(tid);//head设置设备的临时id
        msgBuilder.setOperate(1);//msg设置控制设备的开关，0表示关闭，1表示开启
        msgBuilder.setAppProtocol(1);//msg设置udp加密传输方式需要加上，旧的输DTLS传输，DTLS进行传输旧不用加，目前就登陆和验证登陆以及查询固件版本使用DTLS（使用DTLS的时候不需要添加）
        headBuilder.setUsig(PbDataUtils.string2ByteString(uidSig));//用户票据信息，相当于token，java中字符串和byteString（源文件定义数据类型byte的时候，编译的时候就会变成byteString类型）需要进行转换
        msgBuilder.setHead(headBuilder);//把head设置到msg里面
        int speed = colorsSelect.getSwitchSpeed();
        int switchSpeed = 500 - (int) Math.ceil(speed / 255.0 * 450);
        msgBuilder.setColorSwitchTime(switchSpeed);//msg设置场景变化速度
        msgBuilder.setPlatform(sence);//msg设置场景0-7为场景中的8个场景,99为白光或者彩光
        msgBuilder.setBrightDark(colorsSelect.getLightDark());//msg设置灯的亮暗值
        msgBuilder.setSuYan(colorsSelect.getSuYan());//msg设置素艳值
        ArrayList<Nodepp.Rgb.Builder> list = new ArrayList<>();//RGB整体类，级别和Head一样，直接给RGB设置完值后，直接把RGB整体设置到Msg中
        Nodepp.Rgb.Builder color1 = Nodepp.Rgb.newBuilder();
        Nodepp.Rgb.Builder color2 = Nodepp.Rgb.newBuilder();
        Nodepp.Rgb.Builder color3 = Nodepp.Rgb.newBuilder();
        Nodepp.Rgb.Builder color4 = Nodepp.Rgb.newBuilder();
        Nodepp.Rgb.Builder color5 = Nodepp.Rgb.newBuilder();
        Nodepp.Rgb.Builder color6 = Nodepp.Rgb.newBuilder();
        color1.setR(colorsSelect.getColorOneR());//给第一个RGB对象赋值
        color1.setG(colorsSelect.getColorOneG());
        color1.setB(colorsSelect.getColorOneB());
        list.add(color1);
        color2.setR(colorsSelect.getColorTwoR());//给第二个RGB对象赋值
        color2.setG(colorsSelect.getColorTwoG());
        color2.setB(colorsSelect.getColorTwoB());
        list.add(color2);
        color3.setR(colorsSelect.getColorThreeR());//给第三个RGB对象赋值
        color3.setG(colorsSelect.getColorThreeG());
        color3.setB(colorsSelect.getColorThreeB());
        list.add(color3);
        color4.setR(colorsSelect.getColorFourR());//给第四个RGB对象赋值
        color4.setG(colorsSelect.getColorFourG());
        color4.setB(colorsSelect.getColorFourB());
        list.add(color4);
        color5.setR(colorsSelect.getColorFiveR());//给第五个RGB对象赋值
        color5.setG(colorsSelect.getColorFiveG());
        color5.setB(colorsSelect.getColorFiveB());
        list.add(color5);
        color6.setR(colorsSelect.getColorSixR());//给第六个RGB对象赋值
        color6.setG(colorsSelect.getColorSixG());
        color6.setB(colorsSelect.getColorSixB());
        list.add(color6);
        for (int i = 0; i < colorsSelect.getColorSize(); i++) {
            msgBuilder.addColors(list.get(i));//把6个RGB对象添加到msg中
        }
        return msgBuilder.build();//生成msg对象
    }

    public static Nodepp.Msg setTimeTaskRequestParam(long uid, long did, long tid, String uidSig,List<TimeTask> lists) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setCmd(0x14);
        headBuilder.setUid(uid);
        headBuilder.setDid(did);
        headBuilder.setTid(tid);
        headBuilder.setUsig(PbDataUtils.string2ByteString(uidSig));
        msgBuilder.setHead(headBuilder);
        msgBuilder.setAppProtocol(1);
        for (TimeTask task : lists) {
            Nodepp.Timer.Builder timerBuilder = Nodepp.Timer.newBuilder();
            timerBuilder.setTimeSet(task.getTimeSet());
            timerBuilder.setTimeRepeat(task.getTimeRepeaat());
            timerBuilder.setTimeOperate(task.getTimeOperate());
            timerBuilder.setTimeIsopen(task.isOpen()?1:0);
            timerBuilder.setOperateIndex(task.getOperateIndex());
            timerBuilder.addTimeStamp(task.getTimeStamp0());
            timerBuilder.addTimeStamp(task.getTimeStamp1());
            timerBuilder.addTimeStamp(task.getTimeStamp2());
            timerBuilder.addTimeStamp(task.getTimeStamp3());
            timerBuilder.addTimeStamp(task.getTimeStamp4());
            timerBuilder.addTimeStamp(task.getTimeStamp5());
            timerBuilder.addTimeStamp(task.getTimeStamp6());
            msgBuilder.addTimers(timerBuilder);
        }
        int count = lists.size();
        for (int i = count ; i < 10 ; i++){
            Nodepp.Timer.Builder timerBuilder = Nodepp.Timer.newBuilder();
            timerBuilder.setTimeSet(0);
            timerBuilder.setTimeRepeat(0);
            timerBuilder.setTimeOperate(0);
            timerBuilder.addTimeStamp(0);
            timerBuilder.addTimeStamp(0);
            timerBuilder.addTimeStamp(0);
            timerBuilder.addTimeStamp(0);
            timerBuilder.addTimeStamp(0);
            timerBuilder.addTimeStamp(0);
            timerBuilder.addTimeStamp(0);
            msgBuilder.addTimers(timerBuilder);
        }
        return msgBuilder.build();
    }

    public static Nodepp.Msg setQueryTimeTaskRequestParam(long uid, long did, long tid, String uidSig) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setCmd(0x14);
        headBuilder.setSubCmd(1);
        headBuilder.setUid(uid);
        headBuilder.setDid(did);
        headBuilder.setTid(tid);
        headBuilder.setUsig(PbDataUtils.string2ByteString(uidSig));
        msgBuilder.setAppProtocol(1);
        msgBuilder.setHead(headBuilder);
        return msgBuilder.build();
    }

    public static Nodepp.Msg getDidFromServer(long uid, String uidSig) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setCmd(0x16);
        headBuilder.setUid(uid);
        headBuilder.setUsig(PbDataUtils.string2ByteString(uidSig));
        msgBuilder.setHead(headBuilder);
        msgBuilder.setAppProtocol(1);
        return msgBuilder.build();
    }

    public static Nodepp.Msg setCheckUserIdParam(long uid, String uidSig) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setCmd(0x1a);
        headBuilder.setUid(uid);
        headBuilder.setUsig(PbDataUtils.string2ByteString(uidSig));
        msgBuilder.setHead(headBuilder);
        return msgBuilder.build();
    }
    public static Nodepp.Msg getErrorMsg(int seq) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setResult(-1);
        headBuilder.setSeq(seq);
        msgBuilder.setHead(headBuilder);
        return msgBuilder.build();
    }
    public static String byteString2String(com.google.protobuf.ByteString bs){
        return bs.toStringUtf8();
    }

    public static ByteString string2ByteString(String s){
        return ByteString.copyFromUtf8(s);
    }
    public static byte[] encryptDataToServer(Nodepp.Msg msg) {
        byte[] pbData = msg.toByteArray();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.writeByte(0xaf);    // app->server 请求包帧头
            dos.writeByte(0x0a);
            dos.writeByte(0xf5);
            dos.writeByte(0xfc);
            dos.writeInt(pbData.length);
            dos.write(pbData);
            dos.writeByte(0x3b);    // app->server 请求包帧尾 ; ascii 为 0x3b
            return bos.toByteArray();
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
            return null;
        }finally {
            if (null != bos){
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != dos){
                try {
                    dos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static byte[] encryptDataToClient(Nodepp.Msg msg,byte[] key) {
        Log.i("key","key=encryp=="+ Utils.bytesToHexString(key));
        byte[] pbData = msg.toByteArray();
        byte[] result = null;
            byte[] encryptData = Utils.encrypt(pbData, key);
            Log.i("qqq","encryp==="+ Utils.bytesToHexString(encryptData));
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(bos);
            try {
                dos.writeByte(0xaf);    // app->client 请求包帧头
                dos.writeByte(0x0a);
                dos.writeByte(0xf5);
                dos.writeByte(0xee);
                dos.writeInt(encryptData.length);
                dos.write(encryptData);
                dos.writeByte(0x3b);    // app->client 请求包帧尾 ; ascii 为 0x3b
                result = bos.toByteArray();
            } catch (Exception e) {
                Log.i(TAG, e.getMessage());
            }finally {
                if (null != bos){
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (null != dos){
                    try {
                        dos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        return result;
    }

    public static byte[] encryptDataToServer(long uid,Nodepp.Msg msg,byte[] key) {
        byte[] pbData = msg.toByteArray();
        byte[] encryptData = Utils.encrypt(pbData, key);
        Outnodepp.Data.Builder builder = Outnodepp.Data.newBuilder();
        builder.setUid(uid);
        builder.setInterdata(ByteString.copyFrom(encryptData));
        return builder.build().toByteArray();
    }
    public static Nodepp.Msg parserPB(byte[] data,int len) {
        nodepp.Nodepp.Msg msg = null;
        try {
            msg = Nodepp.Msg.PARSER.parseFrom(data,0,len);
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }
        return msg;
    }

    public static Nodepp.Msg decryptAndParserResponse(byte[] data, int dataLen,byte[] key) {
        Nodepp.Msg msg = null;
        if (key != null){
            ByteArrayInputStream bis = new ByteArrayInputStream(data, 0, dataLen);
            DataInputStream dis = new DataInputStream(bis);
            try {
                int frameStart = dis.readInt();
                Log.i(TAG,frameStart + Integer.toHexString(frameStart));
                int pbDataLen = dis.readInt();
                Log.i(TAG, "pbDataLen " + pbDataLen);
                byte[] pbData = new byte[pbDataLen];
                dis.read(pbData, 0, pbDataLen);
                Log.i("key","key=decrypt=="+ Utils.bytesToHexString(key));
                byte[] decrypt = Utils.decrypt(pbData, key);
                msg = parserPB(decrypt,decrypt.length);
            } catch (Exception e) {
                msg = null;
            }finally {
                if (null != bis){
                    try {
                        bis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (null != dis){
                    try {
                        dis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return msg;
    }
    public static byte[] packMessage(Nodepp.Msg msg){
        byte[] data = null;
        byte[] pbData = msg.toByteArray();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(bos);
        try {
            dos.writeByte(0xaf);    // app->server 请求包帧头
            dos.writeByte(0x0a);
            dos.writeByte(0xf5);
            dos.writeByte(0xfc);
            dos.writeInt(pbData.length);
            dos.write(pbData);
            dos.writeByte(0x3b);    // 请求包帧尾 ; ascii 为 0x3b
            data = bos.toByteArray();
            bos.close();
            dos.close();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Exception==" + e.toString());
            data = null;
        }
        return data;
    }
    public static Nodepp.Msg parserResponse(byte[] data, int dataLen) {
        ByteArrayInputStream bis = new ByteArrayInputStream(data, 0, dataLen);
        DataInputStream dis = new DataInputStream(bis);
        Nodepp.Msg msg = null;
        try {
            int frameStart = dis.readInt();
            Log.i(TAG,frameStart + Integer.toHexString(frameStart));
            int pbDataLen = dis.readInt();
            Log.i(TAG,"pbDataLen " + pbDataLen);
            dis.read(buff, 0, pbDataLen);
            msg = parserPB(buff,pbDataLen);
            int frameEnd = dis.readByte();
            Log.i(TAG, "frameEnd " + Integer.toHexString(frameEnd));
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }finally {
            if (null != bis){
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != dis){
                try {
                    dis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return msg;
    }

    //设置串口数据传输请求参数
    public static Nodepp.Msg sendUserDataRequestParam(long uid, long did,long tid, String uidSig,String userData) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setCmd(0x1f);
        headBuilder.setUid(uid);
        headBuilder.setSubCmd(0);
        headBuilder.setDid(did);
        headBuilder.setTid(tid);
        headBuilder.setUsig(PbDataUtils.string2ByteString(uidSig));
        msgBuilder.setHead(headBuilder);
        msgBuilder.setAppProtocol(1);
        msgBuilder.setUserData(PbDataUtils.string2ByteString(userData));
        return msgBuilder.build();
    }
    //查询串口数据传输请求参数
    public static Nodepp.Msg queryUserDataRequestParam(long uid, long did,long tid, String uidSig) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setCmd(0x1f);
        headBuilder.setUid(uid);
        headBuilder.setSubCmd(1);
        headBuilder.setDid(did);
        headBuilder.setTid(tid);
        headBuilder.setUsig(PbDataUtils.string2ByteString(uidSig));
        msgBuilder.setHead(headBuilder);
        msgBuilder.setAppProtocol(1);
        return msgBuilder.build();
    }


    //设置浴霸数据传输请求参数
    public static Nodepp.Msg querybathroom(long uid, long did, long tid, String uidSig, byte[] userData) {
        nodepp.Nodepp.Msg.Builder msgBuilder = nodepp.Nodepp.Msg.newBuilder();
        nodepp.Nodepp.Head.Builder headBuilder = nodepp.Nodepp.Head.newBuilder();
        headBuilder.setSeq(getCurrentSeq());
        headBuilder.setCmd(0x1f);
        headBuilder.setUid(uid);
        headBuilder.setSubCmd(0);
        headBuilder.setDid(did);
        headBuilder.setTid(tid);
        headBuilder.setUsig(PbDataUtils.string2ByteString(uidSig));
        msgBuilder.setHead(headBuilder);
        msgBuilder.setAppProtocol(1);
        msgBuilder.setUserData(ByteString.copyFrom((userData)));
        return msgBuilder.build();
    }


}
