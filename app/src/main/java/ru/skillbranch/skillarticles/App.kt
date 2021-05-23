package ru.skillbranch.skillarticles

import android.app.Application
import android.content.Context
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

    private fun testUser(pref: PrefManager) {
        val user1 = User(
            id = "test_id",
            name = "user_name",
            avatar = "non_null_avatar",
            rating = 5,
            respect = 6,
            about = "non_null_adapter"
        )

        pref.testUser = null
        Log.e("App", "pref.user1 ${pref.testUser}")
        pref.testUser = user1
        Log.e("App", "pref.user1 ${pref.testUser}")
        Log.e("App", "equals ${pref.testUser == user1}")

        val user2 = User(
            id = "id2",
            name = "name2",
            avatar = null,
            rating = 7,
            respect = 62,
            about = null
        )
        pref.testUser = null
        Log.e("App", "pref.user2 ${pref.testUser}")
        pref.testUser = user2
        Log.e("App", "pref.user2 ${pref.testUser}")
        Log.e("App", "equals ${pref.testUser == user2}")

    }
}