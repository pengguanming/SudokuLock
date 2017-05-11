package com.example.ngfngf.sudokulock;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * Created by ngfngf on 2017/4/7.
 */

public class WelcomeActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weclcome_layout);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initSP();
                Toast.makeText(WelcomeActivity.this, "WelcomeActivity", Toast.LENGTH_LONG).show();
            }
        }, 1000);
    }

    private void initSP() {
        SharedPreferences sp = getSharedPreferences("sp", Context.MODE_PRIVATE);
        String passWord = sp.getString("passWord", "");
        //没有密码
        if (TextUtils.isEmpty(passWord)) {
            Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            //密码检查
            getSupportFragmentManager().beginTransaction().replace(android.R.id.content, PassWordFragment.newInstance(PassWordFragment.TYPE_CHECK)).commit();
        }
    }
}
