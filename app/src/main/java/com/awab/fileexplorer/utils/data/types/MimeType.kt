package com.awab.fileexplorer.utils.data.types

/**
 * the mime type of the media or the extension of the file
 */
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