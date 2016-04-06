package com.zcy.imagelib.tools;

import android.content.Intent;

import com.zcy.imagelib.tools.Config.ClipType;
import com.zcy.imagelib.tools.Config.ConfigType;
import com.zcy.imagelib.tools.Config.PhotoType;
import com.zcy.imagelib.ui.PicSelectActivity;

/*
 *  1.  此框架的功能-安卓手机图片截取
 *      选取单张或者多张图片，返回图片的path,有图片的裁剪，预览以及压缩功能
 *      
 *  2. 调用此框架需要在AndroidManifest.xml中添加相应的权限
 *      <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
 *      <uses-permission android:name="android.permission.CAMERA" />
 *      <uses-feature android:name="android.hardware.camera2" />
 *      <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 *      <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
 *  3.  调用此框架需要在AndroidManifest.xml中注册相应的Activity活动
 *       </activity>
        <activity
            android:name="com.zcy.imagelib.widget.PicSelectActivity"
            android:screenOrientation="portrait" >
        </activity>
        <activity
            android:name="com.zcy.imagelib.camera.CameraActivity"
            android:screenOrientation="portrait" >
        </activity>
        <activity
            android:name="com.zcy.imagelib.widget.ImageBrowserActivity"
            android:screenOrientation="portrait" >
        </activity>
        <activity
            android:name="com.zcy.imagelib.crop.CropPhotoActivity"
            android:screenOrientation="portrait" >
        </activity>
        <activity
            android:name="com.zcy.imagelib.crop.ClipPictureActivity"
            android:hardwareAccelerated="false"
            android:screenOrientation="portrait" >
        </activity>
    注：以上几个Activity请使用竖屏模式，对于ClipPictureActivity请务必加上属性 android:hardwareAccelerated="false"
            针对此Activity禁止使用硬件加速功能，以避免部分安卓机型开启硬件加速后无法绘制该Activity中的特定组件视图
 *   4.  调用方法(框架入口)重载的多个方法调用满足不同需求：
 *       PictureSelectdUtil.selectPicture(config)
 *   5. 参数接收：
 *      调用者在调用的Activity中重写onActivityResult()方法中接收参数
 *      可能存在以下类似代码片段：
 *      @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		    super.onActivityResult(requestCode, resultCode, data);
		         switch (requestCode) {
		         case REQUESTCODE:// 此处的REQUESTCODE务必与调用方法(框架入口)中的requestCode保持一致
			     if(null!=data&&resultCode==RESULT_OK){
			           // 图片单选模式
				       String filePath=data.getStringExtra(PictureSelectedUtil.IMAGE);
				       // 图片多选模式
				       ArrayList<String>imagesPath = (ArrayList<String>)data.getSerializableExtra(PictureSelectedUtil.IMAGES);
			      }
			      break;
		      }
          }
 * 
 * 
 */

/**
 * 图片选取框架调用入口
 * 使用缺陷：使用相机取景时只能每次取一张，不能做到拍照多张一次性返回，用户体验度低(后续扩展完善)
 * @author zcy
 *
 */
public class PictureSelectedUtil{

	public static final String  IMAGES = "imagesPath";

	public static final String IMAGE = "bitmap";

	public static Config CONFIG;

	public static ConfigType CONFIGTYPE;


	 /* 图片选择框架调用入口
	  @param config 配置信息*/



	public static void selectPicture(Config config){
		PictureSelectedUtil.CONFIG = config;
		if(config.photoType() == PhotoType.TYPE_SINGLE
				&& config.isCrop()
				&&config.clipType() == ClipType.CLIP_SQUARE){// 单选裁剪方形区域
			CONFIGTYPE = ConfigType.A;
		}
		else if(config.photoType() == PhotoType.TYPE_SINGLE
				&& config.isCrop()
				&&config.clipType() == ClipType.CLIP_ROUND){// 单选裁剪圆形区域
			CONFIGTYPE = ConfigType.B;
		}
		else if(config.photoType() == PhotoType.TYPE_SINGLE 
				&& ! config.isCrop()
				&& ! config.isCompress()){ // 单选不裁剪也不压缩
			CONFIGTYPE = ConfigType.C;
		}
		else if(config.photoType() == PhotoType.TYPE_SINGLE 
				&& ! config.isCrop()
				&&  config.isCompress()){ // 单选不裁剪但压缩
			CONFIGTYPE = ConfigType.D;
		}
		else if(config.photoType() ==PhotoType.TYPE_MULTIPLE
				&& config.isCompress()){// 多选压缩
			CONFIGTYPE = ConfigType.E;
		}
		else if(config.photoType() ==PhotoType.TYPE_MULTIPLE
				&& !config.isCompress()){ // 多选不压缩
			CONFIGTYPE = ConfigType.F;
		}
		Intent intent = new Intent(config.context(),PicSelectActivity.class);
		config.context().startActivityForResult(intent, config.requestCode());
	}

}
