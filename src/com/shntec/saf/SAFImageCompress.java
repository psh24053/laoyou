package com.shntec.saf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.ThumbnailUtils;
import android.util.Log;

/**
 * 图片下载压缩工具
 * @author shihao
 *
 */
public class SAFImageCompress {

	private onTransportProgressListener listener;
	
	public SAFImageCompress(){}
	public SAFImageCompress(onTransportProgressListener listener){
		this.listener = listener;
		
	}
	
	/**
	 * 从本地根据fid获取一个Bitmap
	 * @param fid
	 * @return
	 * @throws SAFException 
	 */
	public Bitmap LocalAutoCompress(String fid) throws SAFException{
		BitmapFactory.Options options = new Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		SAFCache cache = SAFCache.getInstance();
		
		try {
			return BitmapFactory.decodeStream(cache.readFilesCache(fid), null, options);
		} catch (FileNotFoundException e) {
			throw new SAFException(0, e.getMessage(), e);
		}
		
	}
	
	/**
	 * 根据url获取一个bitmap,适应于全屏模式
	 * @param url
	 * @return
	 * @throws SAFException 
	 */
	public Bitmap HttpFullScreenCompress(String url) throws SAFException{
		BitmapFactory.Options options = new Options();
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		
		SAFHTTPTransport httptransport = new SAFHTTPTransport();
		SAFCache cache = SAFCache.getInstance();
		
		String md5 = SAFUtils.getMD5Str(url);
		
		// 如果缓存中已经存在这张图片，则直接返回
		if(cache.hasFilesCache(md5)){
			// 生成bitmap
			try {
				return BitmapFactory.decodeStream(cache.readFilesCache(md5), null, options);
			} catch (FileNotFoundException e) {
				throw new SAFException(0, e.getMessage(), e);
			}
		}
		
		// 首先将图片存入本地缓存
		try {
			cache.SaveFilesCache(md5, httptransport.download(url, listener));
		} catch (IllegalStateException e) {
			throw new SAFException(0, e.getMessage(), e);
		} catch (IOException e) {
			throw new SAFException(0, e.getMessage(), e);
		}
		// 生成bitmap
		try {
			return BitmapFactory.decodeStream(cache.readFilesCache(md5), null, options);
		} catch (FileNotFoundException e) {
			throw new SAFException(0, e.getMessage(), e);
		}
		
	}
	/**
	 * 根据url获取一个bitmap，根据传入的大小来进行缩放
	 * 这个方法会借助缓存的帮助，首先将图片保存至缓存，然后再进行压缩
	 * @param url
	 * @param width
	 * @param height
	 * @return
	 * @throws SAFException 
	 */
	public Bitmap HttpFixedCompress(String url, int width, int height) throws SAFException{
		BitmapFactory.Options options = new Options();
		// 设置这个Bitmap不获取像素矩阵，只获取大小参数
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		options.inJustDecodeBounds = true;

		// 使用safhttp从网络上拉取图片
		SAFHTTPTransport httptransport = new SAFHTTPTransport();
		SAFCache cache = SAFCache.getInstance();
		
		String md5 = SAFUtils.getMD5Str(url);
		
		// 如果缓存中存在这张图片，则不下载直接进行读取
//		if(!cache.hasFilesCache(md5)){
			// 首先将图片存入本地缓存
			try {
				cache.SaveFilesCache(md5, httptransport.download(url, listener));
			} catch (IllegalStateException e) {
				throw new SAFException(0, e.getMessage(), e);
			} catch (IOException e) {
				throw new SAFException(0, e.getMessage(), e);
			}
//		}
		
		
		
		
		
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeStream(cache.readFilesCache(md5), null, options);
		} catch (IllegalStateException e) {
			throw new SAFException(0, e.getMessage(), e);
		} catch (IOException e) {
			throw new SAFException(0, e.getMessage(), e);
		}
		
		int bwidth = options.outWidth;
		int bheight = options.outHeight;
		
//		Log.i("SAF", "w "+bwidth+" ,h "+bheight);
		
		int be = 1;//be=1表示不缩放  
	    if (bwidth > bheight && bwidth > width) {//如果宽度大的话根据宽度固定大小缩放  
	        be = (int) (bwidth / width);  
	    } else if (bwidth < bheight && bheight > height) {//如果高度高的话根据宽度固定大小缩放  
	        be = (int) (bheight / height);  
	    }
	    // 缩放比例不能小于1
	    if (be <= 0) {
	    	be = 1;  
	    } 
	    // 计算好缩放比例之后重新生成一个bitmap，然后使用thumbnailutils工具生成缩略图，并且释放之前图片所占资源
		options.inSampleSize = be;
		options.inJustDecodeBounds = false;
		try {
			bitmap = BitmapFactory.decodeStream(cache.readFilesCache(md5), null, options);
		} catch (IllegalStateException e) {
			throw new SAFException(0, e.getMessage(), e);
		} catch (IOException e) {
			throw new SAFException(0, e.getMessage(), e);
		}
		Bitmap newbitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height);
		bitmap.recycle();
		
		return newbitmap;
	}
	/**
	 * 根据url获取一个bitmap,自动适应图片压缩，将图片压缩至100k以下
	 * @param url
	 * @return
	 * @throws SAFException
	 */
	public Bitmap HttpAutoCompress(String url) throws SAFException{
		BitmapFactory.Options options = new Options();
		// 设置这个Bitmap不获取像素矩阵，只获取大小参数
		options.inJustDecodeBounds = true;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		
		SAFCache cache = SAFCache.getInstance();
		SAFHTTP http = new SAFHTTP();
		SAFHTTPTransport httptransport = new SAFHTTPTransport();
		
		String md5 = SAFUtils.getMD5Str(url);
		
		// 如果缓存中存在这张图片，则不下载直接进行读取
		if(!cache.hasFilesCache(md5)){
			// 首先将图片存入本地缓存
			try {
				cache.SaveFilesCache(md5, httptransport.download(url, listener));
			} catch (IllegalStateException e) {
				throw new SAFException(0, e.getMessage(), e);
			} catch (IOException e) {
				throw new SAFException(0, e.getMessage(), e);
			}
		}
		
		
		Bitmap bitmap = null;
		long totalSize = 0;
		try {
			totalSize = cache.readFileSize(md5);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		// 将bitmap设置为正常加载模式
		options.inJustDecodeBounds = false;
		// 设置bitmap的色彩模式为RGB565，每个像素占两个字节
		
		// 如果图片的大小大于100k则执行压缩
		if(totalSize / 1024 > 100){
			int m = (int) (totalSize / 1024 / 100);
			if(m > 3){
				m /= 2;
			}
			if(m <= 0){
				m = 1;
			}
			options.inSampleSize = m;
		}
		
		try {
			bitmap = BitmapFactory.decodeStream(cache.readFilesCache(md5), null, options);
		} catch (FileNotFoundException e) {
			throw new SAFException(0, e.getMessage(), e);
		}
		
		return bitmap;
		
	}
	
}
