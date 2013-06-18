package com.shntec.saf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * 更新管理器
 * @author panshihao
 *
 */
public class SAFUpdateManager {

	private static SAFUpdateManager safUpdateManager = new SAFUpdateManager();
	private SAFUpdateManager(){}
	private Context context;
	
	// 更新专用目录
	private File updateDir;
	// apk文件名称，每次都覆盖
	private String apk_name = null;
	
	private String checkUrl = null;
	
	private JSONObject response = null;
	
	/**
	 * 获取一个safupdatemanager的实例
	 * @param context
	 * @return
	 * @throws SAFException 
	 */
	public static SAFUpdateManager getInstance(Context context) throws SAFException{
		safUpdateManager.context = context;
		safUpdateManager.initUpdate();
		return safUpdateManager;
	}
	/**
	 * 初始化更新管理器
	 * @throws SAFException 
	 */
	private void initUpdate() throws SAFException{
		// 获取更新专用目录
		updateDir = new File(context.getFilesDir(),"updateDir");
		if(!updateDir.exists()){
			updateDir.mkdirs();
		}
		// 获取apk名称
		apk_name = SAFConfig.getInstance().getString("ApkName", "ff.apk");
		
		checkUrl = SAFConfig.getInstance().getString("CheckVersionServlet", null);
		
	}
	/**
	 * 检查是否存在新版本
	 * @return
	 * @throws SAFException 
	 */
	public boolean checkNewVersion() throws SAFException{
		SAFHTTP http = new SAFHTTP();
		String response = http.GETtoString(checkUrl);
		
		try {
			this.response = new JSONObject(response);
		} catch (JSONException e) {
			throw new SAFException(0, e.getMessage(), e);
		}
		
		// 如果返回值为空，则代表没有新版本
		if(this.response == null){
			return false;
		}
		
		// 如果获取到的版本号大于本地的版本号，则代表有新版本
		try {
			if(this.response.getInt("VersionCode") > SAFUtils.getLocalVersionCode(context)){
				return true;
			}
		} catch (JSONException e) {
			throw new SAFException(0, e.getMessage(), e);
		}
		
		
		return false;
	}
	/**
	 * 检查本地apk信息，比较远程传入的md5和本地的apk的md5是否相同，如果相同则返回true,否则返回false
	 * @return
	 * @throws SAFException 
	 */
	public boolean checkLocalAPK(String md5) throws SAFException{
		File apkFile = new File(updateDir,apk_name);
		if(!apkFile.exists()){
			return false;
		}
		
		FileInputStream input = null;
		byte[] bytes = null;
		try {
			input = new FileInputStream(apkFile);
			bytes = SAFUtils.readInputStream(input);
			input.close();
		} catch (FileNotFoundException e) {
			throw new SAFException(0, e.getMessage(), e);
		} catch (IOException e) {
			throw new SAFException(0, e.getMessage(), e);
		}
		
		String localMd5 = SAFUtils.getMD5ByteArray(bytes);
		// 比较两个md5，如果相同则返回true
		if(md5.equals(localMd5)){
			return true;
		}
		
		return false;
	}
	/**
	 * 从input中读入数据，并写入指定目录
	 * @param input
	 * @return
	 * @throws SAFException 
	 */
	public boolean writeLocal(InputStream input) throws SAFException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		File apkFile = new File(updateDir,apk_name);
		FileOutputStream output = null;
		byte[] readByte = new byte[1024];
		int readCount = -1;
		
		try {
			while((readCount = input.read(readByte, 0, 1024)) != -1){
				baos.write(readByte, 0, readCount);
			}
			input.close();
			output = new FileOutputStream(apkFile);
			baos.writeTo(output);
			output.flush();
			output.close();
			baos.close();
			
		} catch (IOException e) {
			throw new SAFException(0, e.getMessage(), e);
		}
		
		return true;
	}
	/**
	 * 直接开始安装apk
	 * @return
	 */
	public boolean install(){
		File apkFile = new File(updateDir,apk_name);
		if(!apkFile.exists()){
			return false;
		}
		 // [文件夹705:drwx---r-x]
        String[] args1 = { "chmod", "705", updateDir.getPath() };
        SAFUtils.exec(args1);
        // [文件604:-rw----r--]
        String[] args2 = { "chmod", "604", apkFile.getPath()};
        SAFUtils.exec(args2);

		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(apkFile),"application/vnd.android.package-archive");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		context.startActivity(intent);
		return true;
	}
	public JSONObject getResponse() {
		return response;
	}
	public void setResponse(JSONObject response) {
		this.response = response;
	}
	
	
	
}
