package org.smartregister.chw.domain;

import org.smartregister.chw.contract.GuideBooksFragmentContract;

public class GuideBooksFragmentVideo implements GuideBooksFragmentContract.RemoteFile {

    private String videoID;
    private String title;
    private String name;
    private Boolean isDownloaded;
    private String localPath;
    private String severUrl;

    public void setVideoID(String videoID) {
        this.videoID = videoID;
    }

    public void setDownloaded(Boolean downloaded) {
        isDownloaded = downloaded;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public void setSeverUrl(String severUrl) {
        this.severUrl = severUrl;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getID() {
        return videoID;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Boolean isDowloaded() {
        return isDownloaded;
    }

    @Override
    public String getLocalPath() {
        return localPath;
    }

    @Override
    public String getServerUrl() {
        return severUrl;
    }
}
