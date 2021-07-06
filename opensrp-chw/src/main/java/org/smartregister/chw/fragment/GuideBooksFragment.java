package org.smartregister.chw.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import org.smartregister.chw.R;
import org.smartregister.chw.activity.GuideBooksCounselingActivity;
import org.smartregister.chw.activity.GuideBooksTutorialsActivity;


public class GuideBooksFragment extends Fragment {

    public static GuideBooksFragment newInstance() {
        GuideBooksFragment fragment = new GuideBooksFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_job_aids_guide_books, container, false);
        ConstraintLayout layoutTutorials = rootView.findViewById(R.id.layoutTutorials);
        ConstraintLayout layoutCounseling = rootView.findViewById(R.id.layoutCounseling);
        layoutCounseling.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), GuideBooksCounselingActivity.class);
            getActivity().startActivity(intent);
        });

        layoutTutorials.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), GuideBooksTutorialsActivity.class);
            getActivity().startActivity(intent);
        });

        return rootView;
    }
}
