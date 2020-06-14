package com.owner.downloader.rxjava.handler;

import android.os.Handler;
import android.os.Looper;

public class MainHandler implements IHandler {
    private Handler handler;

    @Override
    public void init() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
    }

    @Override
    public void submit(Runnable runnable) {
        handler.post(runnable);
    }
}
