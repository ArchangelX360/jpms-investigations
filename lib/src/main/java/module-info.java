module lib {
    requires kotlin.stdlib;
    requires transitive kotlinx.serialization.core;
    requires transitive kotlinx.serialization.json;
    exports org.mycompany2;
}