package com.ubtrobot.navigation;

public class LocateOption {

    public static final LocateOption DEFAULT = new LocateOption.Builder().build();

    private boolean useNearby;
    private Location nearby;

    private LocateOption() {
    }

    public boolean useNearby() {
        return useNearby;
    }

    public Location getNearby() {
        return nearby;
    }

    public static final class Builder {

        private boolean useNearby;
        private Location nearby = Location.DEFAULT;

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

        public LocateOption build() {
            LocateOption option = new LocateOption();
            option.useNearby = useNearby;
            option.nearby = nearby;
            return option;
        }
    }

}
