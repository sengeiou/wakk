package com.ubtrobot.upgrade;

/**
 * 升级检测选项
 */
public class DetectOption {

    private final boolean remoteOrLocalSource;
    private String localSourcePath;

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

    @Override
    public String toString() {
        return "DetectOption{" +
                "remoteOrLocalSource=" + remoteOrLocalSource +
                ", localSourcePath='" + localSourcePath + '\'' +
                '}';
    }

    public static class Builder {

        private boolean remoteOrLocalSource;
        private String localSourcePath;

        public Builder(boolean remoteOrLocalSource) {
            this.remoteOrLocalSource = remoteOrLocalSource;
        }

        public Builder setLocalSourcePath(String localSourcePath) {
            this.localSourcePath = localSourcePath;
            return this;
        }

        public DetectOption build() {
            DetectOption option = new DetectOption(remoteOrLocalSource);
            option.localSourcePath = localSourcePath;
            return option;
        }
    }
}
