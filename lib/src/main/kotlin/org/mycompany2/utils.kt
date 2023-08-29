package org.mycompany2

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class MyEntity(val v: String)

fun serializeStuff(e: MyEntity): String {
    return Json.encodeToString(e)
}

fun defaultJson() = Json
