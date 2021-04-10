package com.github.haocen2004.login_simulation.Database.Announcement;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "Announcement")
public class AnnouncementData {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo
    private String aid;
    @ColumnInfo
    private String title;
    @ColumnInfo
    private String desc;
    @ColumnInfo
    private String time;
    @ColumnInfo
    private Boolean readable;
    @ColumnInfo
    private Boolean display;
    @ColumnInfo
    private int level;

    public AnnouncementData(String aid, Integer level, String title, String desc, String time) {
        this.aid = aid;
        this.level = level;
        this.title = title;
        this.desc = desc;
        this.time = time;
        readable = true;
        display = true;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Boolean getReadable() {
        return readable;
    }

    public void setReadable(Boolean readable) {
        this.readable = readable;
    }

    public Boolean getDisplay() {
        return display;
    }

    public void setDisplay(Boolean display) {
        this.display = display;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "AnnouncementData{" +
                "id=" + id +
                ", aid='" + aid + '\'' +
                ", title='" + title + '\'' +
                ", desc='" + desc + '\'' +
                ", time='" + time + '\'' +
                ", readable=" + readable +
                ", display=" + display +
                ", level=" + level +
                '}';
    }
}

