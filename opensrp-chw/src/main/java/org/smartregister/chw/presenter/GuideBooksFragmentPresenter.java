package org.smartregister.chw.presenter;

import org.jetbrains.annotations.Nullable;
import org.smartregister.chw.contract.GuideBooksFragmentContract;

import java.lang.ref.WeakReference;
import java.util.List;

public class GuideBooksFragmentPresenter implements GuideBooksFragmentContract.Presenter, GuideBooksFragmentContract.InteractorCallBack {

    private WeakReference<GuideBooksFragmentContract.View> view;
    private GuideBooksFragmentContract.Interactor interactor;

    public GuideBooksFragmentPresenter(GuideBooksFragmentContract.View view, GuideBooksFragmentContract.Interactor interactor) {
        this.view = new WeakReference<>(view);
        this.interactor = interactor;
    }

    @Override
    public void initialize(String fileName, String directory) {
        if (getView() != null)
            interactor.getFiles(getView().getViewContext(), fileName, directory, this);
    }

    @Override
    @Nullable
    public GuideBooksFragmentContract.View getView() {
        if (view.get() != null)
            return view.get();

        return null;
    }

    @Override
    public void onDataFetched(List<GuideBooksFragmentContract.RemoteFile> videos) {
        if (getView() == null)
            return;

        getView().onDataReceived(videos);
    }
}
