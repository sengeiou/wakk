package com.ubtrobot.play;

import com.ubtrobot.async.Promise;

public interface Player {

    Promise<Void, PlayException> play();
}