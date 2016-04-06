package com.zcy.imagelib;

import com.bumptech.glide.request.target.ViewTarget;

import android.app.Application;

public class App extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		ViewTarget.setTagId(R.id.glide_tag);
	}
}
