package com.xbw.mvp.ui;

import android.content.Context;
import android.text.SpannableString;

import com.opendanmaku.DanmakuItem;
import com.opendanmaku.DanmakuView;
import com.opendanmaku.IDanmakuItem;
import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;
import com.xbw.mvp.R;

/**
 * Created by xubowen on 16/7/29.
 */
public class MessageReceiver extends XGPushBaseReceiver {
    private DanmakuView mDanmakuView;
    @Override
    public void onRegisterResult(Context context, int i, XGPushRegisterResult xgPushRegisterResult) {

    }

    @Override
    public void onUnregisterResult(Context context, int i) {

    }

    @Override
    public void onSetTagResult(Context context, int i, String s) {

    }

    @Override
    public void onDeleteTagResult(Context context, int i, String s) {

    }
    @Override
    public void onNotifactionClickedResult(Context context, XGPushClickedResult xgPushClickedResult) {

    }

    @Override
    public void onNotifactionShowedResult(Context context, XGPushShowedResult xgPushShowedResult) {

    }
    // 消息透传
    @Override
    public void onTextMessage(Context context, XGPushTextMessage message) {
        mDanmakuView = MainActivity.mDanmakuView;
        danmushow(message.getTitle().toString()+" : "+message.getContent().toString(),context);
    }
    private void danmushow(String a,Context context){
        //int[] color= {Color.RED,Color.BLACK,Color.BLUE,Color.WHITE,Color.YELLOW,Color.CYAN,Color.DKGRAY,Color.GRAY,Color.GREEN,Color.LTGRAY,Color.MAGENTA};
        //Random random = new Random();
        //int p = random.nextInt(color.length);
        IDanmakuItem item = new DanmakuItem(context, new SpannableString(a), mDanmakuView.getWidth(),0, R.color.white,20,1);//参数 上下文，弹幕内容，位置x，位置y，颜色，字体大小，速度。
        //IDanmakuItem item = new DanmakuItem(context, new SpannableString(a), mDanmakuView.getWidth());
        //item.setTextColor(context.getResources().getColor(Color.parseColor(Colors[p])));
        //item.setTextSize(14);
        //item.setTextColor(getRandomColor());
        mDanmakuView.addItemToHead(item);
    }

}
