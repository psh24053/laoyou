package com.shntec.saf;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

/**
 * 多线程管理器
 * @author panshihao
 *
 */
public class SAFRunner {

	private static SAFRunner safRunner = new SAFRunner();
	private static Looper looper;
	private Context context;
	private SAFRunner(){
	}
	
	private ExecutorService executorService;
	
	
	/**
	 * 初始化safRunner
	 */
	public static void init(Context context){
		safRunner.context = context;
		safRunner.looper = context.getMainLooper();
		// 首先获取CPU核心数来计算当前机器的最大线程数
		int threadSize = SAFUtils.getCPUcores() * 2 + 2 + 2;
//		System.out.println("threadSize -> "+threadSize);
		safRunner.executorService = Executors.newFixedThreadPool(threadSize);
		
		
	}
	/**
	 * 在非UI环境下执行
	 * @param runnable
	 */
	public static void execute(Runnable runnable){
		safRunner.executorService.submit(runnable);
	}
	/**
	 * 在UI环境下执行
	 * @param runnable
	 */
	public static void runUI(Runnable runnable){
		new Handler(safRunner.looper).post(runnable);
	}
	/**
	 * 获取线程池对象
	 * @return
	 */
	public static ExecutorService getExecutorService() {
		return safRunner.executorService;
	}
	public static Looper getLooper() {
		return looper;
	}
	public static void setLooper(Looper looper) {
		SAFRunner.looper = looper;
	}
	
	
}
