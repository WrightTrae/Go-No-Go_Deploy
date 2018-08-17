package com.wright.android.t_minus.universal_utils;

import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;

// Trae Wright
// JAV2 - Term Number
// Java File Name
public class DialogUtils {
    public static void showUnexpectedError(@NonNull Context context){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder
                .setTitle("Unexpected Error")
                .setMessage("An unexpected error has occurred please try again later.")
                .setNeutralButton("ok", null).show();
    }
}
