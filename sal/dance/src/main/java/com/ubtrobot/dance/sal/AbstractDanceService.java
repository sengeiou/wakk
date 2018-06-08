package com.ubtrobot.dance.sal;

import android.content.Context;

import com.ubtrobot.async.InterruptibleAsyncTask;
import com.ubtrobot.async.Promise;
import com.ubtrobot.dance.DanceManager;
import com.ubtrobot.master.competition.InterruptibleTaskHelper;
import com.ubtrobot.play.PlayException;

import java.util.Set;

public class AbstractDanceService implements DanceService {

    private static final String TASK_RECEIVER_EXPRESSOR = "expressor";
    private static final String TASK_NAME_EXPRESS = "express";
    private static final String TASK_NAME_DISMISS = "dismiss";

    private final InterruptibleTaskHelper mInterruptibleTaskHelper;

    private DanceManager mDanceManager;
    private Promise<Void, PlayException> mPromise;

    public AbstractDanceService(Context context) {
        mDanceManager = new DanceManager(context);
        mInterruptibleTaskHelper = new InterruptibleTaskHelper();
    }

    @Override
    public Promise<Void, PlayException> express(final String danceName) {
        return mInterruptibleTaskHelper.start(
                TASK_RECEIVER_EXPRESSOR,
                TASK_NAME_EXPRESS,
                new InterruptibleAsyncTask<Void, PlayException>() {
                    @Override
                    protected void onStart() {
                        mPromise = mDanceManager.play(danceName);
                    }

                    @Override
                    protected void onCancel() {
                        // Ignore
                    }
                },
                new InterruptibleTaskHelper.InterruptedExceptionCreator<PlayException>() {
                    @Override
                    public PlayException
                    createInterruptedException(Set<String> interrupters) {
                        // todo
                        return new PlayException.Factory().
                                forbidden("Interrupted by " + interrupters, null);
                    }
                }
        );
    }

    @Override
    public Promise<Void, PlayException> dismiss() {
        return mInterruptibleTaskHelper.start(
                TASK_RECEIVER_EXPRESSOR,
                TASK_NAME_DISMISS,
                new InterruptibleAsyncTask<Void, PlayException>() {
                    @Override
                    protected void onStart() {
                        mPromise.cancel();
                        mPromise = null;
                    }

                    @Override
                    protected void onCancel() {
                        // Ignore
                    }
                },
                new InterruptibleTaskHelper.InterruptedExceptionCreator<PlayException>() {
                    @Override
                    public PlayException
                    createInterruptedException(Set<String> interrupters) {
                        // todo
                        return new PlayException.Factory().
                                forbidden("Interrupted by " + interrupters, null);
                    }
                }
        );
    }

}
