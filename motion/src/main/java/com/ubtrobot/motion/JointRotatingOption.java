package com.ubtrobot.motion;

public class JointRotatingOption {

    private String jointId;
    private float angle;
    private boolean angleAbsolute;
    private long duration;
    private float speed;

    private JointRotatingOption() {
    }

    public String getJointId() {
        return jointId;
    }

    public float getAngle() {
        return angle;
    }

    public boolean isAngleAbsolute() {
        return angleAbsolute;
    }

    public long getDuration() {
        return duration;
    }

    public float getSpeed() {
        return speed;
    }

    public Builder toBuilder() {
        Builder builder = new Builder();
        builder.jointId = jointId;
        builder.angle = angle;
        builder.angleAbsolute = angleAbsolute;
        builder.duration = duration;
        builder.speed = speed;
        return builder;
    }

    @Override
    public String toString() {
        return "JointRotatingOption{" +
                "jointId='" + jointId + '\'' +
                ", angle=" + angle +
                ", angleAbsolute=" + angleAbsolute +
                ", duration=" + duration +
                ", speed=" + speed +
                '}';
    }

    public static class Builder {

        private String jointId;
        private float angle;
        private boolean angleAbsolute;
        private long duration;
        private float speed;

        public Builder setJointId(String jointId) {
            this.jointId = jointId;
            return this;
        }

        public Builder setAngle(float angle) {
            if (angle == 0) {
                throw new IllegalArgumentException("Argument angle == 0.");
            }
            if (duration > 0 && speed != 0) {
                throw new IllegalStateException("Already setSpeed and setDuration.");
            }

            this.angle = angle;
            return this;
        }

        public Builder setAngleAbsolute(boolean angleAbsolute) {
            this.angleAbsolute = angleAbsolute;
            return this;
        }

        public Builder setDuration(long duration) {
            if (duration < 0) {
                throw new IllegalArgumentException("Argument duration < 0.");
            }
            if (duration > 0 && angle != 0 && speed != 0) {
                throw new IllegalStateException("Already setAngle and setSpeed.");
            }

            this.duration = duration;
            return this;
        }

        public Builder setSpeed(float speed) {
            if (speed == 0) {
                throw new IllegalArgumentException("Argument speed <= 0.");
            }
            if (angle != 0 && duration > 0) {
                throw new IllegalStateException("Already setAngle and setDuration.");
            }

            this.speed = speed;
            return this;
        }

        public JointRotatingOption build() {
            JointRotatingOption option = new JointRotatingOption();
            option.jointId = jointId == null ? "" : jointId;
            option.angle = angle;
            option.angleAbsolute = angleAbsolute;
            option.duration = duration;
            option.speed = speed;
            return option;
        }
    }
}
