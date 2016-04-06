package com.zcy.imagelib.adapter;

import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.zcy.imagelib.adapter.AlbumAdapter.OnImageSelectedCountListener;
import com.zcy.imagelib.adapter.AlbumAdapter.OnImageSelectedListener;
import com.zcy.imagelib.entity.AlbumBean;
import com.zcy.imagelib.entity.ImageBean;
import com.zcy.imagelib.tools.Config;
import com.zcy.imagelib.tools.PictureSelectedUtil;
import com.zcy.imagelib.view.MyImageView;
import com.zcy.imagelib.view.MyImageView.OnMeasureListener;

import java.io.File;

public class PicSelectAdapter extends BaseAdapter {

    private Context context;
    private Point mPoint = new Point(0, 0);
    private AlbumBean bean;
    private OnImageSelectedListener onImageSelectedListener;
    private OnImageSelectedCountListener onImageSelectedCountListener;

    public PicSelectAdapter(Context context,
                            OnImageSelectedCountListener onImageSelectedCountListener) {
        this.context = context;
        this.onImageSelectedCountListener = onImageSelectedCountListener;
    }

    public void taggle(AlbumBean bean) {
        this.bean = bean;
        notifyDataSetChanged();
    }

    public void setOnImageSelectedListener(
            OnImageSelectedListener onImageSelectedListener) {
        this.onImageSelectedListener = onImageSelectedListener;
    }

    @Override
    public int getCount() {
        return bean == null || bean.count == 0 ? 0 : bean.count;
    }

    @Override
    public Object getItem(int position) {
        return bean == null ? null : bean.sets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int index = position;
        final ImageBean ib = (ImageBean) getItem(index);
        final ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = View.inflate(context,
                    context.getResources().getIdentifier("the_picture_selection_item", "layout", context.getPackageName()), null);
            viewHolder.mImageView = (MyImageView) convertView
                    .findViewById(context.getResources().getIdentifier("child_image", "id", context.getPackageName()));
            viewHolder.mCheckBox = (CheckBox) convertView
                    .findViewById(context.getResources().getIdentifier("child_checkbox", "id", context.getPackageName()));
            viewHolder.mImageView.setOnMeasureListener(new OnMeasureListener() {
                @Override
                public void onMeasureSize(int width, int height) {
                    mPoint.set(width, height);
                }
            });
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.mImageView.setTag(ib.path);
        if (index == 0) {
            viewHolder.mImageView.setImageResource(context.getResources().getIdentifier("tk_photo", "drawable", context.getPackageName()));
            viewHolder.mCheckBox.setVisibility(View.GONE);
        } else {
            viewHolder.mCheckBox.setVisibility(
                    PictureSelectedUtil.CONFIG.photoType() == Config.PhotoType.TYPE_SINGLE
                            && PictureSelectedUtil.CONFIG.isCrop() ? View.GONE : View.VISIBLE);
            viewHolder.mCheckBox
                    .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {
                            int count = onImageSelectedCountListener
                                    .getImageSelectedCount();
                            if (count == PictureSelectedUtil.CONFIG.limit() && isChecked) {
                                Toast.makeText(context,
                                        "只能选择" + PictureSelectedUtil.CONFIG.limit() + "张图片",
                                        Toast.LENGTH_SHORT).show();
                                viewHolder.mCheckBox.setChecked(ib.isChecked);
                            } else {
                                if (isChecked) {
                                    addAnimation(viewHolder.mCheckBox);
                                }
                                ib.isChecked = isChecked;
                            }
                            onImageSelectedListener.notifyChecked();
                        }
                    });
            if (ib.isChecked) {
                viewHolder.mCheckBox.setChecked(true);
            } else {
                viewHolder.mCheckBox.setChecked(false);
            }
            File file = new File(ib.path);
            Glide.with(context)
                    .load(file)
                    .placeholder(context.getResources().getIdentifier("icon_placeholder", "drawable", context.getPackageName()))
                    .crossFade(500)
                    .error(context.getResources().getIdentifier("icon_failure", "drawable", context.getPackageName()))
                    .into(viewHolder.mImageView);
        }
        return convertView;
    }

    /**
     * @param view
     */
    private void addAnimation(View view) {
        float[] vaules = new float[]{0.5f, 0.6f, 0.7f, 0.8f, 0.9f, 1.0f,
                1.1f, 1.2f, 1.3f, 1.25f, 1.2f, 1.15f, 1.1f, 1.0f};
        AnimatorSet set = new AnimatorSet();
        set.playTogether(ObjectAnimator.ofFloat(view, "scaleX", vaules),
                ObjectAnimator.ofFloat(view, "scaleY", vaules));
        set.setDuration(500);
        set.start();
    }

    private class ViewHolder {
        public MyImageView mImageView;
        public CheckBox mCheckBox;
    }

}
