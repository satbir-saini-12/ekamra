package com.ekamra.ott

import android.app.PictureInPictureParams
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.util.Rational
import android.view.WindowManager
import androidx.core.view.WindowCompat
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterFragmentActivity() {
    private val CHANNEL = "com.ekamra.ott/foldable"
    private var methodChannel: MethodChannel? = null
    
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        
        // Set up method channel for foldable state communication
        methodChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, CHANNEL)
    }
    
    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        // Enable edge-to-edge display (Android 15+ / API 35+)
        // API 36 (Android 16) also supports edge-to-edge
        if (Build.VERSION.SDK_INT >= 35) {
            WindowCompat.setDecorFitsSystemWindows(window, false)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
        
        // Enable foldable support (Android 12L+ / API 32+)
        if (Build.VERSION.SDK_INT >= 32) {
            window.attributes.layoutInDisplayCutoutMode = 
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        
        super.onCreate(savedInstanceState)
        
        // Notify Flutter about initial fold state
        notifyFoldState()
    }
    
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Notify Flutter when configuration changes (e.g., fold/unfold)
        notifyFoldState()
    }
    
    // Multi-window mode support (Android 7.0+ / API 24+)
    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean) {
        super.onMultiWindowModeChanged(isInMultiWindowMode)
        methodChannel?.invokeMethod("onMultiWindowModeChanged", isInMultiWindowMode)
    }
    
    // Picture-in-Picture support
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (isPictureInPictureSupported()) {
                val pipParams = createPipParams()
                if (pipParams != null) {
                    enterPictureInPictureMode(pipParams)
                }
            }
        }
    }
    
    private fun isPictureInPictureSupported(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        } else {
            false
        }
    }
    
    private fun createPipParams(): PictureInPictureParams? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Use 16:9 aspect ratio for video
            val aspectRatio = Rational(16, 9)
            PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .build()
        } else {
            null
        }
    }
    
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: android.content.res.Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        methodChannel?.invokeMethod("onPictureInPictureModeChanged", isInPictureInPictureMode)
    }
    
    // Notify Flutter about fold state and screen information
    private fun notifyFoldState() {
        if (Build.VERSION.SDK_INT >= 32) { // Android 12L+ / API 32+
            val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            val windowMetrics = windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds
            
            // Check if device is in tabletop mode (folded) or book mode (unfolded)
            val isTabletopMode = bounds.width() < bounds.height() && 
                                 bounds.width() < resources.displayMetrics.widthPixels * 0.7
            
            val foldInfo = mapOf(
                "width" to bounds.width(),
                "height" to bounds.height(),
                "isTabletopMode" to isTabletopMode,
                "isFoldable" to packageManager.hasSystemFeature(PackageManager.FEATURE_SENSOR_HINGE_ANGLE)
            )
            
            methodChannel?.invokeMethod("onFoldStateChanged", foldInfo)
        } else {
            // For older devices, just send basic screen info
            val displayMetrics = resources.displayMetrics
            val screenInfo = mapOf(
                "width" to displayMetrics.widthPixels,
                "height" to displayMetrics.heightPixels,
                "isTabletopMode" to false,
                "isFoldable" to false
            )
            methodChannel?.invokeMethod("onFoldStateChanged", screenInfo)
        }
    }
}

