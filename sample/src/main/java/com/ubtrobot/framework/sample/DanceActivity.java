package com.ubtrobot.framework.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ubtrobot.async.DoneCallback;
import com.ubtrobot.async.FailCallback;
import com.ubtrobot.async.Promise;
import com.ubtrobot.dance.DanceManager;
import com.ubtrobot.play.PlayException;

public class DanceActivity extends AppCompatActivity {

    private DanceManager danceManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        danceManager = new DanceManager(this);

    }

    public void click(View view) {
        System.out.println("----- dance click");
        danceManager.play("小提琴").done(new DoneCallback<Void>() {
            @Override
            public void onDone(Void aVoid) {
                System.out.println("----完成");
            }
        }).fail(new FailCallback<PlayException>() {
            @Override
            public void onFail(PlayException e) {
                System.out.println("---失败");
            }
        });
    }
}
