package com.wright.android.t_minus.universal_utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;

import java.io.File;

public class ImageUtils {

    private static final String STRING_AUTHORITY = "com.wright.android.t_minus.ACCESS_DATA";

    public static Bitmap getRotatedBitmap(Bitmap bitmap, ExifInterface exif){
        int rotate = 0;
        int orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(rotate);
        return Bitmap.createBitmap(bitmap , 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    public static Uri getOutputUri(@NonNull Context context, File imageFile) {
        return (imageFile==null?null: FileProvider.getUriForFile(context,STRING_AUTHORITY,imageFile));
    }
}
