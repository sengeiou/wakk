package com.ubtrobot.speech.sal;

import com.ubtrobot.async.ProgressivePromise;
import com.ubtrobot.async.Promise;
import com.ubtrobot.exception.AccessServiceException;
import com.ubtrobot.speech.Configuration;
import com.ubtrobot.speech.RecognizeException;
import com.ubtrobot.speech.RecognizeListener;
import com.ubtrobot.speech.RecognizeOption;
import com.ubtrobot.speech.Recognizer;
import com.ubtrobot.speech.Speaker;
import com.ubtrobot.speech.SynthesizeException;
import com.ubtrobot.speech.SynthesizeOption;
import com.ubtrobot.speech.Synthesizer;
import com.ubtrobot.speech.UnderstandException;
import com.ubtrobot.speech.UnderstandOption;
import com.ubtrobot.speech.UnderstandResult;

import java.util.List;

public interface SpeechService {

    ProgressivePromise<Void, SynthesizeException, Synthesizer.SynthesizingProgress> synthesize(
            String sentence, SynthesizeOption option);

    boolean isSynthesizing();

    ProgressivePromise<Recognizer.RecognizeResult, RecognizeException, Recognizer.RecognizingProgress> recognize(
            RecognizeOption option);

    boolean isRecognizing();

    Promise<UnderstandResult, UnderstandException> understand(String question, UnderstandOption option);

    Promise<List<Speaker>, AccessServiceException> getSpeakerList();

    Promise<Configuration, AccessServiceException> getConfiguration();

    Promise<Void, AccessServiceException> setConfiguration(Configuration configuration);

    void registerRecognizeListener(RecognizeListener listener);
    void unregisterRecognizeListener(RecognizeListener listener);
}
