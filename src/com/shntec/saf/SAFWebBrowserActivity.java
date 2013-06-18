package com.shntec.saf;

import cn.panshihao.laoyou.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

/**
 * 内置web浏览器
 * @author panshihao
 *
 */
public class SAFWebBrowserActivity extends SAFBaseActivity {

	private WebView webView;
	private String url;
	private ClickListener clickListener;
	private ProgressBar progressBar;
	
	public static WebViewListener webViewListener;
	
	/**
	 * webView的行为监听器
	 * @author panshihao
	 *
	 */
	public interface WebViewListener{
		
		public boolean shouldOverrideUrlLoading(WebView view, String url);
		
	}
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_webbrowser);
		
		// 如果传入的参数里没有url
		url = getIntent().getStringExtra("url");
		if(url == null || url.length() == 0){
			return;
		}
		progressBar = (ProgressBar) findViewById(R.id.webbrowser_progressbar);
		
		
		webView = (WebView) findViewById(R.id.webbrowser_webview);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// TODO Auto-generated method stub
				if(webViewListener != null){
					return webViewListener.shouldOverrideUrlLoading(view, url);
				}
				
				view.loadUrl(url);
				return true;
			}
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				// TODO Auto-generated method stub
				super.onPageStarted(view, url, favicon);
				progressBar.setVisibility(View.VISIBLE);
			}
			
			@Override
			public void onPageFinished(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onPageFinished(view, url);
				progressBar.setVisibility(View.GONE);
			}
			
		});
		webView.setWebChromeClient(new WebChromeClient(){
			
			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				// TODO Auto-generated method stub
				progressBar.setProgress(newProgress);
			}
			
			
		});
		webView.loadUrl(url);
		
		initClick();
	}
	/**
	 * 初始化点击事件
	 */
	public void initClick(){
		
		clickListener = new ClickListener();
		
		findViewById(R.id.webbrowser_back).setOnClickListener(clickListener);
		findViewById(R.id.webbrowser_refresh).setOnClickListener(clickListener);
		findViewById(R.id.webbrowser_buttons_advance).setOnClickListener(clickListener);
		findViewById(R.id.webbrowser_buttons_back).setOnClickListener(clickListener);
		findViewById(R.id.webbrowser_buttons_stop).setOnClickListener(clickListener);
	}
	/**
	 * 点击事件监听器
	 * @author panshihao
	 *
	 */
	private class ClickListener implements OnClickListener{

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.webbrowser_back:
				finish();
				overridePendingTransition(R.anim.slide_up_in,
						R.anim.slide_down_out);
				break;
			case R.id.webbrowser_refresh:
				webView.reload();
				break;
			case R.id.webbrowser_buttons_advance:
				if(webView.canGoForward()){
					webView.goForward();
				}
				break;
			case R.id.webbrowser_buttons_back:
				if(webView.canGoBack()){
					webView.goBack();
				}
				break;
			case R.id.webbrowser_buttons_stop:
				webView.stopLoading();
					
				break;
			default:
				break;
			}
		}
		
	}
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_up_in,
					R.anim.slide_down_out);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {       
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {       
        	webView.goBack();       
                   return true;       
        }    
        
        return super.onKeyDown(keyCode, event);       
    }  
	
}
