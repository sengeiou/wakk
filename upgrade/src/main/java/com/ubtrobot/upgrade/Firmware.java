package com.ubtrobot.upgrade;

/**
 * 固件
 */
public class Firmware {

    private static final Firmware DEFAULT = new Firmware.Builder("", "").build();

    private String name;
    private String version;
    private long upgradeTime;
    private FirmwarePackage currentPackage;

    private Firmware(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public long getUpgradeTime() {
        return upgradeTime;
    }

    /**
     * 获取固件当前烧录或安装的固件包
     *
     * @return 固件包
     */
    public FirmwarePackage getCurrentPackage() {
        return currentPackage == null ? FirmwarePackage.DEFAULT : currentPackage;
    }

    @Override
    public String toString() {
        return "Firmware{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", upgradeTime=" + upgradeTime +
                ", currentPackage=" + currentPackage +
                '}';
    }

    public static class Builder {

        private final String name;
        private final String version;
        private long upgradeTime;
        private FirmwarePackage currentPackage;

        public Builder(String name, String version) {
            if (name == null || version == null) {
                throw new IllegalArgumentException("Argument name or version is null.");
            }

            this.name = name;
            this.version = version;
        }

        public Builder setUpgradeTime(long upgradeTime) {
            this.upgradeTime = upgradeTime;
            return this;
        }

        public Builder setCurrentPackage(FirmwarePackage currentPackage) {
            if (currentPackage == null) {
                throw new IllegalArgumentException("Argument currentPackage is null.");
            }

            this.currentPackage = currentPackage;
            return this;
        }

        public Firmware build() {
            Firmware firmware = new Firmware(name, version);
            firmware.upgradeTime = upgradeTime;
            firmware.currentPackage = currentPackage;
            return firmware;
        }
    }
}