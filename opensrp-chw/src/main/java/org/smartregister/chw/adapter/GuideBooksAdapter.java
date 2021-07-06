package org.smartregister.chw.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import org.smartregister.chw.R;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.contract.GuideBooksFragmentContract;
import org.smartregister.chw.util.DownloadGuideBooksUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.smartregister.chw.adapter.GuideBooksAdapter.FileType.PDF;
import static org.smartregister.chw.adapter.GuideBooksAdapter.FileType.VIDEO;

public class GuideBooksAdapter extends RecyclerView.Adapter<GuideBooksAdapter.MyViewHolder> {
    private List<GuideBooksFragmentContract.RemoteFile> remoteFiles;
    private GuideBooksFragmentContract.View view;
    private String directory;

    public GuideBooksAdapter(List<GuideBooksFragmentContract.RemoteFile> remoteFiles, GuideBooksFragmentContract.View view, String directory) {
        this.remoteFiles = remoteFiles;
        this.view = view;
        this.directory = directory;
    }

    @NonNull
    @Override
    public GuideBooksAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_job_aids_guide_books_item, viewGroup, false);
        return new GuideBooksAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull GuideBooksAdapter.MyViewHolder myViewHolder, int position) {
        GuideBooksFragmentContract.RemoteFile remoteFile = remoteFiles.get(position);

        myViewHolder.icon.setVisibility(View.VISIBLE);
        if (remoteFile.isDowloaded()) {
            showDownloadedIcon(myViewHolder);

        } else {
            myViewHolder.icon.setImageResource(R.drawable.ic_save_outline_black);
        }
        myViewHolder.progressBar.setVisibility(View.GONE);
        AtomicReference<DownloadGuideBooksUtils> downloadTask = new AtomicReference<>(getDownloadTask(remoteFile, myViewHolder, position));

        myViewHolder.progressBar.setOnClickListener(v -> {
            if (myViewHolder.progressBar.getVisibility() == View.VISIBLE) {
                new AlertDialog.Builder(myViewHolder.getContext())
                        .setTitle(myViewHolder.getContext().getString(R.string.cancel_download))
                        .setMessage(myViewHolder.getContext().getString(R.string.cancel_download_confirmation))
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                            // Continue with delete operation
                            if (downloadTask.get() != null)
                                downloadTask.get().cancelDownload();

                            downloadTask.set(getDownloadTask(remoteFile, myViewHolder, position));

                            dialog.dismiss();
                            myViewHolder.progressBar.setVisibility(View.GONE);
                            myViewHolder.icon.setVisibility(View.VISIBLE);
                        })
                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });

        myViewHolder.icon.setOnClickListener(v -> {
            if (remoteFile.isDowloaded()) {
                view.openFile(remoteFile);
            } else {
                if (downloadTask.get() != null) {
                    if (downloadTask.get().getStatus() == AsyncTask.Status.FINISHED)
                        downloadTask.set(getDownloadTask(remoteFile, myViewHolder, position));

                    downloadTask.get().execute();
                }
            }
        });
        myViewHolder.title.setText(remoteFile.getTitle());
    }

    private void showDownloadedIcon(@NonNull MyViewHolder myViewHolder) {
        switch (getFileType(directory)) {
            case PDF:
                myViewHolder.icon.setImageResource(R.drawable.ic_pdf_icon);
                break;
            case VIDEO:
                myViewHolder.icon.setImageResource(R.drawable.ic_play_circle_black);
                break;
            default:
                myViewHolder.icon.setImageResource(R.drawable.ic_save_outline_black);
                break;
        }
    }

    @Nullable
    private DownloadGuideBooksUtils getDownloadTask(GuideBooksFragmentContract.RemoteFile remoteFile, @NonNull GuideBooksAdapter.MyViewHolder myViewHolder, int position) {
        DownloadGuideBooksUtils downloadTask = null;
        if (!remoteFile.isDowloaded()) {
            GuideBooksFragmentContract.DownloadListener listener = new GuideBooksFragmentContract.DownloadListener() {
                @Override
                public void onDownloadComplete(boolean successful, String localPath) {

                    remoteFile.setDownloaded(successful);
                    remoteFile.setLocalPath(localPath);

                    myViewHolder.progressBar.setVisibility(View.GONE);
                    myViewHolder.icon.setVisibility(View.VISIBLE);

                    if (successful) {
                        remoteFiles.get(position).setDownloaded(true);
                    } else {
                        remoteFiles.get(position).setDownloaded(false);
                        if (view.getViewContext() != null)
                            Toast.makeText(view.getViewContext(),
                                    view.getViewContext().getString(R.string.jobs_aid_failed_download, remoteFile.getTitle())
                                    , Toast.LENGTH_SHORT).show();
                    }
                    notifyDataSetChanged();
                }

                @Override
                public void onStarted() {
                    myViewHolder.progressBar.setVisibility(View.VISIBLE);
                    myViewHolder.icon.setVisibility(View.GONE);
                }
            };

            downloadTask = new DownloadGuideBooksUtils(listener, remoteFile.getName(), directory, myViewHolder.getContext());
        }
        return downloadTask;
    }

    @Override
    public int getItemCount() {
        return remoteFiles.size();
    }


    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private ImageView icon;
        private ProgressBar progressBar;
        private View view;

        private MyViewHolder(View view) {
            super(view);
            this.view = view;
            title = view.findViewById(R.id.tvVideoTitle);
            icon = view.findViewById(R.id.ivIcon);
            progressBar = view.findViewById(R.id.progress_bar);
        }

        private Context getContext() {
            return view.getContext();
        }
    }

    private FileType getFileType(String directory) {
        if (directory.equals(ChwApplication.getGuideBooksDirectory()))
            return VIDEO;
        else if (directory.equals(ChwApplication.getCounselingDocsDirectory()))
            return PDF;
        return VIDEO;
    }

    public enum FileType {
        VIDEO,
        PDF
    }
}
