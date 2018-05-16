package com.ubtrobot.speech.sal;

import com.ubtrobot.async.AsyncTask;
import com.ubtrobot.async.InterruptibleProgressiveAsyncTask;
import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.master.competition.InterruptibleTaskHelper;
import com.ubtrobot.speech.Configuration;
import com.ubtrobot.speech.RecognizeException;
import com.ubtrobot.speech.RecognizeOption;
import com.ubtrobot.speech.Recognizer;
import com.ubtrobot.speech.Speaker;
import com.ubtrobot.speech.SynthesizeException;
import com.ubtrobot.speech.SynthesizeOption;
import com.ubtrobot.speech.Synthesizer;
import com.ubtrobot.speech.UnderstandException;
import com.ubtrobot.speech.UnderstandOption;
import com.ubtrobot.speech.Understander;
import com.ubtrobot.speech.understand.UnderstandResult;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

import java.util.List;
import java.util.Set;

public abstract class AbstractSpeechService implements SpeechService {
    private static final Logger LOGGER = FwLoggerFactory.getLogger("AbstractSpeechService");

    private static final String TASK_RECEIVER_SYNTHESIZER = "receiver_synthesizer";
    private static final String TASK_NAME_SYNTHESIZE = "synthesize";

    private static final String TASK_RECEIVER_RECOGNIZER = "receiver_recognizer";
    private static final String TASK_NAME_RECOGNIZE = "recognize";

    private final InterruptibleTaskHelper mInterruptibleTaskHelper;

    public AbstractSpeechService() {
        mInterruptibleTaskHelper = new InterruptibleTaskHelper();
    }

    @Override
    public ProgressivePromise<Void, SynthesizeException, Synthesizer.SynthesizingProgress>
    synthesize(final String sentence, final SynthesizeOption option) {
        return mInterruptibleTaskHelper.start(
                TASK_RECEIVER_SYNTHESIZER, TASK_NAME_SYNTHESIZE,
                new InterruptibleProgressiveAsyncTask<
                        Void, SynthesizeException, Synthesizer.SynthesizingProgress>() {
                    @Override
                    protected void onCancel() {
                        stopSynthesizing();
                    }

                    @Override
                    protected void onStart() {
                        startSynthesizing(sentence, option);
                    }
                },
                new InterruptibleTaskHelper.InterruptedExceptionCreator<SynthesizeException>() {
                    @Override
                    public SynthesizeException
                    createInterruptedException(Set<String> interrupters) {
                        LOGGER.d("createInterruptedException");
                        return new SynthesizeException.Factory().interrupted(
                                "Interrupted by " + interrupters);
                    }
                }
        );
    }

    protected abstract void startSynthesizing(String sentence, SynthesizeOption option);

    protected abstract void stopSynthesizing();

    public void notifySynthesizingProgress(Synthesizer.SynthesizingProgress progress) {
        mInterruptibleTaskHelper.report(TASK_RECEIVER_SYNTHESIZER, TASK_NAME_SYNTHESIZE, progress);
    }

    public void resolveSynthesizing() {
        mInterruptibleTaskHelper.resolve(TASK_RECEIVER_SYNTHESIZER, TASK_NAME_SYNTHESIZE, null);
    }

    /**
     * Abort Stop 的情况统一回调这里
     *
     * @param e
     */
    public void rejectSynthesizing(final SynthesizeException e) {
        if (null == e) {
            throw new IllegalArgumentException("SynthesizeException must not be null.");
        }

        mInterruptibleTaskHelper.reject(TASK_RECEIVER_SYNTHESIZER, TASK_NAME_SYNTHESIZE, e);
    }

    @Override
    public boolean isSynthesizing() {
        return mInterruptibleTaskHelper.isRunning(TASK_RECEIVER_SYNTHESIZER, TASK_NAME_SYNTHESIZE);
    }

    @Override
    public ProgressivePromise<Recognizer.RecognizeResult, RecognizeException, Recognizer.RecognizingProgress> recognize(
            final RecognizeOption option) {
        return mInterruptibleTaskHelper.start(TASK_RECEIVER_RECOGNIZER, TASK_NAME_RECOGNIZE,
                new InterruptibleProgressiveAsyncTask<
                        Recognizer.RecognizeResult, RecognizeException, Recognizer.RecognizingProgress>() {
                    @Override
                    protected void onCancel() {
                        stopRecognizing();
                    }

                    @Override
                    protected void onStart() {
                        startRecognizing(option);
                    }
                },
                new InterruptibleTaskHelper.InterruptedExceptionCreator<RecognizeException>() {
                    @Override
                    public RecognizeException createInterruptedException(Set<String> interrupters) {
                        return new RecognizeException.Factory().interrupted(
                                "Interrupted by " + interrupters);
                    }
                });
    }

    protected abstract void startRecognizing(RecognizeOption option);

    protected abstract void stopRecognizing();

    @Override
    public boolean isRecognizing() {
        return mInterruptibleTaskHelper.isRunning(TASK_RECEIVER_RECOGNIZER, TASK_NAME_RECOGNIZE);
    }

    public void notifyRecognizingProgress(Recognizer.RecognizingProgress progress) {
        mInterruptibleTaskHelper.report(TASK_RECEIVER_RECOGNIZER, TASK_NAME_RECOGNIZE, progress);
    }

    public void resolveRecognizing(Recognizer.RecognizeResult done) {
        mInterruptibleTaskHelper.resolve(TASK_RECEIVER_RECOGNIZER, TASK_NAME_RECOGNIZE, done);
    }

    /**
     * Abort Stop 的情况统一回调这里
     *
     * @param e
     */
    public void rejectRecognizing(final RecognizeException e) {
        if (null == e) {
            throw new IllegalArgumentException("RecognizeException must not be null.");
        }

        mInterruptibleTaskHelper.reject(TASK_RECEIVER_RECOGNIZER, TASK_NAME_RECOGNIZE, e);
    }

    @Override
    public Promise<Understander.UnderstandResult, UnderstandException> understand(final String question, UnderstandOption option) {
        AsyncTask<Understander.UnderstandResult, UnderstandException> task = createUnderstandTask(question, option);
        if (task == null) {
            throw new IllegalStateException("createUnderstandTask return null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<Understander.UnderstandResult, UnderstandException>
    createUnderstandTask(String question, UnderstandOption option);

    @Override
    public Promise<List<Speaker>, AccessServiceException> getSpeakerList() {
        AsyncTask<List<Speaker>, AccessServiceException> task = createGetSpeakerListTask();

        if (task == null) {
            throw new IllegalStateException("createGetSpeakerListTask return null.");
        }

        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<List<Speaker>, AccessServiceException> createGetSpeakerListTask();

    @Override
    public Promise<Configuration, AccessServiceException> getConfiguration() {
        AsyncTask<Configuration, AccessServiceException> task = createGetConfigurationTask();
        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<Configuration, AccessServiceException> createGetConfigurationTask();

    @Override
    public Promise<Void, AccessServiceException> setConfiguration(Configuration configuration) {
        AsyncTask<Void, AccessServiceException> task = createSetConfigurationTask(configuration);
        task.start();
        return task.promise();
    }

    protected abstract AsyncTask<Void, AccessServiceException> createSetConfigurationTask(Configuration configuration);
}
