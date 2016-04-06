package com.zcy.imagelib.crop;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.zcy.imagelib.tools.PictureSelectedUtil;
import com.zcy.imagelib.tools.Utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import uk.co.senab.photoview.PhotoView;

/**
 * 裁剪方形区域图片
 * @author zcy
 *
 */
public class CropPhotoActivity extends Activity implements OnTouchListener,
OnClickListener{
	private	PhotoView srcPic;
	private ClipView clipview;
	private TextView tv_rotate;
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;
	private PointF start = new PointF();
	private PointF mid = new PointF();
	float oldDist = 1f;
	private TextView tv_complete;
	private TextView tv_back;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(getResources().getIdentifier("clip_photo", "layout", getPackageName()));
		srcPic = (PhotoView) this.findViewById(getResources().getIdentifier("src_pic", "id", getPackageName()));
		clipview = (ClipView)findViewById(getResources().getIdentifier("clipview", "id", getPackageName()));
		srcPic.setOnTouchListener(this);
		tv_complete = (TextView) this.findViewById(getResources().getIdentifier("tv_complete", "id", getPackageName()));
		tv_complete.setOnClickListener(this);
		tv_rotate = (TextView)findViewById(getResources().getIdentifier("tv_rotate", "id", getPackageName()));
		tv_rotate.setOnClickListener(this);
		tv_back = (TextView) this.findViewById(getResources().getIdentifier("tv_back", "id", getPackageName()));
		tv_back.setOnClickListener(this);
		try {
			Uri mUri = getIntent().getData();
			srcPic.setImageURI(mUri);
		} catch (Exception e) {
			Toast.makeText(this, "载入图片失败", Toast.LENGTH_SHORT).show();
		}
		// 此方法是关键，获取控件刚加载完成时的矩阵保存下来
		srcPic.post(new Runnable() {
			@Override
			public void run() {
				matrix=srcPic.getDisplayMatrix();
			}
		});
	}

	public boolean onTouch(View v, MotionEvent event)
	{
		PhotoView view = (PhotoView) v;
		switch (event.getAction() & MotionEvent.ACTION_MASK)
		{
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());
			mode = DRAG;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			oldDist = spacing(event);
			if (oldDist > 10f)
			{
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
			}
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			break;
		case MotionEvent.ACTION_MOVE:
			if (mode == DRAG)
			{
				matrix.set(savedMatrix);
				matrix.postTranslate(event.getX() - start.x, event.getY()
						- start.y);
			} else if (mode == ZOOM)
			{
				float newDist = spacing(event);
				if (newDist > 10f)
				{
					matrix.set(savedMatrix);
					float scale = newDist / oldDist;
					matrix.postScale(scale, scale, mid.x, mid.y);
				}
			}
			break;
		}
		view.setImageMatrix(matrix);
		return true; 
	}

	@SuppressLint("FloatMath")
	private float spacing(MotionEvent event)
	{
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float)Math.sqrt(x * x + y * y);
	}

	private void midPoint(PointF point, MotionEvent event)
	{
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	@SuppressLint("SimpleDateFormat")
	public void onClick(View v){
		if(v.getId()==getResources().getIdentifier("tv_complete", "id", getPackageName())){
			Bitmap finalBitmap = getBitmap();
			finalBitmap=Utils.reduce(finalBitmap, PictureSelectedUtil.CONFIG.width(), PictureSelectedUtil.CONFIG.width(), true);
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss");// 格式化时间  
				String fileName = format.format(new Date()) + ".png";
				File fileFolder = new File(PictureSelectedUtil.CONFIG.savePathClip());
				if (!fileFolder.getParentFile().exists()) { // 如果目录不存在，则创建
					fileFolder.getParentFile().mkdirs(); 
				}  
				if(!fileFolder.exists()){ 
					fileFolder.mkdir();
				}
				File file2 = new File(fileFolder,fileName);
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file2));
				finalBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
				bos.flush();// 刷新此缓冲区的输出流
				bos.close();
				Intent intent=new Intent();
				intent.putExtra("bitmap", file2.getAbsolutePath());
				setResult(RESULT_OK, intent);
				finish();
			} catch (IOException e) {
				setResult(RESULT_OK, null);
				finish();
				e.printStackTrace();
			}
		}else if(v.getId()==getResources().getIdentifier("tv_back", "id", getPackageName())){
			setResult(RESULT_OK, null);
			Toast.makeText(this, "放弃裁剪", Toast.LENGTH_SHORT).show();
			finish();			
		}else if(v.getId()==getResources().getIdentifier("tv_rotate", "id", getPackageName())){
			Point point=Utils.getScreenMetrics(this);
			matrix.postRotate(90f, point.x/2, point.y/2);
			srcPic.setImageMatrix(matrix);
		}
	}

	private Bitmap getBitmap()
	{
		getBarHeight();
		Bitmap screenShoot = takeScreenShot();
		int width = clipview.getWidth();
		int height = clipview.getHeight();
		Bitmap finalBitmap = Bitmap.createBitmap(screenShoot,
				(width - height / 2) / 2, height / 4 + titleBarHeight + statusBarHeight, height / 2, height / 2);
		return finalBitmap;
	}

	int statusBarHeight = 0;
	int titleBarHeight = 0;

	private void getBarHeight()
	{
		Rect frame = new Rect();
		this.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
		statusBarHeight = frame.top;
		int contenttop = this.getWindow()
				.findViewById(Window.ID_ANDROID_CONTENT).getTop();
		titleBarHeight = contenttop - statusBarHeight;
	}

	private Bitmap takeScreenShot()
	{
		View view = this.getWindow().getDecorView();
		view.setDrawingCacheEnabled(true);
		view.buildDrawingCache();
		return view.getDrawingCache();
	}
}
