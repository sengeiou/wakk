package com.ubtrobot.upgrade;

/**
 * 升级检测选项
 */
public class DetectOption {

    private final boolean remoteOrLocalSource;
    private String localSourcePath;
    private long timeout;

    private DetectOption(boolean remoteOrLocalSource) {
        this.remoteOrLocalSource = remoteOrLocalSource;
    }

    /**
     * 固件检测的源是远程还是本地
     *
     * @return true 为远程，false 为本地
     */
    public boolean isRemoteOrLocalSource() {
        return remoteOrLocalSource;
    }

    /**
     * 获取本地固件检测源的路径
     *
     * @return 本地固件检测源的路径
     */
    public String getLocalSourcePath() {
        return localSourcePath;
    }

    /**
     * 获取检测超时时间
     *
     * @return 超时时间（毫秒数）
     */
    public long getTimeout() {
        return timeout;
    }

    @Override
    public String toString() {
        return "DetectOption{" +
                "remoteOrLocalSource=" + remoteOrLocalSource +
                ", localSourcePath='" + localSourcePath + '\'' +
                ", timeout=" + timeout +
                '}';
    }

    public static class Builder {

        private boolean remoteOrLocalSource;
        private String localSourcePath;
        private long timeout;

        public Builder(boolean remoteOrLocalSource) {
            this.remoteOrLocalSource = remoteOrLocalSource;
        }

        public Builder setLocalSourcePath(String localSourcePath) {
            this.localSourcePath = localSourcePath;
            return this;
        }

        public Builder setTimeout(long timeout) {
            this.timeout = timeout;
            return this;
        }

        public DetectOption build() {
            DetectOption option = new DetectOption(remoteOrLocalSource);
            option.localSourcePath = localSourcePath;
            option.timeout = timeout;
            return option;
        }
    }
}
