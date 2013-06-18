package com.shntec.saf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.digest.DigestUtils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;

public class SAFUtils {

	/**
	 * 获取CPU核心数
	 * @return
	 */
	public static int getCPUcores() {
	    //Private Class to display only CPU devices in the directory listing
	    class CpuFilter implements FileFilter {
	        @Override
	        public boolean accept(File pathname) {
	            //Check if filename is "cpu", followed by a single digit number
	            if(Pattern.matches("cpu[0-9]", pathname.getName())) {
	                return true;
	            }
	            return false;
	        }      
	    }

	    try {
	        //Get directory containing CPU info
	        File dir = new File("/sys/devices/system/cpu/");
	        //Filter to only list the devices we care about
	        File[] files = dir.listFiles(new CpuFilter());
	        //Return the number of cores (virtual CPU devices)
	        return files.length;
	    } catch(Exception e) {
	        //Default to return 1 core
	        return 1;
	    }
	}
	
	/**
	 * 将一个inputstream读入，以Byte数组的形式返回
	 * @param in
	 * @return
	 */
	public static byte[] readInputStream(InputStream in){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		byte[] readByte = new byte[1024];
		int readCount = -1;
		
		try {
			while((readCount = in.read(readByte, 0, 1024)) != -1){
				baos.write(readByte, 0, readCount);
			}
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return baos.toByteArray();
	}
	/**
	 * 将一个inputstream，以字符串的形式返回
	 * @param in
	 * @return
	 */
	public static String readInputStreamToString(InputStream in){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		byte[] readByte = new byte[1024];
		int readCount = -1;
		
		try {
			while((readCount = in.read(readByte, 0, 1024)) != -1){
				baos.write(readByte, 0, readCount);
			}
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return new String(baos.toByteArray());
	}
	
	/**
	 * 将输入的dp转换为对应的px值，并返回px值
	 * @param dp
	 * @param c
	 * @return
	 */
	public static int dp2px(int dp, Context c) {

		return (int) (dp * c.getResources().getDisplayMetrics().density + 0.5f);
	}
	/**
	 * 获取DP数量
	 * @param c
	 * @return
	 */
	public static int getDensity(Context c){
		return (int) c.getResources().getDisplayMetrics().density;
	}
	/**
	 * 半角转换为全角
	 * 
	 * @param input
	 * @return
	 */
	public static String ToDBC(String input) {
		char[] c = input.toCharArray();
		for (int i = 0; i < c.length; i++) {
			if (c[i] == 12288) {
				c[i] = (char) 32;
				continue;
			}
			if (c[i] > 65280 && c[i] < 65375)
				c[i] = (char) (c[i] - 65248);
		}
		return new String(c);
	}
	/**
	 * 去除特殊字符或将所有中文标号替换为英文标号
	 * 
	 * @param str
	 * @return
	 */
	public static String stringFilter(String str) {
		str = str.replaceAll("【", "[").replaceAll("】", "]")
				.replaceAll("！", "!").replaceAll("：", ":");// 替换中文标号
		String regEx = "[『』]"; // 清除掉特殊字符
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(str);
		return m.replaceAll("").trim();
	}
	/**
	 * 将一个时间戳转换成提示性时间字符串，如刚刚，1秒前
	 * @param tms
	 * @return
	 */
	public static String convertTimeToFormat(long tms){
		
		
		long curTime = System.currentTimeMillis();
		
		long time = (curTime - tms) / (long)1000;
		
		
		if(time < 60 && time >= 0){
			return "刚刚";
		}else if(time >= 60 && time < 3600){
			return time / 60 +"分钟前";
		}else if(time >= 3600 && time < 3600 * 24){
			return time / 3600 + "小时前";
		}else if(time >= 3600 * 24 && time < 3600 * 24 * 30 ){
			return time / 3600 / 24 + "天前";
		}else if(time >= 3600 * 24 * 30 && time < 3600 * 24 * 30 * 12){
			return time / 3600 / 24 / 30 + "个月前";
		}else if(time >= 3600 * 24 * 30 * 12){
			return time / 3600 / 24 / 30 / 12 + "年前";
		}else{
			return "刚刚";
		}
		
	}
	
	/**
	 * MD5加密
	 * @param str
	 */
	public static String getMD5Str(String str) {
		
		String md5 = DigestUtils.md5Hex(str);
		
		return md5;
	}
	/**
	 * 根据传入的byte数组生成MD5
	 * @param bytes
	 * @return
	 */
	public static String getMD5ByteArray(byte[] bytes){
		String md5 = DigestUtils.md5Hex(bytes);
		
		return md5;
	}
	/**
	 * 判断传入的字符串是否是一个邮箱地址
	 * @param strEmail
	 * @return
	 */
	public static boolean isEmail(String strEmail){
		Pattern p = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
        Matcher m = p.matcher(strEmail);
        return m.find();
	}
	/**
	 * 生成一个随机的字符串
	 * @param count
	 * @return
	 */
	public static String generate(int count){
		String str = getMD5Str(""+System.currentTimeMillis() * new Random().nextDouble());
		return str.substring(0, count);
	}
	/**
	 * 判断传入的字符串是否是一个手机号码
	 * @param strPhone
	 * @return
	 */
	public static boolean isPhoneNumber(String strPhone){
		Pattern p = Pattern.compile("^(13[0-9]|15[0-9]|18[0-9])\\d{8}$");
        Matcher m = p.matcher(strPhone);
        return m.find();
	}
	/**
	 * 将一个时间戳转化成时间字符串，如2010-12-12 23:24:33
	 * @param time
	 * @return
	 */
	public static String convertTime(long time){
		if(time == 0){
			return "";
		}
		
		Date date = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
		
	}
	/**
	 * 将一个时间戳转化成时间字符串，自定义格式 
	 * @param time
	 * @param format  如 yyyy-MM-dd HH:mm:ss
	 * @return
	 */
	public static String convertTime(long time, String format){
		if(time == 0){
			return "";
		}
		
		Date date = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}
	
	/**
	 * 获取本地版本号 versionCode
	 * @param context
	 * @return
	 */
	public static int getLocalVersionCode(Context context){
		PackageInfo info = null;
		try {
			info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return info.versionCode;
	}
	/**
	 * 获取本地版本号名称 versionName
	 * @param context
	 * @return
	 */
	public static String getLocalVersionName(Context context){
		PackageInfo info = null;
		try {
			info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return info.versionName;
	}
	
	/** 执行Linux命令，并返回执行结果。 */
	public static String exec(String[] args) {
		String result = "";
		ProcessBuilder processBuilder = new ProcessBuilder(args);
		Process process = null;
		InputStream errIs = null;
		InputStream inIs = null;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int read = -1;
			process = processBuilder.start();
			errIs = process.getErrorStream();
			while ((read = errIs.read()) != -1) {
				baos.write(read);
			}
			baos.write('\n');
			inIs = process.getInputStream();
			while ((read = inIs.read()) != -1) {
				baos.write(read);
			}
			byte[] data = baos.toByteArray();
			result = new String(data);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (errIs != null) {
					errIs.close();
				}
				if (inIs != null) {
					inIs.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (process != null) {
				process.destroy();
			}
		}
		return result;
	}
	
	/**
	 * 获取当前的网络状态  -1：没有网络  1：WIFI网络 2：wap网络 3：net网络
	 * @author panshihao
	 * @param context
	 * @return
	 */
	public static int getNetworkState(Context context){
    	int netType = -1; 
    	ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    	
    	if(networkInfo==null){
    		return netType;
   	    }
    	int nType = networkInfo.getType();
    	if(nType==ConnectivityManager.TYPE_MOBILE){
    		Log.e("networkInfo.getExtraInfo()", "networkInfo.getExtraInfo() is "+networkInfo.getExtraInfo());
    		if(networkInfo.getExtraInfo().toLowerCase().equals("cmnet")){
    			netType = 3;
    		}
    		else{
    			netType = 2;
    		}
    	}
    	else if(nType==ConnectivityManager.TYPE_WIFI){
    		netType = 1;
    	}
	    return netType;
    }
	/**
	 * 将View转换为bitmap	
	 * @param view
	 * @return
	 */
	public static Bitmap convertViewToBitmap(View view) {
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache();
		Bitmap bitmap = view.getDrawingCache();

		return bitmap;
	}
	
}
