package com.files.codes.view.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.ekamra.tv.R;
import com.files.codes.database.DatabaseHelper;
import com.files.codes.model.subscription.ActiveStatus;
import com.files.codes.utils.Constants;
import com.files.codes.utils.PreferenceUtils;
import com.files.codes.view.LoginChooserActivity;
import com.files.codes.view.MainActivity;
import com.files.codes.view.fragments.testFolder.GridFragment;

public class MyAccountFragment extends GridFragment  {
    private final String TAG = "MyAccountFragment";
    private Button sign_out, login;
    private TextView user_name;
    private TextView user_email;
    private TextView expire_date;
    private TextView active_plan;
    private TextView status_tv;
    private DatabaseHelper db;
    private LinearLayout userDataLayout;


    public MyAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_account, container, false);
        db = new DatabaseHelper(getContext());
        initViews(view);

        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), LoginChooserActivity.class));
                //getActivity().finish();
            }
        });
        return view;
    }

    private void initViews(View view) {
        sign_out = view.findViewById(R.id.sign_out_button);
        login = view.findViewById(R.id.login_button);
        user_name = view.findViewById(R.id.userNameTv);
        user_email = view.findViewById(R.id.userEmailTv);
        active_plan = view.findViewById(R.id.activePlanTv);
        expire_date = view.findViewById(R.id.expireDateTv);
        userDataLayout = view.findViewById(R.id.user_data_layout);
        status_tv = view.findViewById(R.id.status_tv);

        Log.e(TAG, "initViews: isUserLoggedIn: " + PreferenceUtils.isLoggedIn(getContext()));
        if (PreferenceUtils.isLoggedIn(getContext())) {
            login.setVisibility(View.GONE);
            user_name.setText(db.getUserData().getName());
            user_email.setText(db.getUserData().getEmail());
            ActiveStatus activeStatus = db.getActiveStatusData();
            active_plan.setText(activeStatus.getPackageTitle());
            expire_date.setText(activeStatus.getExpireDate());
        } else {
            userDataLayout.setVisibility(View.GONE);
            login.setVisibility(View.VISIBLE);
            user_name.setVisibility(View.GONE);
            user_email.setVisibility(View.GONE);
            sign_out.setVisibility(View.GONE);
            status_tv.setText(R.string.you_are_not_logged_in);
        }
    }

    private void signOut() {
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
        String userId = databaseHelper.getUserData().getUserId();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseAuth.getInstance().signOut();
        }
        if (userId != null) {
            SharedPreferences.Editor editor = getContext().getSharedPreferences(Constants.USER_LOGIN_STATUS, MODE_PRIVATE).edit();
            editor.putBoolean(Constants.USER_LOGIN_STATUS, false);
            editor.apply();

            databaseHelper.deleteUserData();
            PreferenceUtils.clearSubscriptionSavedData(getContext());

            startActivity(new Intent(getContext(), MainActivity.class));
            getActivity().finish();
        }
    }

}
