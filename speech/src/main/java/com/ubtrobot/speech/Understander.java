package com.ubtrobot.speech;

import com.ubtrobot.async.Promise;

public interface Understander {

    Promise<UnderstandResult, UnderstandException>
    understand(String question, UnderstandOption option);
}