package com.ubtrobot.navigation;

public class LocateOption {

    public static final LocateOption DEFAULT = new LocateOption.Builder().build();

    private boolean useNearby;
    private Location nearby;
    private int timeout;

    private LocateOption() {
    }

    public boolean useNearby() {
        return useNearby;
    }

    public Location getNearby() {
        return nearby;
    }

    public int getTimeout() {
        return timeout;
    }

    @Override
    public String toString() {
        return "LocateOption{" +
                "useNearby=" + useNearby +
                ", nearby=" + nearby +
                ", timeout=" + timeout +
                '}';
    }

    public static final class Builder {

        private boolean useNearby;
        private Location nearby = Location.DEFAULT;
        private int timeout;

        public Builder() {
        }

        public Builder setNearby(Location nearBy) {
            if (nearBy == null) {
                throw new IllegalArgumentException("Argument nearBy is null.");
            }

            nearby = nearBy;
            useNearby = true;
            return this;
        }

        public Builder setTimeout(int timeout) {
            if (timeout < 0) {
                throw new IllegalArgumentException("Argument timeout < 0.");
            }

            this.timeout = timeout;
            return this;
        }

        public LocateOption build() {
            LocateOption option = new LocateOption();
            option.useNearby = useNearby;
            option.nearby = nearby;
            option.timeout = timeout;
            return option;
        }
    }

}
