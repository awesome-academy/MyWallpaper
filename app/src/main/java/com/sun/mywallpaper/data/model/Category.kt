package com.sun.mywallpaper.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Category(
    @SerializedName(JSON_KEY_ID) val id: Int,
    @SerializedName(JSON_KEY_TITLE) val title: String,
    @SerializedName(JSON_KEY_PHOTO_COUNT) val photoCount: Int,
    @SerializedName(JSON_KEY_LINKS) val links: CategoryLinks
) : Parcelable {

    @Parcelize
    data class CategoryLinks(
        @SerializedName(JSON_KEY_LINKS_SELF) val self: String,
        @SerializedName(JSON_KEY__LINKS_PHOTOS) val photos: String
    ) : Parcelable

    companion object {
        private const val JSON_KEY_ID = "id"
        private const val JSON_KEY_TITLE = "title"
        private const val JSON_KEY_PHOTO_COUNT = "photo_count"
        private const val JSON_KEY_LINKS = "links"
        private const val JSON_KEY_LINKS_SELF = "self"
        private const val JSON_KEY__LINKS_PHOTOS = "photos"
    }
}
