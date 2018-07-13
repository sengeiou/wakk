package com.ubtrobot.emotion.sal;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.InterruptibleAsyncTask;
import com.ubtrobot.async.Promise;
import com.ubtrobot.emotion.Emotion;
import com.ubtrobot.emotion.ExpressException;
import com.ubtrobot.emotion.ExpressOption;
import com.ubtrobot.emotion.ipc.EmotionConstants;
import com.ubtrobot.emotion.ipc.EmotionConverters;
import com.ubtrobot.emotion.ipc.EmotionProto;
import com.ubtrobot.emotion.ipc.master.EmotionSystemService;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.competition.InterruptibleTaskHelper;
import com.ubtrobot.master.context.ContextRunnable;
import com.ubtrobot.master.param.ProtoParam;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.List;
import java.util.Set;

public abstract class AbstractEmotionService implements EmotionService {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("emotion");

    private static final String TASK_RECEIVER_EXPRESSOR = "expressor";
    private static final String TASK_NAME_EXPRESS = "express";
    private static final String TASK_NAME_DISMISS = "dismiss";

    private final InterruptibleTaskHelper mInterruptibleTaskHelper;

    public AbstractEmotionService() {
        mInterruptibleTaskHelper = new InterruptibleTaskHelper();
    }

    @Override
    public Promise<List<Emotion>, AccessServiceException> getEmotionList() {
        AsyncTask<List<Emotion>, AccessServiceException> task = createGetEmotionListTask();
        if (task == null) {
            throw new IllegalArgumentException("Argument task is null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<List<Emotion>, AccessServiceException>
    createGetEmotionListTask();

    @Override
    public Promise<Void, ExpressException>
    express(final String emotionId, final ExpressOption option) {
        return mInterruptibleTaskHelper.start(
                TASK_RECEIVER_EXPRESSOR,
                TASK_NAME_EXPRESS,
                new InterruptibleAsyncTask<Void, ExpressException>() {
                    @Override
                    protected void onStart() {
                        doStartExpressing(emotionId, option);
                    }

                    @Override
                    protected void onCancel() {
                        // Ignore
                    }
                },
                new InterruptibleTaskHelper.InterruptedExceptionCreator<ExpressException>() {
                    @Override
                    public ExpressException
                    createInterruptedException(Set<String> interrupters) {
                        return new ExpressException.Factory().interrupted(
                                "Interrupted by " + interrupters);
                    }
                }
        );
    }

    protected abstract void doStartExpressing(String emotionId, ExpressOption expressOption);

    public void resolveExpressing() {
        mInterruptibleTaskHelper.resolve(TASK_RECEIVER_EXPRESSOR, TASK_NAME_EXPRESS, null);
    }

    public void rejectExpressing(ExpressException e) {
        if (e == null) {
            throw new IllegalArgumentException("Argument e is null.");
        }

        mInterruptibleTaskHelper.reject(TASK_RECEIVER_EXPRESSOR, TASK_NAME_EXPRESS, e);
    }

    @Override
    public Promise<Void, ExpressException> dismiss() {
        return mInterruptibleTaskHelper.start(
                TASK_RECEIVER_EXPRESSOR,
                TASK_NAME_DISMISS,
                new InterruptibleAsyncTask<Void, ExpressException>() {
                    @Override
                    protected void onStart() {
                        doStartDismissing();
                    }

                    @Override
                    protected void onCancel() {
                        // Ignore
                    }
                },
                new InterruptibleTaskHelper.InterruptedExceptionCreator<ExpressException>() {
                    @Override
                    public ExpressException
                    createInterruptedException(Set<String> interrupters) {
                        return new ExpressException.Factory().interrupted(
                                "Interrupted by " + interrupters);
                    }
                }
        );
    }

    protected abstract void doStartDismissing();

    public void resolveDismissing() {
        mInterruptibleTaskHelper.resolve(TASK_RECEIVER_EXPRESSOR, TASK_NAME_DISMISS, null);
        publishEmotionDismiss();
    }

    public void rejectDismissing(ExpressException e) {
        if (e == null) {
            throw new IllegalArgumentException("Argument e is null.");
        }

        mInterruptibleTaskHelper.reject(TASK_RECEIVER_EXPRESSOR, TASK_NAME_DISMISS, e);
        publishEmotionDismiss();
    }

    protected void publishEmotionDismiss() {
        publishEmotionState(false);
    }

    private void publishEmotionState(final boolean isExpresser) {
        boolean emotionSystemServiceStarted = Master.get().execute(
                EmotionSystemService.class,
                new ContextRunnable<EmotionSystemService>() {
                    @Override
                    public void run(EmotionSystemService emotionSystemService) {
                        ProtoParam<EmotionProto.EmotionState> param = ProtoParam.create(
                                EmotionConverters.toEmotionStateProto(isExpresser));

                        emotionSystemService.publish(
                                EmotionConstants.ACTION_EMOTION_EXPRESS_CHANGE, param);
                    }
                }
        );

        if (!emotionSystemServiceStarted) {
            LOGGER.e("Publish Emotion failed. Pls start EmotionSystemService first.");
        }
    }
}
