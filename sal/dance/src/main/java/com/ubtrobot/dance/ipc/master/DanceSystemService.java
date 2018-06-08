package com.ubtrobot.dance.ipc.master;

import android.app.Application;
import android.os.Handler;

import com.google.protobuf.Message;
import com.ubtrobot.async.Promise;
import com.ubtrobot.dance.Dance;
import com.ubtrobot.dance.DanceManager;
import com.ubtrobot.dance.ipc.DanceConstants;
import com.ubtrobot.dance.sal.AbstractDanceService;
import com.ubtrobot.dance.sal.DanceFactory;
import com.ubtrobot.dance.sal.DanceService;
import com.ubtrobot.emotion.ExpressException;
import com.ubtrobot.master.Master;
import com.ubtrobot.master.adapter.CallProcessAdapter;
import com.ubtrobot.master.adapter.ProtoCallProcessAdapter;
import com.ubtrobot.master.annotation.Call;
import com.ubtrobot.master.competition.CompetingCallDelegate;
import com.ubtrobot.master.competition.CompetingItemDetail;
import com.ubtrobot.master.competition.ProtoCompetingCallDelegate;
import com.ubtrobot.master.service.MasterSystemService;
import com.ubtrobot.play.PlayException;
import com.ubtrobot.transport.message.CallException;
import com.ubtrobot.transport.message.Request;
import com.ubtrobot.transport.message.Responder;

import java.util.Collections;
import java.util.List;

public class DanceSystemService extends MasterSystemService {

    private DanceService mService;
    private ProtoCompetingCallDelegate mCompetingCallDelegate;

    @Override
    protected void onServiceCreate() {
        Application application = getApplication();
        if (!(application instanceof DanceFactory)) {
            throw new IllegalStateException(
                    "Your application should implement EmotionFactory interface.");
        }

        mService = ((DanceFactory) application).createDanceService();
        if (mService == null || !(mService instanceof AbstractDanceService)) {
            throw new IllegalStateException("Your application 's createEmotionService returns null" +
                    " or does not return a instance of AbstractEmotionService.");
        }

        Handler handler = new Handler(getMainLooper());
        mCompetingCallDelegate = new ProtoCompetingCallDelegate(this, handler);
    }

    @Override
    protected List<CompetingItemDetail> getCompetingItems() {
        return Collections.singletonList(new CompetingItemDetail.Builder(
                getName(), DanceConstants.COMPETING_ITEM_EXPRESSER).
                setDescription("Competing item for expressing emotion.").
                addCallPath(DanceConstants.CALL_PATH_PLAY_RANDOM_DANCE).
                build()
        );
    }

    @Call(path = DanceConstants.CALL_PATH_DANCE_LIST)
    public void onGetDanceList(Request request, Responder responder) {
        // TODO 获取舞蹈列表
    }

    @Call(path = DanceConstants.CALL_PATH_STOP_DANCE)
    public void onStopDance(Request request, Responder responder) {
        // TODO 停止跳舞
        mCompetingCallDelegate.onCall(request,
                DanceConstants.COMPETING_ITEM_EXPRESSER,
                responder,
                new CompetingCallDelegate.SessionCallable<Void, PlayException>() {
                    @Override
                    public Promise<Void, PlayException>
                    call() throws CallException {
                        return mService.dismiss();
                    }
                },
                new ProtoCompetingCallDelegate.FConverter<PlayException>() {
                    @Override
                    public CallException convertFail(PlayException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                });

    }

    @Call(path = DanceConstants.CALL_PATH_LAST_DANCE)
    public void onLastDance(Request request, Responder responder) {
        // TODO 上一个舞蹈

    }

    @Call(path = DanceConstants.CALL_PATH_NEXT_DANCE)
    public void onNextDance(Request request, Responder responder) {
        // TODO 下一个舞蹈

    }

    @Call(path = DanceConstants.CALL_PATH_AGAIN_DANCE)
    public void onAgainDance(Request request, Responder responder) {
        // TODO 再跳一次

    }

    @Call(path = DanceConstants.CALL_PATH_PLAY_RANDOM_DANCE)
    public void onPlayDance(Request request, Responder responder) {
        // TODO 随机跳个舞： 不区分舞蹈种类
        // todo
        final String danceName = "跳舞";
        System.out.println("----service: 跳舞");
        mCompetingCallDelegate.onCall(
                request,
                DanceConstants.COMPETING_ITEM_EXPRESSER,
                responder,
                new CompetingCallDelegate.SessionCallable<Void, PlayException>() {
                    @Override
                    public Promise<Void, PlayException> call() throws CallException {
                        return mService.express(danceName);
                    }
                },
                new ProtoCompetingCallDelegate.FConverter<PlayException>() {
                    @Override
                    public CallException convertFail(PlayException e) {
                        return new CallException(e.getCode(), e.getMessage());
                    }
                }
        );
    }

    // TODO 区分舞蹈种类的跳舞
}
