package com.shntec.saf;

import android.content.Context;
import android.content.Intent;

/**
 * 推送监听器
 * @author panshihao
 *
 */
public interface SAFPushListener {

	/**
	 * SDK 向 JPush Server 注册所得到的注册 ID 。
	 * @param context
	 * @param intent
	 */
	public void onRegisteaction(Context context, Intent intent);
	/**
	 * 收到了自定义消息 Push 。
	 * @param context
	 * @param intent
	 */
	public void onMessageReceived(Context context, Intent intent);
	/**
	 * 收到了通知 Push。可用于统计。
	 * @param context
	 * @param intent
	 */
	public void onNotificationReceived(Context context, Intent intent);
	/**
	 * 用户点击了通知。
	 * @param context
	 * @param intent
	 */
	public void onNotificationOpend(Context context, Intent intent);
	
}
