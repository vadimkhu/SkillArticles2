package ru.skillbranch.skillarticles.data.delegates

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.skillbranch.skillarticles.data.PrefManager
import ru.skillbranch.skillarticles.data.adapters.JsonAdapter
import ru.skillbranch.skillarticles.data.local.User
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PrefObjDelegate<T>(
    private val adapter: JsonAdapter<T>,
    private val customKey: String? = null
) {
    operator fun provideDelegate(
        thisRef: PrefManager,
        prop: KProperty<*>
    ) : ReadWriteProperty<PrefManager, T?> {
        return object : ReadWriteProperty<PrefManager, T?> {
            private var _storedValue: T? = null
            val key = createKey(prop)

            override fun getValue(thisRef: PrefManager, property: KProperty<*>): T? {
                if (_storedValue == null) {
                    val flowValue = thisRef.dataStore.data
                        .map { prefs ->
                            prefs[key] ?: ""
                        }
                    _storedValue = runBlocking {
                        flowValue
                            .map { adapter.fromJson(it) }
                            .first()
                    }
                }
                return _storedValue
            }

            override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T?) {
                _storedValue = value
                thisRef.scope.launch {
                    thisRef.dataStore.edit { prefs ->
                        prefs[key] = adapter.toJson(value)
                    }
                }
            }

            private fun createKey(property: KProperty<*>) = stringPreferencesKey(customKey ?: property.name)
        }
    }
}
