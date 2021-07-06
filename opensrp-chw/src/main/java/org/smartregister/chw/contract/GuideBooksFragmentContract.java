package org.smartregister.chw.contract;

import android.content.Context;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface GuideBooksFragmentContract {

    interface View {

        void initializePresenter();

        Presenter getPresenter();

        void onDataReceived(List<RemoteFile> videos);

        @Nullable
        Context getViewContext();

        void displayLoadingState(boolean state);

        void openFile(RemoteFile remoteFile);

        void downloadFile(DownloadListener downloadListener, RemoteFile video);
    }

    interface Presenter {

        void initialize(String fileName, String directory);

        @Nullable View getView();

    }

    interface DownloadListener {

        void onDownloadComplete(boolean successful, String localPath);

        void onStarted();
    }

    interface Interactor {

        void getFiles(Context context, String fileName, String directory, InteractorCallBack callBack);

    }

    interface InteractorCallBack {

        void onDataFetched(List<RemoteFile> videos);
    }

    interface RemoteFile {

        String getID();

        String getName();

        String getTitle();

        Boolean isDowloaded();

        void setDownloaded(Boolean downloaded);

        String getLocalPath();

        void setLocalPath(String localPath);

        String getServerUrl();
    }
}
