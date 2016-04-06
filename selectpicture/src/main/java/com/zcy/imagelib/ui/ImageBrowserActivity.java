package com.zcy.imagelib.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.zcy.imagelib.adapter.ImageBrowserAdapter;
import com.zcy.imagelib.entity.ImageBean;
import com.zcy.imagelib.transformer.DepthPageTransformer;
import com.zcy.imagelib.view.PhotoTextView;
import com.zcy.imagelib.view.ScrollViewPager;

import java.io.Serializable;
import java.util.List;

public class ImageBrowserActivity extends Activity implements
		OnPageChangeListener, OnClickListener {

	private ScrollViewPager mSvpPager;
	private PhotoTextView mPtvPage;
	private ImageBrowserAdapter mAdapter;
	private int mPosition;
	List<ImageBean> imagesList;
	private int mTotal;
	ImageView delete;
	LinearLayout back;
	boolean isDel = false;

	public static final String POSITION = "position";
	public static final String ISDEL = "isdel";
	public static final String IMAGES = "images";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(getResources().getIdentifier("activity_imagebrowser",
				"layout", getPackageName()));
		initViews();
		initEvents();
		init();
	}

	private void initViews() {
		mSvpPager = (ScrollViewPager) findViewById(getResources()
				.getIdentifier("imagebrowser_svp_pager", "id", getPackageName()));
		mPtvPage = (PhotoTextView) findViewById(getResources().getIdentifier(
				"imagebrowser_ptv_page", "id", getPackageName()));
		delete = (ImageView) findViewById(getResources().getIdentifier(
				"delete", "id", getPackageName()));
		back = (LinearLayout) findViewById(getResources().getIdentifier("back",
				"id", getPackageName()));
	}

	@SuppressWarnings("deprecation")
	private void initEvents() {
		mSvpPager.setOnPageChangeListener(this);
		delete.setOnClickListener(this);
		back.setOnClickListener(this);
	}

	@SuppressWarnings("unchecked")
	private void init() {
		isDel = getIntent().getBooleanExtra(ISDEL, false);
		mPosition = getIntent().getIntExtra(POSITION, 0);
		if (isDel)
			delete.setVisibility(View.VISIBLE);
		imagesList = (List<ImageBean>) getIntent().getSerializableExtra(IMAGES);
		mTotal = imagesList.size();
		if (mPosition > mTotal) {
			mPosition = mTotal - 1;
		}
		if (mTotal > 1) {
			mPosition += 1000 * mTotal;
			mPtvPage.setText((mPosition % mTotal) + 1 + "/" + mTotal);
			mAdapter = new ImageBrowserAdapter(this, imagesList);
			mSvpPager.setAdapter(mAdapter);
			mSvpPager.setPageTransformer(true, new DepthPageTransformer());
			mSvpPager.setCurrentItem(mPosition, false);
		}
		if (mTotal == 1) {
			mPtvPage.setText("1/1");
			mAdapter = new ImageBrowserAdapter(this, imagesList);
			mSvpPager.setAdapter(mAdapter);
			mSvpPager.setPageTransformer(true, new DepthPageTransformer());
			mSvpPager.setCurrentItem(mPosition, false);
		}

	}

	@Override
	public void onPageScrollStateChanged(int position) {
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int arg0) {
		mPosition = arg0;
		mPtvPage.setText((mPosition % mTotal) + 1 + "/" + mTotal);
	}

	@Override
	public void onClick(View view) {
		
		if (view.getId() == getResources().getIdentifier("delete", "id", getPackageName())) {
			mPosition = (mSvpPager.getCurrentItem() % mTotal);
			imagesList.remove(mPosition);
			mTotal = imagesList.size();
			if (mPosition > mTotal) {
				mPosition = mTotal - 1;
			}
			if (mTotal >= 1) {
				mPosition += 1000 * mTotal;
				mPtvPage.setText((mPosition % mTotal) + 1 + "/" + mTotal);
				mAdapter = new ImageBrowserAdapter(this, imagesList);
				mSvpPager.setAdapter(mAdapter);
				mSvpPager.setCurrentItem(mPosition, false);
			} else if (mTotal == 0) {
				imagesList.clear();
				onBackPressed();
			} else {
				onBackPressed();
			}
			
		} else if (view.getId() == getResources().getIdentifier("back", "id", getPackageName())) {
			onBackPressed();
		}
	}

	@Override
	public void onBackPressed() {
		Intent data = new Intent();
		data.putExtra("M_LIST", (Serializable) imagesList);
		setResult(RESULT_OK, data);
		ImageBrowserActivity.this.finish();
	}

}
