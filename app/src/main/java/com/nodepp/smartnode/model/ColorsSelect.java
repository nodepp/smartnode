package com.nodepp.smartnode.model;

import com.lidroid.xutils.db.annotation.Column;
import com.lidroid.xutils.db.annotation.Table;

/**
 * Created by yuyue on 2017/6/2.
 */
@Table(name = "tb_colors_select")  // 建议加上注解， 混淆后表名不受影响
public class ColorsSelect extends EntityBase{
    @Column(column = "did")
    private long did = 0;

    @Column(column = "tid")
    private long tid = 0;

    @Column(column = "scene")
    private int scene;//场景 4代表柔光，5代表缤纷，6代表炫彩，7代表斑斓

    @Column(column = "deviceId")
    private int deviceId;//设备主键id

    @Column(column = "colorOneW")
    private int colorOneW = 0;

    @Column(column = "colorOneR")
    private int colorOneR = 255;

    @Column(column = "colorOneG")
    private int colorOneG = 247;

    @Column(column = "colorOneB")
    private int colorOneB = 0;

    @Column(column = "colorTwoW")
    private int colorTwoW = 0;

    @Column(column = "colorTwoR")
    private int colorTwoR = 0;

    @Column(column = "colorTwoG")
    private int colorTwoG = 255;

    @Column(column = "colorTwoB")
    private int colorTwoB = 196;

    @Column(column = "colorThreeW")
    private int colorThreeW = 0;

    @Column(column = "colorThreeR")
    private int colorThreeR = 255;

    @Column(column = "colorThreeG")
    private int colorThreeG = 119;

    @Column(column = "colorThreeB")
    private int colorThreeB = 0;

    @Column(column = "colorFourW")
    private int colorFourW = 0;

    @Column(column = "colorFourR")
    private int colorFourR = 9;

    @Column(column = "colorFourG")
    private int colorFourG = 0;

    @Column(column = "colorFourB")
    private int colorFourB = 255;

    @Column(column = "colorFiveW")
    private int colorFiveW = 0;

    @Column(column = "colorFiveR")
    private int colorFiveR = 59;

    @Column(column = "colorFiveG")
    private int colorFiveG = 255;

    @Column(column = "colorFiveB")
    private int colorFiveB = 0;

    @Column(column = "colorSixW")
    private int colorSixW = 0;

    @Column(column = "colorSixR")
    private int colorSixR = 116;

    @Column(column = "colorSixG")
    private int colorSixG = 0;

    @Column(column = "colorSixB")
    private int colorSixB = 255;

    @Column(column = "lightDark")
    private int lightDark = 255;//亮暗

    @Column(column = "suYan")
    private int suYan = 0;//素艳

    @Column(column = "switchSpeed")
    private int switchSpeed = 200;

    @Column(column = "colorSize")
    private int colorSize = 6;//多少种颜色

    public int getScene() {
        return scene;
    }

    public void setScene(int scene) {
        this.scene = scene;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public int getColorOneW() {
        return colorOneW;
    }

    public void setColorOneW(int colorOneW) {
        this.colorOneW = colorOneW;
    }

    public int getColorOneR() {
        return colorOneR;
    }

    public void setColorOneR(int colorOneR) {
        this.colorOneR = colorOneR;
    }

    public int getColorOneG() {
        return colorOneG;
    }

    public void setColorOneG(int colorOneG) {
        this.colorOneG = colorOneG;
    }

    public int getColorOneB() {
        return colorOneB;
    }

    public void setColorOneB(int colorOneB) {
        this.colorOneB = colorOneB;
    }

    public int getColorTwoW() {
        return colorTwoW;
    }

    public void setColorTwoW(int colorTwoW) {
        this.colorTwoW = colorTwoW;
    }

    public int getColorTwoR() {
        return colorTwoR;
    }

    public void setColorTwoR(int colorTwoR) {
        this.colorTwoR = colorTwoR;
    }

    public int getColorTwoG() {
        return colorTwoG;
    }

    public void setColorTwoG(int colorTwoG) {
        this.colorTwoG = colorTwoG;
    }

    public int getColorTwoB() {
        return colorTwoB;
    }

    public void setColorTwoB(int colorTwoB) {
        this.colorTwoB = colorTwoB;
    }

    public int getColorThreeW() {
        return colorThreeW;
    }

    public void setColorThreeW(int colorThreeW) {
        this.colorThreeW = colorThreeW;
    }

    public int getColorThreeR() {
        return colorThreeR;
    }

    public void setColorThreeR(int colorThreeR) {
        this.colorThreeR = colorThreeR;
    }

    public int getColorThreeG() {
        return colorThreeG;
    }

    public void setColorThreeG(int colorThreeG) {
        this.colorThreeG = colorThreeG;
    }

    public int getColorThreeB() {
        return colorThreeB;
    }

    public void setColorThreeB(int colorThreeB) {
        this.colorThreeB = colorThreeB;
    }

    public int getColorFourW() {
        return colorFourW;
    }

    public void setColorFourW(int colorFourW) {
        this.colorFourW = colorFourW;
    }

    public int getColorFourR() {
        return colorFourR;
    }

    public void setColorFourR(int colorFourR) {
        this.colorFourR = colorFourR;
    }

    public int getColorFourG() {
        return colorFourG;
    }

    public void setColorFourG(int colorFourG) {
        this.colorFourG = colorFourG;
    }

    public int getColorFourB() {
        return colorFourB;
    }

    public void setColorFourB(int colorFourB) {
        this.colorFourB = colorFourB;
    }

    public int getColorFiveW() {
        return colorFiveW;
    }

    public void setColorFiveW(int colorFiveW) {
        this.colorFiveW = colorFiveW;
    }

    public int getColorFiveR() {
        return colorFiveR;
    }

    public void setColorFiveR(int colorFiveR) {
        this.colorFiveR = colorFiveR;
    }

    public int getColorFiveG() {
        return colorFiveG;
    }

    public void setColorFiveG(int colorFiveG) {
        this.colorFiveG = colorFiveG;
    }

    public int getColorFiveB() {
        return colorFiveB;
    }

    public void setColorFiveB(int colorFiveB) {
        this.colorFiveB = colorFiveB;
    }

    public int getColorSixW() {
        return colorSixW;
    }

    public void setColorSixW(int colorSixW) {
        this.colorSixW = colorSixW;
    }

    public int getColorSixR() {
        return colorSixR;
    }

    public void setColorSixR(int colorSixR) {
        this.colorSixR = colorSixR;
    }

    public int getColorSixG() {
        return colorSixG;
    }

    public void setColorSixG(int colorSixG) {
        this.colorSixG = colorSixG;
    }

    public int getColorSize() {
        return colorSize;
    }

    public void setColorSize(int colorSize) {
        this.colorSize = colorSize;
    }

    public int getColorSixB() {
        return colorSixB;
    }

    public void setColorSixB(int colorSixB) {
        this.colorSixB = colorSixB;
    }

    public int getLightDark() {
        return lightDark;
    }

    public void setLightDark(int lightDark) {
        this.lightDark = lightDark;
    }

    public int getSuYan() {
        return suYan;
    }

    public void setSuYan(int suYan) {
        this.suYan = suYan;
    }

    public int getSwitchSpeed() {
        return switchSpeed;
    }

    public void setSwitchSpeed(int switchSpeed) {
        this.switchSpeed = switchSpeed;
    }

    @Override
    public String toString() {
        return "ColorsSelect{" +
                "id=" + getId() +
                ", did=" + did +
                ", tid=" + tid +
                ", scene=" + scene +
                ", deviceId=" + deviceId +
                ", colorOneW=" + colorOneW +
                ", colorOneR=" + colorOneR +
                ", colorOneG=" + colorOneG +
                ", colorOneB=" + colorOneB +
                ", colorTwoW=" + colorTwoW +
                ", colorTwoR=" + colorTwoR +
                ", colorTwoG=" + colorTwoG +
                ", colorTwoB=" + colorTwoB +
                ", colorThreeW=" + colorThreeW +
                ", colorThreeR=" + colorThreeR +
                ", colorThreeG=" + colorThreeG +
                ", colorThreeB=" + colorThreeB +
                ", colorFourW=" + colorFourW +
                ", colorFourR=" + colorFourR +
                ", colorFourG=" + colorFourG +
                ", colorFourB=" + colorFourB +
                ", colorFiveW=" + colorFiveW +
                ", colorFiveR=" + colorFiveR +
                ", colorFiveG=" + colorFiveG +
                ", colorFiveB=" + colorFiveB +
                ", colorSixW=" + colorSixW +
                ", colorSixR=" + colorSixR +
                ", colorSixG=" + colorSixG +
                ", colorSixB=" + colorSixB +
                ", lightDark=" + lightDark +
                ", suYan=" + suYan +
                ", switchSpeed=" + switchSpeed +
                ", colorSize=" + colorSize +
                '}';
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

}
