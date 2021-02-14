package com.github.haocen2004.login_simulation.Database;

import android.content.Context;

import com.tencent.bugly.crashreport.CrashReport;

import java.util.List;

import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class SponsorRepo {
    private final List<SponsorData> allSponsors;
    private final SponsorDao sponsorDao;

    public SponsorRepo(Context context) {
        SponsorDatabase sponsorDatabase = SponsorDatabase.getDatabase(context.getApplicationContext());
        sponsorDao = sponsorDatabase.getSponsorDao();
        allSponsors = sponsorDao.getAllSponsors();
    }

    public SponsorDao getSponsorDao() {
        return sponsorDao;
    }

    public void refreshSponsors() {
        ((Runnable) () -> {
            getSponsorDao().deleteAllSponsors();
            AVQuery<AVObject> query = new AVQuery<>("Sponsors");
            query.findInBackground().subscribe(new Observer<List<AVObject>>() {
                public void onSubscribe(Disposable disposable) {
                }

                public void onNext(List<AVObject> Sponsors) {
                    // students 是包含满足条件的 Student 对象的数组
                    for (AVObject object : Sponsors) {
                        getSponsorDao().insertSponsors(new SponsorData(object.getString("name"), object.getString("desc"), object.getString("avatarImgUrl"), object.getString("personalPageUrl"), object.getString("deviceId"), object.getString("scannerKey")));
                    }
                }

                public void onError(Throwable throwable) {
                    CrashReport.postCatchedException(throwable);
                }

                public void onComplete() {
                }
            });
        }).run();
    }


    public List<SponsorData> getAllSponsors() {
        return allSponsors;
    }

}
