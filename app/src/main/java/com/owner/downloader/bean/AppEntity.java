package com.owner.downloader.bean;

public class AppEntity {
    public String url;
    public String appIcon;
    public String name;
    public String version;
    public String size;
    public String downLoadCount;

    public AppEntity(String url, String appIcon, String name, String version, String size, String downLoadCount) {
        this.url = url;
        this.appIcon = appIcon;
        this.name = name;
        this.version = version;
        this.size = size;
        this.downLoadCount = downLoadCount;
    }
}
