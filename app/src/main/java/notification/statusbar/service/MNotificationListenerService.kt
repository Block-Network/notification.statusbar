package notification.statusbar.service

import StatusBarLyric.API.StatusBarLyric
import android.app.Notification
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import java.util.*

class MNotificationListenerService : NotificationListenerService() {
    private var appList: MutableList<PackageInfo>? = null
    var time: Long = 0
    var am: AudioManager? = null
    val activity = this
    var oldString: String = ""
    var isDisplay = false
    var preferences: SharedPreferences? = null
    var wlistPreferences: SharedPreferences? = null

    private val handler by lazy { Handler(Looper.getMainLooper()) }

    private var mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (!am!!.isMusicActive) {
                time = Date().time
                val bundle = msg.data
                val title = bundle.getString("title")
                val text = bundle.getString("text")
                val packageName = bundle.getString("packageName")
                Log.e("NotificationListener", "11111111")
                if (title != null && text != null && packageName != null) {
                    Log.e("NotificationListener", "2222222")
                    Log.e("NotificationListener", wlistPreferences!!.all[packageName].toString())
                    Log.e("NotificationListener", getApplicationNameByPackageName(packageName))

                    if (wlistPreferences!!.all[packageName] != null) {
                        if (wlistPreferences!!.all[packageName] as Boolean) {
                            if (!(preferences!!.getBoolean("showSameMessage", false)) && oldString != text) {
                                oldString = text
                                val appLabel = getApplicationNameByPackageName(packageName)
//                Log.e("NotificationListener", msgString)
                                StatusBarLyric(
                                    activity, null,
                                    "notification.statusbar", false
                                ).updateLyric(String.format("%s：%s %s", appLabel, title, text))
                                isDisplay = true
//                Toast.makeText(this@MNotificationListenerService, msgString, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        showToastOnLooper(activity, "启动成功")
        time = Date().time
        preferences =
            activity.getSharedPreferences("notification.statusbar.preferences", MODE_PRIVATE)
        wlistPreferences =
            activity.getSharedPreferences("whiteList", MODE_PRIVATE)
        appList = packageManager.getInstalledPackages(0)
        Log.e("NotificationListener", preferences!!.getInt("refreshInterval", 1000).toString())
        Timer().schedule(object : TimerTask() {
            override fun run() {
                if (am != null) {
                    if (!am!!.isMusicActive && isDisplay) {
                        if (((Date().time - time).toInt() > preferences!!.getInt("displayTime", 3000)) && isDisplay) {
                            StatusBarLyric(
                                activity, null,
                                "notification.statusbar", false
                            ).stopLyric()
//                    Log.e("NotificationListener", "stop")
                            time = Date().time
                            isDisplay = false
                        }
                    }
                }
            }
        }, 0, preferences!!.getInt("refreshInterval", 1000).toLong())
        Log.e("NotificationListener", preferences!!.getInt("refreshInterval", 1000).toString())
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras: Bundle = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE, "")
        val text = extras.getString(Notification.EXTRA_TEXT, "")
        am = this.getSystemService(AUDIO_SERVICE) as AudioManager
        val bundle = Bundle()
        bundle.putString("title", title)
        bundle.putString("text", text)
        bundle.putString("packageName", sbn.packageName)

        val message = mHandler.obtainMessage()
        message.data = bundle
//        message.obj = "$title：$content"
        mHandler.sendMessage(message)


        Log.e("NotificationListener", "333333333333")
        Log.e("NotificationListener", sbn.packageName)

    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {}

    // 弹出toast
    private fun showToastOnLooper(context: Context?, message: String?) {
        try {
            handler.post { Toast.makeText(context, message, Toast.LENGTH_LONG).show() }
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }

    private fun getApplicationNameByPackageName(packageName: String): String {
        val pm = activity.packageManager;
        return pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
    }

}