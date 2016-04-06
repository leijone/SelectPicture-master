package com.zcy.imagelib.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zcy.imagelib.adapter.AlbumAdapter;
import com.zcy.imagelib.adapter.AlbumAdapter.OnImageSelectedCountListener;
import com.zcy.imagelib.adapter.AlbumAdapter.OnImageSelectedListener;
import com.zcy.imagelib.adapter.PicSelectAdapter;
import com.zcy.imagelib.camera.CameraActivity;
import com.zcy.imagelib.crop.ClipPictureActivity;
import com.zcy.imagelib.crop.CropPhotoActivity;
import com.zcy.imagelib.entity.AlbumBean;
import com.zcy.imagelib.entity.ImageBean;
import com.zcy.imagelib.tools.AlbumHelper;
import com.zcy.imagelib.tools.Config;
import com.zcy.imagelib.tools.DialogUtils;
import com.zcy.imagelib.tools.PictureSelectedUtil;
import com.zcy.imagelib.tools.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PicSelectActivity extends Activity implements
        OnItemClickListener {
    private static final int PHOTO_GRAPH = 0x134452; // 相机取景
    private static final int PHOTO_PREVIEW = 0x4561; // 图片预览
    private static final int PHOTO_CROP = 0x1121213; // 图片裁剪
    private GridView gridView;
    private RelativeLayout topbanner;
    private PicSelectAdapter adapter;
    private TextView album;
    private TextView complete;
    private TextView preView;
    private TextView back;
    private static final int 浏览图片 = 0x1001;
    private static boolean isOpened = false;
    private PopupWindow popWindow;
    private int selectedCount = 0;
    private List<AlbumBean> mAlbumBean = null;
    private AlbumBean mAlbumBeanData = null;
    private String picture;
    private boolean flag = false;
    private AlbumAdapter albumAdapter;
    private ExecutorService threadPool;
    private static final int 多选压缩图片 = 1111;
    private static final int 单选压缩图片 = 222;


    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(getResources().getIdentifier("the_picture_selection",
                "layout", getPackageName()));
        threadPool = Executors.newFixedThreadPool(3);
        initView();
        setListeners();
        setAdapter();
        showPic();
        handleConfig();
    }

    private void setListeners() {
        back.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        preView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                List<ImageBean> imagesList = getSelectedItem();
                if (imagesList != null && imagesList.size() != 0) {
                    Intent intent = new Intent(PicSelectActivity.this, ImageBrowserActivity.class);
                    intent.putExtra("images", (Serializable) imagesList);
                    intent.putExtra("position", 0);
                    intent.putExtra("isdel", true);
                    startActivityForResult(intent, PHOTO_PREVIEW);
                } else {
                    Toast.makeText(PicSelectActivity.this, "至少选择一张图片", Toast.LENGTH_SHORT).show();
                }
            }
        });

        complete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                final List<ImageBean> selecteds = getSelectedItem();
                Intent intent = new Intent();
                if (null == selecteds || selecteds.size() == 0) {
                    Toast.makeText(PicSelectActivity.this, "至少选择一张图片", Toast.LENGTH_SHORT).show();
                    return;
                }
                switch (PictureSelectedUtil.CONFIGTYPE) {
                    case A:

                        break;
                    case B:

                        break;
                    case C:
                        intent.putExtra(PictureSelectedUtil.IMAGE, selecteds.get(0).path);
                        setResult(RESULT_OK, intent);
                        finish();
                        break;
                    case D:
                        DialogUtils.showLoadingDialog(PicSelectActivity.this, "处理图片中");
                        threadPool.execute(new Runnable() {

                            @Override
                            public void run() {
                                ArrayList<String> images = new ArrayList<String>();
                                for (ImageBean bean : selecteds) {
                                    String sacePath = Utils.compressBitmapFromImageBean(bean);
                                    if (null != sacePath) {
                                        images.add(sacePath);
                                    }
                                }
                                Message msg = handler.obtainMessage();
                                msg.what = 单选压缩图片;
                                msg.obj = images.get(0);
                                msg.sendToTarget();
                            }
                        });
                        break;
                    case E:
                        DialogUtils.showLoadingDialog(PicSelectActivity.this, "处理图片中");
                        threadPool.execute(new Runnable() {

                            @Override
                            public void run() {
                                ArrayList<String> images = new ArrayList<String>();
                                for (ImageBean bean : selecteds) {
                                    String sacePath = Utils.compressBitmapFromImageBean(bean);
                                    if (null != sacePath) {
                                        images.add(sacePath);
                                    }
                                }
                                Message msg = handler.obtainMessage();
                                msg.what = 多选压缩图片;
                                msg.obj = images;
                                msg.sendToTarget();
                            }
                        });
                        break;
                    case F:
                        intent.putExtra(PictureSelectedUtil.IMAGES, (Serializable) selecteds);
                        setResult(RESULT_OK, intent);
                        finish();
                        break;
                    default:
                        break;
                }
            }
        });

        album.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!isOpened && popWindow != null) {
                    WindowManager.LayoutParams ll = getWindow().getAttributes();
                    ll.alpha = 0.3f;
                    getWindow().setAttributes(ll);
                    popWindow.showAtLocation(
                            findViewById(android.R.id.content),
                            Gravity.TOP, 0, topbanner.getMeasuredHeight() + 60
                    );

                } else {
                    if (popWindow != null) {
                        popWindow.dismiss();
                    }
                }
            }
        });
        gridView.setOnItemClickListener(this);
    }

    private void setAdapter() {
        adapter = new PicSelectAdapter(PicSelectActivity.this,
                onImageSelectedCountListener);
        gridView.setAdapter(adapter);
        adapter.setOnImageSelectedListener(onImageSelectedListener);
    }

    private void initView() {
        back = (TextView) this.findViewById(getResources().getIdentifier(
                "back", "id", getPackageName()));
        album = (TextView) this.findViewById(getResources().getIdentifier(
                "album", "id", getPackageName()));
        complete = (TextView) this.findViewById(getResources().getIdentifier(
                "complete", "id", getPackageName()));
        preView = (TextView) this.findViewById(getResources().getIdentifier(
                "preview", "id", getPackageName()));
        topbanner = (RelativeLayout) findViewById(getResources().getIdentifier(
                "topbanner", "id", getPackageName()));
        gridView = (GridView) this.findViewById(getResources().getIdentifier(
                "child_grid", "id", getPackageName()));
    }

    /**
     * 图片单选模式下隐藏完成按钮和预览按钮
     */
    private void handleConfig() {
        complete.setVisibility(PictureSelectedUtil.CONFIG.photoType() == Config.PhotoType.TYPE_SINGLE
                && PictureSelectedUtil.CONFIG.isCrop() ? View.GONE : View.VISIBLE);
        preView.setVisibility(PictureSelectedUtil.CONFIG.photoType() == Config.PhotoType.TYPE_SINGLE
                && PictureSelectedUtil.CONFIG.isCrop() ? View.GONE : View.VISIBLE);
    }

    /**
     * 相机拍照
     */
    private void takePhoto() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivityForResult(intent, PHOTO_GRAPH);
        } else {
            Toast.makeText(PicSelectActivity.this, "未检测到SDcard，拍照不可用!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case PHOTO_GRAPH:
                if (null != data && resultCode == RESULT_OK) {
                    Intent intent;
                    Uri mUri = data.getData();
                    String path = mUri.getPath();
                    ImageBean mImageBean = new ImageBean(path);
                    switch (PictureSelectedUtil.CONFIGTYPE) {
                        case A:
                            intent = new Intent(PicSelectActivity.this, CropPhotoActivity.class);
                            intent.setData(mUri);
                            startActivityForResult(intent, PHOTO_CROP);
                            break;
                        case B:
                            intent = new Intent(PicSelectActivity.this, ClipPictureActivity.class);
                            intent.setData(mUri);
                            startActivityForResult(intent, PHOTO_CROP);
                            break;
                        case C:
                            intent = new Intent();
                            intent.putExtra(PictureSelectedUtil.IMAGE, data.getData().getPath());
                            setResult(RESULT_OK, intent);
                            finish();
                            break;
                        case D:
                            intent = new Intent();
                            String Dpath = Utils.compressBitmapFromImageBean(mImageBean);
                            intent.putExtra(PictureSelectedUtil.IMAGE, Dpath);
                            setResult(RESULT_OK, intent);
                            finish();
                            break;
                        case E:
                            这个名字真难取(path, mImageBean);
                            break;
                        case F:
                            这个名字真难取(path, mImageBean);
                            break;
                        default:
                            break;
                    }
                }
                break;

            case PHOTO_PREVIEW:
                if (null != data && resultCode == RESULT_OK) {
                    List<ImageBean> imagesList = (List<ImageBean>) data.getSerializableExtra("M_LIST");
                    refreshSelectedItem(imagesList);
                }
                break;
            case PHOTO_CROP:
                if (null != data && resultCode == RESULT_OK) {
                    String filePath = data.getStringExtra(PictureSelectedUtil.IMAGE);
                    Intent intent = new Intent();
                    intent.putExtra(PictureSelectedUtil.IMAGE, filePath);
                    setResult(RESULT_OK, intent);
                    finish();
                }
                break;
        }
    }

    private void 这个名字真难取(String path, ImageBean mImageBean) {
        DialogUtils.showLoadingDialog(this, "刷新图库中");
        flag = true;
        mImageBean.displayName = path.substring(path.lastIndexOf("/") + 1, path.length());
        picture = mImageBean.displayName;
        new Thread() {
            public void run() {
                try {
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                showPic();
            }

            ;
        }.start();
    }


    private void refreshSelectedItem(List<ImageBean> imagesList) {
        if (imagesList.size() == 0) {
            for (ImageBean mImageBean : mAlbumBeanData.sets) {
                mImageBean.isChecked = false;
            }
        }
        outer:
        for (ImageBean mImageBean : mAlbumBeanData.sets) {
            boolean isSame = true;
            for (ImageBean mReBean : imagesList) {
                if (mImageBean.equals(mReBean)) {
                    isSame = true;
                    continue outer;
                } else {
                    isSame = false;
                }
            }
            if (isSame == false) {
                mImageBean.isChecked = false;
            }
        }
        adapter.taggle(mAlbumBeanData);
    }

    private OnImageSelectedCountListener onImageSelectedCountListener = new OnImageSelectedCountListener() {

        @Override
        public int getImageSelectedCount() {
            return selectedCount;
        }
    };

    private OnImageSelectedListener onImageSelectedListener = new OnImageSelectedListener() {

        @Override
        public void notifyChecked() {
            selectedCount = getSelectedCount();
            complete.setText("完成(" + selectedCount + "/" + PictureSelectedUtil.CONFIG.limit() + ")");
            preView.setText("预览(" + selectedCount + "/" + PictureSelectedUtil.CONFIG.limit() + ")");
        }
    };

    private void showPic() {
        threadPool.execute(new Runnable() {

            @Override
            public void run() {
                Message msg = handler.obtainMessage();
                msg.what = 浏览图片;
                msg.obj = AlbumHelper.newInstance()
                        .getFolders(PicSelectActivity.this);
                msg.sendToTarget();
            }
        });
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @SuppressWarnings("unchecked")
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 浏览图片:
                    List<ImageBean> selectItem = getSelectedItem();
                    if (mAlbumBean != null) {
                        mAlbumBean.clear();
                    }
                    mAlbumBean = (List<AlbumBean>) msg.obj;
                    if (mAlbumBean != null && mAlbumBean.size() != 0) {
                        mAlbumBeanData = mAlbumBean.get(0);
                        if (!flag) {
                            adapter.taggle(mAlbumBeanData);
                            popWindow = showPopWindow();
                        } else {
                            DialogUtils.dismissLoadingDialog();
                            for (ImageBean bean : mAlbumBeanData.sets) {
                                if (bean.displayName != null && bean.displayName.equals(picture)) {
                                    bean.isChecked = true;
                                    break;
                                }
                            }
                            for (ImageBean bean1 : selectItem) {
                                for (ImageBean bean2 : mAlbumBeanData.sets) {
                                    if ((bean1.path).equals(bean2.path)) {
                                        bean2.isChecked = true;
                                    }
                                }
                            }
                        }
                        adapter.taggle(mAlbumBeanData);
                        albumAdapter.setData(mAlbumBean, mAlbumBeanData);
                        flag = false;
                    } else {
                        ArrayList<ImageBean> sets = new ArrayList<ImageBean>();
                        sets.add(new ImageBean());
                        AlbumBean b = new AlbumBean("", 1, sets, "");
                        mAlbumBeanData = b;
                        adapter.taggle(b);
                    }
                    break;
                case 多选压缩图片:
                    DialogUtils.dismissLoadingDialog();
                    Intent intent = new Intent();
                    intent.putExtra(PictureSelectedUtil.IMAGES, (ArrayList<String>) msg.obj);
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
                case 单选压缩图片:
                    DialogUtils.dismissLoadingDialog();
                    Intent in = new Intent();
                    in.putExtra(PictureSelectedUtil.IMAGE, (String) msg.obj);
                    setResult(RESULT_OK, in);
                    finish();
                    break;
                default:

                    break;
            }
        }

        ;
    };


    private int getSelectedCount() {
        int count = 0;
        if (null != mAlbumBeanData.sets) {
            for (ImageBean b : mAlbumBeanData.sets) {
                if (b.isChecked == true) {
                    count++;
                }
            }
        }
        return count;
    }

    private List<ImageBean> getSelectedItem() {
        int count = 0;
        List<ImageBean> beans = new ArrayList<ImageBean>();
        if (null != mAlbumBeanData && null != mAlbumBeanData.sets) {
            for (ImageBean b : mAlbumBeanData.sets) {
                if (b.isChecked == true) {
                    beans.add(b);
                    count++;
                }
                if (count == PictureSelectedUtil.CONFIG.limit()) {
                    break;
                }
            }
        }
        return beans;
    }

    @SuppressWarnings("deprecation")
    private PopupWindow showPopWindow() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(
                getResources().getIdentifier("the_picture_selection_pop",
                        "layout", getPackageName()), null);
        final PopupWindow mPopupWindow = new PopupWindow(view,
                LayoutParams.MATCH_PARENT, gridView.getMeasuredHeight() - 50, true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setAnimationStyle(android.R.style.Animation_InputMethod);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        ListView listView = (ListView) view.findViewById(getResources()
                .getIdentifier("list", "id", getPackageName()));
        albumAdapter = new AlbumAdapter(PicSelectActivity.this);
        albumAdapter.setData(mAlbumBean, mAlbumBeanData);
        listView.setAdapter(albumAdapter);
        mPopupWindow.setOnDismissListener(new OnDismissListener() {

            @Override
            public void onDismiss() {
                WindowManager.LayoutParams ll = getWindow().getAttributes();
                ll.alpha = 1f;
                getWindow().setAttributes(ll);
            }
        });
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                mAlbumBeanData = (AlbumBean) parent.getItemAtPosition(position);
                for (ImageBean mBean : mAlbumBeanData.sets) {
                    mBean.isChecked = false;
                }
                adapter.taggle(mAlbumBeanData);
                album.setText(mAlbumBeanData.folderName);
                albumAdapter.setData(mAlbumBean, mAlbumBeanData);
                mPopupWindow.dismiss();
            }
        });
        return mPopupWindow;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position,
                            long id) {
        if (position == 0) {
            takePhoto();
        } else {
            Intent intent;
            Uri uri = Uri.parse(mAlbumBeanData.sets.get(position).path);
            switch (PictureSelectedUtil.CONFIGTYPE) {
                case A:
                    intent = new Intent(PicSelectActivity.this, CropPhotoActivity.class);
                    intent.setData(uri);
                    startActivityForResult(intent, PHOTO_CROP);
                    break;
                case B:
                    intent = new Intent(PicSelectActivity.this, ClipPictureActivity.class);
                    intent.setData(uri);
                    startActivityForResult(intent, PHOTO_CROP);
                    break;
                case C:
                    checkCheckedAndLimit(position);
                    break;
                case D:
                    checkCheckedAndLimit(position);
                    break;
                case E:
                    checkCheckedAndLimit(position);
                    break;
                case F:
                    checkCheckedAndLimit(position);
                    break;
                default:
                    break;
            }
        }
    }

    private void checkCheckedAndLimit(int position) {
        if (mAlbumBeanData.sets.get(position).isChecked == true) {
            mAlbumBeanData.sets.get(position).isChecked = false;
        } else {
            if (selectedCount == PictureSelectedUtil.CONFIG.limit()) {
                Toast.makeText(PicSelectActivity.this, "只能选择" + PictureSelectedUtil.CONFIG.limit() + "张图片", Toast.LENGTH_SHORT).show();
            } else {
                mAlbumBeanData.sets.get(position).isChecked = true;
            }
        }
        adapter.taggle(mAlbumBeanData);
    }
}
