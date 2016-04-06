package com.zcy.imagelib.tools;

import android.content.Context;

import com.zcy.imagelib.view.ProgressDialog;

public class DialogUtils {
	
	private static ProgressDialog dialog = null;

	/* 显示进度对话框 * 显示进度对话框

	  @param context
	  @param message 提示信息*/

	public static void showLoadingDialog(Context context, String message) {
		dismissLoadingDialog();
		dialog = new ProgressDialog(context);
		dialog.show(message);
	}

	public static void dismissLoadingDialog() {
		if (dialog != null) {
			dialog.cancel();
			dialog = null;
		}
	}
}
