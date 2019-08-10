package com.github.thomasfox.saildatalogger.camera;

import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.hardware.Camera;
import android.view.SurfaceView;

import java.io.IOException;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "saildatalogger";

    private final Camera camera;

    private final CameraManager cameraManager;

    public CameraPreview(Context context, Camera camera, CameraManager cameraManger) {
        super(context);
        this.camera = camera;
        this.cameraManager = cameraManger;

        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
            post(new Runnable() { public void run() { cameraManager.startVideo(); } });
        } catch (IOException | RuntimeException e) {
            Log.w(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    }
}
