package com.owner.downloader.download;

import android.util.Log;

import com.owner.downloader.entity.DownloadEntity;
import com.owner.downloader.manager.DaoManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author: owner_zhou
 * @Data：6/14/2020
 * @Description: 核心下载任务
 */
public class DownloadTask {
    private static final String TAG = DownloadTask.class.getSimpleName();
    //文件下载的url
    private String url;
    //文件的名称
    private String name;
    //文件的大小
    private long mContentLength;
    //下载文件的线程的个数
    private int mThreadSize;
    //线程下载成功的个数，AtomicInteger
    private AtomicInteger mSuccessNumber = new AtomicInteger();
    //总进度=每个线程的进度的和
    private long mTotalProgress;
    //正在执行下载任务的runnable
    private List<DownloadRunnable> mDownloadRunnableList;
    private DownloadListener mDownloadListener;

    public DownloadTask(String name, String url, int threadSize, long contentLength, DownloadListener listener) {
        this.name = name;
        this.url = url;
        this.mThreadSize = threadSize;
        this.mContentLength = contentLength;
        this.mDownloadListener = listener;
        this.mDownloadRunnableList = new ArrayList<>();
    }

    public void execute() {
        //每个线程的下载的大小threadSize
        long threadSize = mContentLength / mThreadSize;
        List<DownloadEntity> entities = DaoManager.getInstance().queryAll(url);
        for (int i = 0; i < mThreadSize; i++) {
            //初始化的时候，需要读取数据库
            //开始下载的位置
            long start = i * threadSize;
            //结束下载的位置
            long end = start + threadSize;
            if (i == mThreadSize - 1) {
                end = mContentLength;
            }
            DownloadEntity downloadEntity = getEntity(i, entities);
            if (downloadEntity == null) {
                downloadEntity = new DownloadEntity(start, end, url, i, 0, mContentLength);
            } else {
                Log.d(TAG, "init: 上次保存的进度progress=" + downloadEntity.toString());
                start = start + downloadEntity.getProgress();
            }
            if (threadSize == downloadEntity.getProgress()) {
                mSuccessNumber.incrementAndGet();
                return;
            }
            DownloadRunnable downloadRunnable = new DownloadRunnable(name, url, mContentLength, i, start, end,
                    downloadEntity.getProgress(), downloadEntity, new DownloadListener() {
                @Override
                public void onFailure(Exception e) {
                    Log.e(TAG, "onFailure: " + e.getMessage());
                    //有一个线程发生异常，下载失败，需要把其它线程停止掉
                    mDownloadListener.onFailure(e);
                    stopDownload();
                }

                @Override
                public void onSuccess(File file) {
                    mSuccessNumber.incrementAndGet();
                    if (mSuccessNumber.get() == mThreadSize) {
                        mDownloadListener.onSuccess(file);
                        DownloadDispatcher.getInstance().release(DownloadTask.this);
                        //如果下载完毕，清除数据库
                        DaoManager.getInstance().remove(url);
                    }
                }

                @Override
                public void onProgress(long progress, long currentLength) {
                    //叠加下progress，实时去更新进度条
                    //这里需要synchronized下
                    synchronized (DownloadTask.this) {
                        mTotalProgress = mTotalProgress + progress;
                        Log.d(TAG, "onProgress: mTotalProgress" + mTotalProgress);
                        mDownloadListener.onProgress(mTotalProgress, currentLength);
                    }
                }

                @Override
                public void onPause(long progress, long currentLength) {
                    mDownloadListener.onPause(mTotalProgress, currentLength);
                }
            });

            //通过线程池去执行
            DownloadDispatcher.getInstance().executorService().execute(downloadRunnable);
            mDownloadRunnableList.add(downloadRunnable);
        }
    }

    private DownloadEntity getEntity(int threadId, List<DownloadEntity> entities) {
        for (DownloadEntity entity : entities) {
            if (threadId == entity.getThreadId()) {
                return entity;
            }
        }
        return null;
    }

    /**
     * 停止下载
     */
    public void stopDownload() {
        for (DownloadRunnable runnable : mDownloadRunnableList) {
            runnable.stop();
        }
    }

    public String getUrl() {
        return url;
    }
}
