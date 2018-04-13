package com.ubtrobot.upgrade;

public class UpgradeProgress {

    public static final UpgradeProgress DEFAULT = new UpgradeProgress.Builder(0).build();

    public static final int STATE_CHECKING_BEGAN = 1;
    public static final int STATE_CHECKING_ENDED = 2;
    public static final int STATE_UPGRADING_ALL_BEGAN = 3;
    public static final int STATE_UPGRADING_SINGLE_BEGAN = 4;
    public static final int STATE_UPGRADING_SINGLE_IN_PROGRESS = 5;
    public static final int STATE_UPGRADING_SINGLE_ENDED = 6;
    public static final int STATE_UPGRADING_ALL_ENDED = 7;

    private final int state;
    private int upgradingSingleProgress;
    private int upgradingAllProgress;
    private String upgradingFirmware;
    private int upgradingFirmwareOrder;
    private int remainingSeconds;
    private String description;
    private boolean willRoboot;

    private UpgradeProgress(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public boolean isCheckingBegan() {
        return state == STATE_CHECKING_BEGAN;
    }

    public boolean isCheckingEnded() {
        return state == STATE_CHECKING_ENDED;
    }

    public boolean isUpgradingAllBegan() {
        return state == STATE_UPGRADING_ALL_BEGAN;
    }

    public boolean isUpgradingSingleBegan() {
        return state == STATE_UPGRADING_SINGLE_BEGAN;
    }

    public boolean isUpgradingSingleInProgress() {
        return state == STATE_UPGRADING_SINGLE_IN_PROGRESS;
    }

    public boolean isUpgradingSingleEnded() {
        return state == STATE_UPGRADING_SINGLE_ENDED;
    }

    public boolean isUpgradingAllEnded() {
        return state == STATE_UPGRADING_ALL_ENDED;
    }

    public int getUpgradingSingleProgress() {
        return upgradingSingleProgress;
    }

    public int getUpgradingAllProgress() {
        return upgradingAllProgress;
    }

    public String getUpgradingFirmware() {
        return upgradingFirmware;
    }

    public int getUpgradingFirmwareOrder() {
        return upgradingFirmwareOrder;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public String getDescription() {
        return description;
    }

    public boolean willRoboot() {
        return willRoboot;
    }

    @Override
    public String toString() {
        return "UpgradeProgress{" +
                "state=" + state +
                ", upgradingSingleProgress=" + upgradingSingleProgress +
                ", upgradingAllProgress=" + upgradingAllProgress +
                ", upgradingFirmware='" + upgradingFirmware + '\'' +
                ", upgradingFirmwareOrder=" + upgradingFirmwareOrder +
                ", remainingSeconds=" + remainingSeconds +
                ", description='" + description + '\'' +
                ", willRoboot=" + willRoboot +
                '}';
    }

    public static class Builder {

        private final int state;
        private int upgradingSingleProgress;
        private int upgradingAllProgress;
        private String upgradingFirmware = "";
        private int upgradingFirmwareOrder;
        private int remainingSeconds;
        private String description = "";
        private boolean willRoboot;

        public Builder(int state) {
            this.state = state;
        }

        public Builder setUpgradingSingleProgress(int upgradingSingleProgress) {
            this.upgradingSingleProgress = upgradingSingleProgress;
            return this;
        }

        public Builder setUpgradingAllProgress(int upgradingAllProgress) {
            this.upgradingAllProgress = upgradingAllProgress;
            return this;
        }

        public Builder setUpgradingFirmware(String upgradingFirmware) {
            if (upgradingFirmware == null) {
                throw new IllegalArgumentException("Argument upgradingFirmware is null.");
            }

            this.upgradingFirmware = upgradingFirmware;
            return this;
        }

        public Builder setUpgradingFirmwareOrder(int upgradingFirmwareOrder) {
            this.upgradingFirmwareOrder = upgradingFirmwareOrder;
            return this;
        }

        public Builder setRemainingSeconds(int remainingSeconds) {
            this.remainingSeconds = remainingSeconds;
            return this;
        }

        public Builder setDescription(String description) {
            if (description == null) {
                throw new IllegalArgumentException("Argument description is null.");
            }

            this.description = description;
            return this;
        }

        public Builder setWillRoboot(boolean willRoboot) {
            this.willRoboot = willRoboot;
            return this;
        }

        public UpgradeProgress build() {
            UpgradeProgress progress = new UpgradeProgress(state);
            progress.upgradingSingleProgress = upgradingSingleProgress;
            progress.upgradingAllProgress = upgradingAllProgress;
            progress.upgradingFirmware = upgradingFirmware;
            progress.upgradingFirmwareOrder = upgradingFirmwareOrder;
            progress.remainingSeconds = remainingSeconds;
            progress.description = description;
            progress.willRoboot = willRoboot;
            return progress;
        }
    }
}
