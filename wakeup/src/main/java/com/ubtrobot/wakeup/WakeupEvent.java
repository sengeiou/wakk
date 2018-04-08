package com.ubtrobot.wakeup;

public class WakeupEvent {

    public static final int TYPE_VOICE = 0;
    public static final int TYPE_SIMULATE = 1;
    public static final int TYPE_VISION = 2;

    private static final int ANGLE_MAX = 180;
    private static final int ANGLE_MIN = -180;

    /**
     * 唤醒类型
     */
    private int type;

    /**
     * 角度制
     */
    private int angle;

    /**
     * 唤醒距离
     */
    private float distance;

    private WakeupEvent(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public int getAngle() {
        return angle;
    }

    public float getDistance() {
        return distance;
    }

    public static class Builder {

        private int type;

        private int angle;

        private float distance;

        public Builder(int type) {
            checkType(type);
            this.type = type;
        }

        public Builder(WakeupEvent event) {
            if (null == event) {
                throw new IllegalArgumentException("Event must not be null.");
            }

            type = event.getType();
            angle = event.getAngle();
            distance = event.getDistance();
        }

        public Builder setAngle(int angle) {
            checkAngle(angle);
            this.angle = angle;
            return this;
        }

        public Builder setDistance(float distance) {
            checkDistance(distance);
            this.distance = distance;
            return this;
        }

        public WakeupEvent build() {
            WakeupEvent event = new WakeupEvent(type);
            event.angle = angle;
            event.distance = distance;
            return event;
        }

        private void checkType(int type) {
            if (type != TYPE_VOICE && type != TYPE_SIMULATE && type != TYPE_VISION) {
                throw new IllegalArgumentException("Invalid type value, verify for WakeupEvent.TYPE_XXX.");
            }
        }

        private void checkAngle(int angle) {
            if (angle < ANGLE_MIN || angle > ANGLE_MAX) {
                throw new IllegalArgumentException("Invalid angle value, verify for [-180,180].");
            }
        }

        private void checkDistance(float distance) {
            if (distance < 0) {
                throw new IllegalArgumentException("Invalid angle value, verify for distance > 0.");
            }
        }
    }

    @Override
    public String toString() {
        return "WakeupEvent{" +
                "type=" + type +
                ", angle=" + angle +
                ", distance=" + distance +
                '}';
    }
}
