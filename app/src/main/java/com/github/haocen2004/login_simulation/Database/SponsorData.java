package com.github.haocen2004.login_simulation.Database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Sponsors")
public class SponsorData {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo
    private String name;
    @ColumnInfo
    private String desc;
    @ColumnInfo
    private String avatarImgUrl;
    @ColumnInfo
    private String avatarImgMeta; //SHA1
    @ColumnInfo
    private String personalPageUrl;
    @ColumnInfo
    private String deviceId;
    @ColumnInfo
    private String scannerKey;

    public SponsorData(String name, String desc, String avatarImgUrl, String personalPageUrl, String deviceId, String scannerKey) {
        this.name = name;
        this.desc = desc;
        this.avatarImgUrl = avatarImgUrl;
        this.personalPageUrl = personalPageUrl;
        this.deviceId = deviceId;
        this.scannerKey = scannerKey;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getAvatarImgUrl() {
        return avatarImgUrl;
    }

    public void setAvatarImgUrl(String avatarImgUrl) {
        this.avatarImgUrl = avatarImgUrl;
    }

    public String getAvatarImgMeta() {
        return avatarImgMeta;
    }

    public void setAvatarImgMeta(String avatarImgMeta) {
        this.avatarImgMeta = avatarImgMeta;
    }

    public String getPersonalPageUrl() {
        return personalPageUrl;
    }

    public void setPersonalPageUrl(String personalPageUrl) {
        this.personalPageUrl = personalPageUrl;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getScannerKey() {
        return scannerKey;
    }

    public void setScannerKey(String scannerKey) {
        this.scannerKey = scannerKey;
    }
}
