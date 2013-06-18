package com.shntec.saf;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 传输进度专用Inputstream
 * @author shihao
 *
 */
public class SAFTransportProgressInputStream extends FilterInputStream {

	public final static int MINIMUM_PROGRESS_STEP = 1024;
	
	private onTransportProgressListener listener;
	private long totalSize = 0;
	private long readSize = 0;
	private boolean complete = false;
	
	protected SAFTransportProgressInputStream(InputStream in,onTransportProgressListener listener) throws IOException {
		super(in);
		
		this.listener = listener;
		this.totalSize = in.available();
	}
	protected SAFTransportProgressInputStream(InputStream in,onTransportProgressListener listener, long totalSize) throws IOException {
		super(in);
		
		this.listener = listener;
		this.totalSize = totalSize;
	}
	
	@Override
	public int read() throws IOException {
		int ret = 0;
		
		try {
			ret = super.read();
			readSize += 1;
			if (readSize % MINIMUM_PROGRESS_STEP == 0 || readSize >= totalSize) {
				listener.onProgress(readSize, totalSize);
			}
			if(readSize > totalSize && !complete){
				complete = true;
				listener.onProgress(totalSize, totalSize);
				listener.onComplete();
			}
		}
		catch (IOException e){
			throw e;
		}
		
		return ret;
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		int ret = 0;
		
		try {
			ret = super.read(b);
			if(ret == -1){
				complete = true;
				listener.onProgress(totalSize, totalSize);
				listener.onComplete();
			}else{
				listener.onProgress(readSize, totalSize);
				readSize += ret;
			}
		}
		catch (IOException e){
			throw e;
		}
		
		return ret;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int ret = 0;

		try {
			ret = super.read(b, off, len);
			// 如果返回的结果为-1 表示读取结束了
			if(ret == -1){
				complete = true;
				listener.onProgress(totalSize, totalSize);
				listener.onComplete();
			}else{
				listener.onProgress(readSize, totalSize);
				readSize += ret;
			}
		}
		catch (IOException e){
			throw e;
		}
		
		return ret;
	}
	
	
	
}
