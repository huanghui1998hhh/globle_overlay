package com.example.globle_overlay

import android.app.Activity
import android.view.Gravity
import android.widget.*
import androidx.annotation.NonNull
import com.example.globle_overlay.enums.ShowPattern
import com.example.globle_overlay.enums.SidePattern
import com.example.globle_overlay.interfaces.OnInvokeView
import com.example.globle_overlay.widget.RoundProgressBar
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar


/** GlobleOverlayPlugin */
class GlobleOverlayPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var registrar : Registrar
  var activity: Activity? = null


    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "globle_overlay")
    channel.setMethodCallHandler(this)
  }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity;
    }

    override fun onDetachedFromActivityForConfigChanges() {

    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {

    }

    override fun onDetachedFromActivity() {

    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    if (call.method == "getPlatformVersion") {
      result.success("Android ${android.os.Build.VERSION.RELEASE}")
    } else if(call.method == "openOverlay"){
      EasyFloat.with(registrar.context())
              .setShowPattern(ShowPattern.ALL_TIME)
              .setSidePattern(SidePattern.RESULT_SIDE)
              .setGravity(Gravity.CENTER)
              .setLayout(R.layout.float_app, OnInvokeView {
                  it.findViewById<ImageView>(R.id.ivClose).setOnClickListener {
                      EasyFloat.dismissAppFloat()
                  }
                  it.findViewById<TextView>(R.id.tvOpenMain).setOnClickListener {
                      startActivity<GlobleOverlayPlugin>(registrar.context())
                  }
                  it.findViewById<CheckBox>(R.id.checkbox)
                          .setOnCheckedChangeListener { _, isChecked ->
                              EasyFloat.appFloatDragEnable(isChecked)
                          }

                  val progressBar = it.findViewById<RoundProgressBar>(R.id.roundProgressBar).apply {
                      setProgress(66, "66")
                      setOnClickListener { toast(getProgressStr()) }
                  }
                  it.findViewById<SeekBar>(R.id.seekBar)
                          .setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                              override fun onProgressChanged(
                                      seekBar: SeekBar?, progress: Int, fromUser: Boolean
                              ) = progressBar.setProgress(progress, progress.toString())

                              override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                              override fun onStopTrackingTouch(seekBar: SeekBar?) {}
                          })

//                // 解决 ListView 拖拽滑动冲突
//                it.findViewById<ListView>(R.id.lv_test).apply {
//                    adapter = MyAdapter(
//                        this@MainActivity,
//                        arrayOf("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "...")
//                    )
//
//                    // 监听 ListView 的触摸事件，手指触摸时关闭拖拽，手指离开重新开启拖拽
//                    setOnTouchListener { _, event ->
//                        logger.e("listView: ${event.action}")
//                        EasyFloat.appFloatDragEnable(event?.action == MotionEvent.ACTION_UP)
//                        false
//                    }
//                }
              })
              .show()
    } else if(call.method == "closeOverlay"){
        EasyFloat.dismissAppFloat()
    } else {
      result.notImplemented()
    }
  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

    private fun toast(string: String = "onClick") =
            Toast.makeText(registrar.context(), string, Toast.LENGTH_SHORT).show()


}
