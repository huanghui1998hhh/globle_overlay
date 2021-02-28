package com.example.globle_overlay

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.*
import android.util.Log
import androidx.annotation.RequiresApi

class ListenerService : Service() {
  lateinit var whiteListSp :SharedPreferences
  lateinit var argument: MutableSet<String>
  lateinit var handler: Handler
  lateinit var context: Context

  lateinit var taskName: String

  private val binder: IBinder = LocalBinder()

  @RequiresApi(Build.VERSION_CODES.M)
  override fun onCreate() {
    super.onCreate()

    handler = Handler(Looper.getMainLooper())

    context = applicationContext

    whiteListSp = context.getSharedPreferences("whiteList",Context.MODE_PRIVATE)

    Log.i("TAG","onCreateD")
  }

  @SuppressLint("HandlerLeak")
  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    try {
      val extras = intent.extras
      if (extras != null) {
        extras.getString("taskName")?.let { taskName = it }
      }
    } catch(e: Exception) {}

    Log.i("TAG","onStartCommand")
    argument = whiteListSp.getStringSet("whiteList", setOf(context.packageName)) as MutableSet<String>
    argument.add(context.packageName)

    Log.i("TAG",argument.toString())

    flag = true

    object : Thread() {
      @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
      override fun run() {
        super.run()
        while(flag) {
          synchronized(ListenerService::class.java) {
            val topApp =  RunningTaskUtil(applicationContext).getTopRunningTasks()?.packageName
            val mIntent = Intent(context, Overlay::class.java)
            Log.i("TAG",topApp.toString())
            if(argument.contains(topApp)){
              handler.post {
                context.stopService(mIntent)
              }
            }else{
              handler.post {
                mIntent.putExtra("argument",taskName)
                context.startService(mIntent)
              }
            }
            SystemClock.sleep(500)
          }
        }
      }
    }.start()

    return START_STICKY
  }

  override fun onDestroy() {
    flag = false
    super.onDestroy()
  }

  override fun onBind(intent: Intent?): IBinder? {
    return binder
  }

  override fun onRebind(intent: Intent?) {
    super.onRebind(intent)
  }

  override fun onUnbind(intent: Intent?): Boolean {
    return false
  }

  companion object {
    private const val TAG = "ListenerService"
    private var flag = true
  }

  inner class LocalBinder : Binder() {
    val service: ListenerService
      get() = this@ListenerService
  }
}

