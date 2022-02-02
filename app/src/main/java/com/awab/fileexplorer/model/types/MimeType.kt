package com.awab.fileexplorer.model.types

enum class MimeType(val mimeString:String) {
    IMAGE("image/*"),
    AUDIO("audio/*"),
    VIDEO("video/*"),
    TEXT("text/plain"),
    HTML("text/html"),
    PDF("application/pdf"),
    APPLICATION("application/vnd.android.package-archive"),
    UNKNOWN("")
}