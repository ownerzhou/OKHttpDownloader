package com.owner.downloader.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {
    private static Toast sToast;

    public static void toast(Context context, String message) {
        if (sToast == null) {
            sToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        } else {
            sToast.setText(message);
        }
        sToast.show();
    }
}
