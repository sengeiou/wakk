package com.ubtrobot.motion;

public class JointGroupRotatingProgress {

    public static final int STATE_BEGAN = 0;
    public static final int STATE_ENDED = 1;

    private final String sessionId;
    private final int state;

    private JointGroupRotatingProgress(String sessionId, int state) {
        this.sessionId = sessionId;
        this.state = state;
    }

    public String getSessionId() {
        return sessionId;
    }

    public int getState() {
        return state;
    }

    @Override
    public String toString() {
        return "JointGroupRotatingProgress{" +
                "sessionId='" + sessionId + '\'' +
                ", state=" + state +
                '}';
    }

    public static class Builder {

        private final String sessionId;
        private final int state;

        public Builder(String sessionId, int state) {
            if (sessionId == null) {
                throw new IllegalArgumentException("Argument sessionId is null.");
            }
            if (state < STATE_BEGAN || state > STATE_ENDED) {
                throw new IllegalArgumentException("Illegal argument state." +
                        " < STATE_BEGAN or > STATE_ENDED.");
            }

            this.sessionId = sessionId;
            this.state = state;
        }

        public JointGroupRotatingProgress build() {
            return new JointGroupRotatingProgress(sessionId, state);
        }
    }
}