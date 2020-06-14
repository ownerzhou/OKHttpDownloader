package com.owner.downloader.manager;

import android.content.Context;

import com.owner.downloader.common.MD5Util;

import java.io.File;

public class FileManager {
    private static final FileManager INSTANCE = new FileManager();
    private File mRootDir;
    private Context mContext;

    private FileManager() {

    }

    public static FileManager getInstance() {
        return INSTANCE;
    }

    public void init(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void rootDir(File file) {
        if (!file.exists()) {
            file.mkdirs();
        }
        if (!file.exists() && file.isDirectory()) {
            mRootDir = file;
        }
    }

    /**
     * 通过网络的路径获取一个本地的文件路径
     *
     * @return
     */
    public File getFile(String url) {
        String fileName = MD5Util.md5(url);
        if (mRootDir == null) {
            mRootDir = mContext.getCacheDir();
        }
        return new File(mRootDir, fileName);
    }
}
