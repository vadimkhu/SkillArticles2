package ru.skillbranch.skillarticles

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import ru.skillbranch.skillarticles.data.PrefManager
import ru.skillbranch.skillarticles.data.local.User

class App : Application() {
    companion object {
        private var instance : App? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(
            if (PrefManager().isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}