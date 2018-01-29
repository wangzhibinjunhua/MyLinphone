/*
LinphoneLauncherActivity.java
Copyright (C) 2011  Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.linphone;

import static android.content.Intent.ACTION_MAIN;

import org.linphone.assistant.RemoteProvisioningActivity;
import org.linphone.core.LinphoneCoreException;
import org.linphone.mediastream.Version;
import org.linphone.tutorials.TutorialLauncherActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

/**
 * 
 * Launch Linphone main activity when Service is ready.
 * 
 * @author Guillaume Beraudo
 *
 */
public class LinphoneLauncherActivity extends Activity {

	private Handler mHandler;
	private ServiceWaitThread mThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Hack to avoid to draw twice LinphoneActivity on tablets
        if (getResources().getBoolean(R.bool.orientation_portrait_only)) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		}
		setContentView(R.layout.launch_screen);
        
		mHandler = new Handler();
		
		if (LinphoneService.isReady()) {
			onServiceReady();
		} else {
			// start linphone as background  
			startService(new Intent(ACTION_MAIN).setClass(this, LinphoneService.class));
			mThread = new ServiceWaitThread();
			mThread.start();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.e("wzb","LinphoneLauncherActivity onResume");
		Uri uri=getIntent().getData();
		if(uri != null){
			String url=uri.toString();
			Log.e("wzb","url:"+url);
			String scheme = uri.getScheme();
			Log.e("wzb", "scheme: " + scheme);
			String tel_number=url.substring(4);
			Log.e("wzb","tel_number="+tel_number);
			callOutgoing(tel_number);
		}
	}

	private void callOutgoing(String number) {
		try {
			if (!LinphoneManager.getInstance().acceptCallIfIncomingPending()) {
				String to = String.format("sip:%s@%s", number, "120.78.138.150");

				LinphoneManager.getInstance().newOutgoingCall(to, "Test Sip");

				startActivity(new Intent()
						.setClass(this, LinphoneActivity.class)
						.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
				finish();
			}
		} catch (LinphoneCoreException e) {
			LinphoneManager.getInstance().terminateCall();
		}

		try {

		}catch(Exception e){

		}
	}

	protected void onServiceReady() {
		//add by wzb test
		if(true)return;
		//end
		final Class<? extends Activity> classToStart;
		if (getResources().getBoolean(R.bool.show_tutorials_instead_of_app)) {
			classToStart = TutorialLauncherActivity.class;
		} else if (getResources().getBoolean(R.bool.display_sms_remote_provisioning_activity) && LinphonePreferences.instance().isFirstRemoteProvisioning()) {
			classToStart = RemoteProvisioningActivity.class;
		} else {
			classToStart = LinphoneActivity.class;
		}

		// We need LinphoneService to start bluetoothManager
		if (Version.sdkAboveOrEqual(Version.API11_HONEYCOMB_30)) {
			BluetoothManager.getInstance().initBluetooth();
		}
		
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				startActivity(new Intent().setClass(LinphoneLauncherActivity.this, classToStart).setData(getIntent().getData()));
				finish();
			}
		}, 0);
	}

	private class ServiceWaitThread extends Thread {
		public void run() {
			while (!LinphoneService.isReady()) {
				try {
					sleep(30);
				} catch (InterruptedException e) {
					throw new RuntimeException("waiting thread sleep() has been interrupted");
				}
			}
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					onServiceReady();
				}
			});
			mThread = null;
		}
	}
}


