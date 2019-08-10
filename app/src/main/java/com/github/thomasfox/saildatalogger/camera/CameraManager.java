package com.github.thomasfox.saildatalogger.camera;

import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;

import com.github.thomasfox.saildatalogger.R;
import com.github.thomasfox.saildatalogger.logger.Files;

import java.io.IOException;

public class CameraManager {

    private static final String TAG = "saildatalogger";

    private final AppCompatActivity activity;

    private Camera camera = null;

    private CameraPreview preview;

    private MediaRecorder mMediaRecorder = new MediaRecorder();

    private final int trackFileNumber;

    private FrameLayout previewLayout;

    public CameraManager(@NonNull AppCompatActivity activity, int trackFileNumber) {
        this.activity = activity;
        this.trackFileNumber = trackFileNumber;
        if (hasCameraHardware()) {
            retrieveCameraInstance();
            preview = new CameraPreview(activity, camera, this);
            previewLayout = activity.findViewById(R.id.camera_preview);
            previewLayout.addView(preview);
        }
    }

    public void startVideo() {
       if (prepareVideoRecorder()) {
           mMediaRecorder.start();
       }
    }

    private boolean hasCameraHardware() {
        return activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private static int getFrontCameraInstanceId() {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return i;
            }
        }
        return 0;
    }

    private void retrieveCameraInstance() {
        try {
            int cameraInstance = getFrontCameraInstanceId();
            this.camera = Camera.open(cameraInstance);
            this.camera.setDisplayOrientation(90);
            final Camera.Parameters parameters = this.camera.getParameters();
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setJpegQuality(80);
            if (parameters.isZoomSupported()) {
                parameters.setZoom(parameters.getMaxZoom());
            }
            Camera.Size minSize = null;
            for (Camera.Size size : parameters.getSupportedPictureSizes()) {
                if (minSize == null || minSize.width > size.width) {
                    minSize =  size;
                }
            }
            if (minSize == null) {
                Log.w(TAG, "cannot get possible sizes for camera " + cameraInstance);
                return;
            }
            parameters.setPictureSize(minSize.width, minSize.height);
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            this.camera.setParameters(parameters);
        }
        catch (Exception e) {
            Log.d(TAG, "Cannot retrieve camera", e);
        }
    }

    public void close() {
        releaseMediaRecorder();
    }

    private boolean prepareVideoRecorder(){

        camera.unlock();
        mMediaRecorder.setCamera(camera);

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_LOW));
        mMediaRecorder.setOutputFile(Files.getVideoFile(trackFileNumber).toString());
        mMediaRecorder.setPreviewDisplay(preview.getHolder().getSurface());

        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }

    private void releaseMediaRecorder(){
        try {
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder.release();
                mMediaRecorder = null;
                camera.release();
            }
        } catch (RuntimeException e) {
            Log.d(TAG, "Exception releasing MediaRecorder: " + e.getMessage());
        }
        previewLayout.removeView(preview);
    }
}
