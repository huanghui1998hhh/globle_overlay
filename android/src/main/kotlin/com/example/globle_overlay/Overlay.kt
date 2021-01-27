package com.example.globle_overlay

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.WindowManager
import android.widget.Button
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import java.util.*

class Overlay : Service() {
    private var windowManager: WindowManager? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var button: Button? = null

    private val binder: IBinder = LocalBinder()

    private var startClickTime: Long = 0

    override fun onCreate() {
        super.onCreate()
        isStarted = true
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        layoutParams = WindowManager.LayoutParams()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams!!.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            layoutParams!!.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        layoutParams!!.format = PixelFormat.RGBA_8888
        layoutParams!!.gravity = Gravity.LEFT or Gravity.TOP
        layoutParams!!.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        layoutParams!!.width = 500
        layoutParams!!.height = 100
        layoutParams!!.x = 300
        layoutParams!!.y = 300
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return false
    }

    override fun onDestroy() {
        try {
            if (button != null) windowManager!!.removeView(button)
            isStarted = false
        } catch (e: Exception) {
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showFloatingWindow()
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showFloatingWindow() {
        try {
            button = Button(applicationContext)
            button!!.text = "Floating Window"
            button!!.setBackgroundColor(Color.BLUE)
            windowManager!!.addView(button, layoutParams)
            //拖拽监听
            button!!.setOnTouchListener(FloatingOnTouchListener())
        }catch (e: Exception) {
            Log.e("globle_overlay", "Exception: " + e.message)
        }
    }

    //拖拽处理函数
    private inner class FloatingOnTouchListener : OnTouchListener {
        private var x = 0
        private var y = 0
        override fun onTouch(view: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    x = event.rawX.toInt()
                    y = event.rawY.toInt()
                    startClickTime = Calendar.getInstance().timeInMillis
                }
                MotionEvent.ACTION_UP ->{
                    val clickDuration = Calendar.getInstance().timeInMillis - startClickTime
                    if (clickDuration < Constants.MAX_CLICK_DURATION) {
                        startAppIntent(packageName)
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    val nowX = event.rawX.toInt()
                    val nowY = event.rawY.toInt()
                    val movedX = nowX - x
                    val movedY = nowY - y
                    x = nowX
                    y = nowY
                    layoutParams!!.x = layoutParams!!.x + movedX
                    layoutParams!!.y = layoutParams!!.y + movedY
                    windowManager!!.updateViewLayout(view, layoutParams)
                }
                else -> {
                }
            }
            return false
        }
    }

    private fun startAppIntent(packageName: String?) {
        val intent = packageManager.getLaunchIntentForPackage(packageName!!)
        intent!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)
        try {
            pendingIntent.send()
        } catch (e: PendingIntent.CanceledException) {
            e.printStackTrace()
        }
    }

    companion object {
        var isStarted = false
    }

    inner class LocalBinder : Binder() {
        val service: Overlay
            get() = this@Overlay
    }
}


