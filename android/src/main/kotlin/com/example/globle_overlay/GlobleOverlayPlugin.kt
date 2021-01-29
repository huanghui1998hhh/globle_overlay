
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
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
         return true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + context!!.packageName))
                if (activity == null) {
                    if (context != null) {
                        context!!.startActivity(intent)
                        Toast.makeText(context, "Please grant, Can Draw Over Other Apps permission.", Toast.LENGTH_SHORT).show()
                        Log.e(Constants.TAG, "Can't detect the permission change, as the activity is null")
                    } else {
                        Log.e(Constants.TAG, "'Can Draw Over Other Apps' permission is not granted")
                        Toast.makeText(context, "Can Draw Over Other Apps permission is required. Please grant it from the app settings", Toast.LENGTH_LONG).show()
                    }
                } else {
                    activity!!.startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE)
                }
            } else {
                return true
            }
        }
        return false
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
    }

}

