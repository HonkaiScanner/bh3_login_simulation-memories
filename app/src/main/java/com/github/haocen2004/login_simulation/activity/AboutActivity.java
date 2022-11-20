package com.github.haocen2004.login_simulation.activity;

import static com.github.haocen2004.login_simulation.util.Constant.QQ_GROUP_URL;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.drakeet.about.AbsAboutActivity;
import com.drakeet.about.Card;
import com.drakeet.about.Category;
import com.drakeet.about.Contributor;
import com.drakeet.about.License;
import com.github.haocen2004.login_simulation.BuildConfig;
import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.util.Tools;

import java.util.List;

public class AboutActivity extends AbsAboutActivity {
    @Override
    protected void onCreateHeader(@NonNull ImageView icon, @NonNull TextView slogan, @NonNull TextView version) {
        icon.setImageResource(R.mipmap.ic_launcher);
        slogan.setText(getApplicationInfo().loadLabel(getPackageManager()));
        version.setText(BuildConfig.VERSION_NAME);
    }

    @Override
    protected void onItemsCreated(@NonNull List<Object> items) {
        items.add(new Category(getString(R.string.about_title)));
        items.add(new Card(getString(R.string.about_description)));

        items.add(new Category(getString(R.string.about_developer)));
        items.add(new Contributor(R.drawable.author, "Hao_cen", "Developer", "https://github.com/Haocen2004"));

        items.add(new Category(getString(R.string.about_repo)));
        items.add(new Card("Github\nhttps://github.com/HonkaiScanner/bh3_login_simulation"));
        items.add(new Card("BiliBili\nhttps://space.bilibili.com/269140934"));
        items.add(new Card("QQ Group\n" + QQ_GROUP_URL));

        items.add(new Category(getString(R.string.about_open_source)));
        items.add(new License("MultiType", "drakeet", License.APACHE_2, "https://github.com/drakeet/MultiType"));
        items.add(new License("about-page", "drakeet", License.APACHE_2, "https://github.com/drakeet/about-page"));
        items.add(new License("WeChatQRCode", "jenly1314", License.APACHE_2, "https://github.com/jenly1314/WeChatQRCode"));
        items.add(new License("opencv", "opencv", License.APACHE_2, "https://github.com/opencv/opencv"));
        items.add(new License("Bugly", "Tencent", "UNKNOWN", "https://github.com/BuglyDevTeam/Bugly-Android"));
        items.add(new License("java-unified-sdk", "leancloud", License.APACHE_2, "https://github.com/leancloud/java-unified-sdk"));
        items.add(new License("RxJava", "ReactiveX", License.APACHE_2, "https://github.com/ReactiveX/RxJava"));
        items.add(new License("ToastUtils", "getActivity", License.APACHE_2, "https://github.com/getActivity/ToastUtils"));
        items.add(new License("XToast", "getActivity", License.APACHE_2, "https://github.com/getActivity/XToast"));
        items.add(new License("sensebot", "Geetest", License.APACHE_2, "https://github.com/GeeTeam/gt3-android-sdk"));
        items.add(new License("glide", "Google", License.APACHE_2, "https://github.com/bumptech/glide"));
        items.add(new License("Gson", "Google", License.APACHE_2, "https://github.com/google/gson"));
        items.add(new License("okhttp", "square", License.APACHE_2, "https://github.com/square/okhttp"));
        items.add(new License("AndroidX", "Google", License.APACHE_2, "https://github.com/androidx-releases"));

        items.add(new Category("您已使用扫码器成功登陆 " + Tools.getInt(this, "succ_count") + " 次"));
        items.add(new Category(Tools.getString(this, "uuid")));
    }

}
