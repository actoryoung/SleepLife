package com.sleeplife.app.ui.screens.pomodoro

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
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
import com.sleeplife.app.data.entities.SessionType
import com.sleeplife.app.data.entities.getDefaultColor
import com.sleeplife.app.data.entities.getDefaultDuration
import com.sleeplife.app.data.entities.getDisplayName
import com.sleeplife.app.ui.viewmodels.PomodoroViewModel
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    viewModel: PomodoroViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val todaySessions by viewModel.todaySessions.collectAsState()

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
                PomodoroTimer(
                    uiState = uiState,
                    onStart = { viewModel.showStartDialog() },
                    onPause = { viewModel.pauseSession() },
                    onResume = { viewModel.resumeSession() },
                    onStop = { viewModel.stopSession() }
                )
            }

            item {
                TodayStatsCard(
                    focusMinutes = uiState.todayFocusMinutes,
                    completedSessions = todaySessions.count { it.completed }
                )
            }

            item {
                Text(
                    text = "今日专注记录",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            items(todaySessions) { session ->
                SessionCard(session = session)
            }
        }
    }

    if (uiState.showStartDialog) {
        StartSessionDialog(
            onDismiss = { viewModel.hideStartDialog() },
            onStart = { taskName, duration ->
                viewModel.startSession(taskName, duration)
                viewModel.hideStartDialog()
            }
        )
    }
}

@Composable
fun PomodoroTimer(
    uiState: com.sleeplife.app.ui.viewmodels.PomodoroUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = if (uiState.totalSeconds > 0) {
            uiState.remainingSeconds.toFloat() / uiState.totalSeconds.toFloat()
        } else 1f,
        animationSpec = tween(1000, easing = LinearEasing),
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F3460).copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (uiState.sessionCompleted) {
                Text(
                    text = "🎉",
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "专注完成！",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
            } else {
                // Circular Progress
                Box(
                    modifier = Modifier.size(250.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        drawCircle(
                            color = Color(0xFF1A1A2E),
                            radius = size.minDimension / 2
                        )
                        drawCircle(
                            color = uiState.sessionType.getDefaultColor(),
                            radius = (size.minDimension / 2) * progress,
                            alpha = 0.3f
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = formatTime(uiState.remainingSeconds),
                            style = MaterialTheme.typography.displayLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )

                        if (uiState.currentTaskName.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.currentTaskName,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!uiState.isRunning) {
                        if (uiState.currentSessionId != null) {
                            // Paused state - Resume or Stop
                            Button(
                                onClick = onResume,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF66BB6A)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Resume"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("继续", style = MaterialTheme.typography.titleMedium)
                            }

                            Button(
                                onClick = onStop,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE94560)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("结束", style = MaterialTheme.typography.titleMedium)
                            }
                        } else {
                            // Not started
                            Button(
                                onClick = onStart,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE53935)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Start"
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("开始专注", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    } else {
                        // Running state
                        Button(
                            onClick = onPause,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF57C00)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Pause"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("暂停", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodayStatsCard(
    focusMinutes: Int,
    completedSessions: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F3460).copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                value = "${focusMinutes}分钟",
                label = "今日专注"
            )
            Spacer(modifier = Modifier.width(16.dp))
            StatItem(
                value = "$completedSessions",
                label = "完成次数"
            )
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFFE94560),
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    }
}

@Composable
fun SessionCard(session: com.sleeplife.app.data.entities.PomodoroSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F3460).copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.taskName,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatSessionTime(session.startTime),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${session.actualDuration}分钟",
                    style = MaterialTheme.typography.titleMedium,
                    color = session.sessionType.getDefaultColor(),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = if (session.completed) Color(0xFF66BB6A).copy(alpha = 0.3f)
                    else Color(0xFFE94560).copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = if (session.completed) "已完成" else "已中断",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (session.completed) Color(0xFF66BB6A) else Color(0xFFE94560)
                    )
                }
            }
        }
    }
}

@Composable
fun StartSessionDialog(
    onDismiss: () -> Unit,
    onStart: (String, Int) -> Unit
) {
    var taskName by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf(25) }

    val durationOptions = listOf(15, 20, 25, 30, 45, 60)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("开始专注") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = taskName,
                    onValueChange = { taskName = it },
                    label = { Text("任务名称") },
                    placeholder = { Text("例如：阅读、学习、工作...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "专注时长: $selectedDuration 分钟",
                    style = MaterialTheme.typography.bodyMedium
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(durationOptions.chunked(3)) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { duration ->
                                FilterChip(
                                    selected = selectedDuration == duration,
                                    onClick = { selectedDuration = duration },
                                    label = { Text("$duration分钟") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onStart(taskName.ifEmpty { "专注任务" }, selectedDuration)
                }
            ) {
                Text("开始")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}

fun formatSessionTime(dateTime: kotlinx.datetime.LocalDateTime): String {
    return "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
}
