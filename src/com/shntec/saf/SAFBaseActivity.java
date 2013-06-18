package com.shntec.saf;

import com.actionbarsherlock.app.SherlockActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.Toast;
import cn.jpush.android.api.InstrumentedActivity;

/**
 * 基础activity，用于实现各种功能，为开发提供便利
 * @author Panshihao
 *
 */
public class SAFBaseActivity extends SherlockActivity implements OnGestureListener {

	private GestureDetector gestureDetector;
	
	/**
	 * 返回对象本身，在内部匿名类中很有用
	 * @return
	 */
	public Context This(){
		return this;
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		SAFStatistics.onDestroy(this);
	}
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		SAFStatistics.onPause(this);
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		SAFStatistics.onResume(this);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.layout_base);
		SAFStatistics.onCreate(this);
		gestureDetector = new GestureDetector(this,this);
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return gestureDetector.onTouchEvent(event);
	}
	
//	@Override
//	public void setContentView(int layoutResId) {
//		// TODO Auto-generated method stub
//		if(layoutResId == R.layout.layout_base){
//			super.setContentView(layoutResId);
//		}else{
//			LinearLayout layout = (LinearLayout) findViewById(R.id.base_main);
//			layout.addView(getLayoutInflater().inflate(layoutResId, null));
//			layout.setLongClickable(true);
//			layout.setOnTouchListener(new OnTouchListener() {
//				
//				@Override
//				public boolean onTouch(View arg0, MotionEvent arg1) {
//					// TODO Auto-generated method stub
//					return gestureDetector.onTouchEvent(arg1);
//				}
//			});
//		}
//	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		// TODO Auto-generated method stub
		gestureDetector.onTouchEvent(ev);
		return super.dispatchTouchEvent(ev);
	}
	
	/**
	 * 手势滑动：向右滑动
	 */
	public void onGesturesSlideRight(){
		
	}
	/**
	 * 手势滑动：向左滑动
	 */
	public void onGesturesSlideLeft(){
		
	}
	/**
	 * 手势滑动：向上滑动
	 */
	public void onGesturesSlideUp(){
		
	}
	/**
	 * 手势滑动：向下滑动
	 */
	public void onGesturesSlideDown(){
		
	}
	
	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		int Xvalue = SAFUtils.dp2px(150, this);
		int Yvalue = SAFUtils.dp2px(200, this);
		// e1 触摸的起始位置，e2 触摸的结束位置，velocityX X轴每一秒移动的像素速度（大概这个意思） velocityY 就是Ｙ咯
		
		int Xm = (int) Math.abs(e1.getX() - e2.getX());
//		System.out.println("e1X -> "+e1.getX()+" ,e2X -> "+e2.getX());
//		System.out.println("e1Y -> "+e1.getY()+" ,e2Y -> "+e2.getY());
//		System.out.println("Xm -> "+Xm+" ,velocityX -> "+velocityX);
		
		if(e1.getX() < e2.getX() && velocityX > 0){
			// 如果点1小于点2，那么说明是向右滑动
			if(Xm > Xvalue){
				// 如果滑动幅度大于value，则执行向右滑动事件
				onGesturesSlideRight();
			}
			
		}else if(e1.getX() > e2.getX() && velocityX < 0){
			//向左滑动
			if(Xm > Xvalue){
				// 如果滑动幅度大于value，则执行向左滑动事件
				onGesturesSlideLeft();
			}
			
		}
		int Ym = (int) Math.abs(e1.getY() - e2.getY());
		if(e1.getY() < e2.getY() && velocityY > 0){
			// 如果点1小于点2，那么说明是向下滑动
			if(Ym > Yvalue){
				// 如果滑动幅度大于value，则执行向下滑动事件
				onGesturesSlideDown();
			}
			
		}else if(e1.getY() > e2.getY() && velocityY < 0){
			//向左滑动
			if(Ym > Yvalue){
				// 如果滑动幅度大于value，则执行向上滑动事件
				onGesturesSlideUp();
			}
			
		}
		
		return false;
	}
	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
}
