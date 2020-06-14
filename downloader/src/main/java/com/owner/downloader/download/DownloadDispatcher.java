package com.owner.downloader.download;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.owner.downloader.manager.OkHttpManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * @author: owner_zhou
 * @Data：6/14/2020
 * @Description: 下载调度器
 */
public class DownloadDispatcher {
    private static final String TAG = DownloadDispatcher.class.getSimpleName();
    private static volatile DownloadDispatcher sDownloadDispatcher;
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int THREAD_SIZE = Math.max(3, Math.min(CPU_COUNT - 1, 5));
    //核心线程数
    private static final int CORE_POOL_SIZE = THREAD_SIZE;
    //线程池
    private ExecutorService mExecutorService;
    //private final Deque<DownloadTask> readyTasks = new ArrayDeque<>();
    private final Deque<DownloadTask> runningTasks = new ArrayDeque<>();
    //private final Deque<DownloadTask> stopTasks = new ArrayDeque<>();
    private Handler handler;

    private DownloadDispatcher() {
        handler = new Handler(Looper.getMainLooper());
    }

    public static DownloadDispatcher getInstance() {
        if (sDownloadDispatcher == null) {
            synchronized (DownloadDispatcher.class) {
                if (sDownloadDispatcher == null) {
                    sDownloadDispatcher = new DownloadDispatcher();
                }
            }
        }
        return sDownloadDispatcher;
    }

    /**
     * 创建线程池
     *
     * @return mExecutorService
     */
    public synchronized ExecutorService executorService() {
        if (mExecutorService == null) {
            mExecutorService = new ThreadPoolExecutor(CORE_POOL_SIZE, Integer.MAX_VALUE, 60, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>(), new ThreadFactory() {
                @Override
                public Thread newThread(@NonNull Runnable r) {
                    Thread thread = new Thread(r);
                    thread.setDaemon(false);
                    return thread;
                }
            });
        }
        return mExecutorService;
    }

    /**
     * @param name     文件名
     * @param url      下载的地址
     * @param listener 回调接口
     */
    public void download(final String url, final String name, final DownloadListener listener) {
        final DownloadListenerWrapper listenerWrapper = new DownloadListenerWrapper(listener);
        Call call = OkHttpManager.getInstance().newCall(url);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull final IOException e) {
                listenerWrapper.onFailure(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                //获取文件的大小
                if (response.code() != 200) {
                    listenerWrapper.onFailure(new FileNotFoundException(response.message()));
                    return;
                }

                long contentLength = response.body().contentLength();
                Log.i(TAG, "contentLength=" + contentLength);
                if (contentLength <= -1) {
                    return;
                }

                DownloadTask downloadTask = new DownloadTask(name, url, THREAD_SIZE, contentLength, listenerWrapper);
                downloadTask.execute();
                runningTasks.add(downloadTask);
            }
        });
    }

    /**
     * 暂停下载任务
     *
     * @param url
     */
    public void stop(String url) {
        //这个停止是不是这个正在下载的
        for (DownloadTask runningTask : runningTasks) {
            if (runningTask.getUrl().equals(url)) {
                runningTask.stopDownload();
            }
        }
    }

    /**
     * 释放下载任务
     *
     * @param task 下载任务
     */
    public void release(DownloadTask task) {
        runningTasks.remove(task);
        //参考OkHttp的Dispatcher()的源码
        //readyTasks.
    }

    public void runUIThread(Runnable runnable) {
        if (handler != null) {
            handler.post(runnable);
        }
    }

    class DownloadListenerWrapper implements DownloadListener {
        private DownloadListener downloadListener;

        public DownloadListenerWrapper(DownloadListener listener) {
            this.downloadListener = listener;
        }

        @Override
        public void onSuccess(final File file) {
            runUIThread(new Runnable() {
                @Override
                public void run() {
                    downloadListener.onSuccess(file);
                }
            });
        }

        @Override
        public void onFailure(final Exception e) {
            runUIThread(new Runnable() {
                @Override
                public void run() {
                    downloadListener.onFailure(e);
                }
            });
        }

        @Override
        public void onProgress(final long progress, final long currentLength) {
            runUIThread(new Runnable() {
                @Override
                public void run() {
                    downloadListener.onProgress(progress, currentLength);
                }
            });
        }

        @Override
        public void onPause(final long progress, final long currentLength) {
            runUIThread(new Runnable() {
                @Override
                public void run() {
                    downloadListener.onPause(progress, currentLength);
                }
            });
        }
    }
}
