package com.quotecards.ui.screens.home

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class HomeScreenSwipeIndexTest {

    @Test
    fun nextIndexAfterLeftSwipe_advancesFromFirst() {
        assertEquals(1, nextIndexAfterLeftSwipe(current = 0, total = 3))
    }

    @Test
    fun nextIndexAfterLeftSwipe_advancesFromMiddle() {
        assertEquals(2, nextIndexAfterLeftSwipe(current = 1, total = 3))
    }

    @Test
    fun nextIndexAfterLeftSwipe_wrapsFromLastToFirst() {
        assertEquals(0, nextIndexAfterLeftSwipe(current = 2, total = 3))
    }

    @Test
    fun nextIndexAfterLeftSwipe_returnsNullForSingleCard() {
        assertNull(nextIndexAfterLeftSwipe(current = 0, total = 1))
    }

    @Test
    fun previousIndexAfterRightSwipe_movesBackFromMiddle() {
        assertEquals(1, previousIndexAfterRightSwipe(current = 2, total = 3))
    }

    @Test
    fun previousIndexAfterRightSwipe_wrapsFromFirstToLast() {
        assertEquals(2, previousIndexAfterRightSwipe(current = 0, total = 3))
    }

    @Test
    fun previousIndexAfterRightSwipe_returnsNullForSingleCard() {
        assertNull(previousIndexAfterRightSwipe(current = 0, total = 1))
    }
}
