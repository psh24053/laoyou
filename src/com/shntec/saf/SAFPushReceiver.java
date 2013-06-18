package com.shntec.saf;

import cn.jpush.android.api.BasicPushNotificationBuilder;
import cn.jpush.android.api.JPushInterface;
import cn.jpush.android.ui.JPushRemoteViews;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * 用于接收来自第三方推送平台的Receiver
 * @author panshihao
 *
 */
public class SAFPushReceiver extends BroadcastReceiver {

	public static SAFPushListener pushListener;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		String action = intent.getAction();
		
		// SDK 向 JPush Server 注册所得到的注册 ID 。
		if(action.equals("cn.jpush.android.intent.REGISTRATION")){
			if(pushListener != null){
				pushListener.onRegisteaction(context, intent);
			}
			
			
		}else if(action.equals("cn.jpush.android.intent.MESSAGE_RECEIVED")){
		// 收到了自定义消息 Push 。
			if(pushListener != null){
				pushListener.onMessageReceived(context, intent);
			}
			
			
		}else if(action.equals("cn.jpush.android.intent.NOTIFICATION_RECEIVED")){
		// 收到了通知 Push。可用于统计。
			if(pushListener != null){
				pushListener.onRegisteaction(context, intent);
			}
			
		}else if(action.equals("cn.jpush.android.intent.NOTIFICATION_OPENED")){
		// 用户点击了通知。
			
			if(pushListener != null){
				pushListener.onRegisteaction(context, intent);
			}
		}
		
		
		
	}

}
