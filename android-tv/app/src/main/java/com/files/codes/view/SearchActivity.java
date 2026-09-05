package com.files.codes.view;


import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.ekamra.tv.R;
import com.files.codes.view.fragments.SearchFragment;
import com.files.codes.view.fragments.testFolder.LeanbackActivity;

public class SearchActivity extends LeanbackActivity {
    private SearchFragment searchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment);
    }


    @Override
    public boolean onSearchRequested() {
        if (searchFragment.hasResults()){
            startActivity(new Intent(this, SearchActivity.class));
        }else{
            searchFragment.startRecognition();
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT && !searchFragment.hasResults()){
            searchFragment.focusOnSearch();
        }
        return super.onKeyDown(keyCode, event);
    }
}