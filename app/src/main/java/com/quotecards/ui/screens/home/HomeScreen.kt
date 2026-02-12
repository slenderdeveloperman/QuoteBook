package com.quotecards.ui.screens.home

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.ui.util.lerp
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TopAppBar
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
import com.quotecards.ui.components.CategoryBar
import com.quotecards.ui.components.CreateCategoryBottomSheet
import com.quotecards.ui.components.QuoteCard
import com.quotecards.ui.components.QuoteDetailBottomSheet
import com.quotecards.ui.theme.HomeTitleFontFamily
import com.quotecards.ui.theme.appCardColors
import com.quotecards.utils.AppConstants
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.isActive
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
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val uncategorizedQuotes by viewModel.uncategorizedQuotes.collectAsStateWithLifecycle()
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    var selectedQuote by remember { mutableStateOf<Quote?>(null) }
    var showCreateCategorySheet by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Handle navigation to specific quote from search
    LaunchedEffect(navigateToQuoteId, quotes) {
        if (navigateToQuoteId != null && quotes.isNotEmpty()) {
            val index = quotes.indexOfFirst { it.id == navigateToQuoteId }
            if (index >= 0) {
                currentIndex = index
            }
            // Always clear navigation state to prevent stale IDs
            onNavigatedToQuote()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Quotebook",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = HomeTitleFontFamily,
                            fontWeight = FontWeight.Normal
                        )
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://x.com/slndrtweeterman"))
                            context.startActivity(intent)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Construction,
                            contentDescription = "Builder",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category filter bar
            CategoryBar(
                categories = categories,
                selectedCategory = selectedCategory,
                onCategorySelected = { category ->
                    viewModel.setCategory(category)
                    currentIndex = 0 // Reset index when category changes
                }
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
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
                        onQuoteTap = { quoteId -> selectedQuote = quotes.find { it.id == quoteId } },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = AppConstants.CARD_STACK_BOTTOM_PADDING_DP.dp)
                            .offset(y = 0.dp) // Cards stack upward
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
                            .padding(bottom = 140.dp)
                    }
                )

                // Bottom center pill with Search + Shuffle
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search button
                    IconButton(onClick = onSearchClick) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search quotes",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Organise button
                    IconButton(onClick = { showCreateCategorySheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Style,
                            contentDescription = "Organise cards",
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Shuffle button (only when multiple quotes)
                    if (quotes.size > 1) {
                        val hapticFeedback = LocalHapticFeedback.current
                        IconButton(
                            onClick = {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                var newIndex: Int
                                do {
                                    newIndex = (0 until quotes.size).random()
                                } while (newIndex == currentIndex)
                                currentIndex = newIndex
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shuffle,
                                contentDescription = "Shuffle cards",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Quote detail bottom sheet
        selectedQuote?.let { quote ->
            QuoteDetailBottomSheet(
                quote = quote,
                onDismiss = { selectedQuote = null },
                onEdit = { quoteId ->
                    selectedQuote = null
                    onEditQuoteClick(quoteId)
                },
                onDelete = { quoteToDelete ->
                    viewModel.deleteQuote(quoteToDelete)
                    selectedQuote = null
                }
            )
        }

        // Create category bottom sheet
        if (showCreateCategorySheet) {
            CreateCategoryBottomSheet(
                uncategorizedQuotes = uncategorizedQuotes,
                onDismiss = { showCreateCategorySheet = false },
                onCreateCategory = { name, quoteIds ->
                    viewModel.createCategory(name, quoteIds)
                    showCreateCategorySheet = false
                }
            )
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
    val density = LocalDensity.current

    // State for the card moving to bottom animation
    var cardMovingToBottom by remember { mutableStateOf<Quote?>(null) }
    var cardMovingToBottomIndex by remember { mutableIntStateOf(0) }
    val cardToBottomProgress = remember { Animatable(0f) }

    // Ensure index is valid when quotes change
    val safeIndex = currentIndex.coerceIn(0, (quotes.size - 1).coerceAtLeast(0))
    LaunchedEffect(safeIndex) {
        if (currentIndex != safeIndex) onIndexChange(safeIndex)
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val maxVisible = minOf(AppConstants.MAX_VISIBLE_CARDS, quotes.size)
        val visibleIndices = List(maxVisible) { i -> (safeIndex + i) % quotes.size }
        val dragProgress = if (offsetX.value < 0) {
            (abs(offsetX.value) / AppConstants.ANIMATION_DIVISOR).coerceIn(0f, 1f)
        } else {
            0f
        }

        // Render the animating card first (behind everything else)
        cardMovingToBottom?.let { quote ->
            val screenWidthPx = with(density) { 400.dp.toPx() }
            val backCardTranslationY = with(density) { -(maxVisible - 1) * AppConstants.CARD_TRANSLATION_Y_DP.dp.toPx() }
            val backCardScale = 1f - ((maxVisible - 1) * AppConstants.CARD_SCALE_FACTOR)
            val backCardAlpha = 1f - ((maxVisible - 1) * AppConstants.CARD_ALPHA_FACTOR)

            QuoteCard(
                quote = quote,
                index = cardMovingToBottomIndex,
                onClick = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(0f)
                    .graphicsLayer {
                        val progress = cardToBottomProgress.value
                        // Arc path: start from left off-screen, curve to center-back
                        translationX = lerp(-screenWidthPx, 0f, progress)
                        // Y: Start slightly below, settle at back position (stacked UP)
                        translationY = lerp(50.dp.toPx(), backCardTranslationY, progress)
                        // Scale down to back card size
                        val currentScale = lerp(1f, backCardScale, progress)
                        scaleX = currentScale
                        scaleY = currentScale
                        // Fade to back card alpha
                        alpha = lerp(1f, backCardAlpha, progress)
                        // Rotation: Start tilted, end straight
                        rotationZ = lerp(-10f, 0f, progress)
                    }
            )
        }

        // Draw from back to front
        for (i in (maxVisible - 1) downTo 0) {
            val cardIndex = visibleIndices[i]
            val quote = quotes.getOrNull(cardIndex) ?: continue
            val isTopCard = (i == 0)

            QuoteCard(
                quote = quote,
                index = cardIndex,
                onClick = { onQuoteTap(quote.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex((maxVisible - i).toFloat())
                    .graphicsLayer {
                        if (isTopCard) {
                            translationX = offsetX.value
                            // Rotation based on swipe direction
                            rotationZ = (offsetX.value / 40f).coerceIn(-AppConstants.MAX_ROTATION_DEGREES, AppConstants.MAX_ROTATION_DEGREES)
                        } else {
                            val effectiveI = i - dragProgress
                            translationY = -effectiveI * AppConstants.CARD_TRANSLATION_Y_DP.dp.toPx()
                            val scale = 1f - (effectiveI * AppConstants.CARD_SCALE_FACTOR)
                            scaleX = scale
                            scaleY = scale
                            alpha = 1f - (effectiveI * AppConstants.CARD_ALPHA_FACTOR)
                        }
                    }
                    .then(
                        if (isTopCard) {
                            Modifier.pointerInput(safeIndex) {
                                detectHorizontalDragGestures(
                                    onDragEnd = {
                                        scope.launch {
                                            // Early exit if coroutine is cancelled (screen disposed)
                                            if (!isActive) return@launch

                                            val threshold = AppConstants.SWIPE_THRESHOLD
                                            val nextIndex = nextIndexAfterLeftSwipe(safeIndex, quotes.size)
                                            val previousIndex = previousIndexAfterRightSwipe(safeIndex, quotes.size)
                                            when {
                                                // Swipe LEFT = next quote (wrap to start at end)
                                                offsetX.value < -threshold && nextIndex != null -> {
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)

                                                    // Store the card for animation
                                                    val swipedQuote = quotes.getOrNull(safeIndex)
                                                    val swipedIndex = safeIndex

                                                    offsetX.animateTo(
                                                        targetValue = -AppConstants.SWIPE_EXIT_OFFSET,
                                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                                    )

                                                    // Check again after animation completes
                                                    if (isActive) {
                                                        // Start the card-to-bottom animation
                                                        if (swipedQuote != null && quotes.size > 1) {
                                                            cardMovingToBottom = swipedQuote
                                                            cardMovingToBottomIndex = swipedIndex
                                                            cardToBottomProgress.snapTo(0f)
                                                        }

                                                        onIndexChange(nextIndex)
                                                        offsetX.snapTo(0f)

                                                        // Animate the card to bottom
                                                        if (swipedQuote != null && quotes.size > 1) {
                                                            cardToBottomProgress.animateTo(
                                                                targetValue = 1f,
                                                                animationSpec = tween(
                                                                    durationMillis = AppConstants.CARD_TO_BOTTOM_DURATION_MS,
                                                                    easing = FastOutSlowInEasing
                                                                )
                                                            )
                                                            cardMovingToBottom = null
                                                        }
                                                    }
                                                }
                                                // Swipe RIGHT = previous quote (wrap to end at start)
                                                offsetX.value > threshold && previousIndex != null -> {
                                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    offsetX.animateTo(
                                                        targetValue = AppConstants.SWIPE_EXIT_OFFSET,
                                                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                                                    )
                                                    // Check again after animation completes
                                                    if (isActive) {
                                                        onIndexChange(previousIndex)
                                                        offsetX.snapTo(0f)
                                                    }
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
    var hasTriggeredNavigation by remember { mutableStateOf(false) }
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
                        onDragStart = {
                            // Reset flag on new drag gesture
                            hasTriggeredNavigation = false
                        },
                        onDragEnd = {
                            scope.launch {
                                // Early exit if cancelled or already triggered
                                if (!isActive || hasTriggeredNavigation) {
                                    return@launch
                                }
                                if (abs(offsetX.value) > threshold) {
                                    offsetX.snapTo(0f)
                                    if (isActive) {
                                        hasTriggeredNavigation = true
                                        onSwipeToAdd()
                                    }
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
                            if (hasTriggeredNavigation) return@detectHorizontalDragGestures
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
            shape = RectangleShape,
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
