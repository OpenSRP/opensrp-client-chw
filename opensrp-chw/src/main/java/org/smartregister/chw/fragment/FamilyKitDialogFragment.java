package org.smartregister.chw.fragment;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.smartregister.chw.R;
import org.smartregister.chw.anc.domain.VisitDetail;
import org.smartregister.chw.dao.FamilyKitDao;
import org.smartregister.util.JsonFormUtils;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class FamilyKitDialogFragment extends DialogFragment implements View.OnClickListener {

    public static final String DIALOG_TAG = "FamilyKitDialogFragment";
    private static final String BASE_ENTITY_ID = "base_entity_id";
    private static final String VISIT_DATE = "visit_date";

    private Long familyKitDate;
    private String baseEntityID;
    private RadioGroup radioGroupFamilyKit;
    private RadioGroup radioGroupFamilyKitUsed;
    private Map<String, String> selectedOptions = new HashMap<>();

    public static FamilyKitDialogFragment getInstance(String familyBaseEntityID, Long visitDate) {
        FamilyKitDialogFragment familyKitDialogFragment = new FamilyKitDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(BASE_ENTITY_ID, familyBaseEntityID);
        bundle.putLong(VISIT_DATE, visitDate);
        familyKitDialogFragment.setArguments(bundle);
        return familyKitDialogFragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Holo_Light_NoActionBar);
    }

    @Override
    public void onStart() {
        super.onStart();
        new Handler().post(() -> {
            if (getDialog() != null && getDialog().getWindow() != null) {
                getDialog().getWindow().setLayout(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_family_kit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        baseEntityID = getArguments().getString(BASE_ENTITY_ID);
        familyKitDate = getArguments().getLong(VISIT_DATE);

        radioGroupFamilyKit = view.findViewById(R.id.radio_group_family_kit);
        radioGroupFamilyKitUsed = view.findViewById(R.id.radio_group_kit_used);
        view.findViewById(R.id.close).setOnClickListener(this);


        Observable<String> observable = Observable.create(e -> {
            Map<String, VisitDetail> washData = FamilyKitDao.getFamilyKitDetails(familyKitDate, baseEntityID);
            if (washData.get("details_info") != null) {
                parseOldData(washData.get("details_info").getDetails());
            } else {
                for (Map.Entry<String, VisitDetail> entry : washData.entrySet()) {
                    selectedOptions.put(entry.getKey(), entry.getValue().getDetails());
                }
            }

            e.onNext("");
            e.onComplete();
        });

        final Disposable[] disposable = new Disposable[1];
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposable[0] = d;
            }

            @Override
            public void onNext(String s) {
                refreshUI();
            }

            @Override
            public void onError(Throwable e) {
                Timber.e(e);
            }

            @Override
            public void onComplete() {
                disposable[0].dispose();
                disposable[0] = null;
            }
        });
    }

    private void parseOldData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONArray field = JsonFormUtils.fields(jsonObject);

            selectedOptions.put("family_kit_received", getValueFromJsonFieldNode(field, "family_kit_received"));
            selectedOptions.put("family_kit_used", getValueFromJsonFieldNode(field, "family_kit_used"));
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public String getValueFromJsonFieldNode(JSONArray field, String key) {
        JSONObject jsonObject = JsonFormUtils.getFieldJSONObject(field, key);
        if (jsonObject == null)
            return null;

        String value = jsonObject.optString(JsonFormUtils.VALUE);
        if (StringUtils.isBlank(value))
            return "";

        return value;
    }

    private void refreshUI() {
        notifyUIValues(radioGroupFamilyKit, "family_kit_received");
        notifyUIValues(radioGroupFamilyKitUsed, "family_kit_used");
    }

    private void notifyUIValues(RadioGroup radioGroup, String optionName) {
        String selectedOptionString = selectedOptions.get(optionName);
        int count = radioGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = radioGroup.getChildAt(i);
            if (view instanceof RadioButton) {
                RadioButton selectedAnswer = (RadioButton) view;
                if (selectedAnswer.getTag().equals(selectedOptionString)) {
                    selectedAnswer.setChecked(true);
                }
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.close) {
            dismiss();
        }
    }
}