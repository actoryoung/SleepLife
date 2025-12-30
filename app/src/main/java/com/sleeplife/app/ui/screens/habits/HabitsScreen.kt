package com.sleeplife.app.ui.screens.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
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
import com.sleeplife.app.ui.viewmodels.HabitsViewModel
import com.sleeplife.app.ui.viewmodels.HabitWithProgress

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    viewModel: HabitsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedHabit by viewModel.selectedHabit.collectAsState()
    val checkIns by viewModel.habitCheckIns.collectAsState()

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
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            if (selectedHabit != null) {
                HabitDetailScreen(
                    habitWithProgress = uiState.habitsWithProgress.find { it.habit.id == selectedHabit!!.id },
                    checkIns = checkIns,
                    onBack = { viewModel.clearSelectedHabit() },
                    onCheckIn = { viewModel.checkInHabit(selectedHabit!!.id) },
                    onUndoCheckIn = { viewModel.undoCheckIn(selectedHabit!!.id) }
                )
            } else {
                HabitsListScreen(
                    habitsWithProgress = uiState.habitsWithProgress,
                    onAddHabit = { viewModel.showAddHabitDialog() },
                    onHabitClick = { viewModel.selectHabit(it) },
                    onDeleteHabit = { viewModel.deleteHabit(it) }
                )
            }
        }
    }

    if (uiState.showAddDialog) {
        AddHabitDialog(
            onDismiss = { viewModel.hideAddHabitDialog() },
            onAddHabit = { name, desc, icon, days, color ->
                viewModel.addHabit(name, desc, icon, days, color)
            }
        )
    }
}

@Composable
fun HabitsListScreen(
    habitsWithProgress: List<HabitWithProgress>,
    onAddHabit: () -> Unit,
    onHabitClick: (com.sleeplife.app.data.entities.Habit) -> Unit,
    onDeleteHabit: (com.sleeplife.app.data.entities.Habit) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "习惯打卡",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (habitsWithProgress.isEmpty()) {
            EmptyHabitsState(onAddHabit = onAddHabit)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(habitsWithProgress) { habitWithProgress ->
                    HabitCard(
                        habitWithProgress = habitWithProgress,
                        onClick = { onHabitClick(habitWithProgress.habit) },
                        onDelete = { onDeleteHabit(habitWithProgress.habit) }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = onAddHabit,
                modifier = Modifier.align(Alignment.CenterEnd),
                containerColor = Color(0xFF66BB6A)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    }
}

@Composable
fun EmptyHabitsState(onAddHabit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "还没有习惯",
            style = MaterialTheme.typography.titleLarge,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击右下角按钮创建第一个习惯",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onAddHabit) {
            Text("创建习惯")
        }
    }
}

@Composable
fun HabitCard(
    habitWithProgress: HabitWithProgress,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = habitWithProgress.habit.icon,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = habitWithProgress.habit.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${habitWithProgress.checkInCount}/${habitWithProgress.habit.targetDays} 天",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFE94560)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { habitWithProgress.progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(habitWithProgress.habit.color),
            )

            if (habitWithProgress.todayCheckedIn) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Checked in",
                        tint = Color(0xFF66BB6A),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "今日已打卡",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF66BB6A)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitWithProgress: HabitWithProgress?,
    checkIns: List<com.sleeplife.app.data.entities.HabitCheckIn>,
    onBack: () -> Unit,
    onCheckIn: () -> Unit,
    onUndoCheckIn: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        TopAppBar(
            title = { Text(habitWithProgress?.habit?.name ?: "") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        habitWithProgress?.let { habit ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0F3460).copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = habit.habit.icon,
                            style = MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${habit.checkInCount}/${habit.habit.targetDays}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { habit.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = Color(habit.habit.color),
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        if (habit.todayCheckedIn) {
                            Button(
                                onClick = onUndoCheckIn,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE94560)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Text("撤销打卡", style = MaterialTheme.typography.titleMedium)
                            }
                        } else {
                            Button(
                                onClick = onCheckIn,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF66BB6A)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                            ) {
                                Text("立即打卡", style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                }

                if (habit.habit.description.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF0F3460).copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "描述",
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = habit.habit.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0F3460).copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "打卡记录 (${checkIns.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.heightIn(max = 300.dp)
                        ) {
                            items(checkIns) { checkIn ->
                                CheckInItem(checkIn = checkIn)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CheckInItem(checkIn: com.sleeplife.app.data.entities.HabitCheckIn) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0xFF1A1A2E),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = formatDate(checkIn.checkInDate),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            if (checkIn.note.isNotEmpty()) {
                Text(
                    text = checkIn.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun AddHabitDialog(
    onDismiss: () -> Unit,
    onAddHabit: (String, String, String, Int, Long) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("📌") }
    var targetDays by remember { mutableStateOf(30) }

    val icons = listOf("📌", "💪", "📚", "🏃", "💧", "🧘", "✍️", "🎯", "🎨", "🎵", "💤", "🥗")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("创建新习惯") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("习惯名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述（可选）") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "选择图标",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                ) {
                    items(icons.chunked(6)) { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { icon ->
                                FilterChip(
                                    selected = selectedIcon == icon,
                                    onClick = { selectedIcon = icon },
                                    label = { Text(icon, style = MaterialTheme.typography.titleLarge) },
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "目标天数: $targetDays",
                    style = MaterialTheme.typography.bodySmall
                )

                Slider(
                    value = targetDays.toFloat(),
                    onValueChange = { targetDays = it.toInt() },
                    valueRange = 7f..100f,
                    steps = 93
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onAddHabit(name, description, selectedIcon, targetDays, 0xFF66BB6A)
                },
                enabled = name.isNotEmpty()
            ) {
                Text("创建")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

fun formatDate(dateTime: kotlinx.datetime.LocalDateTime): String {
    return "${dateTime.monthNumber}月${dateTime.dayOfMonth}日 ${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
}
