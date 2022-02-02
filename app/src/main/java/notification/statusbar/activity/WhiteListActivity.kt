@file:Suppress("DEPRECATION")

package notification.statusbar.activity

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.os.Bundle
import android.preference.Preference
import android.preference.Preference.OnPreferenceChangeListener
import android.preference.PreferenceActivity
import android.preference.SwitchPreference
import notification.statusbar.R


@SuppressLint("ExportedPreferenceActivity")
class WhiteListActivity : PreferenceActivity() {
    private var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addPreferencesFromResource(R.xml.wlist_preferences)
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        progressDialog = ProgressDialog(this)
        progressDialog!!.setTitle("白名单软件")
        progressDialog!!.setMessage("加载中软件列表中...")
        progressDialog!!.isIndeterminate = true
        progressDialog!!.setCancelable(false)
        progressDialog!!.show()
        showList()

//        progressDialog.dismiss()
    }

    // 弹出toast
    private fun showList() {
        try {
            object : Thread() {
                override fun run() {
                    val preferences = this@WhiteListActivity.getSharedPreferences("whiteList", MODE_PRIVATE)
                    val packages = packageManager.getInstalledPackages(0)
//                    packages.contains()
//                    for (key in preferences.all.keys) {
//                        try {
//                         this@WhiteListActivity.packageManager.getPackageInfo(key, 0);
//                        } catch (_ : PackageManager.NameNotFoundException) {
//                        }
//                        Log.e(getString(R.string.app_name), key)
//                    }
                    for (packageInfo in packages) {
                        val switchPreference = SwitchPreference(this@WhiteListActivity)
                        switchPreference.isChecked = preferences.getBoolean(packageInfo.packageName, false)
                        switchPreference.summary = packageInfo.packageName
                        switchPreference.title = packageInfo.applicationInfo.loadLabel(packageManager)
                        switchPreference.icon = packageInfo.applicationInfo.loadIcon(packageManager)
                        switchPreference.onPreferenceChangeListener =
                            OnPreferenceChangeListener { _: Preference, newValue: Any ->
                                preferences.edit().putBoolean(switchPreference.summary.toString(), newValue as Boolean)
                                    .apply()
                                true
                            }
                        preferenceScreen.addPreference(switchPreference)
                    }
                    progressDialog!!.dismiss()
                }
            }.start()

        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
    }
}