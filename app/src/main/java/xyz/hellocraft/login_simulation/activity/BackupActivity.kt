package xyz.hellocraft.login_simulation.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.haocen2004.login_simulation.R
import com.github.haocen2004.login_simulation.databinding.ActivityBackupBinding
import xyz.hellocraft.login_simulation.data.Constant
import xyz.hellocraft.login_simulation.data.dialog.ButtonData
import xyz.hellocraft.login_simulation.data.dialog.DialogData
import xyz.hellocraft.login_simulation.data.dialog.DialogLiveData
import xyz.hellocraft.login_simulation.utils.Encrypt
import xyz.hellocraft.login_simulation.utils.Logger
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Date
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class BackupActivity : BaseActivity() {


    private lateinit var binding: ActivityBackupBinding
    private lateinit var callback: (Uri?) -> Unit
    private lateinit var secretKey: SecretKey
    private var actionFlag = 0
    private var restoreStep = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBackupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.buttonBackup.setOnClickListener {
            actionFlag = 1
            setKeyDialog()
        }
        binding.buttonRestore.setOnClickListener {
            actionFlag = 2
            setKeyDialog()
        }
        showTipsDialog()
    }

    @Deprecated("Deprecated in Java", ReplaceWith("super.onBackPressed()"))
    override fun onBackPressed() {
        when (restoreStep) {
            0 -> {
                super.onBackPressed()
            }

            1 -> {
                Logger.getLogger(this).makeToast("正在恢复，请勿退出")
            }

            2 -> {
                activityManager.clearActivity()
            }

            else -> {
                activityManager.clearActivity()
            }
        }
    }

    private fun showTipsDialog() {
        val dialog = DialogData(
            "备份与恢复",
            getString(R.string.restore_tips),
        )
        dialog.positiveButtonData = ButtonData("我已知晓")
        dialog.isCancelable = false
        DialogLiveData.getINSTANCE().addNewDialog(dialog)
    }

    private val backupResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            Logger.d("BackupActivityResult", "Result code: ${result.resultCode}")
//        println(result)
            if (result.resultCode == Activity.RESULT_OK) {
//            println(result.data)
                val uri = result.data?.data
                if (uri != null) {
                    callback(uri)
                } else {
                    Logger.e("openFileSavePicker", "No file selected")
                    callback(null)
                }
            } else {
                Logger.e("openFileSavePicker", "File save selection canceled")
                callback(null)
            }
        }

    private fun restore() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "application/zip" // 限制选择 zip 文件
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        restoreResultLauncher.launch(intent) // 启动文件选择器
        restoreStep = 1
    }

    private val restoreResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri: Uri? = result.data?.data // 获取用户选择的文件 URI
                uri?.let {
                    // 解压 zip 文件并写入到私有目录
                    Logger.d("RestoreActivityResult", "Selected file: $uri")
                    decryptAndExtractZip(uri)
                }
            } else {
                restoreStep = 0
                Logger.e("RestoreActivityResult", "File selection canceled")
                Logger.getLogger(this).makeToast("文件选择已取消")
            }
        }

    private fun decryptAndExtractZip(uri: Uri) {
        val cacheDir = cacheDir // 获取应用的缓存目录

        val zeroIV = ByteArray(16) { 0 } // 16 字节的全零 IV

        // 创建 IvParameterSpec（初始化向量）
        val ivSpec = IvParameterSpec(zeroIV)
        val decryptedZipFile = File(cacheDir, "decrypted_backup.zip")

        try {
            // 打开加密的 zip 文件流
            val inputStream = contentResolver.openInputStream(uri) ?: return
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec)

            // 使用 CipherInputStream 进行解密
            val cipherInputStream = CipherInputStream(inputStream, cipher)

            // 将解密后的数据写入到临时文件
            val outputStream = FileOutputStream(decryptedZipFile)
            val buffer = ByteArray(1024)
            var bytesRead: Int
            while (cipherInputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            cipherInputStream.close()
            outputStream.close()

            // 验证zip文件头
            val fileInputStream = FileInputStream(decryptedZipFile)
            val bytes = ByteArray(4) // 读取文件头部的 4 个字节
            fileInputStream.read(bytes)
            val header = bytes.joinToString(", ") { it.toString() }
            Logger.d("decryptAndExtractZip", "Decrypted file header: $header")
            fileInputStream.close()

            Logger.d("decryptAndExtractZip", "Decrypted file: ${decryptedZipFile.absolutePath}")

            // 解密后的文件可以进行解压
            extractZipToPrivateStorage(decryptedZipFile)

            Logger.d("decryptAndExtractZip", "Decrypted and extracted successfully!")
            restoreStep = 2

        } catch (e: Exception) {
            e.printStackTrace()
            Logger.e("decryptAndExtractZip", "Error during decryption: " + e.message)
            restoreStep = 0
            Logger.getLogger(this).makeToast("还原失败，请检查密钥是否正确\n如有问题请联系开发者")
        } finally {
            // 处理完后删除临时的解密文件
            if (decryptedZipFile.exists()) {
                decryptedZipFile.delete()
            }
        }
    }

    private fun extractZipToPrivateStorage(zipFile: File) {
        val privateDir = filesDir.parentFile // 获取应用的私有存储目录
        try {
            // 使用 ZipFile 打开解密后的 zip 文件
            val zip = ZipFile(zipFile)

            // 获取所有条目
            val entries = zip.entries()
//            println(entries)

            // 遍历 zip 文件中的每个条目
            while (entries.hasMoreElements()) {
                val zipEntry = entries.nextElement()
                Logger.d("extractZipToPrivateStorage", "Extracting: ${zipEntry.name}")
//                println(zipEntry)
                val outputFile = File(privateDir, zipEntry.name)
                outputFile.parentFile?.mkdirs() // 创建文件夹

                // 如果条目是文件，提取它
                if (!zipEntry.isDirectory) {
                    val inputStream = zip.getInputStream(zipEntry)
                    val outputStream = FileOutputStream(outputFile)
                    val buffer = ByteArray(1024)
                    var bytesRead: Int
                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                    }

                    outputStream.close()
                    inputStream.close()
                }
            }

            zip.close() // 完成后关闭 zip 文件

        } catch (e: IOException) {
            e.printStackTrace()
        }

        Logger.getLogger(this).makeToast("恢复已完成，将在下次启动时生效\n3秒后自动退出")
        // 3秒后自动退出 新建线程
        Thread {
            Thread.sleep(3000)
            activityManager.clearActivity()
        }.start()
    }

    private fun setKeyDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("请输入密钥")
        builder.setCancelable(false)

        // 创建一个输入框
        val input = android.widget.EditText(this)
        builder.setView(input)

        builder.setPositiveButton("确定") { _, _ ->
            val userInputKey = input.text.toString()
            if (userInputKey.isNotEmpty()) {
                // 使用输入的密钥生成 AES 密钥并打印出来
                secretKey = generateAESKeyFromInput(userInputKey)
                when (actionFlag) {
                    1 -> {
                        backup(this)
                    }

                    2 -> {
                        restore()
                    }

                    else -> {
                        Logger.getLogger(this).makeToast("未指定操作")
                    }
                }
//                println("用户输入的密钥生成的 AES 密钥: ${secretKey.encoded.joinToString("") { "%02x".format(it) }}")
            }
        }

        builder.setNegativeButton("取消", null)

        builder.show()
    }

    private fun backup(activity: AppCompatActivity) {
        Logger.d("BackupActivity", "Backup button clicked")
        val dateStr =
            android.text.format.DateFormat.format("yyyy-MM-dd-hh-mm-ss", Date()).toString()
        callback = { uri ->
            if (uri != null) {
//                val backupFile = File(uri.path!!)
//                Logger.d("BackupActivity", "Backup file: ${backupFile.absolutePath}")
//                println("AES Key: ${secretKey.encoded.joinToString("") { "%02x".format(it) }}")

                backupPrivateDataToZip(activity, uri, secretKey)
            }
        }
        openFileSavePicker("scanner-backup-$dateStr-.zip", "application/zip")
    }

    private fun generateAESKeyFromInput(input: String): SecretKey {
        // 生成 HMAC-SHA256 哈希
        val result = Encrypt.sha256HMAC(input, Constant.BH_APP_KEY, false)

        // 截取前 16 或 32 字节作为 AES 密钥
//        Logger.d("generateAESKeyFromInput", "HMAC-SHA256 result: $result")

        val aesKeyBytes = result.toByteArray().copyOf(32) // 32 字节对应 AES-256，16 字节对应 AES-128
        return SecretKeySpec(aesKeyBytes, "AES")
    }

    private fun openFileSavePicker(fileName: String, mimeType: String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = mimeType
            putExtra(Intent.EXTRA_TITLE, fileName) // 设置默认文件名
        }

        backupResultLauncher.launch(intent)
    }

    private fun backupPrivateDataToZip(context: Context, uri: Uri, secretKey: SecretKey) {
        val dataDir = context.filesDir.parentFile // 获取应用私有目录的根目录
        if (dataDir == null || !dataDir.exists()) {
            Logger.e("backupPrivateDataToZip", "Data directory does not exist!")
            return
        }

        try {
            val zeroIV = ByteArray(16) { 0 } // 16 字节的全零 IV

            // 创建 IvParameterSpec（初始化向量）
            val ivSpec = IvParameterSpec(zeroIV)
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)

            val contentResolver = context.contentResolver
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                CipherOutputStream(outputStream, cipher).use { cipherOutputStream ->
                    ZipOutputStream(cipherOutputStream).use { zos ->
                        zipDirectory(dataDir, dataDir.absolutePath, zos)
                    }
                }

            }
            Logger.i(
                "backupPrivateDataToZip",
                "Backup completed successfully! Saved to: ${uri.path}"
            )
        } catch (e: IOException) {
            e.printStackTrace()
            Logger.e("backupPrivateDataToZip", "Error during backup: " + e.message)
        }
    }

    private fun zipDirectory(sourceDir: File, basePath: String, zipOutStream: ZipOutputStream) {
        val files = sourceDir.listFiles() ?: return

        for (file in files) {
//            Logger.d("zipDirectory", "Zipping file: ${file.name}")
            val relativePath = file.absolutePath.removePrefix("$basePath/")

            if (file.isDirectory) {
                zipDirectory(file, basePath, zipOutStream) // 递归压缩子目录
            } else {
                FileInputStream(file).use { fis ->
                    zipOutStream.putNextEntry(ZipEntry(relativePath))
                    val buffer = ByteArray(1024)
                    var length: Int
                    while (fis.read(buffer).also { length = it } > 0) {
                        zipOutStream.write(buffer, 0, length)
                    }
                    zipOutStream.closeEntry()
                }
            }
        }

        Logger.getLogger(this).makeToast("备份已完成")
    }
}