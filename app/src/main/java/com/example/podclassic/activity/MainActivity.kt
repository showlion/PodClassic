package com.example.podclassic.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Outline
import android.graphics.Point
import android.os.Bundle
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.podclassic.R
import com.example.podclassic.`object`.Core
import com.example.podclassic.`object`.MediaPlayer
import com.example.podclassic.fragment.SplashFragment
import com.example.podclassic.service.MediaPlayerService
import com.example.podclassic.storage.SPManager
import com.example.podclassic.util.FileUtil
import com.example.podclassic.util.MediaUtil
import com.example.podclassic.util.ThreadUtil
import com.example.podclassic.util.Values
import com.example.podclassic.view.MusicPlayerView
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    companion object {
        const val TOP_MARGIN = 24
        var screenRatio = 0f
        var statusBarHeight = 0
    }


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        initMediaPlayer(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initScreen()

        setContentView(R.layout.activity_main)

        if (!SPManager.getBoolean(SPManager.SP_STARTED)) {
            SPManager.setBoolean(SPManager.SP_STARTED, true)
            SPManager.reset()
        }

        initView()

        Core.init(slide_controller, screen, title_bar, night_mode, this)

        checkPermission()

        initMediaPlayer(intent)

        startService(Intent(this, MediaPlayerService::class.java))

        val autoStop = SPManager.getInt(SPManager.AutoStop.SP_NAME)
        if (autoStop != 0) {
            MediaPlayer.scheduleToStop(SPManager.AutoStop.getMinute(autoStop))
        }
    }

    private fun prepare() {
        Core.lock(true)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.frame_layout, SplashFragment(Runnable {
                MediaUtil.prepare()
                if (SPManager.getBoolean(SPManager.SP_AUTO_START)) {
                    MediaPlayer.shufflePlay()
                    if (Core.getView() !is MusicPlayerView) {
                        ThreadUtil.runOnUiThread(Runnable {
                            Core.addView(MusicPlayerView(this))
                        })
                    }
                }
            }))
            .commit()
    }

    override fun onBackPressed() {
        if (Core.lock) {
            return
        }
        if (!Core.removeView()) {
            Core.exit()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            Core.setNightMode(SPManager.NightMode.nightMode(SPManager.getInt(SPManager.NightMode.SP_NAME)))
        }
    }

    private fun initMediaPlayer(intent: Intent?) {
        val uri = intent?.data
        if (uri != null) {
            MediaPlayer.add(MediaUtil.getMusic(FileUtil.uriToPath(uri)))
        }
        if ((uri != null || intent?.action == MediaPlayerService.ACTION_MAIN) && Core.getView() !is MusicPlayerView) {
            Core.addView(MusicPlayerView(this))
        }
    }

    private fun initScreen() {
        val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            statusBarHeight = resources.getDimensionPixelSize(resourceId)
        }

        val point = Point()
        windowManager.defaultDisplay.getSize(point)

        val height = point.y
        val width = point.x

        Values.resolution = width
        screenRatio = height.toFloat() / width.toFloat()
    }

    private fun initView() {
        val layoutParams = slide_controller.layoutParams as RelativeLayout.LayoutParams

        when {
            screenRatio < 16f / 9f -> {
                layoutParams.leftMargin = layoutParams.leftMargin * 7 / 6
                layoutParams.rightMargin = layoutParams.rightMargin * 7 / 6
                layoutParams.topMargin /= 2
                layoutParams.bottomMargin /= 4
            }
            screenRatio == 16f / 9f -> {
                layoutParams.topMargin /= 2
                layoutParams.bottomMargin /= 2
            }
            screenRatio <= 17f / 9f -> {
                layoutParams.bottomMargin /= 2
            }
            screenRatio >= 21f / 10f -> {
                layoutParams.topMargin = layoutParams.topMargin * 2
            } else -> {
                layoutParams.topMargin = layoutParams.topMargin * 3 / 2
            }
        }

        (frame_layout.layoutParams as RelativeLayout.LayoutParams).topMargin = statusBarHeight + TOP_MARGIN

        frame_layout.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, 14f)
            }
        }
        frame_layout.clipToOutline = true

    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        } else {
            prepare()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            exitProcess(0)
        } else {
            prepare()
        }
    }
}
