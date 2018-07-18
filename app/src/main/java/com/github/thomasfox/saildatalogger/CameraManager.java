package com.github.thomasfox.saildatalogger;

import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;

import com.github.thomasfox.saildatalogger.logger.Files;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraManager implements Camera.PictureCallback {

    private static final String TAG = "CameraManager";

    private AppCompatActivity activity;

    private Camera camera = null;

    private CameraPreview preview;

    private MediaRecorder mMediaRecorder = new MediaRecorder();

    private boolean recording = false;

    public CameraManager(@NonNull AppCompatActivity activity) {
        this.activity = activity;
        if (hasCameraHardware()) {
            retrieveCameraInstance();
            preview = new CameraPreview(activity, camera, this);
            FrameLayout previewLayout = (FrameLayout) activity.findViewById(R.id.camera_preview);
            previewLayout.addView(preview);
        }
    }

    public void startVideo() {
       if (prepareVideoRecorder()) {
           mMediaRecorder.start();
           recording = true;
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
            parameters.setPictureSize(minSize.width, minSize.height);
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            this.camera.setParameters(parameters);
        }
        catch (Exception e) {
            Log.d(TAG, "Cannot retrieve camera", e);
        }
    }

    public void takePicture() {
        if (camera != null) {
            camera.takePicture(null, null, this);
        }
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {

        File pictureFile = getOutputJpegFile();
        if (pictureFile == null){
            Log.d(TAG, "Error creating media file, check storage permissions");
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    private static File getOutputJpegFile() {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(Files.getStorageDir() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
    }

    private static File getOutputVideoFile() {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(Files.getStorageDir() + File.separator +
                "VID_"+ timeStamp + ".mp4");
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
        mMediaRecorder.setOutputFile(getOutputVideoFile().toString());
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
        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            recording = false;
            mMediaRecorder.reset();
            mMediaRecorder.release();
            mMediaRecorder = null;
            camera.release();
        }
    }
}
