package com.jonathanev.review.Data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class FolderAction: Parcelable {
    CREATING_FOLDER,
    RENAMING_FILE,
    RENAMING_FOLDER,
    CREATING_FILE,
    NONE
}