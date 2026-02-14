package com.quotecards.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.quotecards.domain.model.Quote

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCategoryBottomSheet(
    uncategorizedQuotes: List<Quote>,
    onDismiss: () -> Unit,
    onCreateCategory: (name: String, selectedQuoteIds: List<Long>) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var categoryName by remember { mutableStateOf("") }
    val selectedQuoteIds = remember { mutableStateListOf<Long>() }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Create Category",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Category name") },
                placeholder = { Text("e.g., Work, Personal, Funny...") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (uncategorizedQuotes.isNotEmpty()) {
                Text(
                    text = "Assign quotes to this category (optional)",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(uncategorizedQuotes, key = { it.id }) { quote ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = quote.id in selectedQuoteIds,
                                onCheckedChange = { checked ->
                                    if (checked) {
                                        selectedQuoteIds.add(quote.id)
                                    } else {
                                        selectedQuoteIds.remove(quote.id)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = quote.text.take(50) + if (quote.text.length > 50) "..." else "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2
                                )
                                Text(
                                    text = "- ${quote.author}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            } else {
                Text(
                    text = "No uncategorized quotes to assign",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    if (categoryName.isNotBlank()) {
                        onCreateCategory(categoryName.trim(), selectedQuoteIds.toList())
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = categoryName.isNotBlank()
            ) {
                Text("Create Category")
            }
        }
    }
}
