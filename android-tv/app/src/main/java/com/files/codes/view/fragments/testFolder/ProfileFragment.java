package com.files.codes.view.fragments.testFolder;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.ekamra.tv.R;
import com.files.codes.database.DatabaseHelper;
import com.files.codes.model.subscription.ActiveStatus;
import com.files.codes.utils.PreferenceUtils;

import java.util.List;

public class ProfileFragment extends GuidedStepSupportFragment {
    private static final int ACTION_ID_SIGN_OUT = 1;
    private DatabaseHelper db;

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        GuidanceStylist.Guidance guidance;
        db = new DatabaseHelper(getActivity());
        if (PreferenceUtils.isLoggedIn(getContext())) {
            ActiveStatus activeStatus = db.getActiveStatusData();
            String packageTitle = "Package: " + activeStatus.getPackageTitle();
            String expireDate = "Valid till: " + activeStatus.getExpireDate();
            String des = packageTitle + "\n" + expireDate;

            guidance = new GuidanceStylist.Guidance(db.getUserData().getName(), des, db.getUserData().getEmail(), null);
        } else {
            guidance = new GuidanceStylist.Guidance(getResources().getString(R.string.something_went_wrong), "", "", null);
        }

        return guidance;
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        GuidedAction action = new GuidedAction.Builder(getActivity())
                .id(ACTION_ID_SIGN_OUT)
                .title(getResources().getString(R.string.signout))
                .editable(false)
                .build();
        actions.add(action);

    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        if (action.getId() == ACTION_ID_SIGN_OUT) {
            GuidedStepSupportFragment fragment =new  SignOutFragment();
            add(getFragmentManager(), fragment);
        }
    }


}
