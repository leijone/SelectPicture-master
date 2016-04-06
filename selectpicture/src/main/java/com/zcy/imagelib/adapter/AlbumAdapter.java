package com.zcy.imagelib.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.zcy.imagelib.entity.AlbumBean;
import com.zcy.imagelib.view.MyImageView;
import java.io.File;
import java.util.List;

/**
 * Created by zcy on 2016/03/28.
 */
public class AlbumAdapter extends BaseAdapter {
	private Context context;
	private List<AlbumBean> albums;
	private AlbumBean mAlbumBeanData;
	private LayoutInflater inflater;

	public AlbumAdapter(Context context) {
		this.context = context;
		this.inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setData(List<AlbumBean> albums, AlbumBean mAlbumBeanData) {
		this.albums = albums;
		this.mAlbumBeanData = mAlbumBeanData;
	}

	@Override
	public int getCount() {
		return albums == null || albums.size() == 0 ? 0 : albums.size();
	}

	@Override
	public Object getItem(int position) {
		return albums.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = inflater.inflate(
					context.getResources().getIdentifier("the_picture_selection_pop_item", "layout", context.getPackageName()), null);
			viewHolder.rl_parent = (RelativeLayout) convertView
					.findViewById(context.getResources().getIdentifier("rl_parent", "id", context.getPackageName()));
			viewHolder.album_count = (TextView) convertView
					.findViewById(context.getResources().getIdentifier("album_count", "id", context.getPackageName()));
			viewHolder.album_name = (TextView) convertView
					.findViewById(context.getResources().getIdentifier("album_name", "id", context.getPackageName()));
			viewHolder.mCheckBox = (ImageView) convertView
					.findViewById(context.getResources().getIdentifier("album_ck", "id", context.getPackageName()));
			viewHolder.mImageView = (MyImageView) convertView
					.findViewById(context.getResources().getIdentifier("album_image", "id", context.getPackageName()));
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		final AlbumBean b = (AlbumBean) getItem(position);
		viewHolder.mImageView.setTag(b.thumbnail);
		viewHolder.album_name.setText(b.folderName);
		viewHolder.album_count.setText(b.count - 1 + "");
		Glide.with(context)
		.load(new File(b.thumbnail))
		.centerCrop()
		.placeholder(context.getResources().getIdentifier("icon_placeholder", "drawable", context.getPackageName()))
		.crossFade(500)
		.error(context.getResources().getIdentifier("icon_failure", "drawable", context.getPackageName()))
		.into(viewHolder.mImageView);
		viewHolder.mCheckBox.setVisibility( b == mAlbumBeanData ? View.VISIBLE : View.GONE);
		return convertView;
	}

	public static class ViewHolder {
		public MyImageView mImageView;
		public ImageView mCheckBox;
		public TextView album_name;
		public TextView album_count;
		public RelativeLayout rl_parent;
	}

	public interface OnImageSelectedListener {
		void notifyChecked();
	}

	public interface OnImageSelectedCountListener {
		int getImageSelectedCount();
	}
}
