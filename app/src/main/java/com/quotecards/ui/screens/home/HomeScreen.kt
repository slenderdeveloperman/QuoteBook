package com.quotecards.ui.screens.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.quotecards.domain.model.Quote
import com.quotecards.ui.components.QuoteCard
import com.quotecards.ui.theme.HomeTitleFontFamily
import com.quotecards.ui.theme.appCardColors
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAddQuoteClick: () -> Unit,
    onEditQuoteClick: (Long) -> Unit,
    onSearchClick: () -> Unit,
    navigateToQuoteId: Long? = null,
    onNavigatedToQuote: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val quotes by viewModel.quotes.collectAsStateWithLifecycle()
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }

    // Handle navigation to specific quote from search
    LaunchedEffect(navigateToQuoteId, quotes) {
        if (navigateToQuoteId != null && quotes.isNotEmpty()) {
            val index = quotes.indexOfFirst { it.id == navigateToQuoteId }
            if (index >= 0) {
                currentIndex = index
                onNavigatedToQuote()
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Quotebook",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = HomeTitleFontFamily,
                            fontWeight = FontWeight.Normal
                        )
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
                    currentIndex = currentIndex,
                    onIndexChange = { currentIndex = it },
                    onQuoteTap = { quoteId -> onEditQuoteClick(quoteId) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 80.dp)
                        .offset(y = (-30).dp) // Position above center
                )
            }

            // Peek card from right for adding quotes
            AddQuotePeekFromRight(
                onSwipeToAdd = onAddQuoteClick,
                quoteCount = quotes.size,
                modifier = if (quotes.isEmpty()) {
                    Modifier.align(Alignment.CenterEnd)
                } else {
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 96.dp)
                }
            )

            // Search icon at bottom center
            IconButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search quotes",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun QuoteCardStack(
    quotes: List<Quote>,
    currentIndex: Int,
    onIndexChange: (Int) -> Unit,
    onQuoteTap: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val hapticFeedback = LocalHapticFeedback.current

    // Ensure index is valid when quotes change
    val safeIndex = currentIndex.coerceIn(0, (quotes.size - 1).coerceAtLeast(0))
    LaunchedEffect(safeIndex) {
        if (currentIndex != safeIndex) onIndexChange(safeIndex)
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val maxVisible = minOf(3, quotes.size)
        val visibleIndices = List(maxVisible) { i -> (safeIndex + i) % quotes.size }
        val dragProgress = if (offsetX.value < 0) {
            (abs(offsetX.value) / 500f).coerceIn(0f, 1f)
        } else {
            0f
        }

        // Draw from back to front
        for (i in (maxVisible - 1) downTo 0) {
            val cardIndex = visibleIndices[i]
            val isTopCard = (i == 0)

            QuoteCard(
                quote = quotes[cardIndex],
                index = cardIndex,
                onClick = { onQuoteTap(quotes[cardIndex].id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex((maxVisible - i).toFloat())
                    .graphicsLayer {
                        if (isTopCard) {
                            translationX = offsetX.value
                            // Rotation based on swipe direction (±15° max)
                            rotationZ = (offsetX.value / 40f).coerceIn(-15f, 15f)
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
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        scope.launch {
                                            val threshold = 200f
                                            val nextIndex = nextIndexAfterLeftSwipe(safeIndex, quotes.size)
                                            val previousIndex = previousIndexAfterRightSwipe(safeIndex, quotes.size)
                                            when {
                                                // Swipe LEFT = next quote (wrap to start at end)
                                                offsetX.value < -threshold && nextIndex != null -> {
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    offsetX.animateTo(
                                                        targetValue = -1500f,
                                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                                    )
                                                    onIndexChange(nextIndex)
                                                    offsetX.snapTo(0f)
                                                }
                                                // Swipe RIGHT = previous quote (wrap to end at start)
                                                offsetX.value > threshold && previousIndex != null -> {
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    offsetX.animateTo(
                                                        targetValue = 1500f,
                                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                                    )
                                                    onIndexChange(previousIndex)
                                                    offsetX.snapTo(0f)
                                                }
                                                else -> {
                                                    offsetX.animateTo(
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
                                    onHorizontalDrag = { change, dragAmount ->
                                        change.consume()
                                        scope.launch {
                                            offsetX.snapTo(offsetX.value + dragAmount)
                                        }
                                    }
                                )
                            }
                        } else Modifier
                    )
            )
        }

        // Card position indicator
        if (quotes.size > 1) {
            Text(
                text = "\u00B7 ${safeIndex + 1}/${quotes.size} \u00B7",
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
fun AddQuotePeekFromRight(
    onSwipeToAdd: () -> Unit,
    quoteCount: Int,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val cardColors = appCardColors()
    val backgroundColor = cardColors[quoteCount % cardColors.size]
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier) {
        val cardWidth = 280.dp
        val visibleWidth = 70.dp // 1/4 of card visible
        val cardWidthPx = with(density) { cardWidth.toPx() }
        val visibleWidthPx = with(density) { visibleWidth.toPx() }
        val threshold = cardWidthPx * 0.5f // 50% reveal threshold

        Card(
            modifier = Modifier
                .width(cardWidth)
                .height(180.dp)
                .offset { IntOffset((cardWidthPx - visibleWidthPx + offsetX.value).roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                if (abs(offsetX.value) > threshold) {
                                    offsetX.snapTo(0f)
                                    onSwipeToAdd()
                                } else {
                                    offsetX.animateTo(
                                        targetValue = 0f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessMedium
                                        )
                                    )
                                }
                            }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            scope.launch {
                                // Only allow dragging left (negative values)
                                val newOffset = (offsetX.value + dragAmount).coerceAtMost(0f)
                                    .coerceAtLeast(-(cardWidthPx - visibleWidthPx))
                                offsetX.snapTo(newOffset)
                            }
                        }
                    )
                },
            shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(visibleWidth - 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add quote",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }

                // Full content revealed on drag
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = visibleWidth - 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "\u201C",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Add a new quote",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
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
            text = "Drag the card from the right edge to add your first inspiring quote",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}

internal fun nextIndexAfterLeftSwipe(current: Int, total: Int): Int? {
    if (total <= 1) return null
    val safeCurrent = current.coerceIn(0, total - 1)
    return (safeCurrent + 1) % total
}

internal fun previousIndexAfterRightSwipe(current: Int, total: Int): Int? {
    if (total <= 1) return null
    val safeCurrent = current.coerceIn(0, total - 1)
    return (safeCurrent - 1 + total) % total
}
