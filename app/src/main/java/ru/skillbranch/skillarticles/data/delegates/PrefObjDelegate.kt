package ru.skillbranch.skillarticles.data.delegates

import androidx.datastore.preferences.core.*
import kotlinx.coroutines.Dispatchers
//import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.skillbranch.skillarticles.data.PrefManager
import ru.skillbranch.skillarticles.data.adapter.JsonAdapter
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PrefObjDelegate<T>(
    private val adapter: JsonAdapter<T>,
    private val customKey: String? = null
) : ReadWriteProperty<PrefManager, T?> {
    private var _storedValue: T? = null

    override fun getValue(thisRef: PrefManager, property: KProperty<*>): T {
        if (_storedValue == null) {
            val flowValue = thisRef.dataStore.data
                .map { prefs ->
                    prefs[createKey(property)]?.let(adapter::fromJson)
                }
            _storedValue = runBlocking(Dispatchers.IO) { flowValue.first() }
        }
        return _storedValue!!
    }

    override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T?) {
        _storedValue = value
        thisRef.scope.launch {
            thisRef.dataStore.edit { prefs ->
                prefs[createKey(property)] = value.let(adapter::toJson)
            }
        }
    }

    private fun createKey(property: KProperty<*>) = stringPreferencesKey(customKey ?: property.name)
}