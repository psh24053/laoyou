package com.shntec.saf;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class SAFTransportProgressOutputStream extends FilterOutputStream {

	private onTransportProgressListener listener;
	private long totalSize = 0;
	private long readSize = 0;
	private boolean complete = false;
	
	public SAFTransportProgressOutputStream(OutputStream out, onTransportProgressListener listener, long totalSize) {
		super(out);
		this.listener = listener;
		this.totalSize = totalSize;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void write(byte[] buffer) throws IOException {
		// TODO Auto-generated method stub
		super.write(buffer);
		readSize += buffer.length;
		if(readSize <= totalSize){
			listener.onProgress(readSize, totalSize);
		}else if(!complete){
			complete = true;
			listener.onComplete();
		}
	}
	@Override
	public void write(byte[] buffer, int offset, int length) throws IOException {
		// TODO Auto-generated method stub
		super.write(buffer, offset, length);
		readSize += buffer.length;
		if(readSize <= totalSize){
			listener.onProgress(readSize, totalSize);
		}else if(!complete){
			complete = true;
			listener.onComplete();
		}
	}
	@Override
	public void write(int oneByte) throws IOException {
		// TODO Auto-generated method stub
		readSize++;
		if(readSize <= totalSize){
			listener.onProgress(readSize, totalSize);
		}else if(!complete){
			complete = true;
			listener.onComplete();
		}
		
		super.write(oneByte);
	}
	
}
