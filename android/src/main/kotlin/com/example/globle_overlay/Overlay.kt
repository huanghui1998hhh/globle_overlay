package com.example.globle_overlay

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.PixelFormat
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.*
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.Nullable
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import com.example.globle_overlay.utils.Constants
import java.util.*


class Overlay : Service() {
    var context: Context? = null

    private var widget: LinearLayout? = null
    private var windowManager: WindowManager? = null
    private var layoutParams: WindowManager.LayoutParams? = null

    private val binder: IBinder = LocalBinder()

    private var startClickTime: Long = 0

    private var argument: String? = null

    @SuppressLint("ServiceCast")
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        widget = LayoutInflater.from(this).inflate(R.layout.layout, null) as LinearLayout
    }

    @Nullable
    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return false
    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
    }

    override fun onDestroy() {
        try {
            if (widget != null) windowManager!!.removeView(widget)
        } catch (e: Exception) {
        }

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        context = applicationContext
        try {
            val extras = intent.extras
            if (extras != null) {
                Log.i(Constants.TAG, extras.toString())
                argument = extras.getString("argument")
            } else {
                Log.i(Constants.TAG, "No intent Extras")
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, e.message)
        }
        showFloatingWindow()
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showFloatingWindow() {
        try {
            layoutParams = WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
            )
//            layoutParams!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    or View.SYSTEM_UI_FLAG_FULLSCREEN
//                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

//            activity?.actionBar?.hide()

            widget?.findViewById<TextView>(R.id.textView)?.text = argument
            widget?.findViewById<Button>(R.id.backButton)?.setOnClickListener {
                startAppIntent(packageName)
                if (widget != null) windowManager!!.removeView(widget)
            }
            widget?.findViewById<TextView>(R.id.textView3)?.setOnClickListener {
                showDialog()
            }
            widget?.findViewById<TextView>(R.id.textView3)?.paint?.flags = Paint. UNDERLINE_TEXT_FLAG;
            widget?.findViewById<ImageView>(R.id.imageView)?.setImageResource(R.drawable.ic_heart_break_icon);
            widget?.findViewById<ImageView>(R.id.imageView2)?.setImageResource(R.drawable.ic_todomato_icon);
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            windowManager!!.addView(widget, layoutParams)

            val valueAnimator = ValueAnimator.ofInt(386, 0).setDuration(15000)
            valueAnimator.addUpdateListener { animation ->
                widget?.findViewById<Button>(R.id.buttonBackground)?.layoutParams?.width = animation.animatedValue as Int
                widget?.findViewById<Button>(R.id.buttonBackground)?.requestLayout()
            }
            valueAnimator.start()
//            widget?.setOnTouchListener(FloatingOnTouchListener())
        }catch (e: Exception){
            Log.e(Constants.TAG, "Exception: " + e.message)
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
                MotionEvent.ACTION_UP -> {
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

    inner class LocalBinder : Binder() {
        val service: Overlay
            get() = this@Overlay
    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.timerDeepFocusAllowListTitle)
        builder.setMessage(R.string.timerDeepFocusAllowListDesc)
        builder.setPositiveButton(R.string.actionOK) { _, _ ->  }
        val alertDialog = builder.create()
        alertDialog.window!!.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        alertDialog.setCanceledOnTouchOutside(true)
        alertDialog.show()
    }
}


