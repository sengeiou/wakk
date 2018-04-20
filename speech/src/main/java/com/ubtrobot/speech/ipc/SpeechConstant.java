package com.ubtrobot.speech.ipc;

public class SpeechConstant {

    private SpeechConstant() {
    }

    public static final String SERVICE_NAME = "speech";

    public final static String CALL_PATH_SYNTHESIZE = "/speech/synthesize";
    public final static String CALL_PATH_SYNTHESIZING = "/speech/synthesize/doing";

    public final static String CALL_PATH_RECOGNIZE = "/speech/recognize";
    public final static String CALL_PATH_RECOGNIZING = "/speech/recognize/doing";

    public static final String COMPETING_ITEM_SYNTHESIZER = "synthesizer";
    public static final String COMPETING_ITEM_RECOGNIZER = "recognizer";
}
