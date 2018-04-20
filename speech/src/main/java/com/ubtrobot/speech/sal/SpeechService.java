package com.ubtrobot.speech.sal;

import com.ubtrobot.async.Promise;
import com.ubtrobot.speech.RecognizeException;
import com.ubtrobot.speech.RecognizeOption;
import com.ubtrobot.speech.Recognizer;
import com.ubtrobot.speech.SynthesizeException;
import com.ubtrobot.speech.SynthesizeOption;
import com.ubtrobot.speech.Synthesizer;

public interface SpeechService {

    Promise<Void, SynthesizeException, Synthesizer.SynthesizingProgress> synthesize(
            String sentence, SynthesizeOption option);

    boolean isSynthesizing();

    Promise<Recognizer.RecognizeResult, RecognizeException, Recognizer.RecognizingProgress> recognize(
            RecognizeOption option);

    boolean isRecognizing();
}
