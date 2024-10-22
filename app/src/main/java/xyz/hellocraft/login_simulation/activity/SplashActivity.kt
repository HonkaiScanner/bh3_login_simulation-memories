package xyz.hellocraft.login_simulation.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import cn.leancloud.LCInstallation
import cn.leancloud.LCLogger
import cn.leancloud.LCObject
import cn.leancloud.LCUser
import cn.leancloud.LeanCloud
import cn.leancloud.push.PushService
import com.github.haocen2004.login_simulation.BuildConfig
import com.github.haocen2004.login_simulation.R
import com.github.haocen2004.login_simulation.databinding.ActivitySplashBinding
import com.king.wechat.qrcode.WeChatQRCodeDetector
import com.tencent.upgrade.core.DefaultUpgradeStrategyRequestCallback
import com.tencent.upgrade.core.UpgradeManager
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.opencv.OpenCV
import xyz.hellocraft.login_simulation.data.Constant
import xyz.hellocraft.login_simulation.data.Constant.BH_VER
import xyz.hellocraft.login_simulation.data.Constant.CHECK_VER
import xyz.hellocraft.login_simulation.data.dialog.ButtonData
import xyz.hellocraft.login_simulation.data.dialog.DialogData
import xyz.hellocraft.login_simulation.data.dialog.DialogLiveData
import xyz.hellocraft.login_simulation.data.sponsor.database.SponsorRepo
import xyz.hellocraft.login_simulation.utils.DeviceFpManager
import xyz.hellocraft.login_simulation.utils.DialogHelper
import xyz.hellocraft.login_simulation.utils.ForegroundCallbacks
import xyz.hellocraft.login_simulation.utils.Logger
import xyz.hellocraft.login_simulation.utils.Network
import xyz.hellocraft.login_simulation.utils.Tools

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity(), ForegroundCallbacks.Listener {

    private var continueBoot = true
    private var needBoot = false
    private lateinit var appPref: SharedPreferences
    private lateinit var binding: ActivitySplashBinding

    override fun onPause() {
        super.onPause()
        continueBoot = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
        ForegroundCallbacks.get(this).addListener(this)
        ViewCompat.setOnApplyWindowInsetsListener(binding.splashMain) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        appPref = PreferenceManager.getDefaultSharedPreferences(this)

        if (BuildConfig.VERSION_NAME.contains("dev") || BuildConfig.DEBUG) {
            appendText("\nDEBUG MODE ENABLE\n")
            LeanCloud.setLogLevel(LCLogger.Level.DEBUG)
        }
        try {
            if (!Constant.HAS_UPDATE_THREAD) {
                OpenCV.initAsync(this)
                WeChatQRCodeDetector.init(this)
            }
        } catch (e: UnsatisfiedLinkError) {
            Logger.e("ABI", "Wrong ABI")
            e.printStackTrace()

            val supportedABI = StringBuilder()
            for (abi in Build.SUPPORTED_ABIS) {
                supportedABI.append(abi)
                supportedABI.append('\n')
            }
            val dialogData = DialogData(
                "错误",
                "你所下载的版本可能不支持在当前设备上运行\n请下载正确的版本\n\n参考数据:\n$supportedABI"
            )
            dialogData.setPositiveButtonData("我已知晓")
            DialogLiveData.getINSTANCE().addNewDialog(dialogData)
            continueBoot = false
            return
        }
        lifecycleScope.launch(Dispatchers.IO) {
            val sponsorChecker = async {
                if (CHECK_VER) {
                    SponsorRepo(applicationContext).refreshSponsors()
                    if (appPref.getBoolean("has_account", false)) {

                        Logger.d("sponsor login check", "Start.")
                        LCUser.becomeWithSessionTokenInBackground(
                            appPref.getString(
                                "account_token", ""
                            )
                        ).subscribe(object : Observer<LCUser> {
                            override fun onSubscribe(disposable: Disposable) {
                            }

                            override fun onNext(user: LCUser) {
                                LCUser.changeCurrentUser(user, true)
                                Constant.HAS_ACCOUNT = true
                                if (user.username == "Hao_cen") {
                                    Logger.d("PUSH", "admin registering")
                                    PushService.subscribe(
                                        this@SplashActivity,
                                        "admin",
                                        NotificationActivity::class.java
                                    )
                                } else {
                                    PushService.unsubscribe(this@SplashActivity, "admin")
                                }

                                LCInstallation.getCurrentInstallation().saveInBackground()
                                    .subscribe(object : Observer<LCObject> {
                                        override fun onSubscribe(d: Disposable) {
                                        }

                                        override fun onNext(lcObject: LCObject) {
                                            val installationId =
                                                LCInstallation.getCurrentInstallation().installationId
                                            Tools.saveString(
                                                applicationContext, "installationId", installationId
                                            )
                                            Logger.d("PUSH", "admin registered")
                                        }

                                        override fun onError(e: Throwable) {
                                        }

                                        override fun onComplete() {
                                        }
                                    })
                                appPref.edit().putBoolean("has_account", true)
                                    .putString("custom_username", user.getString("custom_username"))
                                    .apply()
                                Logger.d("sponsor login check", "Succeed.")
                                Constant.SP_CHECKED = true
                                appendText("Sponsor Checked")
                            }

                            override fun onError(throwable: Throwable) {
                                LCUser.changeCurrentUser(null, true)
                                appPref.edit().putBoolean("has_account", false)
                                    .putString("custom_username", "崩坏3扫码器用户").apply()
                                throwable.printStackTrace()
                                Logger.d("sponsor login check", "Failed. Reset custom username")
                                Logger.getLogger(this@SplashActivity)
                                    .makeToast("赞助者身份验证已过期...")
                                Constant.SP_CHECKED = true

                                appendText("[Failed] Sponsor Check")
                            }

                            override fun onComplete() {
                            }
                        })
                    } else {
                        appendText("[Skip] Sponsor Check")
                    }
                }
            }
            val hi3VerTask = async {
                val response = Network.sendGet(Constant.API_ENDPOINT + "/hi3_version", false)
                if (response != null) {
                    try {
                        val json = JSONObject(response)
                        if (json.has("retcode") && json.getInt("retcode") == 0) {
                            val newHI3Version = json.getString("version")
                            BH_VER = newHI3Version
                            appPref.edit().putString("bh_ver", newHI3Version).apply()
                            Logger.d(
                                "VersionCheck", "cloud bh ver: $newHI3Version"
                            )
                        } else if (json.has("message")) {
                            Logger.getLogger(this@SplashActivity)
                                .makeToast(json.getString("message"))
                        }

                        appendText("HI3 Version Checked")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Logger.d("Update", "Check BH Version Failed")
                        Logger.getLogger(this@SplashActivity).makeToast("检查崩坏3版本失败...")

                        appendText("[Failed] HI3 Version Check")
                    }
                } else {
                    Logger.d("Update", "Check BH Version Failed")
                    Logger.getLogger(this@SplashActivity).makeToast("检查崩坏3版本失败...")
                    appendText("[Failed] HI3 Version Check")
                }
            }
            val fpTask = async {
                DeviceFpManager.getInstance().getDeviceFp(this@SplashActivity)
                appendText(
                    "[DeviceFp] " + DeviceFpManager.getInstance().getDeviceFp(this@SplashActivity)
                )
            }
            val updateChecker = async {
                val response = Network.sendGet(Constant.API_ENDPOINT + "/update", false)
                if (response != null) {
                    Logger.i("Update", "handleMessage: $response")
                    try {
                        val json = JSONObject(response)
                        if (json.has("disable_scanner") && json.getBoolean("disable_scanner")) {
                            val disableIntent = Intent(
                                applicationContext, DisableActivity::class.java
                            )
                            startActivity(disableIntent)
                            continueBoot = false
                            return@async
                        }
                        appPref.edit().putString("mdk_ver", json.getString("mdk_ver"))
                            .putString("sp_url", json.getString("sp_url"))
                            .putString("afd_url", json.getString("afd_url"))
                            .putString("qq_group_url", json.getString("qq_group_url")).putString(
                                "custom_username", json.getString("default_name")
                            ).apply()
                        Logger.d("Update", "cloud ver:" + json.getInt("ver"))
                        Logger.d("Update", "local ver:" + BuildConfig.VERSION_CODE)
                        if (Constant.BETA_VER && json.has("beta_ver") && BuildConfig.VERSION_CODE < json.getInt(
                                "beta_ver"
                            )
                        ) {
                            Logger.i("Update", "Open Beta Update Window")
                            val dialogData = DialogData(
                                "获取到新测试版: " + json.getString("beta_ver_name"),
                                "更新日志：${
                                    json.getString("beta_logs").replace("&n".toRegex(), "\n")
                                }".trimIndent()
                            )
                            dialogData.positiveButtonData = object : ButtonData("打开更新链接") {
                                override fun callback(dialogHelper: DialogHelper) {
                                    try {
                                        Tools.openUrl(
                                            json.getString("beta_update_url"), applicationContext
                                        )
                                    } catch (ignore: Exception) {
                                    }
                                    activityManager.clearActivity()
                                }
                            }
                            dialogData.isCancelable = false
                            DialogLiveData.getINSTANCE().addDelayDialog(dialogData)
                        } else if (!packageName.contains("dev") && (BuildConfig.VERSION_CODE < json.getInt(
                                "ver"
                            )) && CHECK_VER && json.getInt("ver") > appPref.getInt(
                                "ignore_ver", 0
                            )
                        ) {
                            Logger.i("Update", "Open Update Window")
                            showUpdateDialog(
                                json.getString("ver_name"),
                                json.getString("apk_url"),
                                json.getString("update_url"),
                                json.getString("logs").replace("&n".toRegex(), "\n")
                            )
                        }

                        appendText("Scanner Version Checked")
                    } catch (e: Exception) {
                        appendText("[Failed] Scanner Version Check")
                        e.printStackTrace()
                        Logger.d("Update", "Check Update Failed")
                        Logger.getLogger(this@SplashActivity).makeToast("检查更新失败...")
                    }
                } else {
                    appendText("[Failed] Scanner Version Check")
                    Logger.d("Update", "Check Update Failed")
                    Logger.getLogger(this@SplashActivity).makeToast("检查更新失败...")
                }

                BH_VER = appPref.getString("bh_ver", BH_VER)
                Constant.MDK_VERSION = appPref.getString("mdk_ver", Constant.MDK_VERSION)
                Constant.SP_URL = appPref.getString("sp_url", Constant.SP_URL)
                Constant.AFD_URL = appPref.getString("afd_url", Constant.AFD_URL)
                Constant.QQ_GROUP_URL = appPref.getString("qq_group_url", Constant.AFD_URL)
            }

            UpgradeManager.getInstance()
                .checkUpgrade(false, null, DefaultUpgradeStrategyRequestCallback())


            awaitAll(fpTask, updateChecker, hi3VerTask, sponsorChecker)
            delay(100L)
            appendText("All Tasks Done")
            needBoot = true
            Constant.SCANNER_INIT = true
            if (continueBoot) {
                finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun appendText(text: String) {
        runOnUiThread {
            binding.textViewCustom.text = binding.textViewCustom.text.toString() + "\n" + text
        }
    }

    override fun onBecameForeground() {
        if (needBoot) {
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    override fun onBecameBackground() {
        continueBoot = false
    }

    private fun showUpdateDialog(ver: String, url: String, lzUrl: String, logs: String) {
        val dialogData = DialogData("获取到新版本: $ver", "更新日志：\n$logs")
        dialogData.positiveButtonData = object : ButtonData("打开更新链接") {
            override fun callback(dialogHelper: DialogHelper) {
                super.callback(dialogHelper)
                Tools.openUrl(url, applicationContext)
            }
        }
        dialogData.negativeButtonData = object : ButtonData("蓝奏云") {
            override fun callback(dialogHelper: DialogHelper) {
                super.callback(dialogHelper)
                Tools.openUrl(lzUrl, applicationContext)
            }
        }
        dialogData.neutralButtonData = object : ButtonData(getString(R.string.btn_cancel)) {
            override fun callback(dialogHelper: DialogHelper) {
                val normalDialog = AlertDialog.Builder(
                    applicationContext
                )
                normalDialog.setTitle("是否关闭更新提示？")
                normalDialog.setMessage("将无法获取扫码器最新更新\n\n赞助者相关功能将同时不可用\n\n崩坏3版本号将保持更新")
                normalDialog.setPositiveButton(R.string.btn_close_update) { dialog, _ ->
                    appPref.edit().putBoolean("check_update", false).apply()
                    dialog.dismiss()
                    super.callback(dialogHelper)
                }
                normalDialog.setNegativeButton(R.string.btn_cancel) { dialog, _ -> dialog.dismiss() }
            }
        }

        DialogLiveData.getINSTANCE().addDelayDialog(dialogData)
    }
}