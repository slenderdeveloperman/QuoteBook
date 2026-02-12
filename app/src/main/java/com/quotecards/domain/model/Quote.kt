package com.quotecards.domain.model

data class Quote(
    val id: Long = 0,
    val text: String,
    val author: String = "Unknown",
    val category: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
