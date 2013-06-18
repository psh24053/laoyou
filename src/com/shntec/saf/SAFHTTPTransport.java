package com.shntec.saf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.InputStreamBody;


/**
 * 基于HTTP的上传下载，支持进度
 * @author Administrator
 *
 */
public class SAFHTTPTransport {

	/**
	 * 下载指定URL，返回inputstream
	 * @param url
	 * @return
	 * @throws SAFException 
	 */
	public InputStream download(String url) throws SAFException{
		return download(url, null);
	}
	/**
	 * 下载指定URL，返回Inputstream，接收进度监听器
	 * @param url
	 * @param listener
	 * @return
	 * @throws SAFException 
	 */
	public InputStream download(String url, onTransportProgressListener listener) throws SAFException{
		if(url == null){
			throw new SAFException(0, "url不能为空");
		}
		
		// 调用safhttp来进行网络操作
		SAFHTTP http = new SAFHTTP();
		System.out.println("download -> "+url);
		HttpResponse httpresponse = http.GET(url);
		
		if(httpresponse == null){
			throw new SAFException(0, "请求失败，返回值httpresponse为空");
		}
		
		// 返回inputstream
		try {
			if(listener != null){
				return new SAFTransportProgressInputStream(httpresponse.getEntity().getContent(), listener, httpresponse.getEntity().getContentLength());
			}else{
				return httpresponse.getEntity().getContent();
			}
		} catch (IllegalStateException e) {
			throw new SAFException(0, e.getMessage(), e);
		} catch (IOException e) {
			throw new SAFException(0, e.getMessage(), e);
		}
	}
	
	
	/**
	 * 以HTTP的方式上传文件信息，返回Httpresponse，接收SAFMultipartEntity对象
	 * @param url
	 * @param entity
	 * @return
	 * @throws SAFException 
	 */
	public HttpResponse upload(String url, SAFMultipartEntity entity) throws SAFException{
		SAFHTTP http = new SAFHTTP();
		
		HttpResponse httpresponse = http.POST(url, entity);
		
		if(httpresponse == null){
			throw new SAFException(0, "请求失败，返回值httpresponse为空");
		}
		
		return httpresponse;
	}
	/**
	 * 以HTTP的方式上传文件信息，返回Httpresponse，接收File以及在http表单提交时的字段名，还有传输进度监听器
	 * @param url
	 * @param file
	 * @param fileField
	 * @param listener
	 * @return
	 * @throws SAFException
	 */
	public HttpResponse upload(String url, File file, String fileField, onTransportProgressListener listener) throws SAFException{
		SAFHTTP http = new SAFHTTP();
		SAFMultipartEntity entity = new SAFMultipartEntity(listener);
		entity.addPart(fileField, new FileBody(file));
		HttpResponse httpresponse = http.POST(url, entity);
		
		if(httpresponse == null){
			throw new SAFException(0, "请求失败，返回值httpresponse为空");
		}
		
		return httpresponse;
	}
	/**
	 * 以http的方式上传文件信息，返回httpresponse，接收InputStream以及在http表单提交时的字段名，还有传输进度监听器
	 * @param url
	 * @param in
	 * @param fileField
	 * @param listener
	 * @return
	 * @throws SAFException
	 */
	public HttpResponse upload(String url, InputStream in, String fileField, onTransportProgressListener listener) throws SAFException{
		SAFHTTP http = new SAFHTTP();
		SAFMultipartEntity entity = new SAFMultipartEntity(listener);
		entity.addPart(fileField, new InputStreamBody(in, fileField));
		
		HttpResponse httpresponse = http.POST(url, entity);
		
		if(httpresponse == null){
			throw new SAFException(0, "请求失败，返回值httpresponse为空");
		}
		
		return httpresponse;
	}
	
}
