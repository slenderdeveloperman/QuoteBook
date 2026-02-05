package com.quotecards.ui.screens.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotecards.domain.model.Quote
import com.quotecards.ui.components.QuoteCard
import com.quotecards.ui.theme.CardColors
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddQuoteClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val quotes by viewModel.quotes.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "QuoteBook",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (quotes.isEmpty()) {
                EmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 100.dp)
                )
            } else {
                QuoteCardStack(
                    quotes = quotes,
                    onDeleteQuote = { quote -> viewModel.deleteQuote(quote) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 100.dp)
                )
            }

            AddQuotePeekCard(
                onSwipeUp = onAddQuoteClick,
                quoteCount = quotes.size,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}

@Composable
fun QuoteCardStack(
    quotes: List<Quote>,
    onDeleteQuote: (Quote) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }

    // Ensure index is valid when quotes change
    val safeIndex = currentIndex.coerceIn(0, (quotes.size - 1).coerceAtLeast(0))
    LaunchedEffect(safeIndex) {
        if (currentIndex != safeIndex) currentIndex = safeIndex
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val maxVisible = minOf(3, quotes.size - safeIndex)
        val dragProgress = if (offsetY.value < 0) {
            (abs(offsetY.value) / 500f).coerceIn(0f, 1f)
        } else {
            0f
        }

        // Draw from back to front
        for (i in (maxVisible - 1) downTo 0) {
            val cardIndex = safeIndex + i
            if (cardIndex < quotes.size) {
                val isTopCard = (i == 0)

                QuoteCard(
                    quote = quotes[cardIndex],
                    index = cardIndex,
                    onDelete = { onDeleteQuote(quotes[cardIndex]) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .zIndex((maxVisible - i).toFloat())
                        .graphicsLayer {
                            if (isTopCard) {
                                translationY = offsetY.value
                                rotationZ = (offsetY.value / 60f).coerceIn(-3f, 3f)
                            } else {
                                val effectiveI = i - dragProgress
                                translationY = effectiveI * 20.dp.toPx()
                                val scale = 1f - (effectiveI * 0.04f)
                                scaleX = scale
                                scaleY = scale
                                alpha = 1f - (effectiveI * 0.15f)
                            }
                        }
                        .then(
                            if (isTopCard) {
                                Modifier.pointerInput(safeIndex) {
                                    detectVerticalDragGestures(
                                        onDragEnd = {
                                            scope.launch {
                                                val threshold = 200f
                                                when {
                                                    offsetY.value < -threshold && safeIndex < quotes.size - 1 -> {
                                                        offsetY.animateTo(
                                                            targetValue = -1500f,
                                                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                                        )
                                                        currentIndex = safeIndex + 1
                                                        offsetY.snapTo(0f)
                                                    }
                                                    offsetY.value > threshold && safeIndex > 0 -> {
                                                        offsetY.animateTo(
                                                            targetValue = 1500f,
                                                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                                        )
                                                        currentIndex = safeIndex - 1
                                                        offsetY.snapTo(0f)
                                                    }
                                                    else -> {
                                                        offsetY.animateTo(
                                                            targetValue = 0f,
                                                            animationSpec = spring(
                                                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                                                stiffness = Spring.StiffnessMedium
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        onVerticalDrag = { change, dragAmount ->
                                            change.consume()
                                            scope.launch {
                                                offsetY.snapTo(offsetY.value + dragAmount)
                                            }
                                        }
                                    )
                                }
                            } else Modifier
                        )
                )
            }
        }

        // Card position indicator
        if (quotes.size > 1) {
            Text(
                text = "${safeIndex + 1} / ${quotes.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
            )
        }
    }
}

@Composable
fun AddQuotePeekCard(
    onSwipeUp: () -> Unit,
    quoteCount: Int,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }
    val backgroundColor = CardColors[quoteCount % CardColors.size]

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .offset { IntOffset(0, offsetY.value.roundToInt()) }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            if (offsetY.value < -150f) {
                                offsetY.snapTo(0f)
                                onSwipeUp()
                            } else {
                                offsetY.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                        }
                    },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            val newOffset = (offsetY.value + dragAmount).coerceAtMost(0f)
                            offsetY.snapTo(newOffset)
                        }
                    }
                )
            },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomEnd = 0.dp, bottomStart = 0.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Drag handle
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "\u201C",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Add a new quote",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FormatQuote,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No quotes yet",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Swipe up from the bottom to add your first inspiring quote",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
