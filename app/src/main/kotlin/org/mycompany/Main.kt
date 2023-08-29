package org.mycompany

import org.mycompany2.MyEntity
import org.mycompany2.defaultJson
import org.mycompany2.serializeStuff

fun main(args: Array<String>) {
    val json = defaultJson()
    println(serializeStuff(MyEntity(v = "Hello")))
    println(json.encodeToString(MyEntity(v = "Hello")))
}
