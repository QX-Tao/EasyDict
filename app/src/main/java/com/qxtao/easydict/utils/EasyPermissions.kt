package com.qxtao.easydict.utils

import android.Manifest.permission.RECORD_AUDIO
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.qxtao.easydict.R

class EasyPermissions(private val activity: Activity) {

    private val permissionFragment: PermissionFragment = PermissionFragment()

    interface Subscribe {
        fun onResult(requestCode: Int, allGranted: Boolean, permissions: Array<String>?)
    }

    interface Callback {
        fun onRequestPermissionsCallback(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
        )
    }

    class PermissionFragment : Fragment() {
        private val callbacks = HashMap<Int, Callback>()
        private val runnable = ArrayList<Runnable>()

        private val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            result.entries.forEach { entry ->
                val permission = entry.key
                val granted = entry.value
                val grantResults = if (granted) PackageManager.PERMISSION_GRANTED else PackageManager.PERMISSION_DENIED
                callbacks.forEach { (requestCode, callback) ->
                    callback.onRequestPermissionsCallback(
                        requestCode,
                        arrayOf(permission),
                        intArrayOf(grantResults)
                    )
                }
            }
        }

        fun registerCallback(requestCode: Int, callback: Callback) {
            callbacks[requestCode] = callback
        }

        override fun onAttach(context: Context) {
            super.onAttach(context)
            runnable.forEach { it.run() }
            runnable.clear()
        }

        fun postRequestPermissions(permissions: Array<String>?) {
            if (isAdded) {
                permissionLauncher.launch(permissions ?: return)
            } else {
                runnable.add(Runnable { permissionLauncher.launch(permissions ?: return@Runnable) })
            }
        }
    }

    init {
        val fragmentManager: FragmentManager = when (activity) {
            is AppCompatActivity -> activity.supportFragmentManager
            else -> throw IllegalArgumentException("Invalid activity type")
        }
        fragmentTransaction(fragmentManager)
    }

    private fun fragmentTransaction(fragmentManager: FragmentManager) {
        fragmentManager.beginTransaction().apply {
            add(permissionFragment, PermissionFragment::class.simpleName)
            try { commitNow() } catch (_: IllegalStateException) {
                commitAllowingStateLoss() }
        }
    }

    fun need(permission: String): RequestPermissionsResult {
        return need(arrayOf(permission))
    }

    fun need(permissions: Array<String>): RequestPermissionsResult {
        return RequestPermissionsResult(activity, permissions, permissionFragment)
    }

    class RequestPermissionsResult(
        private val activity: Activity,
        private val permissions: Array<String>,
        private val permissionFragment: PermissionFragment
    ) : Callback {

        private var subscribe: Subscribe? = null
        private var showDialog = true

        fun showDialog(showDialog: Boolean): RequestPermissionsResult {
            this.showDialog = showDialog
            return this
        }

        private fun requestPermissions(permissions: Array<String>, requestCode: Int): Boolean {
            permissions.forEach { permission ->
                if (ActivityCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) {
                    permissionFragment.registerCallback(requestCode, this)
                    permissionFragment.postRequestPermissions(permissions)
                    return false
                }
            }
            return true
        }

        fun request(requestCode: Int) {
            if (!requestPermissions(permissions, requestCode)) return
            publish(requestCode, true, permissions)
        }

        private fun publish(requestCode: Int, allGranted: Boolean, permissions: Array<String>) {
            subscribe?.onResult(requestCode, allGranted, permissions)
        }

        fun subscribe(subscribe: Subscribe?): RequestPermissionsResult {
            this.subscribe = subscribe
            return this
        }

        override fun onRequestPermissionsCallback(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
        ) {
            activity.runOnUiThread {
                onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }

        private fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
        ) {
            grantResults.forEachIndexed { index, grant ->
                if (grant == PackageManager.PERMISSION_DENIED) {
                    if (showDialog) {
                        showDialogTips(permissions[index]) { _, _ -> publish(requestCode, false, permissions) }
                    } else {
                        publish(requestCode, false, permissions)
                    }
                    return
                }
            }
            publish(requestCode, true, permissions)
        }

        private fun showDialogTips(permission: String, onDenied: (DialogInterface, Int) -> Unit) {
            val permissionName = when(permission) {
                WRITE_EXTERNAL_STORAGE -> activity.getString(R.string.permission_write_external_storage)
                RECORD_AUDIO -> activity.getString(R.string.permission_record_audio)
                else -> throw IllegalArgumentException("Unknown permission: $permission")
            }
            AlertDialog.Builder(activity)
                .setTitle(activity.getString(R.string.hint))
                .setMessage(activity.getString(R.string.notify_eee_permission_desc, permissionName))
                .setCancelable(true)
                .setPositiveButton(activity.getString(R.string.confirm)) { _, _ ->
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data = Uri.fromParts("package", activity.packageName, null)
                    activity.startActivity(intent)}
                .setNegativeButton(activity.getString(R.string.cancel), onDenied)
                .create()
                .show()
        }
    }
}