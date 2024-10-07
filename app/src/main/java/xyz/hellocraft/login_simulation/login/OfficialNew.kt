package xyz.hellocraft.login_simulation.login

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.github.haocen2004.login_simulation.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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

class OfficialNew(activity: AppCompatActivity, callback: LoginCallback) :
    Official(activity, callback) {

    private val tag = "OfficialNew"
    private var isLogin = false
    private var roleData: RoleData? = null
    private var username: String = "Unknown"
    private var password: String = "Unknown"
    private var callback: LoginCallback? = null
    private var activity: AppCompatActivity? = null
    private var preferences: SharedPreferences? = null
    private var logger: Logger? = null
    private var gameToken: String? = null
    private var uid: String? = null
    private var stoken: String? = null
    private var mid: String? = null


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
            return TokenResult(true, "")
        } else if (feedbackJsonObject.has("message")) {
            Logger.d(tag, "verifyGameToken: $feedback")
            return TokenResult(false, feedbackJsonObject.getString("message"))
        }

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
                verifyToken()
                return TokenResult(true, "")
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
            if (username.isEmpty()) {
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
                        preferences?.edit()!!.putString("stoken", data.getString("new_token"))
                            .apply()
                        stoken = data.getString("new_token")
                        return TokenResult(true, "")
                    }
                }
            }
            return TokenResult(true, "")
        } else {
            Logger.d(tag, "verifyToken: $feedback")
            return if (feedbackJsonObject.has("message")) {
                TokenResult(false, feedbackJsonObject.getString("message"))
            } else {
                TokenResult(false, "verifyToken unexpected failure")
            }
        }
    }

    private fun getGameToken() {
        val cookie = "mid=$mid;stoken=$stoken"
        val map: MutableMap<String, String> = HashMap()
        map["Cookie"] = cookie

        val feedback = Network.sendGet(
            "https://api-takumi.mihoyo.com/auth/api/getGameToken",
            map,
            true
        )
        val feedbackJsonObject = JSONObject(feedback)
        if (feedbackJsonObject.has("retcode") && feedbackJsonObject.get("retcode") == 0) {
            val dataJson: JSONObject = feedbackJsonObject.getJSONObject("data")
            gameToken = dataJson.getString("token")
            Logger.addBlacklist(gameToken)
        } else {
            Logger.d(tag, "getGameToken: $feedback")
            preferences?.edit()!!.putBoolean("has_token", false).apply()
            callback?.onLoginFailed("getGameToken unexpected failure")
        }

    }

    private fun verifyAccount() {
        if (gameToken == null) {
            getGameToken()
        }
        Logger.addBlacklist(gameToken)
        val requestBody = "{\"uid\":\"$uid\",\"guest\":false,\"token\":\"$gameToken\"}"
        val verifyAccount = Tools.verifyAccount(activity!!, "1", requestBody)
        val verifyAccountJsonObject = JSONObject(verifyAccount)
        if (verifyAccountJsonObject.has("retcode") && verifyAccountJsonObject.getInt("retcode") == 0) {
            val data = verifyAccountJsonObject.getJSONObject("data")
            val comboId = data.getString("combo_id")
            val openId = data.getString("open_id")
            val comboToken = data.getString("combo_token")
            Logger.addBlacklist(comboToken)
            roleData = RoleData(openId, "", comboId, comboToken, "1", "1", "", 0, callback)
            isLogin = true
        } else {
            preferences?.edit()!!.putBoolean("has_token", false).apply()
            callback?.onLoginFailed()
        }
    }

    private fun loginByPassword() {
        val jsonObject = JsonObject()
        jsonObject.addProperty("account", Encrypt.encryptByPublicKey(username, BH_PUBLIC_KEY))
        jsonObject.addProperty("password", Encrypt.encryptByPublicKey(password, BH_PUBLIC_KEY))

        val requestBody = jsonObject.toString()
        Logger.d(tag, "loginByAccount: $requestBody")

        val header = DeviceFpManager.getDSPRODHeader(activity, requestBody)

        val feedback = Network.sendPost(
            "https://passport-api.mihoyo.com/account/ma-cn-passport/app/loginByPassword",
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
            val verifyToken = verifyToken()
            if (verifyToken.success) {
                verifyAccount()
            } else {
                preferences?.edit()!!.putBoolean("has_token", false).apply()
                callback?.onLoginFailed(verifyToken.message)
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

    private fun smsLogin() {
        callback?.onLoginFailed("新方案暂未支持短信登陆")
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
                    val result = verifyToken()
                    if (result.success) {
                        V2_OFFICIAL_TOKEN = true
                        verifyAccount()
                    } else {
                        preferences?.edit()!!.putBoolean("has_token", false).apply()
                        callback?.onLoginFailed(result.message)
                    }
                }
                return
            } else if (gameToken?.isNotEmpty() == true) {
                CoroutineScope(Dispatchers.IO).launch {
                    val result = transferToken()
                    if (result.success) {
                        V2_OFFICIAL_TOKEN = true
                        verifyAccount()
                    } else {
                        preferences?.edit()!!.putBoolean("has_token", false).apply()
                        callback?.onLoginFailed(result.message)
                    }
                }
                return
            }
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
}