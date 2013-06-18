package com.shntec.saf;

import java.lang.ref.SoftReference;

import cn.panshihao.laoyou.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

/**
 * 大图片浏览器
 * @author shihao
 *
 */
public class SAFImageViewActivity extends SAFBaseActivity implements OnTouchListener,OnClickListener,OnGestureListener,OnDoubleTapListener {
	
	private int imageWidth;
	private int imageHeight;
	
	private int displayWidth;
	private int displayHeight;
	
	private float defaultScale;
	
	private ImageView imageView;
	private Bitmap bitmap;
	private GestureDetector mGestureDetector;
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	private PointF first = new PointF();
	private PointF start = new PointF();
	private PointF mid = new PointF();;
	private float oldDist;
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;
	private long beginTime, endTime;
	
	private String imageFID;
	private String bigImageUrl;
	private FrameLayout progress_framelayout;
	private ProgressBar progress_bar;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		// 设置无标题
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 设置全屏
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.layout_imageview);
		
		// 获取手机屏幕的宽和高
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		displayWidth = dm.widthPixels;
		displayHeight = dm.heightPixels;
		
		imageView = (ImageView) findViewById(R.id.imageview);
		progress_framelayout = (FrameLayout) findViewById(R.id.progress_framelayout);
		progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
		
		// 获取传递过来的imageFID和bigImageUrl
		imageFID = getIntent().getStringExtra("imageFID");
		bigImageUrl = getIntent().getStringExtra("bigImageUrl");
		
		initButtonEvent();
		
		// 如果imageFID为空，则不执行任何逻辑
		if(imageFID == null || imageFID == ""){
			return;
		}
		
		
		// 根据imageFID加载图片
		try {
			loadImageByFID();
		} catch (SAFException e) {
			e.printStackTrace();
		}
		// 初始化图片信息
		calculateScale();
		initImageInfo();
		
		// 如果bigImageUrl不为空，则开始从网络上下载
		if(bigImageUrl != null && bigImageUrl != ""){
			progress_framelayout.setVisibility(View.VISIBLE);
			new loadBigImageUrl().execute(bigImageUrl);
		}
		
	}
	/**
	 * 加载大图片
	 * @author Panshihao
	 *
	 */
	private class loadBigImageUrl extends SAFRunnerAdapter<String, Integer, Bitmap>{

		@Override
		public Bitmap doInBackground(String... params) {
			// TODO Auto-generated method stub
			SAFImageCompress imageCompress = new SAFImageCompress(new onTransportProgressListener() {
				
				@Override
				public void onProgress(long readSize, long totalSize) {
					// TODO Auto-generated method stub
					final int pro = (int) ((float)readSize / totalSize * 100);
					runOnUiThread(new Runnable() {
						public void run() {
							if(pro > progress_bar.getProgress()){
								progress_bar.setProgress(pro);
							}
						}
					});
				}
				
				@Override
				public void onComplete() {
					// TODO Auto-generated method stub
					System.out.println("onComplete");
				}
			});
			
			// 从网络上下载图片，然后重新判断newBitmap是否为空并且是否与原bitmap相同
			Bitmap newBitmap = null;
			try {
				newBitmap = imageCompress.HttpFullScreenCompress(params[0]);
			} catch (SAFException e) {
				e.printStackTrace();
			}
			if(newBitmap != null && newBitmap != bitmap){
				bitmap.recycle();
				bitmap = newBitmap;
			}
			
			
			return bitmap;
		}
		@Override
		public void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			// 初始化图片信息
			calculateScale();
			initImageInfo();
			// 隐藏进度条
			progress_framelayout.setVisibility(View.GONE);
			progress_bar.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 初始化按钮事件
	 */
	public void initButtonEvent(){
		findViewById(R.id.header_buttons_back).setOnClickListener(this);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		// 页面被注销后，需要释放内存
		imageView.setImageBitmap(null);
		if(bitmap != null && !bitmap.isRecycled()){
			bitmap.recycle();
		}
		
		
	}
	
	
	/**
	 * 初始化图片信息
	 */
	public void initImageInfo(){
		// 初始化图片信息 
		imageView.setImageBitmap(bitmap);
		matrix.reset();
		matrix.postScale(defaultScale, defaultScale);
		matrix.postTranslate(displayWidth / 2 - defaultScale * imageWidth / 2, displayHeight / 2 - defaultScale
				* imageHeight / 2);
		imageView.setImageMatrix(matrix);

		imageView.setOnTouchListener(this);
		imageView.setOnClickListener(this);
		imageView.setLongClickable(true);
		
		mGestureDetector = new GestureDetector(this, this);
		
		savedMatrix.set(matrix);
	}
	

	/**
	 * 计算各种比例
	 */
	public void calculateScale(){
		imageWidth = bitmap.getWidth();
		imageHeight = bitmap.getHeight();
		
		float scaleWid = (float) displayWidth / imageWidth;
		float scaleHeight = (float) displayHeight / imageHeight;

		// 如果宽的 比列大于搞的比列 则用高的比列 否则用宽的

		if (scaleWid > scaleHeight) {
			defaultScale = scaleHeight;
		} else
			defaultScale = scaleWid;
		
	}
	/**
	 * 根据imageFID加载本地图片
	 * @throws SAFException 
	 */
	public void loadImageByFID() throws SAFException{

		SAFImageCompress imageCompress = new SAFImageCompress();
		
		bitmap = imageCompress.LocalAutoCompress(imageFID);
		
		
	}
	
	/**
	 * 判断缩放比例是否比默认比例大了
	 * 
	 * @return
	 */
	public boolean isScale() {
		float[] values = new float[9];
		imageView.getImageMatrix().getValues(values);

		// matrix矩阵中的宽度和高度缩放值
		float widthScale = values[0];
		float heightScale = values[4];

		// matrix矩阵中的x,y偏移量
		float matrixX = values[2];
		float matrixY = values[5];
		return widthScale <= defaultScale;
	}
	/**
	 * 将大小设置为最大
	 */
	public void setMaxScale(){
		matrix.setScale(1, 1);
		imageView.setImageMatrix(matrix);
	}
	
	/**
	 * 还原图片大小位置
	 */
	public void resetPoint() {

		matrix.setScale(defaultScale, defaultScale);
		imageView.setImageMatrix(matrix);
		matrix.postTranslate(displayWidth / 2 - defaultScale * imageWidth / 2,
				displayHeight / 2 - defaultScale * imageHeight / 2);
		imageView.setImageMatrix(matrix);
	}

	/**
	 * 还原图片位置
	 */
	public void resetLocal() {
		float[] values = new float[9];
		imageView.getImageMatrix().getValues(values);
		float widthScale = values[0];
		float heightScale = values[4];
		matrix.postTranslate(displayWidth / 2 - defaultScale * imageWidth / 2, 0);
		imageView.setImageMatrix(matrix);
	}
	/**
	 * 计算拖动的距离
	 * 
	 * @param event
	 * @return
	 */
	private float spacing(MotionEvent event) {
		if (event.getPointerCount() > 1) {
			float x = event.getX(0) - event.getX(1);
			float y = event.getY(0) - event.getY(1);
			return FloatMath.sqrt(x * x + y * y);
		}
		return 0;
	}

	/**
	 * 计算两点的之间的中间点
	 * 
	 * @param point
	 * @param event
	 */

	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		 mGestureDetector.onTouchEvent(event);
		// System.out.println("action==="+event.getAction());
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:

			beginTime = System.currentTimeMillis();

			mode = DRAG;
			// System.out.println("down");
			first.set(event.getX(), event.getY());
			start.set(event.getX(), event.getY());
			break;
		case MotionEvent.ACTION_UP:

			endTime = System.currentTimeMillis();

			// System.out.println("endTime=="+(endTime - beginTime));
			float x = event.getX(0) - first.x;
			float y = event.getY(0) - first.y;
			// 多长的距离
			float move = FloatMath.sqrt(x * x + y * y);

			// System.out.println("move=="+(move));

			// 计算时间和移动的距离 来判断你想要的操作，经过测试90%情况能满足
			if (endTime - beginTime < 500 && move > 20) {
				// 这里就是做你上一页下一页的事情了。
				// Toast.makeText(this, "----do something-----", 1000).show();
				// 如果缩放比例小于默认比例则还原图片的位置
			}
			if (isScale()) {
				resetPoint();
			}

			break;
		case MotionEvent.ACTION_MOVE:

			// System.out.println("move");
			if (mode == DRAG) {
				float[] values = new float[9];
				imageView.getImageMatrix().getValues(values);
				// for(int i = 0 ; i < values.length ; i ++){
				// System.out.println(values[i]);
				// }

				// matrix矩阵中的宽度和高度缩放值
				float widthScale = values[0];
				float heightScale = values[4];

				// matrix矩阵中的x,y偏移量
				float matrixX = values[2];
				float matrixY = values[5];

				float dx = event.getX() - start.x;
				float dy = event.getY() - start.y;

				// 如果当前大小比例大于默认比例
				if (widthScale > defaultScale) {
//					System.out.println("widthScale -> " + widthScale
//							+ " ,heightScale -> " + heightScale);
//					System.out.println("matrixX -> " + matrixX
//							+ " ,matrixY -> " + matrixY);

					// 获取边界比例值
					float cScale = widthScale - defaultScale;

//					System.out.println(heightScale * imageHeight);

					// 左边界判断，左边界的x值为0
					if (matrixX + dx >= 0) {
						dx = 0;
					}

					// 右边界判断，右边界的x值为cScale*displayWidth
					if (matrixX + dx + -displayWidth <= -(widthScale * imageWidth)) {
						dx = 0;
					}

					// 上边界判断，上边界的y值为0
					if (matrixY + dy >= 0) {
						dy = 0;
					}

					// 下边界判断，下边界的y值为cScale*displayHeight
					if (matrixY + dy + -displayHeight <= -(heightScale * imageHeight)) {
						dy = 0;
					}
					//

				} else if (widthScale <= defaultScale) {
					// 如果当前大小比例小于默认比例
					dx = 0;
					dy = 0;
				}

				matrix.postTranslate(dx, dy);

				start.set(event.getX(), event.getY());

			} else {
				float newDist = spacing(event);
				if (newDist > 10f) {
					// matrix.set(savedMatrix);
					float scale = newDist / oldDist;
					// System.out.println("scale=="+scale);
					float[] values = new float[9];
					imageView.getImageMatrix().getValues(values);

					// matrix矩阵中的宽度和高度缩放值
					float widthScale = values[0];
					float heightScale = values[4];

					// matrix矩阵中的x,y偏移量
					float matrixX = values[2];
					float matrixY = values[5];

					// 如果scale大于1则代表是在放大
					if (scale > 1) {
						if (widthScale < 1) {
							matrix.postScale(scale, scale, displayWidth / 2, displayHeight / 2);
						}
					} else {
						matrix.postScale(scale, scale, displayWidth / 2, displayHeight / 2);
					}

				}
				oldDist = newDist;
			}
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			if (oldDist > 10f) {
				midPoint(mid, event);
				mode = ZOOM;
			}

			// System.out.println("ACTION_POINTER_DOWN");
			break;
		case MotionEvent.ACTION_POINTER_UP:
			// System.out.println("ACTION_POINTER_UP");

			// 如果缩放比例小于默认比例则还原图片的位置
			if (isScale()) {
				resetPoint();
			} else {
				float dx = event.getX() - start.x;
				float dy = event.getY() - start.y;
				float[] values = new float[9];
				imageView.getImageMatrix().getValues(values);

				// matrix矩阵中的宽度和高度缩放值
				float widthScale = values[0];
				float heightScale = values[4];

				// matrix矩阵中的x,y偏移量
				float matrixX = values[2];
				float matrixY = values[5];
				float cScale = widthScale - defaultScale;

				if (matrixX + dx > 0 || matrixX + dx < -(cScale * imageWidth)) {
					
					matrix.postTranslate(-matrixX, 0);
				}

			}

			break;
		}
		imageView.setImageMatrix(matrix);
		return false;
	}

	
	
	
	@Override
	public boolean onDoubleTap(MotionEvent e) {
		// TODO Auto-generated method stub
		float[] values = new float[9];
		imageView.getImageMatrix().getValues(values);

		// matrix矩阵中的宽度和高度缩放值
		float widthScale = values[0];
		float heightScale = values[4];

		if(widthScale >= 1){
			
			//			resetPoint();
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					boolean stop = false;
					
					while(!stop){
						float[] values = new float[9];
						imageView.getImageMatrix().getValues(values);

						// matrix矩阵中的宽度和高度缩放值
						float widthScale = values[0];
						float heightScale = values[4];

						// matrix矩阵中的x,y偏移量
						float matrixX = values[2];
						float matrixY = values[5];
						if(widthScale >= defaultScale){
							matrix.postScale(0.9f, 0.9f, displayWidth / 2, displayHeight / 2);
							
							runOnUiThread(new Runnable() {
								public void run() {
									imageView.setImageMatrix(matrix);
								}
							});
						}else{
							stop = true;
							runOnUiThread(new Runnable() {
								public void run() {
									resetPoint();
								}
							});
						}
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
					}
					
				}
			}).start();
			
		}else{
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					boolean stop = false;
					
					while(!stop){
						float[] values = new float[9];
						imageView.getImageMatrix().getValues(values);

						// matrix矩阵中的宽度和高度缩放值
						float widthScale = values[0];
						float heightScale = values[4];

						// matrix矩阵中的x,y偏移量
						float matrixX = values[2];
						float matrixY = values[5];
						if(widthScale <= 1){
							matrix.postScale(1.1f, 1.1f, displayWidth / 2, displayHeight / 2);
							
							runOnUiThread(new Runnable() {
								public void run() {
									imageView.setImageMatrix(matrix);
								}
							});
						}else{
							stop = true;
						}
						try {
							Thread.sleep(10);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						
					}
					
				}
			}).start();
			
			
		}
		
		
		return true;
	}
	@Override
	public boolean onDoubleTapEvent(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean onSingleTapConfirmed(MotionEvent arg0) {
		// TODO Auto-generated method stub
		// 点击图片打开退出按钮
		int visibile = findViewById(R.id.header_buttons).getVisibility();
		if(visibile == View.GONE){
			findViewById(R.id.header_buttons).setVisibility(View.VISIBLE);
		}else {
			findViewById(R.id.header_buttons).setVisibility(View.GONE);
		}
					
		return false;
	}
	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.header_buttons_back:
			// 离开页面事件
			finish();
			break;
		case R.id.imageview:
			
			break;
		default:
			break;
		}
		
		
	}
	
	
	
}
