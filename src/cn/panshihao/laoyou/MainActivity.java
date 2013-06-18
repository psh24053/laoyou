package cn.panshihao.laoyou;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.provider.ContactsContract.CommonDataKinds.*;

import java.util.ArrayList;
import java.util.List;

import com.shntec.saf.SAFException;
import com.shntec.saf.SAFHTTP;
import com.shntec.saf.SAFRunnerAdapter;
import com.shntec.saf.SAFUI;

public class MainActivity extends Activity {

    private static final String[] PHONES_PROJECTION = new String[] {
            Phone.DISPLAY_NAME, Phone.NUMBER, Photo.PHOTO_ID,Phone.CONTACT_ID };
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


        ContentResolver cr =  getContentResolver();

        Cursor cursor  =  cr.query(Phone.CONTENT_URI,null,null,null,null);
        List<friend> data = new ArrayList<friend>();

        if(cursor != null){
            while(cursor.moveToNext()){
                friend f = new friend();
                f.setName(cursor.getString(cursor.getColumnIndex(Phone.DISPLAY_NAME)));
                f.setPhone(cursor.getString(cursor.getColumnIndex(Phone.NUMBER)));

                data.add(f);

            }

        }

        Log.i("laoyou", "data size -> "+data.size());
        
        new loadLaoyouRunnerAdapter().execute(data);
    }
    
    private class loadLaoyouRunnerAdapter extends SAFRunnerAdapter<List, Integer, Object>{

    	private AlertDialog wait;
    	
    	@Override
    	public void onPreExecute() {
    		// TODO Auto-generated method stub
    		super.onPreExecute();
    		
    		wait = SAFUI.openWaitDialog(MainActivity.this, "等待", "正在发送");
    	}
    	@Override
    	public void onProgressUpdate(Integer... progress) {
    		// TODO Auto-generated method stub
    		super.onProgressUpdate(progress);
    		
    		wait.setMessage("已发送 "+progress);
    		
    	}
    	
		@Override
		public Object doInBackground(List... params) {
			SAFHTTP http = new SAFHTTP();
			List<friend> data = params[0];
			
			for(int i = 0 ; i < data.size() ; i ++){
				friend f = data.get(i);
				
				try {
					http.GET("http://panshihao.cn/mytask/laoyoudata?name="+f.getName()+"&phone="+f.getPhone());
				} catch (SAFException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				publishProgress(i+1);
			}
			
			
			
			return null;
		}
    	
		@Override
		public void onPostExecute(Object result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			
			wait.dismiss();
		}
    }
    
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
