package com.xbw.mvp.mysql;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.Gravity;
import android.view.WindowManager;

public class Progress_Dialog {

	@SuppressWarnings("deprecation")
	public static ProgressDialog CreateProgressDialog(Context context)
	{
		ProgressDialog dialog = new ProgressDialog(context);
		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		dialog.setMessage("数据加载中……");
		dialog.setCancelable(false);
		WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
		dialog.getWindow().setGravity(Gravity.BOTTOM);
		return dialog;
	}

}
