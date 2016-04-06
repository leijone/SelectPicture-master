package com.zcy.imagelib.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.bumptech.glide.Glide;
import com.zcy.imagelib.entity.ImageBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import uk.co.senab.photoview.PhotoView;

public class ImageBrowserAdapter extends PagerAdapter {

	private List<ImageBean> mPhotos = new ArrayList<ImageBean>();
	Context context;

	public ImageBrowserAdapter(Context context, List<ImageBean> photos) {
		this.context = context;
		if (photos != null) {
			mPhotos = photos;
		}
	}

	@Override
	public int getCount() {
		if (mPhotos.size() > 1) {
			return Integer.MAX_VALUE;
		}
		return mPhotos.size();
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	@Override
	public View instantiateItem(ViewGroup container, int position) {
		final PhotoView photoView = new PhotoView(container.getContext());

		String path = mPhotos.get(position % mPhotos.size()).path;
		if (path.startsWith("http://") || path.startsWith("https://")) {
			// 这里进行图片的缓存操作(网络图片的加载)
		} else {
			Glide.with(context)
			.load(new File(path))
			.placeholder(context.getResources().getIdentifier("icon_placeholder", "drawable", context.getPackageName()))
			.crossFade(500)
			.error(context.getResources().getIdentifier("icon_failure", "drawable", context.getPackageName()))
			.into(photoView);
		}
		container.addView(photoView, LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		return photoView;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}
}
