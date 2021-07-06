package org.smartregister.chw.activity;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.util.FitPolicy;

import org.jetbrains.annotations.Nullable;
import org.smartregister.chw.R;
import org.smartregister.chw.adapter.GuideBooksAdapter;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.contract.GuideBooksFragmentContract;
import org.smartregister.chw.interactor.GuideBooksFragmentInteractor;
import org.smartregister.chw.presenter.GuideBooksFragmentPresenter;
import org.smartregister.chw.util.DownloadGuideBooksUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GuideBooksCounselingActivity extends Activity implements GuideBooksFragmentContract.View {

    protected RecyclerView.Adapter mAdapter;
    protected PDFView pdfView;
    protected GuideBooksFragmentContract.Presenter presenter;
    private List<GuideBooksFragmentContract.RemoteFile> videos = new ArrayList<>();
    private ProgressBar progressBar;
    private Map<String, GuideBooksFragmentContract.RemoteFile> allVideos = new HashMap<>();

    @Override
    protected void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guide_books_counseling);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progress_bar);
        pdfView = findViewById(R.id.pdfView);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getViewContext());
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new GuideBooksAdapter(videos, this, ChwApplication.getCounselingDocsDirectory());
        recyclerView.setAdapter(mAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        initializePresenter();
        presenter.initialize("pdf_files.json", ChwApplication.getCounselingDocsDirectory());
    }

    @Override
    public void initializePresenter() {
        presenter = new GuideBooksFragmentPresenter(this, new GuideBooksFragmentInteractor());
    }

    @Override
    public GuideBooksFragmentContract.Presenter getPresenter() {
        return presenter;
    }

    @Override
    public void onDataReceived(List<GuideBooksFragmentContract.RemoteFile> receivedVideos) {

        for (GuideBooksFragmentContract.RemoteFile video : receivedVideos) {
            GuideBooksFragmentContract.RemoteFile available = allVideos.get(video.getID());
            if (available == null) {
                allVideos.put(video.getID(), video);
            } else if (video.isDowloaded() && !available.isDowloaded()) {
                allVideos.put(video.getID(), video);
            }
        }

        List<GuideBooksFragmentContract.RemoteFile> res = new ArrayList<>(allVideos.values());
        Collections.sort(res, (video1, video2) -> video1.getTitle().compareTo(video2.getTitle()));

        this.videos.clear();
        this.videos.addAll(res);
        this.mAdapter.notifyDataSetChanged();
        this.displayLoadingState(false);
    }

    @Override
    public @Nullable Context getViewContext() {
        return this;
    }

    @Override
    public void displayLoadingState(boolean state) {
        progressBar.setVisibility(state ? View.VISIBLE : View.GONE);
    }

    @Override
    public void openFile(GuideBooksFragmentContract.RemoteFile remoteFile) {
        pdfView.setVisibility(View.VISIBLE);
        pdfView.fromFile(new File(remoteFile.getLocalPath()))
                .pageFitPolicy(FitPolicy.WIDTH)
                .spacing(0)
                .load();
    }

    @Override
    public void downloadFile(GuideBooksFragmentContract.DownloadListener downloadListener, GuideBooksFragmentContract.RemoteFile video) {
        new DownloadGuideBooksUtils(downloadListener, video.getName(), ChwApplication.getCounselingDocsDirectory(), getViewContext()).execute();
    }

    @Override
    public void onBackPressed() {
        if (pdfView.getVisibility() == View.VISIBLE) {
            pdfView.recycle();
            pdfView.setVisibility(View.GONE);
        }
        else
            super.onBackPressed();
    }

    public void onBackIconClicked(View view) {
        onBackPressed();
    }
}
