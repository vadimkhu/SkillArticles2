package ru.skillbranch.skillarticles.data.adapters

interface JsonAdapter<T> {
    fun toJson(obj: T?) : String
    fun fromJson(json: String) : T?
}