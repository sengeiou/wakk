package com.ubtrobot.dance.sal;

import com.ubtrobot.async.Promise;
import com.ubtrobot.play.PlayException;

public interface DanceService {

    Promise<Void, PlayException> express(String danceName);

    Promise<Void, PlayException> dismiss();
}
