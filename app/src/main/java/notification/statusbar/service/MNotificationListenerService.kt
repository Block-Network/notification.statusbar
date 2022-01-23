package notification.statusbar.service

import StatusBarLyric.API.StatusBarLyric
import android.app.Notification
import android.media.AudioManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import java.util.*

class MNotificationListenerService : NotificationListenerService() {
    var time: Long = 0
    var am: AudioManager? = null
    var oldString: String = ""

    //    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private var mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            if (!am!!.isMusicActive && msg.obj != null) {
                time = Date().time
                val msgString = msg.obj as String
                if (oldString != msgString) {
                    oldString = msgString
//                Log.e("NotificationListener", msgString)
                    StatusBarLyric(
                        this@MNotificationListenerService, null,
                        "notification.statusbar", false
                    ).updateLyric(msgString)
//                Toast.makeText(this@MNotificationListenerService, msgString, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        time = Date().time
        Timer().schedule(object : TimerTask() {
            override fun run() {
//                showToastOnLooper(this@MNotificationListenerService, (((Date().time - time) > 2).toString()))
//                Log.e("NotificationListener", (Date().time - time).toString())
                if (am != null) {
                    if (!am!!.isMusicActive) {
                        if ((Date().time - time).toInt() > 3000) {
                            StatusBarLyric(
                                this@MNotificationListenerService, null,
                                "notification.statusbar", false
                            ).stopLyric()
//                    Log.e("NotificationListener", "stop")
                            time = Date().time
                        }
                    }
                }
            }
        }, 0, 1000)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val extras: Bundle = sbn.notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE, "")
        val content = extras.getString(Notification.EXTRA_TEXT, "")
        am = this.getSystemService(AUDIO_SERVICE) as AudioManager
        val message = mHandler.obtainMessage()
        message.obj = "$title：$content"
        mHandler.sendMessage(message)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {}

//    // 弹出toast
//    fun showToastOnLooper(context: Context?, message: String?) {
//        try {
//            handler.post { Toast.makeText(context, message, Toast.LENGTH_LONG).show() }
//        } catch (e: RuntimeException) {
//            e.printStackTrace()
//        }
//    }
}