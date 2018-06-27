package com.ubtrobot.framework.sample;

//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

//import com.ubtrobot.analytics.mobile.AnalyticsKit;

import java.util.HashMap;
import java.util.Map;
//
//import com.ubtrobot.analytics.mobile.AnalyticsKit;
//import com.ubtrobot.analytics.mobile.DeviceInfoUtils;

public class MainActivity extends AppCompatActivity {

    int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        AnalyticsKit.initialize(this, null, "appId", "appKey", "deviceId");
//        AnalyticsKit.initialize(this,null,"100010012",
//                "91eaee92605948c78c975cad475f02f5","d1001");
    }

    public void click(View view) {
//        DeviceInfoUtils.getDeviceId(this);
        System.out.println("----click");
        for (int i = 0; i < 5; i++) {
//            AnalyticsKit.recordEvent("100" + count);
            Map<String, String> cusMap = new HashMap<>();
            cusMap.put("s1", "s1001");
//            AnalyticsKit.recordEvent("100" + count, 5000, cusMap);
            count++;
        }
    }
}
