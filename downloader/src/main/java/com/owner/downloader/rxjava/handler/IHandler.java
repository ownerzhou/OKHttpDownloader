package com.owner.downloader.rxjava.handler;

public interface IHandler {
    void init();
    void submit(Runnable runnable);
}