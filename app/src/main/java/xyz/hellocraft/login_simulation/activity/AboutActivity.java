package xyz.hellocraft.login_simulation.activity;

import static xyz.hellocraft.login_simulation.data.Constant.FA;
import static xyz.hellocraft.login_simulation.data.Constant.VISITOR_COUNT;

import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import com.drakeet.about.Card;
import com.drakeet.about.Category;
import com.drakeet.about.Contributor;
import com.drakeet.about.License;
import com.github.haocen2004.login_simulation.BuildConfig;
import com.github.haocen2004.login_simulation.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import xyz.hellocraft.login_simulation.utils.Logger;
import xyz.hellocraft.login_simulation.utils.Tools;

public class AboutActivity extends BaseAbsActivity {

    private int counter = 0;

    @Override
    protected void onCreateHeader(@NonNull ImageView icon, @NonNull TextView slogan, @NonNull TextView version) {
        if (FA) {
            icon.setImageResource(R.mipmap.ic_launcher_3);
        } else {
            icon.setImageResource(R.mipmap.ic_launcher);
        }
        icon.setOnClickListener(v -> {
            SharedPreferences app_pref = PreferenceManager.getDefaultSharedPreferences(this);

            if (!app_pref.getBoolean("no_crash_page", false)) {
                if (app_pref.getBoolean("adv_setting", false)) {
                    if (counter < 20) {
                        if (counter >= 10) {
                            Logger.getLogger(this).makeToast("还需点击 " + (20 - counter) + " 下");
                        }
                        counter++;
                    } else {
                        app_pref.edit().putBoolean("no_crash_page", true).apply();
                        Logger.getLogger(this).makeToast("已关闭崩溃界面显示");
                    }
                }
            } else {
                Logger.getLogger(this).makeToast("您已关闭崩溃界面显示！");
            }
        });
        slogan.setText(getApplicationInfo().loadLabel(getPackageManager()));
        version.setOnClickListener(v -> {
            SharedPreferences app_pref = PreferenceManager.getDefaultSharedPreferences(this);

            if (!app_pref.getBoolean("adv_setting", false)) {
                if (counter < 7) {
                    if (counter >= 3) {
                        Logger.getLogger(this).makeToast("还需点击 " + (7 - counter) + " 下");
                    }
                    counter++;
                } else {
                    app_pref.edit().putBoolean("adv_setting", true).apply();
                    Logger.getLogger(this).makeToast("已启用高级设置");
                }
            } else {
                Logger.getLogger(this).makeToast("您已经启用高级设置了！");
            }
        });
        version.setText(BuildConfig.VERSION_NAME);
    }

    @Override
    protected void onItemsCreated(@NonNull List<Object> items) {
        items.add(new Category(getString(R.string.about_title)));
        items.add(new Card(getString(R.string.about_description)));

        items.add(new Category(getString(R.string.about_developer)));
        items.add(new Contributor(R.drawable.author, "Hao_cen", "Developer", "https://github.com/Haocen2004"));

        items.add(new Category(getString(R.string.about_repo)));
        items.add(new Card("Github\nhttps://github.com/HonkaiScanner/bh3_login_simulation-memories"));
        items.add(new Card("BiliBili\nhttps://space.bilibili.com/269140934"));
//        items.add(new Card("QQ Group\n" + QQ_GROUP_URL));

        items.add(new Category(getString(R.string.about_open_source)));
        items.add(new License("MultiType", "drakeet", License.APACHE_2, "https://github.com/drakeet/MultiType"));
        items.add(new License("about-page", "drakeet", License.APACHE_2, "https://github.com/drakeet/about-page"));
        items.add(new License("WeChatQRCode", "jenly1314", License.APACHE_2, "https://github.com/jenly1314/WeChatQRCode"));
        items.add(new License("opencv", "opencv", License.APACHE_2, "https://github.com/opencv/opencv"));
        items.add(new License("Bugly", "Tencent", "UNKNOWN", "https://github.com/BuglyDevTeam/Bugly-Android"));
        items.add(new License("BuglyBetaUploader", "Tencent", License.APACHE_2, "https://github.com/BuglyDevTeam/BuglyBetaUploader"));
        items.add(new License("java-unified-sdk", "leancloud", License.APACHE_2, "https://github.com/leancloud/java-unified-sdk"));
        items.add(new License("RxJava", "ReactiveX", License.APACHE_2, "https://github.com/ReactiveX/RxJava"));
        items.add(new License("Toaster", "getActivity", License.APACHE_2, "https://github.com/getActivity/Toaster"));
        items.add(new License("EasyWindow", "getActivity", License.APACHE_2, "https://github.com/getActivity/EasyWindow"));
        items.add(new License("sensebot", "Geetest", License.APACHE_2, "https://github.com/GeeTeam/gt3-android-sdk"));
        items.add(new License("glide", "Google", License.APACHE_2, "https://github.com/bumptech/glide"));
        items.add(new License("RikkaX", "Rikka", License.MIT, "https://github.com/RikkaApps/RikkaX"));
        items.add(new License("StringFog", "MegatronKing", License.APACHE_2, "https://github.com/MegatronKing/StringFog"));
        items.add(new License("Android_CN_OAID", "gzu-liyujiang", "http://license.coscl.org.cn/MulanPSL2", "https://github.com/MegatronKing/StringFog"));
        items.add(new License("Gson", "Google", License.APACHE_2, "https://github.com/google/gson"));
        items.add(new License("okhttp", "square", License.APACHE_2, "https://github.com/square/okhttp"));
        items.add(new License("AndroidX", "Google", License.APACHE_2, "https://github.com/androidx-releases"));

        items.add(new Category(getString(R.string.about_assets)));
        items.add(new License("主应用图标", "Peano", "Pixiv", "https://www.pixiv.net/artworks/77505884"));
        items.add(new License("抽屉上方Banner", "艾晓萌", "Pixiv", "https://www.pixiv.net/artworks/89418903"));
        items.add(new License("应用图标-A", "miHoYo", "游戏Pv截取", "https://www.bh3.com/"));

        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            long installedTime = System.currentTimeMillis() - packageInfo.firstInstallTime;
            int installedDay = (int) (installedTime / 1000 / 60 / 60 / 24);
            items.add(new Category("扫码器已经陪伴了您 " + installedDay + " 天"));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            items.add(new Category("初次安装时间: " + dateFormat.format(new Date(packageInfo.firstInstallTime))));
        } catch (PackageManager.NameNotFoundException ignore) {
        }
        items.add(new Category("您已使用扫码器成功登陆 " + Tools.getInt(this, "succ_count") + " 次"));
        items.add(new Category(Tools.getUUID(this)));
        items.add(new Category(Tools.getString(this, "installationId")));
        if (VISITOR_COUNT != 0) {
            items.add(new Category("累计有 " + VISITOR_COUNT + " 台设备使用过扫码器 (统计访问更新服务的IP)"));
        }
    }
}
