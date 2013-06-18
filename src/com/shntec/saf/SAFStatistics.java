package com.shntec.saf;

import android.content.Context;

/**
 * 统计工具
 * @author Panshihao
 *
 */
public class SAFStatistics {

	private static SAFStatistics safStatistics = new SAFStatistics();
	private SAFStatistics(){}
	
	public static SAFStatistics getInstance(Context context){
		return safStatistics;
	}
	/**
	 * 当前页面的统计入口
	 * @param context
	 */
	public static void onCreate(Context context){
		
	}
	/**
	 * 当前页面的统计出口
	 * @param context
	 */
	public static void onDestroy(Context context){
		
	}
	/**
	 * 当前页面被暂停时
	 * @param context
	 */
	public static void onPause(Context context){
		
	}
	/**
	 * 当前页面继续开始时
	 * @param context
	 */
	public static void onResume(Context context){
		
	}
}
