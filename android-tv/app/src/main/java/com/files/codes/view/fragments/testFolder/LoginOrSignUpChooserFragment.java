package com.files.codes.view.fragments.testFolder;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.leanback.app.GuidedStepSupportFragment;
import androidx.leanback.widget.GuidanceStylist;
import androidx.leanback.widget.GuidedAction;

import com.ekamra.tv.R;
import com.files.codes.view.LoginChooserActivity;
import com.files.codes.view.SignUpActivity;

import java.util.List;

public class LoginOrSignUpChooserFragment extends GuidedStepSupportFragment {
    private static final int ACTION_ID_POSITIVE = 1;
    private static final int ACTION_ID_NEGATIVE = ACTION_ID_POSITIVE + 1;

    @NonNull
    @Override
    public GuidanceStylist.Guidance onCreateGuidance(Bundle savedInstanceState) {
        // Provide the guidance text
        return new GuidanceStylist.Guidance(
                getString(R.string.login_or_signup_first),
                "",
                getString(R.string.app_name),
                null // You can set a Drawable icon here if needed
        );
    }

    @Override
    public void onCreateActions(@NonNull List<GuidedAction> actions, Bundle savedInstanceState) {
        // Create the "Login" button
        actions.add(new GuidedAction.Builder(getContext())
                .id(ACTION_ID_POSITIVE)
                .title(getString(R.string.login))
                .focusable(true) // Ensure it's focusable
                .build());

        // Create the "Sign Up" button
        actions.add(new GuidedAction.Builder(getContext())
                .id(ACTION_ID_NEGATIVE)
                .title(getString(R.string.sign_up))
                .focusable(true) // Ensure it's focusable
                .build());
    }

    @Override
    public void onGuidedActionClicked(GuidedAction action) {
        // Handle the click event for each button
        if (ACTION_ID_POSITIVE == action.getId()) {
            // Navigate to the login screen
            startActivity(new Intent(getContext(), LoginChooserActivity.class));
        } else if (ACTION_ID_NEGATIVE == action.getId()) {
            // Navigate to the sign-up screen
            startActivity(new Intent(getContext(), SignUpActivity.class));
        }
    }
}
