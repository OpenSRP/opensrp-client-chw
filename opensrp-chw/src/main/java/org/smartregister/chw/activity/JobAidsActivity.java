package org.smartregister.chw.activity;

import android.os.Bundle;
import android.support.design.bottomnavigation.LabelVisibilityMode;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import org.smartregister.chw.BuildConfig;
import org.smartregister.chw.R;
import org.smartregister.chw.fragment.JobAidsDashboardFragment;
import org.smartregister.chw.fragment.JobAidsGuideBooksFragment;
import org.smartregister.chw.listener.JobsAidsBottomNavigationListener;
import org.smartregister.helper.BottomNavigationHelper;

public class JobAidsActivity extends FamilyRegisterActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_aids);
        setUpView();
        registerBottomNavigation();
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return JobAidsDashboardFragment.newInstance();
                case 1:
                    return JobAidsGuideBooksFragment.newInstance();
                default:
                    return JobAidsDashboardFragment.newInstance();
            }

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }
    }

    private void setUpView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(" ");
        }
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
    }

    @Override
    protected void registerBottomNavigation() {

        bottomNavigationHelper = new BottomNavigationHelper();
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (bottomNavigationView != null) {

            bottomNavigationView.getMenu().add(Menu.NONE, org.smartregister.R.string.action_me, Menu.NONE, org.smartregister.R.string.me)
                    .setIcon(bottomNavigationHelper
                            .writeOnDrawable(org.smartregister.R.drawable.bottom_bar_initials_background, "", getResources()));
            bottomNavigationHelper.disableShiftMode(bottomNavigationView);

            bottomNavigationView.getMenu().removeItem(org.smartregister.R.string.action_me);

            bottomNavigationView.setLabelVisibilityMode(LabelVisibilityMode.LABEL_VISIBILITY_LABELED);

            bottomNavigationHelper.disableShiftMode(bottomNavigationView);
            bottomNavigationView.setSelectedItemId(org.smartregister.family.R.id.action_job_aids);

            JobsAidsBottomNavigationListener childBottomNavigationListener = new JobsAidsBottomNavigationListener(this);
            bottomNavigationView.setOnNavigationItemSelectedListener(childBottomNavigationListener);

        }

        if (!BuildConfig.SUPPORT_QR) {
            bottomNavigationView.getMenu().removeItem(org.smartregister.family.R.id.action_scan_qr);
        }
    }
}
