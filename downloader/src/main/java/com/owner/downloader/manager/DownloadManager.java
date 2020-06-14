package com.owner.downloader.manager;

import android.content.Context;

import com.owner.downloader.download.DownloadDispatcher;
import com.owner.downloader.download.DownloadListener;


public class DownloadManager {
    private static final DownloadManager sDownloadManager = new DownloadManager();

    private DownloadManager() {
    }

    public static DownloadManager getInstance() {
        return sDownloadManager;
    }

    public void init(Context context) {
        FileManager.getInstance().init(context);
        DaoManager.getInstance().init(context);
    }

    public void download(String url, String name, DownloadListener listener) {
        DownloadDispatcher.getInstance().download(url, name, listener);
    }

    public void stop(String url) {
        DownloadDispatcher.getInstance().stop(url);
    }

    public void download(String url) {
        // DownloadDispatcher.getInstance().download(url);
    }
}
