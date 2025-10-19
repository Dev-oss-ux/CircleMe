package com.barry.circleme.utils

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File

class VoiceRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null

    fun start(outputFile: File): Boolean {
        return try {
            recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                // Use the simpler, more reliable method of setting the output file path
                setOutputFile(outputFile.absolutePath)

                prepare()
                start()
            }
            true
        } catch (e: Exception) {
            Log.e("VoiceRecorder", "Failed to start recording", e)
            false
        }
    }

    fun stop() {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            // The recorder might have already been stopped or released, or failed to start.
            Log.w("VoiceRecorder", "Failed to stop recording gracefully", e)
        } finally {
            recorder = null
        }
    }
}
