package com.quotecards.domain.model

data class Quote(
    val id: Long = 0,
    val text: String,
    val author: String = "Unknown",
    val createdAt: Long = System.currentTimeMillis()
)
