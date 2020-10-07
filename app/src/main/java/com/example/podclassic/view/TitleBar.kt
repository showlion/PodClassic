package com.example.podclassic.view

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.os.BatteryManager
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import com.example.podclassic.`object`.MediaPlayer
import com.example.podclassic.activity.MainActivity
import com.example.podclassic.util.Colors
import com.example.podclassic.util.Icons
import com.example.podclassic.util.ThreadUtil
import com.example.podclassic.util.Values
import com.example.podclassic.util.Values.DEFAULT_PADDING
import java.text.SimpleDateFormat
import java.util.*


class TitleBar(context: Context, attributeSet: AttributeSet) : FrameLayout(context, attributeSet), MediaPlayer.OnMediaChangeListener {

    private val paint by lazy { Paint() }

    private var title = TextView(context)
    private var  playState = ImageView(context)
    private var battery = BatteryView(context)

    init {
        setBackgroundColor(Colors.white)
        val layoutParams1 = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        layoutParams1.gravity = Gravity.LEFT
        addView(playState, layoutParams1)
        val layoutParams2 = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT)
        layoutParams2.gravity = Gravity.RIGHT
        addView(battery, layoutParams2)
        val layoutParams3 = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        layoutParams3.gravity = Gravity.CENTER
        title.textSize = 14f
        title.setPadding(DEFAULT_PADDING * 4, DEFAULT_PADDING / 4, DEFAULT_PADDING * 4, DEFAULT_PADDING / 4)
        addView(title, layoutParams3)
    }

    private var timer : Timer? = null
    @SuppressLint("SimpleDateFormat")
    private val simpleDateFormat = SimpleDateFormat("HH:mm")

    private fun setTimer() {
        if (timer == null) {
            timer = Timer()
            timer!!.schedule(object : TimerTask() {
                override fun run() {
                    ThreadUtil.runOnUiThread(Runnable { title.text = simpleDateFormat.format(Date()) })
                }
            }, 0, 60 * 1000)
        }
    }

    private fun cancelTimer() {
        timer?.cancel()
        timer = null
    }

    fun showTime() {
        setTimer()
    }

    fun showTitle(title : String) {
        cancelTimer()
        this.title.text = title
    }

    override fun onMediaChange() {}

    override fun onPlayStateChange() {
        if (MediaPlayer.isPlaying) {
            playState.setImageDrawable(Icons.PLAY_BLUE.drawable)
        } else {
            playState.setImageDrawable(if (MediaPlayer.getCurrent() == null) null else Icons.PAUSE_BLUE.drawable)
        }
    }
    private var broadcastReceiverRegistered = false

    private fun onStart() {
        MediaPlayer.addOnMediaChangeListener(this)
        registerBroadcastReceiver()
        setTimer()
        onPlayStateChange()
    }

    private fun onStop() {
        MediaPlayer.removeOnMediaChangeListener(this)
        unregisterBroadcastReceiver()
        cancelTimer()
    }

    override fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        super.onWindowFocusChanged(hasWindowFocus)
        if (hasWindowFocus) {
            onStart()
        } else {
            onStop()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        onStop()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.shader = Colors.getShader(width / 2f, 0f, width / 2f, height.toFloat(), Colors.background_dark_1, Colors.background_dark_2)
        canvas?.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        paint.shader = null
        paint.color = Colors.line
        canvas?.drawRect(0f, height - 1f, width.toFloat(), height.toFloat(), paint)
    }

    private fun registerBroadcastReceiver() {
        if (!broadcastReceiverRegistered) {
            val intentFilter = IntentFilter()
            intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
            context.registerReceiver(batteryBroadcastReceiver, intentFilter)
            broadcastReceiverRegistered = true
        }
    }

    private fun unregisterBroadcastReceiver() {
        if (broadcastReceiverRegistered) {
            context.unregisterReceiver(batteryBroadcastReceiver)
            broadcastReceiverRegistered = false
        }

    }

    private val batteryBroadcastReceiver : BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            battery.batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 100) * 100 / intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
            battery.isCharging = (intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) != 0).or(intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1) == BatteryManager.BATTERY_STATUS_CHARGING)
            battery.refreshBattery()
        }
    }

    class BatteryView(context: Context) : View(context) {

        private val paint by lazy { Paint() }

        var isCharging = false
        var batteryLevel = 100

        override fun onDraw(canvas: Canvas?) {
            super.onDraw(canvas)
            paint.color = Colors.line

            val padding = height / 5f

            canvas?.drawRoundRect(0f, padding, width - padding * 3, height - padding, 4f, 4f, paint)
            canvas?.drawRoundRect(width - padding * 3 - 2f, padding * 1.5f, width - padding * 2.5f + 2f, height - padding * 1.5f, 4f, 4f, paint)
            //canvas?.drawRoundRect(0f, PADDING, width - PADDING * 5, measuredHeight - PADDING, 4f, 4f, paint)
            //canvas?.drawRoundRect(measuredWidth - PADDING * 5 - 2, PADDING * 1.5f, measuredWidth - PADDING * 6 - 2, measuredHeight - PADDING * 1.5f, 4f, 4f, paint)
            val batteryWidth =  batteryLevel * (width - padding * 3) / 100f

            when {
                (isCharging && batteryLevel != 100) -> paint.color = Colors.battery_yellow
                (batteryLevel <= 20) -> paint.color = Colors.battery_red
                else -> paint.color = Colors.battery_green
            }
            canvas?.drawRoundRect(2f, padding + 2, batteryWidth - 2, height - padding - 2, 4f, 4f, paint)
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            val height = MeasureSpec.getSize(heightMeasureSpec)
            when {
                Values.resolution >= Values.RESOLUTION_HIGH -> setMeasuredDimension(150, height)
                Values.resolution <= Values.RESOLUTION_LOW -> setMeasuredDimension(72, height)
                else -> setMeasuredDimension(120, height)
            }
        }

        fun refreshBattery() {
            invalidate()
        }
    }
}