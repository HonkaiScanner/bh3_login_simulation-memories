package com.github.haocen2004.login_simulation.data.database.announcement;

import android.content.Context;
import android.os.Looper;

import androidx.appcompat.app.AlertDialog;

import com.tencent.bugly.crashreport.CrashReport;

import java.util.List;

import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

public class AnnouncementRepo {
    private final Context context;
    private final AnnouncementDao announcementDao;
    private List<AnnouncementData> allAnnouncements;


    public AnnouncementRepo(Context context) {
        this.context = context;
        AnnouncementDatabase announcementDatabase = AnnouncementDatabase.getDatabase(context.getApplicationContext());
        announcementDao = announcementDatabase.getAnnouncementDao();
        allAnnouncements = announcementDao.getAllAnnouncements();
    }

    public AnnouncementDao getAnnouncementDao() {
        return announcementDao;
    }

    public void refreshAnnouncements() {
        new Thread(() -> {
            AVQuery<AVObject> query = new AVQuery<>("Announcements");
            query.findInBackground().subscribe(new Observer<List<AVObject>>() {
                public void onSubscribe(@NonNull Disposable disposable) {
                }

                public void onNext(@NonNull List<AVObject> Announcements) {
                    // students 是包含满足条件的 Student 对象的数组
                    new Thread(() -> {
                        Looper.prepare();
//                            getSponsorDao().deleteAllSponsors();
//                            for (AVObject object : Sponsors) {
//                                getSponsorDao().insertSponsors(new SponsorData(object.getString("name"), object.getString("desc"), object.getString("avatarImgUrl"), object.getString("personalPageUrl"), object.getString("deviceId"), object.getString("scannerKey")));
//                            }

                        for (AVObject object : Announcements) {
                            boolean hasData = false;
                            for (AnnouncementData data : allAnnouncements) {
                                if (data.getAid().equals(object.getInt("aid") + "")) {
                                    if (!data.getTime().equals(object.getDate("updatedAt").toString())) {
                                        data.setTime(object.getDate("updatedAt").toString());
                                        data.setTitle(object.getString("title"));
                                        data.setDesc(object.getString("desc"));
                                        data.setDisplay(object.getBoolean("display"));
                                        data.setLevel(object.getInt("level"));
                                        data.setChecked(true);
                                        if (object.getBoolean("reshow") && object.getBoolean("display")) {
                                            data.setReadable(true);
                                            showAnnDialog(data);
                                        }
                                        announcementDao.updateAnnouncement(data);
                                    }
                                    hasData = true;
                                }
                            }
                            if (!hasData) {
                                AnnouncementData data = new AnnouncementData(
                                        object.getInt("aid") + "",
                                        object.getInt("level"),
                                        object.getString("title"),
                                        object.getString("desc"),
                                        object.getDate("updatedAt").toString(),
                                        object.getBoolean("display")
                                );
                                if (object.getBoolean("reshow") && object.getBoolean("display")) {
                                    data.setReadable(true);
                                    showAnnDialog(data);
                                }
                                announcementDao.insertAnnouncement(data);

                            }
                        }
                        for (AnnouncementData data : allAnnouncements) {
                            if (!data.isChecked()) {
                                announcementDao.deleteAnnouncement(data);
                            }
                        }
                        allAnnouncements = announcementDao.getAllAnnouncements();
                        Looper.loop();
                    }).start();
                }

                public void onError(@NonNull Throwable throwable) {
                    CrashReport.postCatchedException(throwable);
                }

                public void onComplete() {
                }
            });
        }).start();
    }

    public List<AnnouncementData> getAllAnnouncements() {
        return allAnnouncements;
    }

    private void showAnnDialog(AnnouncementData data) {
        if (data.getReadable() && data.getDisplay()) {
            AlertDialog.Builder normalDialog = new AlertDialog.Builder(context);
            normalDialog.setTitle("公告: " + data.getTitle());
            normalDialog.setMessage(data.getDesc() + "\n\n更新日期：\n" + data.getTime());
            normalDialog.setPositiveButton("已读",
                    (dialog, which) -> {
                        data.setReadable(false);
                        announcementDao.updateAnnouncement(data);
                    });
            normalDialog.setCancelable(false);
            normalDialog.show();
        }
    }
}
