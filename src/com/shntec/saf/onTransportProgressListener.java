package com.shntec.saf;

/**
 * 传输进度监听器
 * @author shihao
 *
 */
public interface onTransportProgressListener{
	
	public void onProgress(long readSize, long totalSize);
	public void onComplete();
}