package com.zcy.imagelib.camera;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class CameraActivity extends FragmentActivity {

	public static final String TAG = CameraActivity.class.getSimpleName();
	private Intent data;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			View decorView = getWindow().getDecorView();
			// Hide the status bar.
			int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
			decorView.setSystemUiVisibility(uiOptions);
		}

		getWindow().setBackgroundDrawable(null);
		setContentView(getResources().getIdentifier("activity_camera", "layout", getPackageName()));
		if (savedInstanceState == null) {
			getSupportFragmentManager()
			.beginTransaction()
			.replace(getResources().getIdentifier("fragment_container", "id", getPackageName()), CameraFragment.newInstance())
			.commit();

		}
	}

	public void returnPhotoUri(Uri uri) {
		data = new Intent();
		data.setData(uri);
		if (getParent() == null) {
			CameraActivity.this.setResult(RESULT_OK, data);
		} else {
			getParent().setResult(RESULT_OK, data);
		}
		finish();
	}


	public void onCancel(View view) {
		getSupportFragmentManager().popBackStack();
	}
}
