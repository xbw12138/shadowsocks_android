/*
 * Copyright (C) 2008 ZXing authors
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

package com.xbw.mvp.scanner.decode;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Browser;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.Result;
import com.xbw.mvp.R;
import com.xbw.mvp.scanner.view.ViewfinderResultPointCallback;
import com.xbw.mvp.ui.SaoYiSao;
import com.xbw.mvp.scanner.camera.CameraManager;
import java.util.Collection;
import java.util.Map;

/**
 * This class handles all the messaging which comprises the state machine for
 * capture.
 * 
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureActivityHandler extends Handler {

	private static final String TAG = CaptureActivityHandler.class
			.getSimpleName();

	private final SaoYiSao activity;

	/**
	 * 鐪熸璐熻矗鎵弿浠诲姟鐨勬牳蹇冪嚎绋�
	 */
	private final DecodeThread decodeThread;

	private State state;

	private final CameraManager cameraManager;

	/**
	 * 褰撳墠鎵弿鐨勭姸鎬�
	 */
	private enum State {
		/**
		 * 棰勮
		 */
		PREVIEW,
		/**
		 * 鎵弿鎴愬姛
		 */
		SUCCESS,
		/**
		 * 缁撴潫鎵弿
		 */
		DONE
	}

	public CaptureActivityHandler(SaoYiSao activity,
								  Collection<BarcodeFormat> decodeFormats,
								  Map<DecodeHintType, ?> baseHints, String characterSet,
								  CameraManager cameraManager) {
		this.activity = activity;

		// 鍚姩鎵弿绾跨▼
		decodeThread = new DecodeThread(activity, decodeFormats, baseHints,
				characterSet, new ViewfinderResultPointCallback(
						activity.getViewfinderView()));
		decodeThread.start();

		state = State.SUCCESS;

		// Start ourselves capturing previews and decoding.
		this.cameraManager = cameraManager;

		// 寮�鍚浉鏈洪瑙堢晫闈�
		cameraManager.startPreview();

		restartPreviewAndDecode();
	}

	@Override
	public void handleMessage(Message message) {
		switch (message.what) {
			case R.id.restart_preview: // 鍑嗗杩涜涓嬩竴娆℃壂鎻�
				Log.d(TAG, "Got restart preview message");
				restartPreviewAndDecode();
				break;
			case R.id.decode_succeeded:
				Log.d(TAG, "Got decode succeeded message");
				state = State.SUCCESS;
				Bundle bundle = message.getData();
				Bitmap barcode = null;
				float scaleFactor = 1.0f;
				if (bundle != null) {
					byte[] compressedBitmap = bundle
							.getByteArray(DecodeThread.BARCODE_BITMAP);
					if (compressedBitmap != null) {
						barcode = BitmapFactory.decodeByteArray(
								compressedBitmap, 0, compressedBitmap.length,
								null);
						// Mutable copy:
						barcode = barcode.copy(Bitmap.Config.ARGB_8888, true);
					}
					scaleFactor = bundle
							.getFloat(DecodeThread.BARCODE_SCALED_FACTOR);
				}
				activity.handleDecode((Result) message.obj, barcode,
						scaleFactor);
				break;
			case R.id.decode_failed:
				// We're decoding as fast as possible, so when one decode fails,
				// start another.
				state = State.PREVIEW;
				cameraManager.requestPreviewFrame(decodeThread.getHandler(),
						R.id.decode);
				break;
			case R.id.return_scan_result:
				Log.d(TAG, "Got return scan result message");
				activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
				activity.finish();
				break;
			case R.id.launch_product_query:
				Log.d(TAG, "Got product query message");
				String url = (String) message.obj;

				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
				intent.setData(Uri.parse(url));

				/**
				 * 杩欐浠ｇ爜鏄痾xing椤圭洰缁勬兂瑕佺敤chrome鎵撳紑娴忚鍣ㄦ祻瑙坲rl
				 */
				ResolveInfo resolveInfo = activity.getPackageManager()
						.resolveActivity(intent,
								PackageManager.MATCH_DEFAULT_ONLY);
				String browserPackageName = null;
				if (resolveInfo != null && resolveInfo.activityInfo != null) {
					browserPackageName = resolveInfo.activityInfo.packageName;
					Log.d(TAG, "Using browser in package " + browserPackageName);
				}

				// Needed for default Android browser / Chrome only apparently
				if ("com.android.browser".equals(browserPackageName)
						|| "com.android.chrome".equals(browserPackageName)) {
					intent.setPackage(browserPackageName);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent.putExtra(Browser.EXTRA_APPLICATION_ID,
							browserPackageName);
				}

				try {
					activity.startActivity(intent);
				}
				catch (ActivityNotFoundException ignored) {
					Log.w(TAG, "Can't find anything to handle VIEW of URI "
							+ url);
				}
				break;
		}
	}

	public void quitSynchronously() {
		state = State.DONE;
		cameraManager.stopPreview();
		Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
		quit.sendToTarget();

		try {
			// Wait at most half a second; should be enough time, and onPause()
			// will timeout quickly
			decodeThread.join(500L);
		}
		catch (InterruptedException e) {
			// continue
		}

		// Be absolutely sure we don't send any queued up messages
		removeMessages(R.id.decode_succeeded);
		removeMessages(R.id.decode_failed);
	}

	/**
	 * 瀹屾垚涓�娆℃壂鎻忓悗锛屽彧闇�瑕佸啀璋冪敤姝ゆ柟娉曞嵆鍙�
	 */
	private void restartPreviewAndDecode() {
		if (state == State.SUCCESS) {
			state = State.PREVIEW;

			// 鍚慸ecodeThread缁戝畾鐨刪andler锛圖ecodeHandler)鍙戦�佽В鐮佹秷鎭�
			cameraManager.requestPreviewFrame(decodeThread.getHandler(),
					R.id.decode);
			activity.drawViewfinder();
		}
	}

}
