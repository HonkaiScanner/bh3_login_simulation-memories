package xyz.hellocraft.login_simulation.login

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.text.TextUtils
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.geetest.sdk.GT3ConfigBean
import com.geetest.sdk.GT3ErrorBean
import com.geetest.sdk.GT3GeetestUtils
import com.geetest.sdk.GT3Listener
import com.geetest.sdk.utils.GT3ServiceNode
import com.github.haocen2004.login_simulation.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import xyz.hellocraft.login_simulation.data.Constant.BH_PUBLIC_KEY
import xyz.hellocraft.login_simulation.data.Constant.V2_OFFICIAL_TOKEN
import xyz.hellocraft.login_simulation.data.RoleData
import xyz.hellocraft.login_simulation.data.TokenResult
import xyz.hellocraft.login_simulation.utils.DeviceFpManager
import xyz.hellocraft.login_simulation.utils.Encrypt
import xyz.hellocraft.login_simulation.utils.Logger
import xyz.hellocraft.login_simulation.utils.Network
import xyz.hellocraft.login_simulation.utils.Tools
import java.util.regex.Pattern


class OfficialNew(activity: AppCompatActivity, callback: LoginCallback) :
    Official(activity, callback) {

    private val tag = "OfficialNew"
    private var isLogin = false
    private var roleData: RoleData? = null
    private var username: String = "Unknown"
    private var password: String = "Unknown"
    private var mobile: String = "Unknown"
    private var callback: LoginCallback? = null
    private var activity: AppCompatActivity? = null
    private var preferences: SharedPreferences? = null
    private var logger: Logger? = null
    private var gameToken: String? = null
    private var uid: String? = null
    private var stoken: String? = null
    private var mid: String? = null
    private var smsJob: Job? = null
    private var getSmsButton: Button? = null
    private var smsActionType: String = "login_by_mobile_captcha"
    private var geetestSessionId: String = ""

    init {
        this.activity = activity
        this.callback = callback
        preferences = activity.getSharedPreferences(
            "official_user_" + PreferenceManager.getDefaultSharedPreferences(activity)
                .getInt("official_slot", 1), Context.MODE_PRIVATE
        )
        logger = Logger.getLogger(activity)

    }

    private fun verifyGameToken(): TokenResult {
        Logger.addBlacklist(gameToken)
        val jsonObject = JsonObject()
        jsonObject.addProperty("uid", uid)
        jsonObject.addProperty("token", gameToken)
        val map: MutableMap<String, String> = HashMap()

        map["x-rpc-device_id"] = Tools.getDeviceID(activity)
        map["x-rpc-client_type"] = "2"
        map["x-rpc-game_biz"] = "bh3_cn"
        map["x-rpc-language"] = "zh-cn"
        map["x-rpc-channel_id"] = "1"
        map["User-Agent"] = "okhttp/3.10.0"

        val feedback = Network.sendPost(
            "https://api-sdk.mihoyo.com/bh3_cn/mdk/shield/api/verify",
            jsonObject.toString(),
            map
        )
        val feedbackJsonObject = JSONObject(feedback)
        if (feedbackJsonObject.has("retcode") && feedbackJsonObject.get("retcode") == 0) {
            val dataJson: JSONObject = feedbackJsonObject.getJSONObject("data")
            val accountJson = dataJson.getJSONObject("account")
            gameToken = accountJson.getString("token")
            uid = accountJson.getString("uid")
            preferences?.edit()!!.putString("token", gameToken).apply()
            return TokenResult(true, "")
        } else if (feedbackJsonObject.has("message")) {
            Logger.d(tag, "verifyGameToken: $feedback")
            preferences?.edit()!!.remove("token").apply()
            return TokenResult(false, feedbackJsonObject.getString("message"))
        }
        preferences?.edit()!!.remove("token").apply()
        return TokenResult(false, "verifyGameToken unexpected failure")

    }

    private fun transferToken(): TokenResult {
        Logger.addBlacklist(gameToken)
        val verifyGameToken = verifyGameToken()
        if (verifyGameToken.success) {
            val jsonObject = JsonObject()

            jsonObject.addProperty("account_id", uid?.toInt())
            jsonObject.addProperty("game_token", gameToken)

            val requestBody = jsonObject.toString()
            Logger.d(tag, "transferToken: $requestBody")

            val header = DeviceFpManager.getDSPRODHeader(activity, requestBody)
            val feedback = Network.sendPost(
                "https://passport-api.mihoyo.com/account/ma-cn-session/app/getTokenByGameToken",
                requestBody,
                header
            )
            val feedbackJsonObject = JSONObject(feedback)
            if (feedbackJsonObject.has("retcode") && feedbackJsonObject.get("retcode") == 0) {
                val dataJsonObject = feedbackJsonObject.getJSONObject("data")
                val tokenJsonObject = dataJsonObject.getJSONObject("token")
                val userInfo = dataJsonObject.getJSONObject("user_info")
                stoken = tokenJsonObject.getString("token")
                mid = userInfo.getString("mid")
                return verifyToken()
            } else if (feedbackJsonObject.has("message")) {
                Logger.d(tag, "transferToken: $feedback")
                return TokenResult(false, feedbackJsonObject.getString("message"))
            }
        } else {
            return verifyGameToken
        }

        return TokenResult(false, "transferToken unexpected failure")
    }

    private fun verifyToken(): TokenResult {
        Logger.addBlacklist(stoken)
        val jsonObject = JsonObject()
        jsonObject.addProperty("mid", mid)
        jsonObject.addProperty("refresh", true)
        val tokenObject = JsonObject()
        tokenObject.addProperty("token", stoken)
        tokenObject.addProperty("token_type", 1)
        jsonObject.add("token", tokenObject)

        val requestBody = jsonObject.toString()
        Logger.d(tag, "verifyToken: $requestBody")

        val header = DeviceFpManager.getDSPRODHeader(activity, requestBody)

        val feedback = Network.sendPost(
            "https://passport-api.mihoyo.com/account/ma-cn-session/app/verify",
            requestBody,
            header
        )
        val feedbackJsonObject = JSONObject(feedback)
        if (feedbackJsonObject.has("retcode") && feedbackJsonObject.get("retcode") == 0) {
            val data = feedbackJsonObject.getJSONObject("data")
            val userInfo = data.getJSONObject("user_info")
            uid = userInfo.getString("aid")
            mid = userInfo.getString("mid")
            val mobile = userInfo.getString("mobile")
            username = mobile
            if (TextUtils.isEmpty(username)) {
                username = userInfo.getString("email")
            }
            preferences?.edit()!!
                .putString("uid", uid)
                .putString("mobile", mobile)
                .putString("mid", mid)
                .putString("stoken", stoken)
                .putBoolean("has_token", true)
                .apply()

            if (data.has("new_token")) {
                if (data.getString("new_token").isNotEmpty()) {
                    if (data.getString("new_token") != "null") {
                        val newToken = JSONObject(data.getString("new_token"))
                        preferences?.edit()!!.putString("stoken", newToken.getString("token"))
                            .apply()
                        stoken = newToken.getString("token")
                        Logger.addBlacklist(stoken)
                        Logger.d(tag, "verifyToken: new_token found, verifying")
                        return verifyToken()
                    }
                }
            }
            Logger.d(tag, "verifyToken: after new_token check")
            V2_OFFICIAL_TOKEN = true
            roleData = RoleData(uid, "", "", "", "1", "1", "", 12, callback)
            isLogin = true
            Logger.d(tag, "verifyToken: $feedback")
            return TokenResult(true, "")
        } else {
            Logger.d(tag, "verifyToken: $feedback")
            if (!TextUtils.isEmpty(gameToken)) {
                return transferToken()
            }
            return if (feedbackJsonObject.has("message")) {
                TokenResult(false, feedbackJsonObject.getString("message"))
            } else {
                TokenResult(false, "verifyToken unexpected failure")
            }
        }
    }

//    private fun getGameToken() {
//        val cookie = "mid=$mid;stoken=$stoken"
//        val map: MutableMap<String, String> = HashMap()
//        map["Cookie"] = cookie
//
//        val feedback = Network.sendGet(
//            "https://api-takumi.mihoyo.com/auth/api/getGameToken",
//            map,
//            true
//        )
//        try {
//            val feedbackJsonObject = JSONObject(feedback)
//            if (feedbackJsonObject.has("retcode") && feedbackJsonObject.get("retcode") == 0) {
//                val dataJson: JSONObject = feedbackJsonObject.getJSONObject("data")
//                gameToken = dataJson.getString("game_token")
//                Logger.addBlacklist(gameToken)
//            } else {
//                Logger.d(tag, "getGameToken: $feedback")
//                preferences?.edit()!!.putBoolean("has_token", false).apply()
//                callback?.onLoginFailed("getGameToken unexpected failure")
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Logger.d(tag, "getGameToken: $feedback")
//            preferences?.edit()!!.putBoolean("has_token", false).apply()
//            callback?.onLoginFailed("getGameToken unexpected failure")
//        }
//
//    }
//
//    private fun verifyAccount() {
//        if (TextUtils.isEmpty(gameToken) || gameToken!!.length < 10) {
//            getGameToken()
//        }
//        Logger.addBlacklist(gameToken)
//        val requestBody = "{\"uid\":\"$uid\",\"guest\":false,\"token\":\"$gameToken\"}"
//        val verifyAccount = Tools.verifyAccount(activity!!, "1", requestBody)
//        val verifyAccountJsonObject = JSONObject(verifyAccount)
//        if (verifyAccountJsonObject.has("retcode") && verifyAccountJsonObject.getInt("retcode") == 0) {
//            val data = verifyAccountJsonObject.getJSONObject("data")
//            val comboId = data.getString("combo_id")
//            val openId = data.getString("open_id")
//            val comboToken = data.getString("combo_token")
//            Logger.addBlacklist(comboToken)
//            roleData = RoleData(openId, "", comboId, comboToken, "1", "1", "", 0, callback)
//            isLogin = true
//        } else {
//            preferences?.edit()!!.putBoolean("has_token", false).apply()
//            callback?.onLoginFailed()
//        }
//    }

    private fun loginByPassword(aigis: String = "") {
        val jsonObject = JsonObject()
        jsonObject.addProperty("account", Encrypt.encryptByPublicKey(username, BH_PUBLIC_KEY))
        jsonObject.addProperty("password", Encrypt.encryptByPublicKey(password, BH_PUBLIC_KEY))

        val requestBody = jsonObject.toString()
        Logger.d(tag, "loginByAccount: $requestBody")

        val header = DeviceFpManager.getDSPRODHeader(activity, requestBody)
        header["x-rpc-aigis"] = aigis

        val feedback = Network.sendPostRaw(
            "https://passport-api.mihoyo.com/account/ma-cn-passport/app/loginByPassword",
            requestBody,
            header
        )
        val feedbackJsonObject = JSONObject(feedback.body)
        if (feedbackJsonObject.has("retcode") && feedbackJsonObject.get("retcode") == 0) {
            val dataJsonObject = feedbackJsonObject.getJSONObject("data")
            val tokenJsonObject = dataJsonObject.getJSONObject("token")
            val userInfo = dataJsonObject.getJSONObject("user_info")
            stoken = tokenJsonObject.getString("token")
            mid = userInfo.getString("mid")
            verifyToken().let {
                if (!it.success) {
                    preferences?.edit()!!.putBoolean("has_token", false).apply()
                    callback?.onLoginFailed(it.message)
                }
            }
        } else {
            if (feedback.headers.containsKey("x-rpc-aigis")) {
                val aigisResp = feedback.headers["x-rpc-aigis"]
                Logger.d(tag, "loginByAccount: $aigisResp")
                val aigisJson = JSONObject(aigisResp?.get(0)!!)
                geetestSessionId = aigisJson.getString("session_id")
                try {
                    val data = aigisJson.getJSONObject("data")
                    checkGeetest(aigis = data, type = "password")
                } catch (e: Exception) {
                    Logger.d(tag, "loginByAccount: aigis using string")
                    val dataStr = aigisJson.getString("data")
                    checkGeetest(aigis = JSONObject(dataStr), type = "password")
                    return
                }

            } else {
                Logger.d(tag, "loginByAccount: $feedback")
                if (feedbackJsonObject.has("message")) {
                    preferences?.edit()!!.putBoolean("has_token", false).apply()
                    callback?.onLoginFailed(feedbackJsonObject.getString("message"))
                } else {
                    preferences?.edit()!!.putBoolean("has_token", false).apply()
                    callback?.onLoginFailed("loginByAccount unexpected failure")
                }
            }
        }
    }

    private fun checkGeetest(aigis: JSONObject, type: String) {
        val gt3GeetestUtils = GT3GeetestUtils(activity)
        val gT3ConfigBean = GT3ConfigBean()
        gT3ConfigBean.pattern = 1
        gT3ConfigBean.isCanceledOnTouchOutside = false
        gT3ConfigBean.lang = null
        gT3ConfigBean.timeout = 10000
        gT3ConfigBean.webviewTimeout = 10000
        gT3ConfigBean.gt3ServiceNode = GT3ServiceNode.NODE_IPV6
        gT3ConfigBean.listener = object : GT3Listener() {

            override fun onButtonClick() {
                gT3ConfigBean.api1Json = aigis
                gt3GeetestUtils.getGeetest()
            }

            override fun onDialogResult(result: String) {
                gt3GeetestUtils.dismissGeetestDialog()
                gt3GeetestUtils.destory()
                CoroutineScope(Dispatchers.IO).launch {
                    val aigisResult = "$geetestSessionId;${
                        Base64.encodeToString(
                            result.toByteArray(),
                            Base64.NO_WRAP
                        )
                    }"
                    if (type == "password") {
                        loginByPassword(aigisResult)
                    } else {
                        createLoginCaptcha(type, aigisResult)
                    }
                }
            }

            override fun onStatistics(s: String) {
            }

            override fun onSuccess(s: String) {
            }

            override fun onFailed(gt3ErrorBean: GT3ErrorBean) {
            }

            override fun onClosed(i: Int) {
            }

            override fun onReceiveCaptchaCode(p0: Int) {
            }
        }
        gt3GeetestUtils.init(gT3ConfigBean)
        activity?.runOnUiThread {
            gt3GeetestUtils.startCustomFlow()
        }
//        gt3GeetestUtils.startCustomFlow()

    }

    private fun createLoginCaptcha(mobile: String, aigis: String = "") {
        val jsonObject = JsonObject()
        jsonObject.addProperty("mobile", Encrypt.encryptByPublicKey(mobile, BH_PUBLIC_KEY))
        jsonObject.addProperty("area_code", Encrypt.encryptByPublicKey("+86", BH_PUBLIC_KEY))

        val requestBody = jsonObject.toString()
        Logger.d(tag, "createLoginCaptcha: $requestBody")

        val header = DeviceFpManager.getDSPRODHeader(activity, requestBody)
        header["x-rpc-aigis"] = aigis

        val feedback = Network.sendPostRaw(
            "https://passport-api.mihoyo.com/account/ma-cn-verifier/verifier/createLoginCaptcha",
            requestBody,
            header
        )
        val feedbackJsonObject = JSONObject(feedback.body)
        if (feedbackJsonObject.has("retcode") && feedbackJsonObject.get("retcode") == 0) {
            val dataJsonObject = feedbackJsonObject.getJSONObject("data")
            val countDown = dataJsonObject.getInt("countdown")

            this.mobile = mobile
            smsJob = CoroutineScope(Dispatchers.IO).launch {
                delay(countDown * 1000L)
                try {
                    getSmsButton?.isEnabled = true
                } catch (ignore: Exception) {
                }
                smsJob = null
            }
            smsActionType = dataJsonObject.getString("action_type")
            logger?.makeToast("验证码已发送，请注意查收！")

        } else {
            if (feedback.headers.containsKey("x-rpc-aigis")) {
                val aigisResp = feedback.headers["x-rpc-aigis"]
                Logger.d(tag, "createLoginCaptcha: $aigisResp")
                val aigisJson = JSONObject(aigisResp?.get(0)!!)
                geetestSessionId = aigisJson.getString("session_id")
                try {
                    val data = aigisJson.getJSONObject("data")
                    checkGeetest(aigis = data, type = mobile)
                } catch (e: Exception) {
                    Logger.d(tag, "createLoginCaptcha: aigis using string")
                    val dataStr = aigisJson.getString("data")
                    checkGeetest(aigis = JSONObject(dataStr), type = mobile)
                    return
                }
            } else {
                Logger.d(tag, "createLoginCaptcha: ${feedback.body}")
                if (feedbackJsonObject.has("message")) {
                    preferences?.edit()!!.putBoolean("has_token", false).apply()
                    callback?.onLoginFailed(feedbackJsonObject.getString("message"))
                } else {
                    preferences?.edit()!!.putBoolean("has_token", false).apply()
                    callback?.onLoginFailed("createLoginCaptcha unexpected failure")
                }
            }

        }

    }

    private fun loginByMobileCaptcha(captcha: String, alertDialog: AlertDialog) {
        val jsonObject = JsonObject()
        jsonObject.addProperty("mobile", Encrypt.encryptByPublicKey(mobile, BH_PUBLIC_KEY))
        jsonObject.addProperty("area_code", Encrypt.encryptByPublicKey("+86", BH_PUBLIC_KEY))
        jsonObject.addProperty("action_type", smsActionType)
        jsonObject.addProperty("captcha", captcha)

        val requestBody = jsonObject.toString()
        Logger.d(tag, "loginByMobileCaptcha: $requestBody")

        val header = DeviceFpManager.getDSPRODHeader(activity, requestBody)

        val feedback = Network.sendPost(
            "https://passport-api.mihoyo.com/account/ma-cn-passport/app/loginByMobileCaptcha",
            requestBody,
            header
        )
        val feedbackJsonObject = JSONObject(feedback)
        if (feedbackJsonObject.has("retcode") && feedbackJsonObject.get("retcode") == 0) {
            val dataJsonObject = feedbackJsonObject.getJSONObject("data")
            val tokenJsonObject = dataJsonObject.getJSONObject("token")
            val userInfo = dataJsonObject.getJSONObject("user_info")
            stoken = tokenJsonObject.getString("token")
            mid = userInfo.getString("mid")
            activity?.runOnUiThread {
                alertDialog.dismiss()
            }
            verifyToken().let {
                if (!it.success) {
                    preferences?.edit()!!.putBoolean("has_token", false).apply()
                    callback?.onLoginFailed(it.message)
                }
            }
        } else {
            Logger.d(tag, "loginByMobileCaptcha: $feedback")
            if (feedbackJsonObject.has("message")) {
                preferences?.edit()!!.putBoolean("has_token", false).apply()
                callback?.onLoginFailed(feedbackJsonObject.getString("message"))
            } else {
                preferences?.edit()!!.putBoolean("has_token", false).apply()
                callback?.onLoginFailed("loginByMobileCaptcha unexpected failure")
            }

        }

    }

    private fun smsLogin() {

        val phonePattern =
            "^(13[0-9]|14[01456879]|15[0-35-9]|16[2567]|17[0-8]|18[0-9]|19[0-35-9])\\d{8}$"
        val smsDialog =
            MaterialAlertDialogBuilder(activity!!)
        val smsDialogView = LayoutInflater.from(activity)
            .inflate(R.layout.offical_sms_login_layout, null)
        smsDialog.setTitle(R.string.types_official)
        smsDialog.setView(smsDialogView)
        smsDialog.setPositiveButton(R.string.btn_login, null)
        smsDialog.setNegativeButton(R.string.btn_cancel) { _, _ ->
//            logger?.makeToast(R.string.login_cancel)
            callback?.onLoginFailed(activity?.getString(R.string.login_cancel))
        }
        smsDialog.setCancelable(false)
        val alertDialog = smsDialog.create()
        alertDialog.setOnShowListener { _: DialogInterface? ->
            val editText = smsDialogView.findViewById<EditText>(R.id.phoneNumber)
            getSmsButton = smsDialogView.findViewById(R.id.btn_sms)
            getSmsButton?.setOnClickListener { v: View ->
                username = editText.text.toString()
                Logger.d(
                    tag,
                    "try get sms code for $username"
                )
                if (Pattern.matches(phonePattern, username)) {
                    v.isEnabled = false
                    CoroutineScope(Dispatchers.IO).launch {
                        createLoginCaptcha(username)
                    }
                } else {
                    logger?.makeToast("手机号格式不正确！")
                }
            }
            val button =
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            button.setOnClickListener { _: View? ->
                val captchaText = smsDialogView.findViewById<EditText>(R.id.sms)
                username = editText.text.toString()
                val captcha = captchaText.text.toString().trim()
                if (captcha.length != 6) {
                    logger?.makeToast("验证码格式不正确！")
                } else {
                    CoroutineScope(Dispatchers.IO).launch {
                        loginByMobileCaptcha(captcha, alertDialog)
                    }
//                    alertDialog.dismiss()
                }
            }
        }
        alertDialog.setOnDismissListener { _: DialogInterface? ->
            smsJob?.cancel()
        }
        alertDialog.show()
    }

    private fun qrLogin() {
        callback?.onLoginFailed("新方案暂未支持二维码登陆")
    }

    override fun login() {

        stoken = preferences?.getString("stoken", "")
        mid = preferences?.getString("mid", "")
        gameToken = preferences?.getString("token", "")
        uid = preferences?.getString("uid", "")

        if (preferences?.getBoolean("has_token", false) == true) {
            if (stoken?.startsWith("v2") == true) {
                CoroutineScope(Dispatchers.IO).launch {
                    verifyToken().let {
                        if (!it.success) {
                            preferences?.edit()!!.putBoolean("has_token", false).apply()
                            callback?.onLoginFailed(it.message)
                        }
                    }
                }
                return
            }
        }
        if (gameToken?.isNotEmpty() == true) {
            CoroutineScope(Dispatchers.IO).launch {
                transferToken().let {
                    if (!it.success) {
                        preferences?.edit()!!.putBoolean("has_token", false).apply()
                        callback?.onLoginFailed(it.message)
                    }
                }
            }
            return
        }
        MaterialAlertDialogBuilder(activity!!)
            .setTitle("点选一个登陆方式")
            .setCancelable(false)
            .setSingleChoiceItems(R.array.official_login_type, 0) { dialogInterface, i ->
                val loginType =
                    activity!!.resources.getStringArray(R.array.official_login_type_values)
                Logger.d(tag, "select " + loginType[i])
                when (loginType[i]) {
                    "password" -> {
                        val customizeDialog =
                            MaterialAlertDialogBuilder(activity!!)
                        val dialogView: View = LayoutInflater.from(activity)
                            .inflate(R.layout.offical_login_layout, null)
                        customizeDialog.setTitle(R.string.types_official)
                        customizeDialog.setView(dialogView)
                        customizeDialog.setPositiveButton(
                            R.string.btn_login
                        ) { _, _ ->
                            val editText = dialogView.findViewById<EditText>(R.id.username)
                            val passwordText = dialogView.findViewById<EditText>(R.id.password)
                            username = editText.text.toString()
                            password = passwordText.text.toString()
                            CoroutineScope(Dispatchers.IO).launch {
                                loginByPassword()
                            }
                        }
                        customizeDialog.setNegativeButton(R.string.btn_cancel) { _, _ ->
                            callback?.onLoginFailed(activity!!.getString(R.string.login_cancel))
                        }
                        customizeDialog.setCancelable(false)
                        customizeDialog.show()
                    }

                    "sms" -> smsLogin()
                    "qrcode" -> qrLogin()
                    else -> {
                        callback?.onLoginFailed(activity!!.getString(R.string.login_cancel))
                    }
                }
                dialogInterface.dismiss()
            }
            .setNegativeButton(
                activity!!.getString(R.string.btn_cancel)
            ) { _: DialogInterface?, _: Int ->
                callback?.onLoginFailed(activity!!.getString(R.string.login_cancel))
            }
            .show()
    }

    override fun logout(): Boolean {
        isLogin = false
        return true
    }

    override fun getRole(): RoleData {
        return roleData!!
    }

    override fun setRole(roleData: RoleData) {
        this.roleData = roleData
        isLogin = roleData.is_setup
    }

    override fun isLogin(): Boolean {
        return isLogin
    }

    override fun getUsername(): String {
        return username
    }

    override fun setUsername(username: String) {
        this.username = username
    }
}