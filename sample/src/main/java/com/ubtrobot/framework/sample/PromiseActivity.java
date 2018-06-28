package com.ubtrobot.framework.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.AsyncTaskParallel;
import com.ubtrobot.async.AsyncTaskSeries;
import com.ubtrobot.async.DoneCallback;
import com.ubtrobot.async.FailCallback;
import com.ubtrobot.async.ProgressCallback;
import com.ubtrobot.async.Promise;
import com.ubtrobot.dance.DanceManager;
import com.ubtrobot.play.PlayException;
import com.ubtrobot.play.Track;

import java.util.Iterator;
import java.util.Map;

public class PromiseActivity extends AppCompatActivity {

    private static AsyncTaskSeries<PlayException> mTaskSeries;
    private static Promise<Void, PlayException> mPromise;
    private static AsyncTaskParallel<PlayException> mTaskParallel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void click(View view) {
        Log.i("promise", "click....");
        test();
        /*if (mTaskParallel != null) {
            mTaskParallel.cancel();
            mTaskParallel = null;
        }

        mTaskParallel = new AsyncTaskParallel<>();
        mTaskParallel.put("k1", new AsyncTask<Object, PlayException>() {
            @Override
            protected void onStart() {
                Log.i("promise", "k1");
                resolve(null);
            }
        });
        mTaskParallel.put("k2", new AsyncTask<Object, PlayException>() {
            @Override
            protected void onStart() {
                Log.i("promise", "k2");
            }

            @Override
            protected void onCancel() {
                super.onCancel();
                Log.i("promise", "k2 onCancel");
                resolve(null);
            }
        });

        mTaskParallel.start();
        mTaskParallel.promise().done(new DoneCallback<Void>() {
            @Override
            public void onDone(Void aVoid) {
                Log.i("promise", "Parallel onDone");
                mTaskParallel = null;
            }
        }).fail(new FailCallback<PlayException>() {
            @Override
            public void onFail(PlayException e) {
                Log.i("promise", "Parallel onFail");
            }
        }).progress(new ProgressCallback<Map.Entry<String,
                AsyncTaskParallel.DoneOrFail<Object, PlayException>>>() {
            @Override
            public void onProgress(Map.Entry<String,
                    AsyncTaskParallel.DoneOrFail<Object, PlayException>> stringDoneOrFailEntry) {
                Log.i("promise", "Parallel progress");
                mTaskParallel.cancel();
//                        mTaskParallel = null;
            }
        });*/
    }

    private void test() {
        if (mTaskSeries != null) {
            if (mPromise != null) {
                mPromise.cancel();
            }
            mPromise = null;
            mTaskSeries = null;
        }

        mTaskSeries = new AsyncTaskSeries<>();
        mTaskSeries.append(new AsyncTask<Void, PlayException>() {
            private AsyncTask<Void, PlayException> mTask = this;

            @Override
            protected void onStart() {
                if (mTaskParallel != null) {
                    mTaskParallel.cancel();
                    mTaskParallel = null;
                }

                mTaskParallel = new AsyncTaskParallel<>();
                mTaskParallel.put("k1", new AsyncTask<Object, PlayException>() {
                    @Override
                    protected void onStart() {
                        Log.i("promise", "k1");
                        resolve(null);
                    }
                });
                mTaskParallel.put("k2", new AsyncTask<Object, PlayException>() {
                    @Override
                    protected void onStart() {
                        Log.i("promise", "k2");
//                        resolve(null);
                    }

                    @Override
                    protected void onCancel() {
                        super.onCancel();
                        Log.i("promise", "k2 onCancel");
                        mTask.resolve(null);
                    }
                });

                mTaskParallel.start();
                mTaskParallel.promise().done(new DoneCallback<Void>() {
                    @Override
                    public void onDone(Void aVoid) {
                        Log.i("promise", "Parallel onDone");
                        mTaskParallel = null;
                        resolve(aVoid);
                    }
                }).fail(new FailCallback<PlayException>() {
                    @Override
                    public void onFail(PlayException e) {
                        Log.i("promise", "Parallel onFail");
                    }
                }).progress(new ProgressCallback<Map.Entry<String,
                        AsyncTaskParallel.DoneOrFail<Object, PlayException>>>() {
                    @Override
                    public void onProgress(Map.Entry<String,
                            AsyncTaskParallel.DoneOrFail<Object, PlayException>> stringDoneOrFailEntry) {
                        Log.i("promise", "Parallel progress");
                        mTaskParallel.cancel();
                    }
                });
            }
        });

        mTaskSeries.start();
        mTaskSeries.promise().done(new DoneCallback<Void>() {
            @Override
            public void onDone(Void aVoid) {
                Log.i("promise", "TaskSeries onDone");
            }
        });
    }
}
