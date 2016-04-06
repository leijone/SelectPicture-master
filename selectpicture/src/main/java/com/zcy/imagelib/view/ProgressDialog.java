package com.zcy.imagelib.view;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;


public class ProgressDialog {
	private Dialog progressDialog;
	private TextView msg;
	public ProgressDialog(Context context){
		progressDialog = new Dialog(context, context.getResources().getIdentifier("progress_dialog", "style", context.getPackageName()));
		progressDialog.setContentView(context.getResources().getIdentifier("dialog", "layout", context.getPackageName()));
		progressDialog.setCancelable(false);
		progressDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
		msg = (TextView) progressDialog.findViewById(context.getResources().getIdentifier("id_tv_loadingmsg", "id", context.getPackageName()));
	}
	public void show(String message){
		if(TextUtils.isEmpty(message)){
			msg.setText("拼命加载中");
		}else{
			msg.setText(message);
		}
		progressDialog.show();
	}

	public void cancel(){
		if(progressDialog != null){
			progressDialog.dismiss();
		}
	}
}
