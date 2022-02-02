@file:Suppress("DEPRECATION")

package notification.statusbar.activity

import StatusBarLyric.API.StatusBarLyric
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.EditTextPreference
import android.preference.Preference
import android.preference.PreferenceActivity
import android.preference.SwitchPreference
import android.provider.Settings
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.util.Log
import android.widget.Toast
import notification.statusbar.R
import notification.statusbar.service.MNotificationListenerService

@SuppressLint("ExportedPreferenceActivity")
class SettingsActivity : PreferenceActivity() {
    private val activity: Activity = this
    private val runSelfCodeTag = 13131
    private val nLSettingCodeTag = 13132

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.root_preferences)
        init()
    }

    @SuppressLint("BatteryLife")
    private fun init() {

        val serviceSwitch = (findPreference("serviceSwitch") as SwitchPreference)
        val iSServiceSwitch = StatusBarLyric(activity, null, "notification.statusbar", false).hasEnable()

        serviceSwitch.isEnabled = iSServiceSwitch
        serviceSwitch.isChecked = iSServiceSwitch
        if (!iSServiceSwitch) {
            serviceSwitch.summary = "此软件为 墨•状态栏歌词 的扩展\n请先安装主模块或，在主模块激活本扩展"
        }
        serviceSwitch.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue: Any ->
            (newValue as Boolean)
            true
        }

        val nLSetting = (findPreference("nLSetting") as SwitchPreference)
        nLSetting.isChecked = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")!!
            .contains(MNotificationListenerService::class.java.name)
        nLSetting.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue: Any ->
            val value = (newValue as Boolean)
            if (value) {
                val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
                startActivityForResult(intent, nLSettingCodeTag)
            }
            true
        }
        val runSelf = findPreference("runSelf")!!
        runSelf.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val packageURI = Uri.parse("package:" + "notification.statusbar")
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI)
            startActivity(intent)
            true
        }
        val batteryLife = findPreference("batteryLife")!!
        batteryLife.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            @Suppress("SameParameterValue") val intent = Intent(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.data = Uri.parse("package:" + activity.packageName)
            startActivity(intent)
            true
        }
        val whiteList = findPreference("whiteList")!!
        whiteList.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            startActivity(Intent(activity, WhiteListActivity::class.java))
            true
        }
        val displayTime = (findPreference("displayTime") as EditTextPreference)
        displayTime.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue: Any ->
            displayTime.summary = newValue.toString()
            true
        }
        val refreshInterval = (findPreference("refreshInterval") as EditTextPreference)
        refreshInterval.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue: Any ->
            refreshInterval.summary = newValue.toString()
            true
        }


        val run = findPreference("run")!!
        run.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            if ((serviceSwitch.isChecked && nLSetting.isChecked)) {
                val intent = Intent(this, MNotificationListenerService::class.java)
                startService(intent)
                toast("正在启动")
                Log.e(getString(R.string.app_name), "正在启动")
            } else {
                toast("请授予全部必要权限")
            }

            true
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == runSelfCodeTag) {
            return
        } else if (requestCode == nLSettingCodeTag) {
            (findPreference("nLSetting") as SwitchPreference).isChecked =
                Settings.Secure.getString(contentResolver, "enabled_notification_listeners")!!
                    .contains(MNotificationListenerService::class.java.name)
        }
    }

    private fun toast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }

}
