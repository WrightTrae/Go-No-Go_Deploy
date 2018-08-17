package com.wright.android.t_minus.universal_utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.Type;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.ViewTreeObserver;

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

    public static Bitmap getBlurredBackground(RenderScript rs, @NonNull View view, int repeat) {
        Bitmap viewBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(viewBitmap);
        view.layout(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
        view.draw(c);

        int width = viewBitmap.getWidth();
        int height = viewBitmap.getHeight();

        // Create allocation type
        Type bitmapType = new Type.Builder(rs, Element.RGBA_8888(rs))
                .setX(width)
                .setY(height)
                .setMipmaps(false) // We are using MipmapControl.MIPMAP_NONE
                .create();

        // Create allocation
        Allocation allocation = Allocation.createTyped(rs, bitmapType);

        // Create blur script
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        // Copy data to allocation
        allocation.copyFrom(viewBitmap);

        // set blur script input
        blurScript.setInput(allocation);

        // invoke the script to blur
        blurScript.forEach(allocation);

        // Repeat the blur for extra effect
        for (int i=0; i<repeat; i++) {
            blurScript.forEach(allocation);
        }

        // copy data back to the bitmap
        allocation.copyTo(viewBitmap);

        // release memory
        allocation.destroy();
        blurScript.destroy();
        allocation = null;
        blurScript = null;

        return viewBitmap;
    }
}
