package com.shntec.saf;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * HTTP协议实现类
 * @author Administrator
 *
 */
public class SAFHTTP {
	
	private HttpClient httpClient = null;
	private long lastTime;
	/**
	 * 初始化httpClient
	 */
	private void initHttpClient(){
	
		httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET,"UTF-8"); //这个是和目标网站的编码有关；
		httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_ELEMENT_CHARSET,"UTF-8"); 
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, new Integer(30000)); 
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,  new Integer(30000) ); 
		
	}
	/**
	 * 以GET的方式请求URL，并返回HttpResponse
	 * @param url
	 * @return
	 * @throws SAFException 
	 */
	public HttpResponse GET(String url) throws SAFException{
		if(httpClient == null){
			initHttpClient();
		}
		// 传入的url不能为null
		if(url == null){
			throw new SAFException(0, "传入的URL不能为null");
		}
		// 声明httpget对象
		HttpGet get = new HttpGet(url);
		HttpResponse response = null;
		System.out.println("GET Request -> "+url);
		// 执行get请求
		try {
			response = httpClient.execute(get);
		} catch (ClientProtocolException e) {
			throw new SAFException(0, "客户端协议错误", e);
		} catch (ConnectTimeoutException e){
			throw new SAFException(0, "连接超时", e);
		} catch (InterruptedIOException e){
			throw new SAFException(0, "IO被中断", e);
		} catch (IOException e) {
			throw new SAFException(0, "IO错误，可能是服务器的问题", e);
		}
		// 判断返回的httpresponse是否为null
		if(response == null){
			throw new SAFException(0, "response 为空");
		}
		return response;
	}
	
	/**
	 * 以GET的方式请求URL，并返回字符串数据
	 * @param url
	 * @return
	 * @throws SAFException 
	 */
	public String GETtoString(String url) throws SAFException{
		
		// 最终返回值
		long startTime = System.currentTimeMillis();
		HttpResponse response = GET(url);
		
		String responseString = null;
		
		switch (response.getStatusLine().getStatusCode()) {
		case 200:
			try {
				responseString = EntityUtils.toString(response.getEntity(), "utf-8");
			} catch (ParseException e) {
				throw new SAFException(0, "解析response Entity 错误", e);
			} catch (IOException e) {
				throw new SAFException(0, "IO错误，可能是服务器的问题", e);
			}
			break;
		case 404:
			throw new SAFException(0, "response 的返回状态为404，找不到指定页面");
		case 500:
			throw new SAFException(0, "response 的返回状态为500，服务器内部错误");
		default:
			break;
		}
		
		lastTime = System.currentTimeMillis() - startTime;
		return responseString;
	}
	
	public HttpResponse POST(String url, HttpEntity entity) throws SAFException{
		if(httpClient == null){
			initHttpClient();
		}
		// 传入的url不能为null
		if(url == null){
			throw new SAFException(0, "传入的URL不能为null");
		}
		// 声明httppost对象
		HttpPost post = new HttpPost(url);
		post.setEntity(entity);
		
		
		HttpResponse response = null;
		// 执行get请求
		try {
			response = httpClient.execute(post);
		} catch (ClientProtocolException e) {
			throw new SAFException(0, "客户端协议错误", e);
		} catch (ConnectTimeoutException e){
			throw new SAFException(0, "连接超时", e);
		} catch (InterruptedIOException e){
			throw new SAFException(0, "IO被中断", e);
		} catch (IOException e) {
			throw new SAFException(0, "IO错误，可能是服务器的问题", e);
		}
		// 判断返回的httpresponse是否为null
		if(response == null){
			throw new SAFException(0, "response 为空");
		}
		
		return response;
	}
	/**
	 * 以POST的方式请求URL，并返回HttpResponse
	 * @param url
	 * @return
	 * @throws SAFException 
	 */
	public HttpResponse POST(String url, JSONObject request) throws SAFException{
		
		if(httpClient == null){
			initHttpClient();
		}
		// 传入的url不能为null
		if(url == null){
			throw new SAFException(0, "传入的URL不能为null");
		}
		// 声明httppost对象
		HttpPost post = new HttpPost(url);
		if(request != null){
			try {
				post.setEntity(new ByteArrayEntity(request.toString().getBytes("utf-8")));
			} catch (UnsupportedEncodingException e1) {
				throw new SAFException(0, e1.getMessage(), e1);
			}
		}
		System.out.println("Request: "+request.toString());
		
		HttpResponse response = null;
		// 执行get请求
		try {
			response = httpClient.execute(post);
		} catch (ClientProtocolException e) {
			throw new SAFException(0, "客户端协议错误", e);
		} catch (ConnectTimeoutException e){
			throw new SAFException(0, "连接超时", e);
		} catch (InterruptedIOException e){
			throw new SAFException(0, "IO被中断", e);
		} catch (IOException e) {
			throw new SAFException(0, "IO错误，可能是服务器的问题", e);
		}
		// 判断返回的httpresponse是否为null
		if(response == null){
			throw new SAFException(0, "response 为空");
		}
		
		return response;
	}
	
	/**
	 * 以POST的方式请求URL，并返回字符串数据
	 * @param url
	 * @param request
	 * @return
	 * @throws SAFException 
	 */
	public String POSTtoString(String url, JSONObject request) throws SAFException{
		
		// 最终返回值
		long startTime = System.currentTimeMillis();
		HttpResponse response = POST(url, request);
		String responseString = null;
		
		switch (response.getStatusLine().getStatusCode()) {
		case 200:
			try {
				responseString = EntityUtils.toString(response.getEntity(), "utf-8");
			} catch (ParseException e) {
				throw new SAFException(0, "解析response Entity 错误", e);
			} catch (IOException e) {
				throw new SAFException(0, "IO错误，可能是服务器的问题", e);
			}
			break;
		case 404:
			throw new SAFException(0, "response 的返回状态为404，找不到指定页面");
		case 500:
			throw new SAFException(0, "response 的返回状态为500，服务器内部错误");
		default:
			throw new SAFException(0, "response 的返回状态为"+response.getStatusLine().getStatusCode()+"，"+response.getStatusLine().getReasonPhrase());
		}
		
		lastTime = System.currentTimeMillis() - startTime;
		System.out.println("Response: "+responseString);
		return responseString;
	}
	/**
	 * 以GET的方式请求URL，并返回JSON对象
	 * @param url
	 * @return
	 * @throws SAFException 
	 */
	public JSONObject GETtoJSON(String url) throws SAFException{
		String res = GETtoString(url);
		// 如果GET的返回值为null则返回空
		if(res == null){
			return null;
		}
		// 返回时构造为json对象
		try {
			return new JSONObject(res);
		} catch (JSONException e) {
			throw new SAFException(0, "构造JSON对象失败", e);
		}
	}
	/**
	 * 以POST的方式请求URL，并返回JSON对象
	 * @param url
	 * @param request
	 * @return
	 * @throws SAFException 
	 */
	public JSONObject POSTtoJSON(String url, JSONObject request) throws SAFException{
		String res = POSTtoString(url, request);
		// 如果POST的返回值为null则返回空
		if(res == null){
			return null;
		}
		// 返回时构造为json对象
		try {
			return new JSONObject(res);
		} catch (JSONException e) {
			throw new SAFException(0, "构造JSON对象失败", e);
		}
	}
	
	public HttpClient getHttpClient() {
		return httpClient;
	}
	public void setHttpClient(HttpClient httpClient) {
		this.httpClient = httpClient;
	}
	public long getLastTime() {
		return lastTime;
	}
	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}
	
	
	
	
	
}
