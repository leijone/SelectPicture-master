package com.zcy.imagelib.camera;


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Size;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.zcy.imagelib.tools.Utils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

@SuppressWarnings("deprecation")
public class CameraFragment extends Fragment implements SurfaceHolder.Callback, Camera.PictureCallback {

    public static final String TAG = CameraFragment.class.getSimpleName();
    public static final String CAMERA_ID_KEY = "camera_id";
    public static final String CAMERA_FLASH_KEY = "flash_mode";
    public static final String PREVIEW_HEIGHT_KEY = "preview_height";

    private static final int PICTURE_SIZE_MAX_WIDTH = 1280;
    private static final int PREVIEW_SIZE_MAX_WIDTH = 1280;

    private int mCameraID;
    private String mFlashMode;
    private Camera mCamera;
    private SquareCameraPreview mPreviewView;
    private SurfaceHolder mSurfaceHolder;

    private int mDisplayOrientation;
    private int mLayoutOrientation;

    private int mCoverHeight;
    private int mPreviewHeight;

    private CameraOrientationListener mOrientationListener;
    private SeekBar mZoomBar;
    private RelativeLayout relativeLayout_zoom;

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    public CameraFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mOrientationListener = new CameraOrientationListener(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(getResources().getIdentifier("fragment_camera", "layout", getActivity().getPackageName()), container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            mCameraID = getBackCameraID();
            mFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
        } else {
            mCameraID = savedInstanceState.getInt(CAMERA_ID_KEY);
            mFlashMode = savedInstanceState.getString(CAMERA_FLASH_KEY);
            mPreviewHeight = savedInstanceState.getInt(PREVIEW_HEIGHT_KEY);
        }

        mOrientationListener.enable();

        mPreviewView = (SquareCameraPreview) view.findViewById(getResources().getIdentifier("camera_preview_view", "id", getActivity().getPackageName())
        );
        mPreviewView.getHolder().addCallback(CameraFragment.this);

        final View topCoverView = view.findViewById(getResources().getIdentifier("cover_top_view", "id", getActivity().getPackageName()));
        final View btnCoverView = view.findViewById(getResources().getIdentifier("cover_bottom_view", "id", getActivity().getPackageName()));
        mZoomBar = (SeekBar) view.findViewById(getResources().getIdentifier("ZoomBar", "id", getActivity().getPackageName()));
        relativeLayout_zoom = (RelativeLayout) view.findViewById(getResources().getIdentifier("relativeLayout_zoom", "id", getActivity().getPackageName()));
        mZoomBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                setZoom(progress);
            }
        });
        if (mCoverHeight == 0) {
            ViewTreeObserver observer = mPreviewView.getViewTreeObserver();
            observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @SuppressLint("NewApi")
                @Override
                public void onGlobalLayout() {
                    int width = mPreviewView.getWidth();
                    mPreviewHeight = mPreviewView.getHeight();
                    mCoverHeight = (mPreviewHeight - width) / 2;
                    Log.d(TAG, "preview width " + width + " height " + mPreviewHeight);
                    //					topCoverView.getLayoutParams().height = mCoverHeight;
                    //					btnCoverView.getLayoutParams().height = mCoverHeight;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mPreviewView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    } else {
                        mPreviewView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                    }
                }
            });
        } else {
            //			topCoverView.getLayoutParams().height = mCoverHeight;
            //			btnCoverView.getLayoutParams().height = mCoverHeight;
        }


        final ImageView swapCameraBtn = (ImageView) view.findViewById(getResources().getIdentifier("change_camera", "id", getActivity().getPackageName()));
        swapCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCameraID == CameraInfo.CAMERA_FACING_FRONT) {
                    mCameraID = getBackCameraID();
                } else {
                    mCameraID = getFrontCameraID();
                }
                restartPreview();
            }
        });
        swapCameraBtn.setVisibility(Utils.hasFrontFacingCamera() ? View.VISIBLE : View.INVISIBLE);
        swapCameraBtn.setClickable(Utils.hasFrontFacingCamera() ? true : false);
        final View changeCameraFlashModeBtn = view.findViewById(getResources().getIdentifier("flash", "id", getActivity().getPackageName()));
        final TextView autoFlashIcon = (TextView) view.findViewById(getResources().getIdentifier("auto_flash_icon", "id", getActivity().getPackageName()));
        changeCameraFlashModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_AUTO)) {
                    mFlashMode = Camera.Parameters.FLASH_MODE_ON;
                    autoFlashIcon.setText("On");
                } else if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_ON)) {
                    mFlashMode = Camera.Parameters.FLASH_MODE_OFF;
                    autoFlashIcon.setText("Off");
                } else if (mFlashMode.equalsIgnoreCase(Camera.Parameters.FLASH_MODE_OFF)) {
                    mFlashMode = Camera.Parameters.FLASH_MODE_AUTO;
                    autoFlashIcon.setText("Auto");
                }

                setupCamera();
            }
        });


        final ImageView takePhotoBtn = (ImageView) view.findViewById(getResources().getIdentifier("capture_image_button", "id", getActivity().getPackageName()));
        takePhotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhotoBtn.setEnabled(false);
                takePicture();
                Observable.just("")
                        .delaySubscription(1500, TimeUnit.MILLISECONDS)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<String>() {
                            @Override
                            public void call(String s) {
                                takePhotoBtn.setEnabled(true);
                            }
                        });
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(CAMERA_ID_KEY, mCameraID);
        outState.putString(CAMERA_FLASH_KEY, mFlashMode);
        outState.putInt(PREVIEW_HEIGHT_KEY, mPreviewHeight);
        super.onSaveInstanceState(outState);
    }

    private void getCamera(int cameraID) {
        Log.d(TAG, "get camera with id " + cameraID);
        try {
            mCamera = Camera.open(cameraID);
            mPreviewView.setCamera(mCamera);
            mZoomBar.setMax(mCamera.getParameters().getMaxZoom());
        } catch (Exception e) {
            Log.d(TAG, "Can't open camera with id " + cameraID);
            e.printStackTrace();
        }
    }

    /**
     * Start the camera preview
     */
    private void startCameraPreview() {
        determineDisplayOrientation();
        setupCamera();

        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
            mCamera.startPreview();
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams
                    (FrameLayout.LayoutParams.WRAP_CONTENT, mPreviewHeight);
            relativeLayout_zoom.setLayoutParams(params);
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams
                    (RelativeLayout.LayoutParams.WRAP_CONTENT, mPreviewHeight / 2);
            param.addRule(RelativeLayout.CENTER_VERTICAL);
            mZoomBar.setLayoutParams(param);
        } catch (IOException e) {
            Log.d(TAG, "Can't start camera preview due to IOException " + e);
            e.printStackTrace();
        }
    }

    /**
     * Stop the camera preview
     */
    private void stopCameraPreview() {
        // Nulls out callbacks, stops face detection
        mCamera.stopPreview();
        mPreviewView.setCamera(null);
    }

    /**
     * Determine the current display orientation and rotate the camera preview
     * accordingly
     */
    private void determineDisplayOrientation() {
        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(mCameraID, cameraInfo);

        int rotation = getActivity().getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: {
                degrees = 0;
                break;
            }
            case Surface.ROTATION_90: {
                degrees = 90;
                break;
            }
            case Surface.ROTATION_180: {
                degrees = 180;
                break;
            }
            case Surface.ROTATION_270: {
                degrees = 270;
                break;
            }
        }

        int displayOrientation;

        // Camera direction
        if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            // Orientation is angle of rotation when facing the camera for
            // the camera image to match the natural orientation of the device
            displayOrientation = (cameraInfo.orientation + degrees) % 360;
            displayOrientation = (360 - displayOrientation) % 360;
        } else {
            displayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        }

        mDisplayOrientation = (cameraInfo.orientation - degrees + 360) % 360;
        mLayoutOrientation = degrees;
        if (null != mCamera) {
            mCamera.setDisplayOrientation(displayOrientation);
        }
    }

    /**
     * Setup the camera parameters
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @SuppressWarnings("deprecation")
    private void setupCamera() {
        // Never keep a global parameters
        Camera.Parameters parameters = mCamera.getParameters();

        Size bestPreviewSize = determineBestPreviewSize(parameters);
        Size bestPictureSize = determineBestPictureSize(parameters);

        parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);
        parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);

        parameters.setPictureFormat(PixelFormat.JPEG);
        parameters.setJpegQuality(100);

        // Set continuous picture focus, if it's supported
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        final View changeCameraFlashModeBtn = getView().findViewById(getResources().getIdentifier("flash", "id", getActivity().getPackageName()));
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes != null && flashModes.contains(mFlashMode)) {
            parameters.setFlashMode(mFlashMode);
            changeCameraFlashModeBtn.setVisibility(View.VISIBLE);
        } else {
            changeCameraFlashModeBtn.setVisibility(View.INVISIBLE);
        }

        // Lock in the changes
        mCamera.setParameters(parameters);
    }

    private Size determineBestPreviewSize(Camera.Parameters parameters) {
        return determineBestSize(parameters.getSupportedPreviewSizes(), PREVIEW_SIZE_MAX_WIDTH);
    }

    private Size determineBestPictureSize(Camera.Parameters parameters) {
        return determineBestSize(parameters.getSupportedPictureSizes(), PICTURE_SIZE_MAX_WIDTH);
    }

    private Size determineBestSize(List<Size> sizes, int widthThreshold) {
        Size bestSize = null;
        Size size;
        int numOfSizes = sizes.size();
        for (int i = 0; i < numOfSizes; i++) {
            size = sizes.get(i);
            boolean isDesireRatio = (size.width / 4) == (size.height / 3);
            boolean isBetterSize = (bestSize == null) || size.width > bestSize.width;

            if (isDesireRatio && isBetterSize) {
                bestSize = size;
            }
        }

        if (bestSize == null) {
            Log.d(TAG, "cannot find the best camera size");
            return sizes.get(sizes.size() - 1);
        }

        return bestSize;
    }

    private void restartPreview() {
        stopCameraPreview();
        mCamera.release();

        getCamera(mCameraID);
        startCameraPreview();
    }

    private int getFrontCameraID() {
        PackageManager pm = getActivity().getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT)) {
            return CameraInfo.CAMERA_FACING_FRONT;
        }

        return getBackCameraID();
    }

    private int getBackCameraID() {
        return CameraInfo.CAMERA_FACING_BACK;
    }

    /**
     * Take a picture
     */
    private void takePicture() {
        mOrientationListener.rememberOrientation();

        // Shutter callback occurs after the image is captured. This can
        // be used to trigger a sound to let the user know that image is taken
        Camera.ShutterCallback shutterCallback = null;

        // Raw callback occurs when the raw image data is available
        Camera.PictureCallback raw = null;

        // postView callback occurs when a scaled, fully processed
        // postView image is available.
        Camera.PictureCallback postView = null;

        // jpeg callback occurs when the compressed image is available
        mCamera.takePicture(shutterCallback, raw, postView, this);
    }

    @Override
    public void onStop() {
        mOrientationListener.disable();

        // stop the preview
        stopCameraPreview();
        mCamera.release();
        super.onStop();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mSurfaceHolder = holder;

        getCamera(mCameraID);
        startCameraPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // The surface is destroyed with the visibility of the SurfaceView is set to View.Invisible
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        switch (requestCode) {
            case 1:
                Uri imageUri = data.getData();
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * A picture has been taken
     *
     * @param data
     * @param camera
     */
    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        int rotation = (
                mDisplayOrientation
                //				+ mOrientationListener.getRememberedNormalOrientation()
                //				+ mLayoutOrientation
        ) % 360;
        getFragmentManager()
                .beginTransaction()
                .replace(
                        getResources().getIdentifier("fragment_container", "id", getActivity().getPackageName()),
                        EditSavePhotoFragment.newInstance(data, rotation, 0, mPreviewHeight),
                        EditSavePhotoFragment.TAG)
                .addToBackStack(null)
                .commit();
    }

    /**
     * When orientation changes, onOrientationChanged(int) of the listener will be called
     */
    private static class CameraOrientationListener extends OrientationEventListener {

        private int mCurrentNormalizedOrientation;
        private int mRememberedNormalOrientation;

        public CameraOrientationListener(Context context) {
            super(context, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            if (orientation != ORIENTATION_UNKNOWN) {
                mCurrentNormalizedOrientation = normalize(orientation);
            }
        }

        private int normalize(int degrees) {
            if (degrees > 315 || degrees <= 45) {
                return 0;
            }

            if (degrees > 45 && degrees <= 135) {
                return 90;
            }

            if (degrees > 135 && degrees <= 225) {
                return 180;
            }

            if (degrees > 225 && degrees <= 315) {
                return 270;
            }

            throw new RuntimeException("The physics as we know them are no more. Watch out for anomalies.");
        }

        public void rememberOrientation() {
            mRememberedNormalOrientation = mCurrentNormalizedOrientation;
        }

        public int getRememberedNormalOrientation() {
            return mRememberedNormalOrientation;
        }
    }

    /**
     * 设置相机焦距
     **/
    private void setZoom(int mValue) {
        try {
            if (null != mCamera) {
                Camera.Parameters mParams = mCamera.getParameters();
                mParams.setZoom(mValue);
                mCamera.setParameters(mParams);
            }
        } catch (Exception e) {

        }
    }
}
