package com.rajohns.testcamera;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity {
    private Camera camera;
    private CameraPreview cameraPreview;
    private int currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        camera = getCameraInstance(this, currentCameraId);
        cameraPreview = new CameraPreview(this, camera);

        FrameLayout preview = findViewById(R.id.camera_preview);
        preview.addView(cameraPreview, 0);

        Button swapCameraButton = findViewById(R.id.swap_button);
        swapCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    showCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
                } else {
                    showCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                }
            }
        });

        Button captureButton = findViewById(R.id.capture_button);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.takePicture(null, null, pictureCallback);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (camera == null) {
            showCamera(currentCameraId);
        }
    }

    @Override
    protected void onPause() {
        recycleCamera();
        super.onPause();
    }

    private Camera.PictureCallback pictureCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, final Camera camera) {
            // Camera preview freezes unless we restart it
            camera.stopPreview();
            camera.startPreview();
            
            int rotationDegrees = rotationDegreesBasedOnCamera();
            new RotateImageTask(rotationDegrees, new ImageRotationCallback() {
                @Override
                public void onImageRotated(byte[] bytes) {
                    new SaveImageTask(SaveImageTask.MEDIA_TYPE_IMAGE).execute(bytes);
                }
            }).execute(bytes);
        }
    };

    // Images taken from camera are rotated differently depending on if they came from front
    // facing or rear facing camera.
    private int rotationDegreesBasedOnCamera() {
        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
            return 90;
        } else {
            return 270;
        }
    }

    private void showCamera(int cameraId) {
        if (camera != null) {
            recycleCamera();
        }
        currentCameraId = cameraId;
        camera = getCameraInstance(MainActivity.this, currentCameraId);
        cameraPreview.resetPreview(camera);
    }

    private void recycleCamera() {
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    private static Camera getCameraInstance(Activity activity, int cameraId){
        Camera c = null;
        try {
            c = Camera.open(cameraId);
            setCameraDisplayOrientation(activity, cameraId, c);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return c;
    }

    // https://developer.android.com/reference/android/hardware/Camera.html#setDisplayOrientation(int)
    private static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
        Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }
}
