package com.example.ngfngf.sudokulock;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, PassWordFragment.newInstance(PassWordFragment.TYPE_SETTING)).commit();
        Toast.makeText(this, "MainActivity", Toast.LENGTH_SHORT).show();
    }
}
