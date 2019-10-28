package com.sun.mywallpaper.di

import com.sun.mywallpaper.adapter.*
import com.sun.mywallpaper.util.DownloadHelper
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val componentModule = module {

    single(named(KoinNames.NEW_ADAPTER)) {
        PhotoAdapter()
    }

    single(named(KoinNames.FEATURED_ADAPTER)) {
        PhotoAdapter()
    }

    single(named(KoinNames.COLLECTION_ADAPTER)) {
        CollectionAdapter()
    }

    single(named(KoinNames.COLLECTION_DETAIL_ADAPTER)) {
        PhotoAdapter()
    }

    single(named(KoinNames.USER_DETAIL_PHOTO_ADAPTER)) {
        PhotoAdapter()
    }

    single(named(KoinNames.USER_DETAIL_LIKE_ADAPTER)) {
        PhotoAdapter()
    }

    single(named(KoinNames.USER_DETAIL_COLLECTION_ADAPTER)) {
        CollectionAdapter()
    }

    single(named(KoinNames.SEARCH_PHOTO_ADAPTER)) {
        PhotoAdapter()
    }

    single(named(KoinNames.SEARCH_COLLECTION_ADAPTER)) {
        CollectionAdapter()
    }

    single(named(KoinNames.SEARCH_USER_ADAPTER)) {
        UserAdapter()
    }

    single(named(KoinNames.FILTER_ADAPTER)) {
        FilterAdapter()
    }

    single(named(KoinNames.COLOR_PICKER_ADAPTER)) {
        ColorPickerAdapter()
    }

    single(named(KoinNames.EMOJI_ADAPTER)) {
        EmojiAdapter()
    }

    single(named(KoinNames.DOWNLOAD_HELPER)) {
        DownloadHelper(androidContext())
    }
}
