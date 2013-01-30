package net.lukegjpotter.app.footballcrestquiz;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * @author Luke Potter
 * 30/December/2012
 * 
 * This is the activity for the splash screen
 */
public class Splash extends Activity {

	// Dictates how long the splash screen appears for
	protected static final long timeout = 2000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.splash);
		
		Thread timer = new Thread() {
			
			public void run() {
				
				try {
					
					sleep(timeout);
				} catch (InterruptedException e) {
					
					e.printStackTrace();
				} finally {
					
					Intent openNextActivity = new Intent("net.lukegjpotter.app.footballcrestquiz.MAINACTIVITY");
					startActivity(openNextActivity);
				}
			} // End run method
		}; // End Thread definition
		
		timer.start();
		
	} // End onCreate method
	
	@Override
	protected void onPause() {

		super.onPause();
		
		finish();
	} // End onPause method
} // End Splash Class
