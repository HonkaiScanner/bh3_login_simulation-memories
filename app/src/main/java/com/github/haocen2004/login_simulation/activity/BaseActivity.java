package com.github.haocen2004.login_simulation.activity;

import androidx.appcompat.app.AppCompatActivity;

import com.github.haocen2004.login_simulation.util.DialogHelper;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onResume() {
        super.onResume();
        DialogHelper.getDialogHelper(this).ref();
    }
}
