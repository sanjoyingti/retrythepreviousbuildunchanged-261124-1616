package com.aibuilt.retrythepreviousbuildunchanged.model

import kotlinx.serialization.json.Json

val AppJson: Json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}
