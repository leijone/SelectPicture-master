package com.zcy.imagelib.tools;

import android.app.Activity;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;

public class Config implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * 图片最多选取数目，最多99张（图片超过99张按99张处理）
     */
    private final int limit;

    public int limit() {
        return this.limit;
    }

    /**
     * 裁剪以后的图片、压缩后的图片保存的父目录
     */
    private final String savePathParent;

    public String savePathParent() {
        return this.savePathParent;
    }

    /**
     * 拍照图片保存目录
     */
    private final String savePathPicture;

    public String savePathPicture() {
        return this.savePathPicture;
    }

    /**
     * 裁剪以后图片保存的目录
     */
    private final String savePathClip;

    public String savePathClip() {
        return this.savePathClip;
    }

    /**
     * 压缩后图片保存的目录
     */
    private final String savePathCompress;

    public String savePathCompress() {
        return this.savePathCompress;
    }

    /**
     * 图片选择模式（PhotoType.TYPE_SINGLE 单张图片，PhotoType.TYPE_MULTIPLE 多张图片）
     */
    private final PhotoType photoType;

    public PhotoType photoType() {
        return this.photoType;
    }

    /**
     * 图片裁剪的模式（ClipType.CLIP_ROUND 裁剪圆形区域，ClipType.CLIP_SQUARE 裁剪方形区域）
     */
    private final ClipType clipType;

    public ClipType clipType() {
        return this.clipType;
    }

    /**
     * Activity请求回调参数
     */
    private final int requestCode;

    public int requestCode() {
        return this.requestCode;
    }

    /**
     * 是否裁剪(默认不裁剪)
     */
    private final boolean isCrop;

    public boolean isCrop() {
        return this.isCrop;
    }

    /**
     * 选取图片后是否压缩(默认不压缩)
     */
    private final boolean isCompress;

    public boolean isCompress() {
        return this.isCompress;
    }

    /**
     * 压缩后图片的宽度
     */
    private final int width;

    public int width() {
        return this.width;
    }

    /**
     * 应用上下文
     */
    private final Activity activity;

    public Activity context() {
        return this.activity;
    }

    /**
     * 图片选择模式（单选或者多选）
     *
     */
    public enum PhotoType {
        /**
         * 选取单张图片
         */
        TYPE_SINGLE,// 选取单张图片
        /**
         * 选取多张图片
         */
        TYPE_MULTIPLE// 选取多张图片
    }

    /**
     * 图片裁剪模式（方形或者圆形）
     *
     */
    public enum ClipType {
        /**
         * 裁剪圆形区域
         */
        CLIP_ROUND,// 裁剪圆形区域
        /**
         * 裁剪方形区域
         */
        CLIP_SQUARE // 裁剪方形区域
    }

    private Config(int limit, String savePathParent, String savePathPicture, String savePathClip,
                   String savePathCompress, PhotoType photoType, ClipType clipType,
                   int requestCode, boolean isCrop, boolean isCompress, int width, Activity activity) {
        this.limit = limit;
        this.savePathParent = savePathParent;
        this.savePathPicture = savePathPicture;
        this.savePathClip = savePathClip;
        this.savePathCompress = savePathCompress;
        this.photoType = photoType;
        this.clipType = clipType;
        this.requestCode = requestCode;
        this.isCrop = isCrop;
        this.isCompress = isCompress;
        this.width = width;
        this.activity = activity;
    }

    public static class ConfigBuilder {
        private int limit = 99;
        private Activity activity;
        private final PhotoType photoType;
        private final int requestCode;
        private String savePathParent;
        private String savePathClip;
        private String savePathCompress;
        private String savePathPicture;
        private ClipType clipType = ClipType.CLIP_ROUND;
        private boolean isCrop = false;
        private boolean isCompress = false;
        private int width = 400;

        /**
         * ConfigBuilder构造方法
         *
         * @param activity    调起框架的Activity
         * @param photoType   图片选取方式，TYPE_SINGLE-选取单张图片
         *                    TYPE_MULTIPLE-选取多张图片
         * @param requestCode activity调起的请求码
         */
        public ConfigBuilder(Activity activity, PhotoType photoType, int requestCode) {
            this.activity = activity;
            this.photoType = photoType;
            this.requestCode = requestCode;
            try {
                savePathParent(Utils.getAPPCacheDir(activity).getAbsolutePath());
                File savePathClip = new File(savePathParent + "/CROP");
                if (!savePathClip.exists()) {
                    savePathClip.mkdirs();
                }
                this.savePathClip = savePathParent + "/CROP";
                File savePathCompress = new File(savePathParent + "/COMPRESS");
                if (!savePathCompress.exists()) {
                    savePathCompress.mkdirs();
                }
                this.savePathCompress = savePathParent + "/COMPRESS";
                File savePathPicture = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + Utils.getApplicationName(activity));
                if (!savePathPicture.exists()) {
                    savePathPicture.mkdirs();
                }
                this.savePathPicture = Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/" + Utils.getApplicationName(activity);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        /**
         * 设置图片保存的公共父目录
         *
         * @param savePathParent 图片保存的公共父目录
         *                       可选参数，不设置默认在应用包名下 cache/目录
         *                       即应用缓存目录，此目录会随应用下载 删除
         * @return ConfigBuilder
         * @throws FileNotFoundException 非文件夹路径抛出此异常
         */
        public ConfigBuilder savePathParent(String savePathParent) throws FileNotFoundException {
            if (!new File(savePathParent).isDirectory()) {
                throw new FileNotFoundException(savePathParent + "  is must not be a directory");
            } else {
                this.savePathParent = savePathParent;
            }
            return this;
        }

        /**
         * 设置裁剪后图片保存的目录
         *
         * @param savePathClip 裁剪后图片保存的目录
         *                     可选参数，不设置默认在应用包名下 cache/CROP目录
         *                     此目录在savePathParent目录之下
         * @return ConfigBuilder
         * @throws FileNotFoundException 非文件夹路径抛出此异常
         */
        public ConfigBuilder savePathClip(String savePathClip) throws FileNotFoundException {
            if (!new File(savePathClip).isDirectory()) {
                throw new FileNotFoundException(savePathClip + "  is must not be a directory");
            } else {
                this.savePathClip = savePathClip;
            }
            return this;
        }

        /**
         * 设置图片压缩后保存的目录
         *
         * @param savePathCompress 图片压缩后保存的目录
         *                         可选参数，不设置默认在应用包名下 cache/COMPRESS目录
         *                         此目录在savePathParent目录之下
         * @return ConfigBuilder
         * @throws FileNotFoundException 非文件夹路径抛出此异常
         */
        public ConfigBuilder savePathCompress(String savePathCompress) throws FileNotFoundException {
            if (!new File(savePathCompress).isDirectory()) {
                throw new FileNotFoundException(savePathCompress + "  is must not be a directory");
            } else {
                this.savePathCompress = savePathCompress;
            }
            return this;
        }

        /**
         * 设置相机拍照保存的图片路径
         *
         * @param savePathPicture 相机拍照保存的图片路径
         *                        可选参数，不设置默认 外存PICTURES 目录
         * @return ConfigBuilder
         * @throws FileNotFoundException 非文件夹路径抛出此异常
         */
        public ConfigBuilder savePathPicture(String savePathPicture) throws FileNotFoundException {
            if (!new File(savePathCompress).isDirectory()) {
                throw new FileNotFoundException(savePathPicture + "  is must not be a directory");
            } else {
                this.savePathPicture = savePathPicture;
            }
            return this;
        }

        /**
         * 设置图片裁剪方式
         * @param clipType CLIP_ROUND-裁剪圆形区域
         *                 CLIP_SQUARE-裁剪方形区域
         * @return ConfigBuilder
         */
        public ConfigBuilder clipType(ClipType clipType) {
            this.clipType = clipType;
            return this;
        }


        /*  设置图片是否裁剪   true - 裁剪  false - 不裁剪
          此参数只针对图片选择模式为单选图片时有效，即PhotoType = TYPE_SINGLE 时生效*/

        public ConfigBuilder isCrop(boolean isCrop) {
            this.isCrop = isCrop;
            return this;
        }


         /*设置图片是否压缩  true - 压缩 false - 不压缩*/

        public ConfigBuilder isCompress(boolean isCompress) {
            this.isCompress = isCompress;
            return this;
        }

       /* 设置图片压缩以后期望宽度*/

        public ConfigBuilder width(int width) {
            this.width = width;
            return this;
        }



        /* 设置多选图片时图片个数
         此参数只在PhotoType == TYPE_MULTIPLE 时生效*/

        public ConfigBuilder limit(int limit) {
            if (limit > 99) {
                this.limit = 99;
            } else {
                this.limit = limit;
            }
            return this;
        }


         /*生成配置*/

        public Config build() {
            if (photoType == PhotoType.TYPE_SINGLE) {
                limit = 1;
            }
            return new Config(
                    limit,
                    savePathParent,
                    savePathPicture,
                    savePathClip,
                    savePathCompress,
                    photoType,
                    clipType,
                    requestCode,
                    isCrop,
                    isCompress,
                    width,
                    activity);
        }
    }


    /*图片选取类型*/

    public enum ConfigType {
        /**
         * 单选裁剪方形区域
         */
        A,
        /**
         * 单选裁剪圆形区域
         */
        B,
        /**
         * 单选不裁剪也不压缩
         */
        C,
        /**
         * 单选不裁剪但压缩
         */
        D,
        /**
         * 多选压缩
         */
        E,
        /**
         * 多选不压缩
         */
        F
    }

}
