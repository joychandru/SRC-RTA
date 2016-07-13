package com.insurance.app;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import java.io.IOException;
import java.util.List;

public class CameraPreview extends SurfaceView implements Callback {
    private Camera mCamera;
    private SurfaceHolder mHolder;

    public CameraPreview(Context context, Camera camera) {
        super(context);
        this.mCamera = camera;
        this.mHolder = getHolder();
        this.mHolder.addCallback(this);
        this.mHolder.setType(3);
    }

    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        refreshCamera(this.mCamera);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (this.mCamera == null) {
                this.mCamera.setPreviewDisplay(holder);
                this.mCamera.startPreview();
            }
        } catch (IOException e) {
            Log.d("View", "Error setting camera preview: " + e.getMessage());
        }
    }

    public void refreshCamera(Camera camera) {
        if (this.mHolder.getSurface() != null) {
            try {
                this.mCamera.stopPreview();
            } catch (Exception e) {
            }
            setCamera(camera);
            try {
                Parameters params = this.mCamera.getParameters();
                List<String> focusModes = params.getSupportedFocusModes();
                Log.e("CUSTOM", "FOCUS MODE:" + focusModes);
                if (focusModes.contains("continuous-picture")) {
                    params.setFocusMode("continuous-picture");
                    this.mCamera.setParameters(params);
                }
                this.mCamera.setPreviewDisplay(this.mHolder);
                this.mCamera.startPreview();
            } catch (Exception e2) {
                Log.d("View", "Error starting camera preview: " + e2.getMessage());
            }
        }
    }

    public void setCamera(Camera camera) {
        this.mCamera = camera;
    }

    public void surfaceDestroyed(SurfaceHolder arg0) {
    }
}
