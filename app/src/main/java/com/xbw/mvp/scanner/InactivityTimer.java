/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xbw.mvp.scanner;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.util.Log;
import com.xbw.mvp.scanner.common.Runnable;
/**
 * Finishes an activity after a period of inactivity if the device is on battery
 * power. <br/>
 * <br/>
 * 
 * 璇ユ椿鍔ㄧ洃鎺у櫒鍏ㄧ▼鐩戞帶鎵弿娲昏穬鐘舵�侊紝涓嶤aptureActivity鐢熷懡鍛ㄦ湡鐩稿悓
 */
public final class InactivityTimer {

	private static final String TAG = InactivityTimer.class.getSimpleName();

	/**
	 * 濡傛灉鍦�5min鍐呮壂鎻忓櫒娌℃湁琚娇鐢ㄨ繃锛屽垯鑷姩finish鎺塧ctivity
	 */
	private static final long INACTIVITY_DELAY_MS = 5 * 60 * 1000L;

	/**
	 * 鍦ㄦ湰app涓紝姝ctivity鍗充负CaptureActivity
	 */
	private final Activity activity;
	/**
	 * 鎺ュ彈绯荤粺骞挎挱锛氭墜鏈烘槸鍚﹁繛閫氱數婧�
	 */
	private final BroadcastReceiver powerStatusReceiver;
	private boolean registered;
	private AsyncTask<?, ?, ?> inactivityTask;

	public InactivityTimer(Activity activity) {
		this.activity = activity;
		powerStatusReceiver = new PowerStatusReceiver();
		registered = false;
		onActivity();
	}

	/**
	 * 棣栧厛缁堟涔嬪墠鐨勭洃鎺т换鍔★紝鐒跺悗鏂拌捣涓�涓洃鎺т换鍔�
	 */
	public synchronized void onActivity() {
		cancel();
		inactivityTask = new InactivityAsyncTask();
		Runnable.execAsync(inactivityTask);
	}

	public synchronized void onPause() {
		cancel();
		if (registered) {
			activity.unregisterReceiver(powerStatusReceiver);
			registered = false;
		}
		else {
			Log.w(TAG, "PowerStatusReceiver was never registered?");
		}
	}

	public synchronized void onResume() {
		if (registered) {
			Log.w(TAG, "PowerStatusReceiver was already registered?");
		}
		else {
			activity.registerReceiver(powerStatusReceiver, new IntentFilter(
					Intent.ACTION_BATTERY_CHANGED));
			registered = true;
		}
		onActivity();
	}

	/**
	 * 鍙栨秷鐩戞帶浠诲姟
	 */
	private synchronized void cancel() {
		AsyncTask<?, ?, ?> task = inactivityTask;
		if (task != null) {
			task.cancel(true);
			inactivityTask = null;
		}
	}

	public void shutdown() {
		cancel();
	}

	/**
	 * 鐩戝惉鏄惁杩為�氱數婧愮殑绯荤粺骞挎挱銆傚鏋滆繛閫氱數婧愶紝鍒欏仠姝㈢洃鎺т换鍔★紝鍚﹀垯閲嶅惎鐩戞帶浠诲姟
	 */
	private final class PowerStatusReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
				// 0 indicates that we're on battery
				boolean onBatteryNow = intent.getIntExtra(
						BatteryManager.EXTRA_PLUGGED, -1) <= 0;
				if (onBatteryNow) {
					InactivityTimer.this.onActivity();
				}
				else {
					InactivityTimer.this.cancel();
				}
			}
		}
	}

	/**
	 * 璇ヤ换鍔″緢绠�鍗曪紝灏辨槸鍦↖NACTIVITY_DELAY_MS鏃堕棿鍚庣粓缁揳ctivity
	 */
	private final class InactivityAsyncTask extends
			AsyncTask<Object, Object, Object> {
		@Override
		protected Object doInBackground(Object... objects) {
			try {
				Thread.sleep(INACTIVITY_DELAY_MS);
				Log.i(TAG, "Finishing activity due to inactivity");
				activity.finish();
			}
			catch (InterruptedException e) {
				// continue without killing
			}
			return null;
		}
	}

}
