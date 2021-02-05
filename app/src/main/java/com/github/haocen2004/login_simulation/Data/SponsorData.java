package com.github.haocen2004.login_simulation.Data;

public class SponsorData {
    private final String name;
    private final String desc;
    private final String avatarImgUrl;
    private final String personalPageUrl;
    private final String deviceId;
    private final String scannerKey;

    public SponsorData(String name, String desc, String avatarImgUrl, String personalPageUrl, String deviceId, String scannerKey) {
        this.name = name;
        this.desc = desc;
        this.avatarImgUrl = avatarImgUrl;
        this.personalPageUrl = personalPageUrl;
        this.deviceId = deviceId;
        this.scannerKey = scannerKey;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public String getAvatarImgUrl() {
        return avatarImgUrl;
    }

    public String getPersonalPageUrl() {
        return personalPageUrl;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getScannerKey() {
        return scannerKey;
    }

}
