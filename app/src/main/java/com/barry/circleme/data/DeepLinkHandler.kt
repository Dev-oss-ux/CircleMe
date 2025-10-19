package com.barry.circleme.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object DeepLinkHandler {
    private val _pendingCircleId = MutableStateFlow<String?>(null)
    val pendingCircleId = _pendingCircleId.asStateFlow()

    fun setPendingCircleId(circleId: String?) {
        _pendingCircleId.value = circleId
    }
}
