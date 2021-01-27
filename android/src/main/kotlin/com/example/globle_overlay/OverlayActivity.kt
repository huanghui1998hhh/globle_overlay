package com.example.globle_overlay.activity

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import com.example.globle_overlay.EasyFloat
import com.example.globle_overlay.R
import com.example.globle_overlay.enums.ShowPattern
import com.example.globle_overlay.enums.SidePattern
import com.example.globle_overlay.interfaces.OnInvokeView
import com.example.globle_overlay.permission.PermissionUtils
import com.example.globle_overlay.startActivity
import com.example.globle_overlay.widget.RoundProgressBar
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel

class OverlayActivity: Activity() {
    lateinit var flutterEngine: FlutterEngine
    lateinit var channel: MethodChannel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        flutterEngine = FlutterEngine(this)
        flutterEngine.dartExecutor.executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault())
        channel = MethodChannel(flutterEngine.dartExecutor, "com.example.globle_overlay")
    }


    private fun showAppFloat() {
        EasyFloat.with(this)
                .setShowPattern(ShowPattern.ALL_TIME)
                .setSidePattern(SidePattern.RESULT_SIDE)
                .setGravity(Gravity.CENTER)
                .setLayout(R.layout.float_app, OnInvokeView {
                    it.findViewById<ImageView>(R.id.ivClose).setOnClickListener {
                        EasyFloat.dismissAppFloat()
                    }
                    it.findViewById<TextView>(R.id.tvOpenMain).setOnClickListener {
                        startActivity<OverlayActivity>(this)
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
    }

    private fun toast(string: String = "onClick") =
            Toast.makeText(this, string, Toast.LENGTH_SHORT).show()

    private fun checkPermission() {
        if (PermissionUtils.checkPermission(this)) {
            showAppFloat()
        } else {
            AlertDialog.Builder(this)
                    .setMessage("使用浮窗功能，需要您授权悬浮窗权限。")
                    .setPositiveButton("去开启") { _, _ ->
                        showAppFloat()
                    }
                    .setNegativeButton("取消") { _, _ -> }
                    .show()
        }
    }

}