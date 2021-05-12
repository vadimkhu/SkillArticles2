package ru.skillbranch.skillarticles.data.delegates

import androidx.datastore.preferences.core.*
import kotlinx.coroutines.Dispatchers
//import com.squareup.moshi.JsonAdapter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.skillbranch.skillarticles.data.PrefManager
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PrefObjDelegate<T>(
    private val adapter: JsonAdapter<T>,
    private val customKey: String? = null
) : ReadWriteProperty<PrefManager, T?> {
    private var _storedValue: T? = null

    override fun getValue(thisRef: PrefManager, property: KProperty<*>): T {
        return _storedValue!!
    }

    override fun setValue(thisRef: PrefManager, property: KProperty<*>, value: T?) {
        _storedValue = value
    }
}

class JsonAdapter<T> {

}