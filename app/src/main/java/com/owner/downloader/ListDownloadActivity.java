package com.owner.downloader;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.owner.downloader.adapter.AppAdapter;
import com.owner.downloader.adapter.LinearLayoutItemDecoration;
import com.owner.downloader.utils.Utils;

public class ListDownloadActivity extends AppCompatActivity {
    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_download);
        rv = findViewById(R.id.rv);
        setupAdapter();
    }


    private void setupAdapter() {
        AppAdapter adapter = new AppAdapter(this, Utils.generateApp(), R.layout.app_item);
        rv.addItemDecoration(new LinearLayoutItemDecoration(this, R.drawable.linear_layout_item));
        rv.setAdapter(adapter);
    }
}
