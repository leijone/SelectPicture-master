package com.zcy.imagelib.crop;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Region.Op;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.zcy.imagelib.tools.Utils;

/**
 * 裁剪区域蒙版（圆形区域）
 * @author zcy
 *
 */
public class ClipBorderView extends View {
	/**
	 * 水平方向与View的间距
	 */
	private int mHorizontalPadding = 10;
	/**
	 * 垂直方向与View的间距
	 */
	private int mVerticalPadding;
	/**
	 * 矩形的宽度
	 */
	private int mWidth;
	/**
	 * 边框的颜色
	 */
	private int mBorderColor=Color.parseColor("#FFFFFF");
	/**
	 * 边框的宽度  单位dp
	 */
	private int mBorderWidth = 2;
	/**
	 * 画笔
	 */
	private Paint mPaint;
	public ClipBorderView(Context context) {
		this(context,null);
		// TODO Auto-generated constructor stub
	}

	public ClipBorderView(Context context, AttributeSet attrs) {
		this(context, attrs,0);
		// TODO Auto-generated constructor stub
	}


	public ClipBorderView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		//计算padding的px
		mPaint=new Paint();
		mPaint.setAntiAlias(true);
		
		TypedArray typedArray=context.obtainStyledAttributes(attrs, Utils.getStyleableArray(context, "clipImageLayout"));
	
		mHorizontalPadding=(int)typedArray.getDimension(Utils.getStyleable(context, "clipImageLayout_horizontalPadding"), 10);
		typedArray.recycle();
		mHorizontalPadding=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 
				mHorizontalPadding, getResources().getDisplayMetrics());
		mBorderWidth=(int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 
				mBorderWidth, getResources().getDisplayMetrics());
	}
	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		// 计算矩形区域的宽度
		mWidth=getWidth()-2*mHorizontalPadding;
		//计算矩形区域距离屏幕垂直方向的间距
		mVerticalPadding=(getHeight()-mWidth)/2;
		float width = getWidth();
		float height = getHeight();
		float radius = (width-2*mHorizontalPadding)/2;
		mPaint.setColor(mBorderColor);
		mPaint.setStrokeWidth(mBorderWidth);
		mPaint.setStyle(Style.STROKE);
		mPaint.setAntiAlias(true);
		canvas.drawCircle(width/2, height/2, radius, mPaint);
		mPaint.setColor(Color.parseColor("#aa000000")); 
		Path circlePath=new Path();
		circlePath.addCircle(width/2, height/2, radius + mBorderWidth/2, Direction.CCW);
		canvas.clipPath(circlePath,Op.DIFFERENCE);
		canvas.save();
		canvas.drawColor(Color.parseColor("#aa000000"));
		canvas.restore();
	}

	public int getHorizontalPadding() {
		return mHorizontalPadding;
	}

	public void setHorizontalPadding(int mHorizontalPadding) {
		this.mHorizontalPadding = mHorizontalPadding;
	}

	public int getVerticalPadding() {
		return mVerticalPadding;
	}

	public void setVerticalPadding(int mVerticalPadding) {
		this.mVerticalPadding = mVerticalPadding;
	}
}
