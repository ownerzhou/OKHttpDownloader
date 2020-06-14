package com.owner.downloader.manager;

import android.content.Context;
import android.util.Log;

import com.owner.downloader.entity.DownloadEntity;
import com.owner.downloader.db.DaoSupportFactory;
import com.owner.downloader.db.IDaoSupport;

import java.util.List;


public class DaoManager {
    private static final String TAG = DaoManager.class.getSimpleName();
    private final static DaoManager sManager = new DaoManager();
    private IDaoSupport<DownloadEntity> mDaoSupport;

    private DaoManager() {

    }

    public static DaoManager getInstance() {
        return sManager;
    }

    public void init(Context context) {
        DaoSupportFactory.getFactory().init(context);
        mDaoSupport = DaoSupportFactory.getFactory().getDao(DownloadEntity.class);
    }

    public void addEntity(DownloadEntity entity) {
        long delete = mDaoSupport.delete("url = ? and threadId = ?", entity.getUrl(), entity.getThreadId() + "");
        long size = mDaoSupport.insert(entity);
        Log.i(TAG, "DaoManagerHelper: " + size);

    }

    public List<DownloadEntity> queryAll(String url) {
        Log.i(TAG, "queryAll: " + url);
        return mDaoSupport.querySupport().selection("url = ?").selectionArgs(url).query();
    }

    public void remove(String url) {
        mDaoSupport.delete("url = ?", url);
    }
}
