package com.sleeplife.app.ui.screens.sleep

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sleeplife.app.data.entities.SleepQuality
import com.sleeplife.app.data.entities.getDisplayString
import com.sleeplife.app.ui.viewmodels.SleepViewModel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScreen(
    viewModel: SleepViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val recentRecords by viewModel.recentSleepRecords.collectAsState()

    var showQualityDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SleepHeader(
                    isTracking = uiState.isTracking,
                    onStartTracking = { viewModel.startSleepTracking() },
                    onStopTracking = { showQualityDialog = true }
                )
            }

            item {
                Text(
                    text = "最近睡眠记录",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            items(recentRecords) { record ->
                SleepRecordCard(
                    record = record,
                    onDelete = { viewModel.deleteSleepRecord(record.id) }
                )
            }
        }
    }

    if (showQualityDialog) {
        SleepQualityDialog(
            onDismiss = { showQualityDialog = false },
            onQualitySelected = { quality, notes ->
                viewModel.stopSleepTracking(quality, notes)
                showQualityDialog = false
            }
        )
    }
}

@Composable
fun SleepHeader(
    isTracking: Boolean,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F3460).copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isTracking) {
                Icon(
                    imageVector = Icons.Default.Brightness2,
                    contentDescription = "Sleeping",
                    modifier = Modifier.size(64.dp),
                    tint = Color(0xFFE94560)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "正在睡眠中...",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "点击下方按钮结束睡眠",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onStopTracking,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE94560)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("结束睡眠", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Brightness2,
                    contentDescription = "Start Sleep",
                    modifier = Modifier.size(48.dp),
                    tint = Color(0xFF5C6BC0)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "开始记录睡眠",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onStartTracking,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF5C6BC0)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("开始睡眠", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
fun SleepRecordCard(
    record: com.sleeplife.app.data.entities.SleepRecord,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F3460).copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = formatDate(record.startTime),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${formatTime(record.startTime)} - ${record.endTime?.let { formatTime(it) } ?: "进行中"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
                Text(
                    text = record.endTime?.let { calculateDuration(record.startTime, it) } ?: "进行中",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFE94560),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = getQualityColor(record.quality),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = record.quality.getDisplayString(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFE94560)
                    )
                }
            }

            if (record.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = record.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepQualityDialog(
    onDismiss: () -> Unit,
    onQualitySelected: (SleepQuality, String) -> Unit
) {
    var notes by remember { mutableStateOf("") }
    var selectedQuality by remember { mutableStateOf<SleepQuality?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("睡眠质量") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SleepQuality.values().forEach { quality ->
                    FilterChip(
                        selected = selectedQuality == quality,
                        onClick = { selectedQuality = quality },
                        label = { Text(quality.getDisplayString()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = getQualityColor(quality),
                            selectedLabelColor = Color.White
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("备注（可选）") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedQuality?.let {
                        onQualitySelected(it, notes)
                    }
                },
                enabled = selectedQuality != null
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

fun getQualityColor(quality: SleepQuality): Color {
    return when (quality) {
        SleepQuality.VERY_POOR -> Color(0xFFD32F2F)
        SleepQuality.POOR -> Color(0xFFF57C00)
        SleepQuality.AVERAGE -> Color(0xFFFBC02D)
        SleepQuality.GOOD -> Color(0xFF388E3C)
        SleepQuality.EXCELLENT -> Color(0xFF1976D2)
    }
}

fun formatDate(dateTime: LocalDateTime): String {
    return "${dateTime.monthNumber}月${dateTime.dayOfMonth}日"
}

fun formatTime(dateTime: LocalDateTime): String {
    return "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
}

fun calculateDuration(start: LocalDateTime, end: LocalDateTime): String {
    val timeZone = TimeZone.currentSystemDefault()
    val startInstant = start.toInstant(timeZone)
    val endInstant = end.toInstant(timeZone)
    val diffMillis = endInstant.toEpochMilliseconds() - startInstant.toEpochMilliseconds()
    val totalMinutes = diffMillis / (1000 * 60)
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return "${hours}小时${minutes}分钟"
}
