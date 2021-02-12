package com.github.haocen2004.login_simulation.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.haocen2004.login_simulation.Adapter.SupportTabLayoutAdapter;
import com.github.haocen2004.login_simulation.Database.SponsorData;
import com.github.haocen2004.login_simulation.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.ArrayList;
import java.util.List;

import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class SupportFragment extends Fragment {
    private SupportTabLayoutAdapter supportTabLayoutAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        ((CollapsingToolbarLayout) requireActivity().findViewById(R.id.collapsingToolbarLayout))
                .setTitle(getString(R.string.list_pay));
        return inflater.inflate(R.layout.fragment_support, container, false);


    }
    // TODO:从网络拉取赞助者数据存入本地数据库
    // 然后LiveData异步加载到ViewPager2
    // 有个数据值用于标记数据库版本 放在更新检测中拉取

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        openUrl("https://afdian.net/@Haocen20004", requireActivity());
        super.onViewCreated(view, savedInstanceState);
        RecyclerView recyclerView = requireActivity().findViewById(R.id.recyclerView);
        TabLayout tabLayout = requireActivity().findViewById(R.id.tabLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false));
        supportTabLayoutAdapter = new SupportTabLayoutAdapter();
        initAdapter(supportTabLayoutAdapter);
        recyclerView.setAdapter(supportTabLayoutAdapter);
    }
    
    private void initAdapter(SupportTabLayoutAdapter adapter){
        List<SponsorData> sponsorDataOld = new ArrayList<>();
        sponsorDataOld.add(new SponsorData("placeholder","desc","a","b","c","d"));
        adapter.setAllSponsors(sponsorDataOld);
        ((Runnable) () -> {
            List<SponsorData> sponsorData = new ArrayList<>();
            AVQuery<AVObject> query = new AVQuery<>("Sponsors");
            query.findInBackground().subscribe(new Observer<List<AVObject>>() {
                public void onSubscribe(Disposable disposable) {}
                public void onNext(List<AVObject> Sponsors) {
                    // students 是包含满足条件的 Student 对象的数组
                    for (AVObject object: Sponsors ) {
                        sponsorData.add(new SponsorData(object.getString("name"),object.getString("desc"),object.getString("avatarImgUrl"),object.getString("personalPageUrl"),object.getString("deviceId"),object.getString("scannerKey")));

                    }
                    adapter.setAllSponsors(sponsorData);
                    adapter.notifyDataSetChanged();
                }
                public void onError(Throwable throwable) {
                    CrashReport.postCatchedException(throwable);
                }
                public void onComplete() {}
            });

        }).run();
    }
}