package org.smartregister.chw.fragment;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.ReflectionHelpers;
import org.smartregister.chw.BaseUnitTest;
import org.smartregister.chw.R;
import org.smartregister.chw.activity.FragmentBaseActivity;
import org.smartregister.chw.adapter.ListableAdapter;
import org.smartregister.chw.domain.ReportType;
import org.smartregister.chw.presenter.ListPresenter;
import org.smartregister.chw.viewholder.ListableViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ReportsFragmentTest extends BaseUnitTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private ReportsFragment fragment;

    @Mock
    private ListPresenter<ReportType> presenter;

    private AppCompatActivity activity;

    @Before
    public void setUp() {
        org.smartregister.Context.bindtypes = new ArrayList<>();
        fragment = Mockito.spy(new ReportsFragment());

        activity = Robolectric.buildActivity(AppCompatActivity.class).create().start().get();
        activity.setContentView(org.smartregister.family.R.layout.activity_family_profile);
        ReflectionHelpers.setField(fragment, "presenter", presenter);
        activity.getSupportFragmentManager().beginTransaction().add(fragment, "Presenter").commit();
    }

    @Test
    public void testRefreshView() {
        ListableAdapter<ReportType, ListableViewHolder<ReportType>> mAdapter = Mockito.mock(ListableAdapter.class);
        ReflectionHelpers.setField(fragment, "mAdapter", mAdapter);
        fragment.refreshView();
        Mockito.verify(mAdapter).reloadData(Mockito.any());
    }

    @Test
    public void testRenderData() {
        List<ReportType> identifiables = new ArrayList<>();
        fragment.renderData(identifiables);
        Assert.assertEquals(identifiables, ReflectionHelpers.getField(fragment, "list"));
    }

    @Test
    public void testSetLoadingState() {
        fragment.setLoadingState(true);
        ProgressBar progressBar = ReflectionHelpers.getField(fragment, "progressBar");
        Assert.assertEquals(View.VISIBLE, progressBar.getVisibility());
    }

    @Test
    public void testOnListItemClickedForEligibleChildren() {
        fragment.onListItemClicked(new ReportType(activity.getString(R.string.eligible_children), activity.getString(R.string.eligible_children)), 12345);
        assertActivityStarted(activity, new FragmentBaseActivity());
    }

    @Test
    public void testOnListItemClickedForDosesNeeded() {
        fragment.onListItemClicked(new ReportType(activity.getString(R.string.doses_needed), activity.getString(R.string.doses_needed)), 12345);
        assertActivityStarted(activity, new FragmentBaseActivity());
    }

    private void assertActivityStarted(Activity currActivity, Activity nextActivity) {

        Intent expectedIntent = new Intent(currActivity, nextActivity.getClass());
        Intent actual = ShadowApplication.getInstance().getNextStartedActivity();
        Assert.assertEquals(expectedIntent.getComponent(), actual.getComponent());
    }
}
