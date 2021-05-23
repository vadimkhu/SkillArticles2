package ru.skillbranch.skillarticles.data.adapter

interface JsonAdapter<T> {
    fun toJson(obj: T?) : String
    fun fromJson(json: String) : T?
}