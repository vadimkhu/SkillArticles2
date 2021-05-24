package ru.skillbranch.skillarticles.data.adapters

import org.json.JSONObject
import ru.skillbranch.skillarticles.data.local.User
import ru.skillbranch.skillarticles.extensions.asMap

class UserJsonAdapter : JsonAdapter<User> {
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

    override fun toJson(obj: User?): String {
        val user = obj ?: return ""
        return JSONObject(user.asMap()).toString()
    }
}