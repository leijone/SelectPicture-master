package com.zcy.imagelib.tools;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.os.Build;
import android.os.Environment;
import android.util.DisplayMetrics;

import com.zcy.imagelib.entity.ImageBean;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;

@SuppressWarnings("deprecation")
public class Utils {

	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	private static boolean checkCameraFacing(final int facing) {
		if (getSdkVersion() < Build.VERSION_CODES.GINGERBREAD) {
			return false;
		}
		final int cameraCount = Camera.getNumberOfCameras();
		CameraInfo info = new CameraInfo();
		for (int i = 0; i < cameraCount; i++) {
			Camera.getCameraInfo(i, info);
			if (facing == info.facing) {
				return true;
			}
		}
		return false;
	}
	/**
	 * 是否有后置摄像头
	 */
	public static boolean hasBackFacingCamera() {
		final int CAMERA_FACING_BACK = 0;
		return checkCameraFacing(CAMERA_FACING_BACK);
	}

	/**
	 * 是否有前置摄像头
	 */
	public static boolean hasFrontFacingCamera() {
		final int CAMERA_FACING_BACK = 1;
		return checkCameraFacing(CAMERA_FACING_BACK);
	}

	/**
	 * 获取安卓SDK版本
	 */
	public static int getSdkVersion() {
		return Build.VERSION.SDK_INT;
	}


	/**
	 * 获取屏幕宽度和高度，单位为px
	 */
	public static Point getScreenMetrics(Context context){
		DisplayMetrics dm =context.getResources().getDisplayMetrics();
		int w_screen = dm.widthPixels;
		int h_screen = dm.heightPixels;
		return new Point(w_screen, h_screen);
	}

	/**
	 * 判断内存卡是否存在
	 */
	public static boolean isSDcardExist() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 获取应用缓存目录
	 */
	public static File getAPPCacheDir(Context context){
		File cacheDir=null;
		if (Utils.isSDcardExist()){
			cacheDir = context.getExternalCacheDir();
		}else
			cacheDir = context.getCacheDir();
		if (!cacheDir.exists())
			cacheDir.mkdirs();
		return cacheDir;
	}

	/**
	 * 压缩位图
	 */
	public static Bitmap compressBitmap(Bitmap image,int maxkb) {
		//L.showlog(压缩图片);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		while ((baos.toByteArray().length / 1024) > maxkb) { // 循环判断如果压缩后图片是否大于(maxkb)50kb,大于继续压缩
			baos.reset();// 重置baos即清空baos
			options -= 10;// 每次都减少10
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中
			if(options == 10){
				break;
			}
		}
		image.recycle();
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		return bitmap;
	}

	/**
	 * 根据图片需要显示的宽高对图片进行压缩
	 */

	public static Bitmap decodeSampledBitmapFromPath(String pathName, int width, int height) throws FileNotFoundException{
		Bitmap bitmap = null;
		Options options=new Options();
		options.inJustDecodeBounds=true;
		BitmapFactory.decodeStream(new FileInputStream(pathName), null, options);
		options.inSampleSize=Utils.caculateInSampleSize(options, width, height);
		options.inJustDecodeBounds=false;
		bitmap = BitmapFactory.decodeStream(new FileInputStream(pathName), null, options);
		return bitmap ;
	}

	/**
	 * 根据需求的宽高和图片实际的宽高计算SampleSize;
	 * 
	 */
	public static int caculateInSampleSize(Options options, int requiredWidth, int requiredHeight){
		int sampleSize=1;
		int width = options.outWidth;
		int height = options.outHeight;
		if(width>requiredWidth||height>requiredHeight){
			int widthRadio=Math.round(width*1.0f/requiredWidth);
			int heightRadio=Math.round(height*1.0f/requiredHeight);
			sampleSize=Math.max(widthRadio, heightRadio);
		}
		return sampleSize;
	}


	public static Bitmap getBitmap(String srcPath) {
		Options newOpts = new Options();
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		int width = newOpts.outWidth;
		int height = newOpts.outHeight;
		int requestHeight = (PictureSelectedUtil.CONFIG.width() * height) / width;
		newOpts.inJustDecodeBounds = false;
		newOpts.inPreferredConfig = Config.RGB_565;
		bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
		if(PictureSelectedUtil.CONFIG.width() >= width){
			return bitmap;
		}		
		float sx = new BigDecimal(PictureSelectedUtil.CONFIG.width()).divide(new BigDecimal(width), 4, BigDecimal.ROUND_DOWN).floatValue();  
		float sy = new BigDecimal(requestHeight).divide(new BigDecimal(height), 4, BigDecimal.ROUND_DOWN).floatValue();  
		sx = (sx < sy ? sx : sy);sy = sx;// 哪个比例小一点，就用哪个比例  
		Matrix matrix = new Matrix();  
		matrix.postScale(sx, sy);// 调用api中的方法进行压缩，就大功告成了  
		return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);  
	}

	/**
	 * 转换图片成圆形
	 */
	public static  Bitmap toRoundBitmap(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float roundPx;
		float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
		if (width <= height) {
			roundPx = width / 2;

			left = 0;
			top = 0;
			right = width;
			bottom = width;

			height = width;

			dst_left = 0;
			dst_top = 0;
			dst_right = width;
			dst_bottom = width;
		} else {
			roundPx = height / 2;

			float clip = (width - height) / 2;

			left = clip;
			right = width - clip;
			top = 0;
			bottom = height;
			width = height;

			dst_left = 0;
			dst_top = 0;
			dst_right = height;
			dst_bottom = height;
		}

		Bitmap output = Bitmap.createBitmap(width, height, Config.RGB_565);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect src = new Rect((int) left, (int) top, (int) right, (int) bottom);
		final Rect dst = new Rect((int) dst_left, (int) dst_top, (int) dst_right, (int) dst_bottom);
		final RectF rectF = new RectF(dst);

		paint.setAntiAlias(true);// 设置画笔无锯齿

		canvas.drawARGB(0, 0, 0, 0); // 填充整个Canvas


		// 以下有两种方法画圆,drawRounRect和drawCircle
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);// 画圆角矩形，第一个参数为图形显示区域，第二个参数和第三个参数分别是水平圆角半径和垂直圆角半径。
		// canvas.drawCircle(roundPx, roundPx, roundPx, paint);

		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));// 设置两张图片相交时的模式,参考http://trylovecatch.iteye.com/blog/1189452
		canvas.drawBitmap(bitmap, src, dst, paint); // 以Mode.SRC_IN模式合并bitmap和已经draw了的Circle
		return output;
	}


	private static Object getResourceId(Context context,String name, String type) {

		String className = context.getPackageName() +".R";

		try {

			Class<?> cls = Class.forName(className);

			for (Class<?> childClass : cls.getClasses()) {

				String simple = childClass.getSimpleName();

				if (simple.equals(type)) {

					for (Field field : childClass.getFields()) {

						String fieldName = field.getName();

						if (fieldName.equals(name)) {

							System.out.println(fieldName);

							return field.get(null);

						}

					}

				}

			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return null;

	}


	public static int getStyleable(Context context, String StringName) {

		return ((Integer)getResourceId(context, StringName,"styleable")).intValue();

	}


	public static int[] getStyleableArray(Context context,String name) {

		return (int[])getResourceId(context, name,"styleable");

	}

	public static String compressBitmapFromImageBean(ImageBean  imageBean){
		try {
			Bitmap bitmap = getBitmap(imageBean.path);
			File fileFolder = new File(PictureSelectedUtil.CONFIG.savePathCompress());
			if(!fileFolder.getParentFile().exists()){
				fileFolder.mkdirs();
			}
			if(!fileFolder.exists()){
				fileFolder.mkdirs();
			}
			File saveFile = new File(fileFolder, imageBean.displayName);
			if(!saveFile.exists()){
				saveFile.createNewFile();
			}
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(saveFile));
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);
			bos.flush();// 刷新此缓冲区的输出流
			bos.close();// 关闭此输出流并释放与此流有关的所有系统资源
			bitmap.recycle();
			return saveFile.getAbsolutePath();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/** 
	 * 压缩图片 
	 * @param bitmap 源图片 
	 * @param width 想要的宽度 
	 * @param height 想要的高度 
	 * @param isAdjust 是否自动调整尺寸, true图片就不会拉伸，false严格按照你的尺寸压缩 
	 * @return Bitmap 
	 */  
	public static Bitmap reduce(Bitmap bitmap, int width, int height, boolean isAdjust) {  
		// 如果想要的宽度和高度都比源图片小，就不压缩了，直接返回原图  
		if (bitmap.getWidth() < width && bitmap.getHeight() < height) {return bitmap;}  
		// 根据想要的尺寸精确计算压缩比例, 方法详解：public BigDecimal divide(BigDecimal divisor, int scale, int roundingMode);  
		// scale表示要保留的小数位, roundingMode表示如何处理多余的小数位，BigDecimal.ROUND_DOWN表示自动舍弃  
		float sx = new BigDecimal(width).divide(new BigDecimal(bitmap.getWidth()), 4, BigDecimal.ROUND_DOWN).floatValue();  
		float sy = new BigDecimal(height).divide(new BigDecimal(bitmap.getHeight()), 4, BigDecimal.ROUND_DOWN).floatValue();  
		if (isAdjust) {// 如果想自动调整比例，不至于图片会拉伸  
			sx = (sx < sy ? sx : sy);sy = sx;// 哪个比例小一点，就用哪个比例  
		}  
		Matrix matrix = new Matrix();  
		matrix.postScale(sx, sy);// 调用api中的方法进行压缩，就大功告成了  
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);  
	}

	/**
	 * 获取应用名称
	 */
	public static  String getApplicationName(Context context) {
		PackageManager packageManager = null; 
		ApplicationInfo applicationInfo = null; 
		try { 
			packageManager = context.getApplicationContext().getPackageManager(); 
			applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0); 
		} catch (PackageManager.NameNotFoundException e) { 
			applicationInfo = null; 
		} 
		String applicationName = 
				(String) packageManager.getApplicationLabel(applicationInfo); 
		return applicationName; 
	} 

}

