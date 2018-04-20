package com.ubtrobot.speech.sal;

import com.ubtrobot.async.Promise;
import com.ubtrobot.speech.SynthesizeException;
import com.ubtrobot.speech.SynthesizeOption;
import com.ubtrobot.speech.Synthesizer;

public interface SpeechService {

    Promise<Void, SynthesizeException, Synthesizer.SynthesizingProgress> synthesize(
            String sentence, SynthesizeOption option);

    boolean isSynthesizing();
}
