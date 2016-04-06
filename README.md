#安卓设备图片选取库
[eclipse版戳我](https://github.com/zhuchunyao164488421/SelectPictureForEclipse) 
##1.此框架的功能-安卓设备图片选取
选取单张或者多张图片，返回图片的绝对路径,有相机拍照，图片的裁剪，预览以及压缩功能
##2.调用此框架需要在你的应用的AndroidManifest.xml中添加相应的权限
    <uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-feature android:name="android.hardware.camera2" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
##3.调用此框架需要在你的应用的AndroidManifest.xml中注册相应的Activity组件
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
 * 注：以上几个Activity请使用竖屏模式，对于ClipPictureActivity请务必加上属性 android:hardwareAccelerated="false"
    针对此Activity禁止使用硬件加速功能，以避免部分安卓机型开启硬件加速后无法绘制该Activity中的特定组件视图
    在你的项目的AndroidManifest.xml中application节点添加以下两行代码：</br>
          
         xmlns:tools=" http://schemas.android.com/tools "
         tools:replace="android:name"

##4.调用方法(框架入口)参考HostActivity：
          Config config = new ConfigBuilder(
						HostActivity.this, // 调用的Activity
						PhotoType.TYPE_MULTIPLE, // 图片选择模式(必填) TYPE_MULTIPLE-多选    TYPE_SINGLE-单选
						12138) // requestCode  此参数在你的Activity的onActivityResult()中作为判断依据
			    .limit(20) // 图片选择上限(此参数只针对多选模式生效)
			    .isCompress(true) // 是否压缩图片
			    .width(800) // 压缩后图片的宽度
			    .build();
			PictureSelectedUtil.selectPicture(config);
			}
##5.参数接收：
     调用者在调用的Activity中重写onActivityResult()方法中接收参数
     可能存在以下类似代码片段：
      @Override
	    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		    super.onActivityResult(requestCode, resultCode, data);
		         switch (requestCode) {
		         case REQUESTCODE:// 此处的REQUESTCODE务必与调用方法(框架入口)中的requestCode保持一致
			     if(null!= data && resultCode == RESULT_OK){
			           // 图片单选模式
				       String filePath=data.getStringExtra(PictureSelectedUtil.IMAGE);
				       // 图片多选模式
				       ArrayList<String>imagesPath = (ArrayList<String>)data.getSerializableExtra(PictureSelectedUtil.IMAGES);
			      }
			      break;
		      }
          }
##6.如何使用：
	dependencies {
	compile 'com.zcy:selectpicture:1.0.0-beta'
	...
	}
##特别感谢：
[鸿洋_](http://blog.csdn.net/lmj623565791)  
[xiaanming](http://blog.csdn.net/xiaanming) <br/>
他们的博客让我学到了很多，欢迎issue，本人新手代码写得乱请多担待和指导！



