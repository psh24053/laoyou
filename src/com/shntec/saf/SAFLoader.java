package com.shntec.saf;

import android.content.Context;

/**
 * 加载器，用于加载SAF在运行时必须要加载的各种参数
 * @author Administrator
 *
 */
public class SAFLoader {

	/**
	 * 加载SAF
	 * @param context
	 * @throws SAFException
	 */
	public static void loader(Context context) throws SAFException{
		SAFCache cache = SAFCache.getInstance(context);
		SAFConfig config = SAFConfig.getInstance(context);
		SAFRunner.init(context);
	}
	/**
	 * 设置推送监听器
	 * @param listener
	 */
	public static void setPushListener(SAFPushListener listener){
		SAFPushReceiver.pushListener = listener;
	}
	
}
