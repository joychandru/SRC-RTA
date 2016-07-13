package com.insurance.app;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ZoomControls;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@SuppressLint({"NewApi"})
@TargetApi(9)
public class CameraActivity extends Activity {
    private static String category;
    private static String incidentId;
    private static String prefix;
    private boolean cameraFront;
    private FrameLayout cameraPreview;
    OnClickListener captrureListener;
    private Button capture;
    private Button flash;
    OnClickListener flashListener;
    private boolean isFlashOn;
    private Camera mCamera;
    private PictureCallback mPicture;
    private CameraPreview mPreview;
    File mediaStorageDir;
    private Context myContext;
    private int photoCount;
    private boolean safeToTakePicture;
    private Button switchCamera;
    OnClickListener switchCameraListener;
    private ZoomControls zoomControls;
    OnClickListener zoomInListener;
    OnClickListener zoomOutListener;

    /* renamed from: com.insurance.app.CameraActivity.1 */
    class C00001 implements OnClickListener {
        C00001() {
        }

        public void onClick(View v) {
            CameraActivity.this.zoomCamera(true);
        }
    }

    /* renamed from: com.insurance.app.CameraActivity.2 */
    class C00012 implements OnClickListener {
        C00012() {
        }

        public void onClick(View v) {
            CameraActivity.this.zoomCamera(false);
        }
    }

    /* renamed from: com.insurance.app.CameraActivity.3 */
    class C00023 implements OnClickListener {
        C00023() {
        }

        public void onClick(View v) {
            if (CameraActivity.this.hasFlash()) {
                Parameters p = CameraActivity.this.mCamera.getParameters();
                if (CameraActivity.this.isFlashOn) {
                    Log.i("info", "torch is turn off!");
                    p.setFlashMode("off");
                    CameraActivity.this.mCamera.setParameters(p);
                    CameraActivity.this.isFlashOn = false;
                    return;
                }
                Log.i("info", "torch is turn on!");
                p.setFlashMode("torch");
                CameraActivity.this.mCamera.setParameters(p);
                CameraActivity.this.isFlashOn = true;
                return;
            }
            Toast.makeText(CameraActivity.this.myContext, "No Camera flash present", Toast.LENGTH_LONG);
            Log.d("No Flash", "Camera doesnt have flash");
        }
    }

    /* renamed from: com.insurance.app.CameraActivity.4 */
    class C00034 implements OnClickListener {
        C00034() {
        }

        public void onClick(View v) {
            if (Camera.getNumberOfCameras() > 1) {
                CameraActivity.this.releaseCamera();
                CameraActivity.this.chooseCamera();
                return;
            }
            Toast.makeText(CameraActivity.this.myContext, "No camera", Toast.LENGTH_LONG).show();
        }
    }

    /* renamed from: com.insurance.app.CameraActivity.5 */
    class C00045 implements OnClickListener {
        C00045() {
        }

        public void onClick(View v) {
            if (CameraActivity.this.safeToTakePicture) {
                CameraActivity.this.mCamera.takePicture(null, null, CameraActivity.this.mPicture);
                CameraActivity.this.safeToTakePicture = false;
            }
        }
    }

    /* renamed from: com.insurance.app.CameraActivity.6 */
    class C00056 implements PictureCallback {
        C00056() {
        }

        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = CameraActivity.getOutputMediaFile(CameraActivity.this.mediaStorageDir, CameraActivity.this.photoCount);
            if (pictureFile != null) {
                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.flush();
                    fos.close();
                    Toast.makeText(CameraActivity.this.myContext, "Picture saved: " + pictureFile.getName(), Toast.LENGTH_LONG).show();
                    CameraActivity cameraActivity = CameraActivity.this;
                    cameraActivity.photoCount = cameraActivity.photoCount + 1;
                    MediaScannerConnection.scanFile(CameraActivity.this.myContext, new String[]{pictureFile.getPath()}, null, null);
                } catch (FileNotFoundException e) {
                } catch (IOException e2) {
                }
                CameraActivity.this.mPreview.refreshCamera(CameraActivity.this.mCamera);
                CameraActivity.this.safeToTakePicture = true;
            }
        }
    }

    public CameraActivity() {
        this.cameraFront = false;
        this.safeToTakePicture = false;
        this.isFlashOn = false;
        this.photoCount = 1;
        this.zoomInListener = new C00001();
        this.zoomOutListener = new C00012();
        this.flashListener = new C00023();
        this.switchCameraListener = new C00034();
        this.captrureListener = new C00045();
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_port);
        getWindow().addFlags(PdfWriter.PageModeUseOutlines);
        this.myContext = this;
        getData();
        createFolder();
        initialize();
    }

    private void createFolder() {
        this.mediaStorageDir = new File(Wrapper.APP_FOLDERNAME + File.separator + incidentId + File.separator + category + File.separator);
        Log.e("INSURANCE", "Path:" + this.mediaStorageDir);
        if (!this.mediaStorageDir.exists()) {
            this.mediaStorageDir.mkdirs();
            this.mediaStorageDir.setWritable(true);
        }
        this.photoCount = this.mediaStorageDir.list().length;
        this.photoCount++;
        MediaScannerConnection.scanFile(this.myContext, new String[]{this.mediaStorageDir.getPath()}, null, null);
    }

    private void getData() {
        Bundle bundle = getIntent().getExtras();
        incidentId = bundle.getString("IncidentId");
        category = bundle.getString("Category");
        prefix = bundle.getString("Prefix");
    }

    public void initialize() {
        this.cameraPreview = (FrameLayout) findViewById(R.id.camera_preview);
        this.mPreview = new CameraPreview(this.myContext, this.mCamera);
        this.cameraPreview.addView(this.mPreview);
        this.capture = (Button) findViewById(R.id.button_capture);
        this.capture.setOnClickListener(this.captrureListener);
        this.flash = (Button) findViewById(R.id.button_flash);
        this.flash.setOnClickListener(this.flashListener);
        this.switchCamera = (Button) findViewById(R.id.button_ChangeCamera);
        this.switchCamera.setOnClickListener(this.switchCameraListener);
        this.zoomControls = (ZoomControls) findViewById(R.id.zoomControls);
        this.zoomControls.setIsZoomInEnabled(true);
        this.zoomControls.setIsZoomOutEnabled(true);
        this.zoomControls.setOnZoomInClickListener(this.zoomInListener);
        this.zoomControls.setOnZoomOutClickListener(this.zoomOutListener);
    }

    private boolean hasFlash() {
        if (this.mCamera.getParameters().getSupportedFlashModes() == null) {
            return false;
        }
        return true;
    }

    public void zoomCamera(boolean zoomInOrOut) {
        if (this.mCamera != null) {
            Parameters parameter = this.mCamera.getParameters();
            if (parameter.isZoomSupported()) {
                int MAX_ZOOM = parameter.getMaxZoom();
                int currnetZoom = parameter.getZoom();
                if (zoomInOrOut && currnetZoom < MAX_ZOOM && currnetZoom >= 0) {
                    parameter.setZoom(currnetZoom + 4);
                } else if (!zoomInOrOut && currnetZoom <= MAX_ZOOM && currnetZoom > 0) {
                    parameter.setZoom(currnetZoom - 4);
                }
            } else {
                Toast.makeText(this.myContext, "Zoom Not Avaliable", Toast.LENGTH_LONG).show();
            }
            this.mCamera.setParameters(parameter);
        }
    }

    @SuppressLint({"NewApi"})
    public void chooseCamera() {
        int cameraId;
        if (this.cameraFront) {
            cameraId = findBackFacingCamera();
            if (cameraId >= 0) {
                this.mCamera = Camera.open(cameraId);
                this.mPicture = getPictureCallback();
                this.mPreview.refreshCamera(this.mCamera);
                return;
            }
            return;
        }
        cameraId = findFrontFacingCamera();
        if (cameraId >= 0) {
            this.mCamera = Camera.open(cameraId);
            this.mPicture = getPictureCallback();
            this.mPreview.refreshCamera(this.mCamera);
        }
    }

    private PictureCallback getPictureCallback() {
        return new C00056();
    }

    private int findBackFacingCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == 0) {
                int cameraId = i;
                this.cameraFront = false;
                return cameraId;
            }
        }
        return -1;
    }

    private int findFrontFacingCamera() {
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == 1) {
                int cameraId = i;
                this.cameraFront = true;
                return cameraId;
            }
        }
        return -1;
    }

    private static File getOutputMediaFile(File folder, int count) {
        return new File(folder, prefix + "_" + count + ".jpg");
    }

    private void releaseCamera() {
        if (this.mCamera != null) {
            this.mCamera.release();
            this.mCamera = null;
        }
    }

    public void onResume() {
        super.onResume();
        if (!hasCamera(this.myContext)) {
            Toast.makeText(this.myContext, "Sorry, your phone does not have a camera!", Toast.LENGTH_LONG).show();
            finish();
        }
        if (this.mCamera == null) {
            if (findFrontFacingCamera() < 0) {
                Toast.makeText(this, "No front facing camera found.", Toast.LENGTH_LONG).show();
                this.switchCamera.setVisibility(View.INVISIBLE);
            }
            this.mCamera = Camera.open(findBackFacingCamera());
            this.mPicture = getPictureCallback();
            this.mPreview.refreshCamera(this.mCamera);
            this.safeToTakePicture = true;
        }
    }

    private boolean hasCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature("android.hardware.camera")) {
            return true;
        }
        return false;
    }

    protected void onPause() {
        super.onPause();
        releaseCamera();
    }

    public void onBackPressed() {
        try {
            Parameters p = this.mCamera.getParameters();
            p.setFlashMode("off");
            this.mCamera.setParameters(p);
        } catch (Exception e) {
        }
        int filesCount = 0;
        try {
            filesCount = new File(Wrapper.APP_FOLDERNAME + File.separator + incidentId + File.separator + category + File.separator).list().length;
        } catch (Exception e2) {
        }
        Bundle camData = new Bundle();
        camData.putInt("RESULT", filesCount);
        Intent intent = new Intent();
        intent.putExtras(camData);
        setResult(-1, intent);
        finish();
    }
}
