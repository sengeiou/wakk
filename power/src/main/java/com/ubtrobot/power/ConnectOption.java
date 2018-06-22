package com.ubtrobot.power;

public class ConnectOption {

    public static final ConnectOption DEFAULT = new Builder().build();

    private int timeout;

    private ConnectOption() {
    }

    public int getTimeout() {
        return timeout;
    }

    @Override
    public String toString() {
        return "ConnectOption{" +
                "timeout=" + timeout +
                '}';
    }

    public static class Builder {

        private int timeout;

        public Builder setTimeout(int timeout) {
            if (timeout < 0) {
                throw new IllegalArgumentException("Argument timeout < 0.");
            }

            this.timeout = timeout;
            return this;
        }

        public ConnectOption build() {
            ConnectOption option = new ConnectOption();
            option.timeout = timeout;
            return option;
        }
    }
}
