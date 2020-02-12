
package com.lister.nestedscrolltest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.lister.nestedscrolltest.bounce.BounceActivity;
import com.lister.nestedscrolltest.mounting.MountingActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_mounting).setOnClickListener(this);
        findViewById(R.id.btn_bounce).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_mounting:
                startActivity(new Intent(this, MountingActivity.class));
                break;
            case R.id.btn_bounce:
                startActivity(new Intent(this, BounceActivity.class));
                break;
        }
    }
}
