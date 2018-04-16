package com.ubtrobot.motion;

import android.os.Handler;
import android.os.Looper;

import com.ubtrobot.master.adapter.ProtoCallAdapter;
import com.ubtrobot.master.context.MasterContext;
import com.ubtrobot.motion.ipc.MotionConstants;

import java.util.List;

public class MotionManager {

    private final JointList mJointList;

    public MotionManager(MasterContext masterContext) {
        if (masterContext == null) {
            throw new IllegalArgumentException("Argument masterContext is null.");
        }


        ProtoCallAdapter motionService = new ProtoCallAdapter(
                masterContext.createSystemServiceProxy(MotionConstants.SERVICE_NAME),
                new Handler(Looper.getMainLooper())
        );
        mJointList = new JointList(motionService);
    }

    /**
     * 获取关节列表
     *
     * @return 关节列表
     */
    public List<Joint> getJointList() {
        return mJointList.all();
    }

    /**
     * 获取某个关节
     *
     * @param jointId 关节 id
     * @return 关节
     */
    public Joint getJoint(String jointId) {
        return mJointList.get(jointId);
    }
}