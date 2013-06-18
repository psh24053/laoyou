package com.shntec.saf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.entity.mime.MultipartEntity;


/**
 * 支持进度的MultipartEntity，HTTP上传专用
 * @author shihao
 *
 */
public class SAFMultipartEntity extends MultipartEntity {
	
	private onTransportProgressListener listener;
	
	public SAFMultipartEntity(onTransportProgressListener listener){
		this.listener = listener;
	}
	
	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		// TODO Auto-generated method stub
		super.writeTo(new SAFTransportProgressOutputStream(outstream, listener, getContentLength()));
	}
	
}
