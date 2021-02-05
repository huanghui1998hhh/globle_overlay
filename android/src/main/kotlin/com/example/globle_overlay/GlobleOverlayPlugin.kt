
package com.example.globle_overlay

import android.app.*
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.*
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import com.example.globle_overlay.utils.Constants
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result


/** GlobleOverlayPlugin */
class GlobleOverlayPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel : MethodChannel
    private lateinit var context: Context
    private lateinit var activity: Activity


    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        context = flutterPluginBinding.applicationContext
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "globle_overlay")
        channel.setMethodCallHandler(this)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity;
    }

    override fun onDetachedFromActivityForConfigChanges() {}

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {}

    override fun onDetachedFromActivity() {}

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "getPlatformVersion" -> {
                result.success("Android ${android.os.Build.VERSION.RELEASE}")
            }
            "openOverlay" -> {
                result.success(true)
                val intent = Intent(context, Overlay::class.java)
                intent.putExtra("argument",call.argument<Any>("argument") as String)
                context.startService(intent)
            }
            "closeOverlay" -> {
                result.success(false)
                val intent = Intent(context, Overlay::class.java)
                context.stopService(intent)
            }
            "checkPermission" -> {
                result.success(checkPermission())
            }
            "openAppWithPackageName" -> {
                startAppIntent(call.arguments<String>())
            }
            "getTopApp" -> {
                result.success(RunningTaskUtil(context).getTopRunningTasks()?.packageName)
            }
            "checkAppPermission" -> {
                result.success(checkAppPermission())
            }
            "startListen" -> {
                Log.i("TAG","开始111")
                result.success(true)
                val intent = Intent(context, ListenerService::class.java)
                intent.putExtra("argument", call.arguments as ArrayList<*>)
                context.startService(intent)
            }
            "endListen" -> {
                result.success(false)
                val intent = Intent(context, ListenerService::class.java)
                context.stopService(intent)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun checkPermission(): Boolean {
        Log.i("TAG","正在获取浮窗权限")
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context!!.packageName))
                activity.startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
            } else {
                return true
            }
        }
        return false
    }

    private fun checkAppPermission(): Boolean {
        Log.i("TAG","正在获取使用情况权限")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                    intent.data = Uri.fromParts("package", context.packageName, null)
                }
                activity.startActivityForResult(intent, MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS)
            } else {
                return true
            }
        }
        return false
    }

    private fun startAppIntent(packageName: String?) {
        val intent = context.packageManager.getLaunchIntentForPackage(packageName!!)
        intent!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent = PendingIntent.getActivity(context.applicationContext, 0, intent, 0)
        try {
            pendingIntent.send()
        } catch(e: PendingIntent.CanceledException) {
            e.printStackTrace()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private fun handleBubblesPermissionForAndroidQ(): Boolean {
        val devOptions = Settings.Secure.getInt(context!!.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0)
        return if (devOptions == 1) {
            Log.d(Constants.TAG, "Android bubbles are enabled")
            true
        } else {
            Log.e(Constants.TAG, "System Alert Window will not work without enabling the android bubbles")
            Toast.makeText(context, "Enable android bubbles in the developer options, for System Alert Window to work", Toast.LENGTH_LONG).show()
            false
        }
    }

    companion object{
        var ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 1237
        var MY_PERMISSIONS_REQUEST_PACKAGE_USAGE_STATS = 1101
    }

}

