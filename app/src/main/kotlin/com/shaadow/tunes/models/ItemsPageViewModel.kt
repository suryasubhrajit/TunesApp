package com.shaadow.tunes.models

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import com.shaadow.innertube.Innertube
import com.shaadow.innertube.utils.plus

class ItemsPageViewModel<T : Innertube.Item> : ViewModel() {
    var itemsMap: MutableMap<String, Innertube.ItemsPage<T>?> = mutableStateMapOf()

    fun setItems(
        tag: String,
        items: Innertube.ItemsPage<T>?
    ) {
        if (!itemsMap.containsKey(tag)) itemsMap[tag] = items
        else itemsMap[tag] += items!!
    }
}