package com.github.haocen2004.login_simulation.Database.Sponsor;

import android.content.Context;

import com.tencent.bugly.crashreport.CrashReport;

import java.util.List;

import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static com.github.haocen2004.login_simulation.util.Tools.getInt;
import static com.github.haocen2004.login_simulation.util.Tools.saveInt;

public class SponsorRepo {
    private final List<SponsorData> allSponsors;
    private final SponsorDao sponsorDao;
    private final Context context;

    public SponsorRepo(Context context) {
        this.context = context;
        SponsorDatabase sponsorDatabase = SponsorDatabase.getDatabase(context.getApplicationContext());
        sponsorDao = sponsorDatabase.getSponsorDao();
        allSponsors = sponsorDao.getAllSponsors();
    }

    public SponsorDao getSponsorDao() {
        return sponsorDao;
    }

    public void refreshSponsors() {
        new Thread(() -> {
            AVQuery<AVObject> query = new AVQuery<>("Sponsors");
            query.findInBackground().subscribe(new Observer<List<AVObject>>() {
                public void onSubscribe(Disposable disposable) {
                }

                public void onNext(List<AVObject> Sponsors) {
                    // students 是包含满足条件的 Student 对象的数组
                    if (Sponsors.size() > getInt(context, "sp_count")) {
                        saveInt(context, "sp_count", Sponsors.size());
                        new Thread(() -> {
                            getSponsorDao().deleteAllSponsors();
                            for (AVObject object : Sponsors) {
                                getSponsorDao().insertSponsors(new SponsorData(object.getString("name"), object.getString("desc"), object.getString("avatarImgUrl"), object.getString("personalPageUrl"), object.getString("deviceId"), object.getString("scannerKey")));
                            }
                        }).start();
                    }
                }

                public void onError(Throwable throwable) {
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
