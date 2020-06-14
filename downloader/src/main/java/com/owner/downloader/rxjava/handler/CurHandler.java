package com.owner.downloader.rxjava.handler;

import android.os.Handler;
import android.os.Looper;

public class CurHandler implements IHandler {
    private Handler handler;

    @Override
    public void init() {
        if (handler == null) {
            handler = new Handler(Looper.myLooper());
        }
    }

    @Override
    public void submit(Runnable runnable) {
        handler.post(runnable);
    }
}
