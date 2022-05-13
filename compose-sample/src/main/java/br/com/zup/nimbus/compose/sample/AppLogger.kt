package br.com.zup.nimbus.compose.sample

import android.util.Log
import com.zup.nimbus.core.log.LogLevel
import com.zup.nimbus.core.log.Logger

class AppLogger : Logger {
    private var LOG_TAG = "NIMBUS"
    private var isEnabled = true

    override fun enable() {
        isEnabled = true
    }

    override fun disable() {
        isEnabled = false
    }

    override fun log(message: String, level: LogLevel) {
        if(isEnabled) {
            Log.d(LOG_TAG, message)
        }
    }

    override fun info(message: String) {
        if(isEnabled) {
            Log.i(LOG_TAG, message)
        }
    }

    override fun warn(message: String) {
        if(isEnabled) {
            Log.w(LOG_TAG, message)
        }
    }

    override fun error(message: String) {
        if(isEnabled) {
            Log.e(LOG_TAG, message)
        }
    }

}