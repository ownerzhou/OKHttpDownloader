package com.owner.downloader.rxjava.handler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SubHandler implements IHandler {
    private static ExecutorService service;

    @Override
    public void init() {
        if (service == null) {
            service = Executors.newSingleThreadExecutor();
        }
    }

    @Override
    public void submit(Runnable runnable) {
        service.submit(runnable);
    }
}
