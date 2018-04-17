package com.ubtrobot.speech.sal;

import com.ubtrobot.async.InterruptibleAsyncTask;
import com.ubtrobot.async.Promise;
import com.ubtrobot.master.competition.InterruptibleTaskHelper;
import com.ubtrobot.speech.SynthesizeException;
import com.ubtrobot.speech.SynthesizeOption;
import com.ubtrobot.speech.Synthesizer;
import com.ubtrobot.ulog.FwLoggerFactory;
import com.ubtrobot.ulog.Logger;

public abstract class AbstractSpeechService implements SpeechService {

    private static final Logger LOGGER = FwLoggerFactory.getLogger("AbstractSpeechService");

    private static final String TASK_RECEIVER_SYNTHESIZER = "receiver_synthesizer";
    private static final String TASK_NAME_SYNTHESIZE = "synthesize";

    private final InterruptibleTaskHelper mInterruptibleTaskHelper;

    public AbstractSpeechService() {
        mInterruptibleTaskHelper = new InterruptibleTaskHelper();
    }

    @Override
    public Promise<Void, SynthesizeException, Synthesizer.SynthesizingProgress>
    synthesize(final String sentence, final SynthesizeOption option) {
        return mInterruptibleTaskHelper.start(TASK_RECEIVER_SYNTHESIZER, TASK_NAME_SYNTHESIZE,
                new InterruptibleAsyncTask<Void, SynthesizeException, Synthesizer.SynthesizingProgress>() {
                    @Override
                    protected void onCancel() {
                        stopSynthesizing();
                    }

                    @Override
                    protected void onStart() {
                        startSynthesizing(sentence, option);
                    }
                }, new InterruptibleTaskHelper.InterruptedExceptionCreator<SynthesizeException>() {
                    @Override
                    public SynthesizeException createInterruptedException(String interrupter) {
                        return new SynthesizeException.Factory().interrupted("Interrupt the " +
                                interrupter + " task.");
                    }
                });
    }

    protected abstract void startSynthesizing(String sentence, SynthesizeOption option);

    protected abstract void stopSynthesizing();

    public void notifySynthesizingProgress(Synthesizer.SynthesizingProgress progress) {
        mInterruptibleTaskHelper.notify(TASK_RECEIVER_SYNTHESIZER, TASK_NAME_SYNTHESIZE, progress);
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
}
