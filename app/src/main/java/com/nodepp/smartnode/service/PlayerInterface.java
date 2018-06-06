package com.nodepp.smartnode.service;

import com.tencent.rtmp.ITXLivePlayListener;
import com.tencent.rtmp.TXLivePlayer;
import com.tencent.rtmp.ui.TXCloudVideoView;

/**
 * Created by yuyue on 2017/2/27.
 */
public interface PlayerInterface {

    VideoService getServiceData();

    TXLivePlayer getLivePlayer();

    void setPlayView(TXCloudVideoView playerView);

    void setRenderRotation(int mCurrentRenderRotation);

    void setRenderMode(int mCurrentRenderMode);

    void setPlayListener(ITXLivePlayListener listener);
}