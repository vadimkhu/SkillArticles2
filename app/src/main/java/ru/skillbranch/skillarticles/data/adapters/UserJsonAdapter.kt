package ru.skillbranch.skillarticles.data.adapters

import org.json.JSONObject
import ru.skillbranch.skillarticles.data.local.User

class UserJsonAdapter : JsonAdapter<User> {
    override fun toJson(obj: User?): String {
        val user = obj ?: return ""
        return JSONObject().apply {
            put("id", user.id)
            put("name", user.name)
            putOpt("avatar", user.avatar)
            put("rating", user.rating)
            put("respect", user.respect)
            putOpt("about", user.about)
        }.toString()
    }

    override fun fromJson(json: String): User? {
        val json = JSONObject(json)
        return User(
            id = json.getString("id"),
            name = json.getString("name"),
            avatar = json.optString("avatar"),
            rating = json.getInt("rating"),
            respect = json.getInt("respect"),
            about = json.optString("about")
        )
    }
}