package org.smartregister.chw.presenter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import org.smartregister.chw.R;
import org.smartregister.chw.application.ChwApplication;
import org.smartregister.chw.interactor.LoginInteractor;
import org.smartregister.configurableviews.model.LoginConfiguration;
import org.smartregister.configurableviews.model.ViewConfiguration;
import org.smartregister.configurableviews.util.Constants;
import org.smartregister.login.model.BaseLoginModel;
import org.smartregister.login.presenter.BaseLoginPresenter;
import org.smartregister.view.contract.BaseLoginContract;

import java.lang.ref.WeakReference;

import timber.log.Timber;

public class LoginPresenter extends BaseLoginPresenter implements BaseLoginContract.Presenter {

    public LoginPresenter(BaseLoginContract.View loginView) {
        mLoginView = new WeakReference<>(loginView);
        mLoginInteractor = new LoginInteractor(this);
        mLoginModel = new BaseLoginModel();
    }

    @Override
    public void processViewCustomizations() {
        try {
            String jsonString = getJsonViewFromPreference(Constants.VIEW_CONFIGURATION_PREFIX + Constants.CONFIGURATION.LOGIN);
            if (jsonString == null) {
                return;
            }

            ViewConfiguration loginView = ChwApplication.getJsonSpecHelper().getConfigurableView(jsonString);
            LoginConfiguration metadata = (LoginConfiguration) loginView.getMetadata();
            LoginConfiguration.Background background = metadata.getBackground();

            CheckBox showPasswordCheckBox = getLoginView().getActivityContext().findViewById(R.id.login_show_password_checkbox);
            TextView showPasswordTextView = getLoginView().getActivityContext().findViewById(R.id.login_show_password_text_view);
            if (!metadata.getShowPasswordCheckbox()) {
                showPasswordCheckBox.setVisibility(View.GONE);
                showPasswordTextView.setVisibility(View.GONE);
            } else {
                showPasswordCheckBox.setVisibility(View.VISIBLE);
                showPasswordTextView.setVisibility(View.VISIBLE);
            }

            if (background.getOrientation() != null && background.getStartColor() != null && background.getEndColor() != null) {
                View loginLayout = getLoginView().getActivityContext().findViewById(R.id.login_layout);
                GradientDrawable gradientDrawable = new GradientDrawable();
                gradientDrawable.setShape(GradientDrawable.RECTANGLE);
                gradientDrawable.setOrientation(
                        GradientDrawable.Orientation.valueOf(background.getOrientation()));
                gradientDrawable.setColors(new int[]{Color.parseColor(background.getStartColor()),
                        Color.parseColor(background.getEndColor())});
                loginLayout.setBackground(gradientDrawable);
            }

        } catch (Exception e) {
            Timber.e(e);
        }
    }

    @Override
    public boolean isServerSettingsSet() {
        return false;
    }
}
