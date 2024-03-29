package com.sun.mywallpaper.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Location(
    @SerializedName(JSON_KEY_TITLE) val title: String,
    @SerializedName(JSON_KEY_NAME) val name: String,
    @SerializedName(JSON_KEY_CITY) val city: String,
    @SerializedName(JSON_KEY_COUNTRY) val country: String,
    @SerializedName(JSON_KEY_POSITION) val position: Position

) : Parcelable {

    @Parcelize
    data class Position(
        @SerializedName(JSON_KEY_POSITION_LATITUDE) val latitude: Float,
        @SerializedName(JSON_KEY_POSITION_LONGITUDE) val longitude: Float
    ) : Parcelable

    companion object {
        private const val JSON_KEY_TITLE = "title"
        private const val JSON_KEY_NAME = "name"
        private const val JSON_KEY_CITY = "city"
        private const val JSON_KEY_COUNTRY = "country"
        private const val JSON_KEY_POSITION = "position"
        private const val JSON_KEY_POSITION_LATITUDE = "latitude"
        private const val JSON_KEY_POSITION_LONGITUDE = "longitude"
    }
}
