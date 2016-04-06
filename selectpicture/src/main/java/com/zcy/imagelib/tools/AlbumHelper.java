package com.zcy.imagelib.tools;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.zcy.imagelib.entity.AlbumBean;
import com.zcy.imagelib.entity.ImageBean;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * 相册辅助类
 * @author zcy
 * 
 */
public class AlbumHelper {

	Context context;

	private static AlbumHelper instance;

	private AlbumHelper() {
	}

	public static AlbumHelper newInstance() {
		if (instance == null) {
			instance = new AlbumHelper();
		}
		return instance;
	};

	public List<AlbumBean> getFolders(Context context) {
		Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		ContentResolver contentResolver = context.getApplicationContext().getContentResolver();
		Cursor mCursor = contentResolver.query(mImageUri, null,
				MediaStore.Images.Media.MIME_TYPE + "=? or "
						+ MediaStore.Images.Media.MIME_TYPE + "=?",
						new String[] { "image/jpeg", "image/png" },
						MediaStore.Images.Media.DATE_TAKEN);
		HashMap<String, ArrayList<ImageBean>> map = capacity(mCursor);

		List<AlbumBean> mAlbumBeans = new ArrayList<AlbumBean>();

		Set<Entry<String, ArrayList<ImageBean>>> set = map.entrySet();

		ArrayList<ImageBean> images = new ArrayList<ImageBean>();

		ArrayList<ImageBean> tempImages = new ArrayList<ImageBean>();

		for (Iterator<Entry<String, ArrayList<ImageBean>>> iterator = set
				.iterator(); iterator.hasNext();) {
			Entry<String, ArrayList<ImageBean>> entry = iterator.next();
			String parentName = entry.getKey();
			ImageBean b = entry.getValue().get(0);
			String name = new File(PictureSelectedUtil.CONFIG.savePathPicture()).getName();
			if(entry.getKey().contains(name)){
				tempImages.addAll(entry.getValue());
			}else{
				images.addAll(entry.getValue());
			}
			AlbumBean tempAlbumBean = new AlbumBean(parentName, entry
					.getValue().size() + 1, entry.getValue(), b.path);
			// 在第0个位置加入了拍照图片 
			tempAlbumBean.sets.add(0, new ImageBean());
			mAlbumBeans.add(tempAlbumBean);
		}
		images.addAll(0, tempImages);
		if(null!=images&&images.size()!=0){
			AlbumBean mAlbumBeanAll = new AlbumBean("所有图片", images.size(), images, images.get(0).path);
			mAlbumBeanAll.sets.add(0,new ImageBean());
			mAlbumBeans.add(0, mAlbumBeanAll);
		}
		return mAlbumBeans;
	}

	private HashMap<String, ArrayList<ImageBean>> capacity(Cursor mCursor) {

		HashMap<String, ArrayList<ImageBean>> beans = new HashMap<String, ArrayList<ImageBean>>();
		mCursor.moveToLast();
		do{
			String path = mCursor.getString(mCursor
					.getColumnIndex(MediaStore.Images.Media.DATA));

			long size = mCursor.getLong(mCursor
					.getColumnIndex(MediaStore.Images.Media.SIZE));

			String display_name = mCursor.getString(mCursor
					.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME));

			String parentName = new File(path).getParentFile().getName();
			ArrayList<ImageBean> sb;
			if (beans.containsKey(parentName)) {
				sb = beans.get(parentName);
				sb.add(new ImageBean(parentName, size, display_name, path,
						false));
			} else {
				sb = new ArrayList<ImageBean>();
				sb.add(new ImageBean(parentName, size, display_name, path,
						false));
			}
			beans.put(parentName, sb);
		}while(mCursor.moveToPrevious());
		mCursor.close();
		return beans;
	}

}
