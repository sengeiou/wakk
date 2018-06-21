package com.ubtrobot.speech;

public interface RecognizeListener {
    //这个地方需要讨论是否保留两接口
    void onRecognizeBegin();

    void onRecognizing(Recognizer.RecognizingProgress progress);

    //这个地方需要讨论是否保留两接口
    void onRecognizeEnd();

    void onRecognizeComplete(Recognizer.RecognizeResult result);

    void OnRecognizeError(RecognizeException e);

}
