package com.shntec.saf;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * 图片加载专用的runnerAdapter
 * @author Panshihao
 *
 */
public class SAFImageLoader extends SAFRunnerAdapter<String, Integer, Bitmap> {

	private ImageView imageView;
	private int width;
	private int height;
	private listener listener;
	private Bitmap bitmap;
	
	/**
	 * 构造方法
	 * @param imageView 
	 * @param width
	 * @param height
	 */
	public SAFImageLoader(ImageView imageView, int width, int height){
		this.imageView = imageView;
		this.width = width;
		this.height = height;
		this.listener = new listener();
	}
	
	@Override
	public Bitmap doInBackground(String... params) {
		// TODO Auto-generated method stub
		SAFImageCompress safimagecompress = new SAFImageCompress(listener);
		
		try {
			bitmap = safimagecompress.HttpFixedCompress(params[0], width, height);
		} catch (SAFException e) {
			e.printStackTrace();
		}
		
		return bitmap;
	}
	
	@Override
	public void onPostExecute(Bitmap result) {
		// TODO Auto-generated method stub
		super.onPostExecute(result);
		
		if(result != null){
			imageView.setImageBitmap(result);
		}
	}
	
	/**
	 * 进度监听器
	 * @author Panshihao
	 *
	 */
	private class listener implements onTransportProgressListener{

		@Override
		public void onProgress(long readSize, long totalSize) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onComplete() {
			// TODO Auto-generated method stub
			
		}
		
	}

	public ImageView getImageView() {
		return imageView;
	}

	public void setImageView(ImageView imageView) {
		this.imageView = imageView;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public listener getListener() {
		return listener;
	}

	public void setListener(listener listener) {
		this.listener = listener;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	
	
}
