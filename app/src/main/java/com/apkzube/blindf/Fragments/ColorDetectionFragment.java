package com.apkzube.blindf.Fragments;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.apkzube.blindf.CameraColorPickerPreview;
import com.apkzube.blindf.Data.ColorItem;
import com.apkzube.blindf.R;
import com.apkzube.blindf.utils.Cameras;

/**
 * A simple {@link Fragment} subclass.
 */
public class ColorDetectionFragment extends Fragment implements
        CameraColorPickerPreview.OnColorSelectedListener, View.OnClickListener{

    //Objects
    View colorDetectionLayout;
    TextView txtColor;
    Context ctx;


    //CameraView
    protected static final String TAG ="ApkZube";
    private static android.hardware.Camera getCameraInstance() {
        android.hardware.Camera c = null;
        try {
            c = android.hardware.Camera.open(android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
        return c;
    }
    protected android.hardware.Camera mCamera;
    protected boolean mIsPortrait;
    protected FrameLayout mPreviewContainer;
    protected CameraColorPickerPreview mCameraPreview;
    protected CameraAsyncTask mCameraAsyncTask;
    protected int mSelectedColor;
    protected View mPickedColorPreviewAnimated;
    protected View mPointerRing;
    protected boolean mIsFlashOn;
    protected String action = null;


    public ColorDetectionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        colorDetectionLayout=inflater.inflate(R.layout.fragment_color_detection, container, false);

        ctx=getContext();

        initViews();

        Intent intent = getActivity().getIntent();
        if (intent != null)
            action = intent.getAction();
        allocation();
        setEvent();

        return colorDetectionLayout;
    }

    private void initViews() {
        mIsPortrait = getResources().getBoolean(R.bool.is_portrait);
        mPreviewContainer = colorDetectionLayout.findViewById(R.id.activity_color_picker_preview_container);

        mPickedColorPreviewAnimated = colorDetectionLayout.findViewById(R.id.activity_color_picker_animated_preview);

        mPointerRing =colorDetectionLayout.findViewById(R.id.activity_color_picker_pointer_ring);


    }

    private void allocation() {

        txtColor=colorDetectionLayout.findViewById(R.id.txtColor);

    }

    private void setEvent() {



    }



    public static ColorDetectionFragment newInstance(String text) {

        ColorDetectionFragment f = new ColorDetectionFragment();
        return f;
    }




    protected boolean isFlashSupported() {
        return ctx.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }
    protected void toggleFlash() {
        if (mCamera != null) {
            final android.hardware.Camera.Parameters parameters = mCamera.getParameters();
            final String flashParameter = mIsFlashOn ? android.hardware.Camera.Parameters.FLASH_MODE_OFF : android.hardware.Camera.Parameters.FLASH_MODE_TORCH;
            parameters.setFlashMode(flashParameter);

            // Set the preview callback to null and stop the preview
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();

            // Change the parameters
            mCamera.setParameters(parameters);

            // Restore the preview callback and re-start the preview
            mCamera.setPreviewCallback(mCameraPreview);
            mCamera.startPreview();

            mIsFlashOn = !mIsFlashOn;
            ActivityCompat.invalidateOptionsMenu(getActivity());
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mCameraPreview) {
            animatePickedColor(mSelectedColor);
        }
    }

    @Override
    public void onColorSelected(int color) {

        mSelectedColor = color;
        mPointerRing.getBackground().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);

    }

    protected void animatePickedColor(int pickedColor) {

        String colorHex= Integer.toHexString(pickedColor);
        txtColor.setText(colorHex);
        txtColor.setBackgroundColor(pickedColor

        );
        Toast.makeText(ctx, "Color : "+pickedColor+" : "+colorHex , Toast.LENGTH_SHORT).show();

    }


    @Override
    public void onResume() {
        super.onResume();

        // Setup the camera asynchronously.
        mCameraAsyncTask = new CameraAsyncTask();
        mCameraAsyncTask.execute();
    }

    @Override
    public void onPause() {
        super.onPause();

        // Cancel the Camera AsyncTask.
        mCameraAsyncTask.cancel(true);

        // Release the camera.
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }

        // Remove the camera preview
        if (mCameraPreview != null) {
            mPreviewContainer.removeView(mCameraPreview);
        }
    }





    private class CameraAsyncTask extends AsyncTask<Void, Void, android.hardware.Camera> {

        /**
         * The {@link android.view.ViewGroup.LayoutParams} used for adding the preview to its container.
         */
        protected FrameLayout.LayoutParams mPreviewParams;

        @Override
        protected android.hardware.Camera doInBackground(Void... params) {
            android.hardware.Camera camera = getCameraInstance();
            if (camera == null) {
                getActivity().finish();
            } else {

                //configure Camera parameters
                android.hardware.Camera.Parameters cameraParameters = camera.getParameters();

                //get optimal camera preview size according to the layout used to display it
                android.hardware.Camera.Size bestSize = Cameras.getBestPreviewSize(
                        cameraParameters.getSupportedPreviewSizes()
                        , mPreviewContainer.getWidth()
                        , mPreviewContainer.getHeight()
                        , mIsPortrait);
                //set optimal camera preview
                cameraParameters.setPreviewSize(bestSize.width, bestSize.height);
                camera.setParameters(cameraParameters);

                //set camera orientation to match with current device orientation
                Cameras.setCameraDisplayOrientation(ctx, camera);

                //get proportional dimension for the layout used to display preview according to the preview size used
                int[] adaptedDimension = Cameras.getProportionalDimension(
                        bestSize
                        , mPreviewContainer.getWidth()
                        , mPreviewContainer.getHeight()
                        , mIsPortrait);

                //set up params for the layout used to display the preview
                mPreviewParams = new FrameLayout.LayoutParams(adaptedDimension[0], adaptedDimension[1]);
                mPreviewParams.gravity = Gravity.CENTER;
            }
            return camera;
        }

        @Override
        protected void onPostExecute(android.hardware.Camera camera) {
            super.onPostExecute(camera);

            // Check if the task is cancelled before trying to use the camera.
            if (!isCancelled()) {
                mCamera = camera;
                if (mCamera == null) {
                    getActivity().finish();
                } else {
                    //set up camera preview
                    mCameraPreview = new CameraColorPickerPreview(ctx, mCamera);
                    mCameraPreview.setOnColorSelectedListener(ColorDetectionFragment.this);
                    mCameraPreview.setOnClickListener(ColorDetectionFragment.this);

                    //add camera preview
                    mPreviewContainer.addView(mCameraPreview, 0, mPreviewParams);
                }
            }
        }

        @Override
        protected void onCancelled(android.hardware.Camera camera) {
            super.onCancelled(camera);
            if (camera != null) {
                camera.release();
            }
        }
    }

}
