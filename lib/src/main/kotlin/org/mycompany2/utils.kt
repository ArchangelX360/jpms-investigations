package org.mycompany2

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.mycompany3.MyEntity

fun serializeStuff(e: MyEntity): String {
    return Json.encodeToString(e)
}

fun defaultJson() = Json
