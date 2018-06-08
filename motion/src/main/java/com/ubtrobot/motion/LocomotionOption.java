package com.ubtrobot.motion;

public class LocomotionOption {

    private float movingSpeed;
    private float movingAngle;
    private float movingDistance;

    private float turningSpeed;
    private float turningAngle;

    private long duration;

    private LocomotionOption() {
    }

    public float getMovingSpeed() {
        return movingSpeed;
    }

    public float getMovingAngle() {
        return movingAngle;
    }

    public float getMovingDistance() {
        return movingDistance;
    }

    public float getTurningSpeed() {
        return turningSpeed;
    }

    public float getTurningAngle() {
        return turningAngle;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "LocomotionOption{" +
                "movingSpeed=" + movingSpeed +
                ", movingAngle=" + movingAngle +
                ", movingDistance=" + movingDistance +
                ", turningSpeed=" + turningSpeed +
                ", turningAngle=" + turningAngle +
                ", duration=" + duration +
                '}';
    }

    public static class Builder {

        private float movingSpeed;
        private float movingAngle;
        private float movingDistance;

        private float turningSpeed;
        private float turningAngle;

        private long duration;

        public Builder() {
        }

        public Builder setMovingSpeed(float movingSpeed) {
            if (movingSpeed < 0) {
                throw new IllegalArgumentException("Argument movingSpeed < 0.");
            }

            this.movingSpeed = movingSpeed;
            return this;
        }

        public Builder setMovingAngle(float movingAngle) {
            this.movingAngle = movingAngle;
            return this;
        }

        public Builder setMovingDistance(float movingDistance) {
            if (movingDistance < 0) {
                throw new IllegalArgumentException("Argument movingDistance < 0.");
            }

            this.movingDistance = movingDistance;
            return this;
        }

        public Builder setTurningSpeed(float turningSpeed) {
            this.turningSpeed = turningSpeed;
            return this;
        }

        public Builder setTurningAngle(float turningAngle) {
            this.turningAngle = turningAngle;
            return this;
        }

        public Builder setDuration(long duration) {
            if (duration < 0) {
                throw new IllegalArgumentException("Argument duration < 0.");
            }

            this.duration = duration;
            return this;
        }

        public LocomotionOption build() {
            LocomotionOption option = new LocomotionOption();
            option.movingSpeed = movingSpeed;
            option.movingAngle = movingAngle;
            option.movingDistance = movingDistance;
            option.turningSpeed = turningSpeed;
            option.turningAngle = turningAngle;
            option.duration = duration;

            return option;
        }
    }
}
