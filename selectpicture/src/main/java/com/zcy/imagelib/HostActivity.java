package com.zcy.imagelib;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.zcy.imagelib.tools.Config;
import com.zcy.imagelib.tools.Config.ClipType;
import com.zcy.imagelib.tools.Config.ConfigBuilder;
import com.zcy.imagelib.tools.Config.PhotoType;
import com.zcy.imagelib.tools.PictureSelectedUtil;
public class HostActivity extends Activity {


	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(getResources().getIdentifier("publish", "layout", getPackageName()));
		Button btn_go = (Button) findViewById(getResources().getIdentifier("btn_go", "id",getPackageName()));
		btn_go.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Config config = new ConfigBuilder(HostActivity.this, PhotoType.TYPE_MULTIPLE, 12138)
				.isCrop(true)
				.limit(20)
				.clipType(ClipType.CLIP_SQUARE)
				.isCompress(true)
				.width(800)
				.build();
				PictureSelectedUtil.selectPicture(
						config);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 12138 
				&& resultCode == RESULT_OK
				&&null!=data) {
			// 参考PictureSelectedUtil 类前面的注释
			//在你的Activity的此方法中处理相应的代码逻辑 
			// 图片单选模式
			//			String filePath=data.getStringExtra(PictureSelectedUtil.IMAGE);
			// 图片多选模式
			//			ArrayList<String>imagesPath = (ArrayList<String>)data.getSerializableExtra(PictureSelectedUtil.IMAGES);
		} 
		super.onActivityResult(requestCode, resultCode, data);
	}
}
