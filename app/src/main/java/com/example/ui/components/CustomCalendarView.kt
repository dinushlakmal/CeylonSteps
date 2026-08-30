package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TripLocation
import com.example.ui.theme.BentoAmberSecondary
import com.example.ui.theme.BentoBorderLight
import com.example.ui.theme.BentoLavenderContainer
import com.example.ui.theme.BentoOnPrimaryContainer
import com.example.ui.theme.BentoPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CustomCalendarView(
    modifier: Modifier = Modifier,
    trips: List<TripLocation>,
    onTripClick: (TripLocation) -> Unit,
    onMapClick: (TripLocation) -> Unit,
    onEditClick: (TripLocation) -> Unit,
    onDeleteClick: (TripLocation) -> Unit
) {
    var calendarMonth by remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        mutableStateOf(cal)
    }

    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }

    // Map trips by start of day (normalized epoch millis)
    val tripsByDate = remember(trips) {
        trips.groupBy { trip ->
            val cal = Calendar.getInstance().apply {
                timeInMillis = trip.dateEpochMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        }
    }

    val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.US)
    val monthTitle = monthYearFormat.format(calendarMonth.time)

    // Calculate calendar grid days
    val daysInMonth = remember(calendarMonth) {
        val days = mutableListOf<CalendarDayInfo>()
        val cal = calendarMonth.clone() as Calendar

        cal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Previous month padding days
        cal.add(Calendar.MONTH, -1)
        val prevMax = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (i in 0 until firstDayOfWeek) {
            val dayNum = prevMax - firstDayOfWeek + i + 1
            cal.set(Calendar.DAY_OF_MONTH, dayNum)
            days.add(
                CalendarDayInfo(
                    dayNumber = dayNum,
                    isCurrentMonth = false,
                    normalizedDateMillis = cal.timeInMillis
                )
            )
        }

        // Current month days
        cal.time = calendarMonth.time
        for (day in 1..maxDays) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            days.add(
                CalendarDayInfo(
                    dayNumber = day,
                    isCurrentMonth = true,
                    normalizedDateMillis = cal.timeInMillis
                )
            )
        }

        // Next month trailing padding to fill 35 or 42 grid cells
        val totalCells = if (days.size > 35) 42 else 35
        val remaining = totalCells - days.size
        cal.add(Calendar.MONTH, 1)
        for (day in 1..remaining) {
            cal.set(Calendar.DAY_OF_MONTH, day)
            days.add(
                CalendarDayInfo(
                    dayNumber = day,
                    isCurrentMonth = false,
                    normalizedDateMillis = cal.timeInMillis
                )
            )
        }

        days
    }

    val selectedDayTrips = remember(selectedDateMillis, trips) {
        if (selectedDateMillis == null) {
            trips
        } else {
            tripsByDate[selectedDateMillis] ?: emptyList()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("travel_calendar_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Navigation Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("calendar_month_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, BentoBorderLight.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Bar (Month Title + Prev/Next Buttons)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = monthTitle,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.3).sp
                                )
                            )
                            Text(
                                text = "Expedition Schedule & Memories",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    val next = calendarMonth.clone() as Calendar
                                    next.add(Calendar.MONTH, -1)
                                    calendarMonth = next
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Previous Month"
                                )
                            }

                            IconButton(
                                onClick = {
                                    val cal = Calendar.getInstance()
                                    cal.set(Calendar.DAY_OF_MONTH, 1)
                                    calendarMonth = cal
                                    selectedDateMillis = null
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Today,
                                    contentDescription = "Today",
                                    tint = BentoPrimary
                                )
                            }

                            IconButton(
                                onClick = {
                                    val next = calendarMonth.clone() as Calendar
                                    next.add(Calendar.MONTH, 1)
                                    calendarMonth = next
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = "Next Month"
                                )
                            }
                        }
                    }

                    // Weekday Headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                            Text(
                                text = day,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }

                    // Calendar Days Grid (7 columns)
                    val rows = daysInMonth.chunked(7)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        rows.forEach { week ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                week.forEach { dayInfo ->
                                    val dayTrips = tripsByDate[dayInfo.normalizedDateMillis] ?: emptyList()
                                    val hasVisited = dayTrips.any { !it.isUpcoming }
                                    val hasUpcoming = dayTrips.any { it.isUpcoming }
                                    val isSelected = selectedDateMillis == dayInfo.normalizedDateMillis

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .padding(2.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isSelected -> BentoPrimary
                                                    hasVisited && hasUpcoming -> BentoPrimary.copy(alpha = 0.2f)
                                                    hasVisited -> BentoPrimary.copy(alpha = 0.15f)
                                                    hasUpcoming -> BentoAmberSecondary.copy(alpha = 0.15f)
                                                    else -> Color.Transparent
                                                }
                                            )
                                            .border(
                                                width = if (isSelected) 2.dp else 0.dp,
                                                color = if (isSelected) BentoPrimary else Color.Transparent,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                selectedDateMillis = if (isSelected) null else dayInfo.normalizedDateMillis
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = dayInfo.dayNumber.toString(),
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (dayTrips.isNotEmpty() || isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = when {
                                                        isSelected -> Color.White
                                                        !dayInfo.isCurrentMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                                                        dayTrips.isNotEmpty() -> MaterialTheme.colorScheme.onSurface
                                                        else -> MaterialTheme.colorScheme.onSurface
                                                    },
                                                    fontSize = 13.sp
                                                )
                                            )

                                            // Trip indicator dot
                                            if (dayTrips.isNotEmpty()) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    if (hasVisited) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(if (isSelected) Color.White else BentoPrimary)
                                                        )
                                                    }
                                                    if (hasUpcoming) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(4.dp)
                                                                .clip(CircleShape)
                                                                .background(if (isSelected) Color.White else BentoAmberSecondary)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Legend
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem(color = BentoPrimary, label = "Visited Footprint")
                        Spacer(modifier = Modifier.size(16.dp))
                        LegendItem(color = BentoAmberSecondary, label = "Upcoming Plan")
                    }
                }
            }
        }

        // Section Title for List of Trips
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedDateMillis != null) {
                        val selDateStr = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(selectedDateMillis!!))
                        "Trips on $selDateStr (${selectedDayTrips.size})"
                    } else {
                        "All Footprints in Journal (${selectedDayTrips.size})"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.2).sp
                    )
                )

                if (selectedDateMillis != null) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedDateMillis = null },
                        shape = RoundedCornerShape(10.dp),
                        color = BentoLavenderContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = "Show All",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = BentoPrimary
                            )
                        )
                    }
                }
            }
        }

        // Trips list for selected day / all
        if (selectedDayTrips.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, BentoBorderLight.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "No footprints recorded on this date",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        } else {
            items(selectedDayTrips, key = { it.id }) { trip ->
                TripCard(
                    trip = trip,
                    index = trips.indexOfFirst { it.id == trip.id } + 1,
                    onClick = { onTripClick(trip) },
                    onMapClick = { onMapClick(trip) },
                    onEditClick = { onEditClick(trip) },
                    onDeleteClick = { onDeleteClick(trip) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

private data class CalendarDayInfo(
    val dayNumber: Int,
    val isCurrentMonth: Boolean,
    val normalizedDateMillis: Long
)

