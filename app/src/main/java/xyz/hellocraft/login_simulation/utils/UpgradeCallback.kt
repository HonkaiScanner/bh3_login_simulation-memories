package xyz.hellocraft.login_simulation.utils

import android.content.Context
import com.github.haocen2004.login_simulation.BuildConfig
import com.tencent.upgrade.bean.UpgradeStrategy
import com.tencent.upgrade.callback.UpgradeStrategyRequestCallback
import com.tencent.upgrade.core.UpgradeManager
import rikka.core.util.ClipboardUtils
import xyz.hellocraft.login_simulation.data.dialog.ButtonData
import xyz.hellocraft.login_simulation.data.dialog.DialogData
import xyz.hellocraft.login_simulation.data.dialog.DialogLiveData

class UpgradeCallback : UpgradeStrategyRequestCallback {
    override fun onReceiveStrategy(p0: UpgradeStrategy?) {
        Logger.d(
            "UpgradeCallback",
            "onReceiveStrategy, strategy = $p0"
        )
        p0?.let {
            if (it.apkBasicInfo.versionCode > BuildConfig.VERSION_CODE) {
                val dialog =
                    DialogData("获取到灰度更新: ${it.clientInfo.title}", it.clientInfo.description)
                dialog.isCancelable = false
                dialog.positiveButtonData = object : ButtonData("更新") {
                    override fun callback(dialogHelper: DialogHelper) {
                        super.callback(dialogHelper)
                        Logger.d(
                            "UpgradeCallback",
                            "startUpdate"
                        )
                        Logger.getLogger(null).makeToast("已开始下载\n可在通知栏查看下载进度")
                        UpgradeManager.getInstance().startDownload()
                    }
                }
                dialog.negativeButtonData = ButtonData("取消")
                dialog.neutralButtonData = object : ButtonData("复制下载链接") {
                    override fun callback(dialogHelper: DialogHelper?, context: Context) {
                        super.callback(dialogHelper)
                        it.apkBasicInfo.downloadUrl?.let { url ->
                            Logger.d(
                                "UpgradeCallback",
                                "copy download url = $url"
                            )
                            ClipboardUtils.put(context, url)
                            Logger.getLogger(null).makeToast("已复制下载链接")

                        } ?: run {
                            Logger.getLogger(null).makeToast("获取下载链接失败")
                        }
                    }
                }
                DialogLiveData.getINSTANCE().addDelayDialog(dialog)
            }
        }
    }

    override fun onFail(p0: Int, p1: String?) {
        Logger.d(
            "UpgradeCallback",
            "onFail errCode = $p0，errMsg = $p1"
        )
    }

    override fun onReceivedNoStrategy() {
        Logger.d(
            "UpgradeCallback",
            "onReceivedNoStrategy"
        )
    }
}