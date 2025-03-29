package xyz.hellocraft.login_simulation.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log.getStackTraceString
import android.view.View
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.github.haocen2004.login_simulation.databinding.ActivitySbeamLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import xyz.hellocraft.login_simulation.data.Constant
import xyz.hellocraft.login_simulation.utils.Encrypt
import xyz.hellocraft.login_simulation.utils.Logger
import xyz.hellocraft.login_simulation.utils.Network
import xyz.hellocraft.login_simulation.utils.Tools
import java.util.Date

class SbeamLoginActivity : BaseActivity() {

    private lateinit var binding: ActivitySbeamLoginBinding
    private var backTime: Long = 0

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySbeamLoginBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())

        val appPref = PreferenceManager.getDefaultSharedPreferences(this)

        appPref.edit().remove("ft").apply()

        var checkSucc = false
        if (intent.extras?.getString("youlookthecode").equals("whyskipit")) {

            lifecycleScope.launch(Dispatchers.IO) {
                val check = async {
                    val fp = Tools.getString(this@SbeamLoginActivity, "device_fp")
                    val response = Network.sendGet(Constant.API_ENDPOINT + "/2a5f?d=" + fp, false)

                    if (response != null) {
                        try {
                            val json = JSONObject(response)
                            if (json.has("data")) {
                                checkSucc = true
                                val date = Date()
                                val time = date.time
                                val format = android.text.format.DateFormat.format(
                                    "yyyy-MM-dd HH:mm:ss",
                                    time
                                )
                                binding.textViewFA.text = json.getString("data") + "\n\n" + format
                                val hash = Encrypt.sha256HMAC(
                                    json.getString("data"),
                                    Constant.BH_APP_KEY,
                                    false
                                )
                                binding.textViewF2A.text =
                                    fp + "\n" + hash.substring(0, 32) + "\n" + hash.substring(32)
                                binding.imageView.visibility = View.VISIBLE
                            }
                        } catch (e: Exception) {
                            Logger.e("SbeamLogin", getStackTraceString(e))
                        }
                    }

                }

                awaitAll(check)

            }
        }

        if (!checkSucc) {
            binding.textViewF2A.text = "Something went wrong"
            binding.imageView.visibility = View.GONE

            lifecycleScope.launch(Dispatchers.IO) {
                delay(2000)
                if (!checkSucc) {
                    startActivity(Intent(this@SbeamLoginActivity, SplashActivity::class.java))
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
            }
        }

        onBackPressedDispatcher.addCallback {
            val currTime = System.currentTimeMillis()
            if (System.currentTimeMillis() - backTime < 2000) {
                startActivity(Intent(this@SbeamLoginActivity, SplashActivity::class.java))
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            } else {
                Logger.getLogger(this@SbeamLoginActivity).makeToast("再次返回来退出该页面")
                backTime = currTime
            }
        }
    }


}