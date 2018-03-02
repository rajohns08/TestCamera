package com.rajohns.testcamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RotateImageTask extends AsyncTask<byte[], Void, byte[]> {
    private int rotationDegrees;
    private ImageRotationCallback imageRotationCallback;

    public RotateImageTask(int rotationDegrees, ImageRotationCallback imageRotationCallback) {
        this.rotationDegrees = rotationDegrees;
        this.imageRotationCallback = imageRotationCallback;
    }

    @Override
    protected byte[] doInBackground(byte[]... bytes) {
        Log.d("tagzzz", "rotate doinbackground");
        byte[] originalImageBytes = bytes[0];

        Bitmap originalBitmap = BitmapFactory.decodeByteArray(originalImageBytes, 0, originalImageBytes.length);
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        Bitmap rotatedBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        rotatedBitmap.recycle();
        byte[] rotatedImageBytes = byteArrayOutputStream.toByteArray();

        try {
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return rotatedImageBytes;
    }

    @Override
    protected void onPostExecute(byte[] bytes) {
        imageRotationCallback.onImageRotated(bytes);
    }
}
