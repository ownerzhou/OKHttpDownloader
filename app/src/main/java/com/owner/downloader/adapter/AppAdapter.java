package com.owner.downloader.adapter;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.owner.downloader.bean.AppEntity;
import com.owner.downloader.R;
import com.owner.downloader.download.DownloadListener;
import com.owner.downloader.manager.DownloadManager;
import com.owner.downloader.utils.Utils;
import com.owner.downloader.widget.CircleProgressBar;

import java.io.File;
import java.util.List;

public class AppAdapter extends CommonRecycleAdapter<AppEntity> {
    private static final String TAG = AppAdapter.class.getSimpleName();
    private Context mContext;

    public AppAdapter(Context context, List<AppEntity> mDatas, int layoutId) {
        super(context, mDatas, layoutId);
        this.mContext = context;
    }

    @Override
    public void convert(CommonViewHolder holder, final AppEntity appEntity, int position) {
        ImageView appIcon = holder.getView(R.id.icon);
        Glide.with(appIcon.getContext())
                .load(appEntity.appIcon)
                .into(appIcon);
        holder.setText(R.id.name, appEntity.name)
                .setText(R.id.size, "大小:" + appEntity.size)
                .setText(R.id.downloadCount, "下载次数:" + appEntity.downLoadCount);
        final CircleProgressBar progressbar = holder.getView(R.id.pb);
        progressbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("progressbar=" + progressbar);
                if (progressbar.getText().equals("继续") || progressbar.getText().equals("下载")) {
                    DownloadManager.getInstance().download(appEntity.url, appEntity.name, new DownloadListener() {
                        @Override
                        public void onSuccess(File file) {
                            Log.d(TAG, file.getName() + "下载成功");

                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.d(TAG, "下载失败" + e.getMessage());

                        }

                        @Override
                        public void onProgress(long progress, long currentLength) {
                            progressbar.setCurrentProgress(Utils.keepTwoBit((float) progress / currentLength));
                        }

                        @Override
                        public void onPause(long progress, long currentLength) {
                            progressbar.setText("继续");
                        }
                    });
                } else {
                    DownloadManager.getInstance().stop(appEntity.url);
                }
            }
        });
    }
}
