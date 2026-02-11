package com.quotecards.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.quotecards.domain.model.Quote

@Entity(tableName = "quotes")
data class QuoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val text: String,
    val author: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): Quote {
        require(text.isNotBlank()) { "Quote text cannot be blank (id=$id)" }
        return Quote(
            id = id,
            text = text,
            author = author.ifBlank { "Unknown" },
            createdAt = createdAt
        )
    }

    companion object {
        fun fromDomainModel(quote: Quote): QuoteEntity = QuoteEntity(
            id = quote.id,
            text = quote.text,
            author = quote.author,
            createdAt = quote.createdAt
        )
    }
}
