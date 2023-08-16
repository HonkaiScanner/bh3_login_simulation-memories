package com.github.haocen2004.login_simulation.data.sponsor.database;

import android.content.Context;

import com.github.haocen2004.login_simulation.utils.Logger;
import com.tencent.bugly.crashreport.CrashReport;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class SponsorRepo {
    private final List<SponsorData> allSponsors;
    private final SponsorDao sponsorDao;

    public SponsorRepo(Context context) {
        SponsorDatabase sponsorDatabase = SponsorDatabase.Companion.getInstance(context.getApplicationContext());
        sponsorDao = sponsorDatabase.sponsorDao();
        allSponsors = sponsorDao.getAllSponsors();
    }

    public void reset() {
        sponsorDao.deleteAllSponsors();
    }

    public void refreshSponsors() {
        new Thread(() -> {
            LCQuery<LCObject> query = new LCQuery<>("Sponsors");
            query.limit(500);
            query.orderByAscending("createdAt");

            query.findInBackground().subscribe(new Observer<>() {
                public void onSubscribe(@NotNull Disposable disposable) {
                }

                public void onNext(@NotNull List<LCObject> Sponsors) {
                    new Thread(() -> {
                        Logger.d("update sponsor", "get " + Sponsors.size() + " sponsors data");
                        List<String> scanner_keys = new ArrayList<>();
                        for (LCObject object : Sponsors) {
                            scanner_keys.add(object.getString("scannerKey"));
                            boolean hasData = false;
                            for (SponsorData sponsorData : allSponsors) {
                                if (sponsorData.getScannerKey().equals(object.getString("scannerKey"))) {
//                                    Log.d("sponsorUpdater","updating "+object.getString("name"));
                                    hasData = true;
                                    sponsorData.setName(object.getString("name"));
                                    sponsorData.setDesc(object.getString("desc"));
                                    sponsorData.setAvatarImgUrl(object.getString("avatarImgUrl"));
                                    sponsorData.setPersonalPageUrl(object.getString("personalPageUrl"));
                                    sponsorData.setDeviceId(object.getString("deviceId"));
                                    sponsorDao.updateSponsors(sponsorData);
                                }
                            }
                            if (!hasData) {
                                sponsorDao.insertSponsors(
                                        new SponsorData(
                                                object.getString("name"),
                                                object.getString("desc"),
                                                object.getString("avatarImgUrl"),
                                                object.getString("personalPageUrl"),
                                                object.getString("deviceId"),
                                                object.getString("scannerKey")
                                        )
                                );
                            }
                        }

                        for (SponsorData sponsorData : allSponsors) {
                            if (!scanner_keys.contains(sponsorData.getScannerKey())) {
                                sponsorDao.deleteSponsors(sponsorData);
                            }
                        }

                    }).start();

                }

                public void onError(@NotNull Throwable throwable) {
                    Logger.getLogger(null).makeToast(throwable.getMessage());
                    throwable.printStackTrace();
                    CrashReport.postCatchedException(throwable);
                }

                public void onComplete() {
                }
            });
        }).start();
    }


    public List<SponsorData> getAllSponsors() {
        return allSponsors;
    }

}
