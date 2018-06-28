package com.ubtrobot.mock;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

//import com.ubtrobot.analytics.mobile.AnalyticsKit;
//import com.ubtrobot.analytics.mobile.AnalyticsKit;
import com.ubtrobot.service.mock.R;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        /*AnalyticsKit.initialize(this, null,
                "appId", "appKey", "deviceId");*/
    }

    public void click(View view) {
        System.out.println("---click");
        for (int i = 0; i < 5; i++) {
//            AnalyticsKit.recordEvent("event00" + i);
        }
    }
}
