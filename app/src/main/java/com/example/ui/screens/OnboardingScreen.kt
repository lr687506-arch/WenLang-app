package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LangViewModel
import com.example.ui.Loc

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: LangViewModel,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(1) }

    // Onboarding States
    var appLanguage by remember { mutableStateOf("Português") }
    var nativeLanguage by remember { mutableStateOf("Português") }
    var secondaryLanguage by remember { mutableStateOf<String?>("Nenhum") }
    var targetLanguage by remember { mutableStateOf("Inglês") }
    var languageLevel by remember { mutableStateOf("N3") }
    val selectedHobbies = remember { mutableStateListOf<String>() }

    val appLanguages = listOf("Alemão", "Espanhol", "Grego", "Hebraico", "Indonésio", "Inglês", "Japonês", "Mandarim", "Português", "Russo")
    val nativeLanguages = listOf("Alemão", "Espanhol", "Grego", "Hebraico", "Indonésio", "Inglês", "Japonês", "Mandarim", "Português", "Russo")
    val secondaryLanguages = listOf("Nenhum", "Alemão", "Espanhol", "Grego", "Hebraico", "Indonésio", "Inglês", "Japonês", "Mandarim", "Português", "Russo")
    val targetLanguages = listOf("Alemão", "Espanhol", "Grego", "Hebraico", "Indonésio", "Inglês", "Japonês", "Mandarim", "Português", "Russo")
    val languageLevels = listOf(
        "N5" to "N5 - Básico (A1)",
        "N4" to "N4 - Elementar (A2)",
        "N3" to "N3 - Intermediário (B1)",
        "N2" to "N2 - Avançado (B2)",
        "N1" to "N1 - Fluente (C1/C2)"
    )

    // Hobby Categories and Subcategories
    val hobbyCategories = listOf(
        HobbyCategory(
            "Educação",
            listOf("Matemática", "Biologia", "Idiomas", "Ciências", "História")
        ),
        HobbyCategory(
            "Tecnologia",
            listOf("Inteligência Artificial", "Programação", "Gadgets", "Segurança", "Hardware")
        ),
        HobbyCategory(
            "Entretenimento",
            listOf("Cinema", "Música", "Literatura", "Jogos", "Fotografia")
        ),
        HobbyCategory(
            "Estilo de Vida",
            listOf("Culinária", "Viagem", "Esportes", "Arquitetura", "Moda")
        )
    )

    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    // Sleek Substack-style Orange Theme Color
    val orangePrimary = Color(0xFFFF6734)

    // Dynamic colors that adjust properly to light and dark theme
    val lightBg = if (isDarkTheme) MaterialTheme.colorScheme.background else Color(0xFFFDFDFD)
    val cardBg = if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.White
    val cardBorder = if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f) else Color(0xFFE2E8F0)
    
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF1E293B)
    val textSecondary = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B)
    val textMuted = if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color(0xFF475569)
    val chipBg = if (isDarkTheme) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f) else Color(0xFFF1F5F9)
    val unselectedText = if (isDarkTheme) Color.White.copy(alpha = 0.8f) else Color(0xFF475569)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = lightBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header / Progress
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Logo",
                        tint = orangePrimary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "WenLang",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = textPrimary
                    )
                }

                Text(
                    text = "${Loc.get("step", nativeLanguage)} $step ${Loc.get("of", nativeLanguage)} 3",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = orangePrimary,
                    modifier = Modifier
                        .background(orangePrimary.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Step Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                when (step) {
                    1 -> {
                        // Step 1: Idiomas
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = Loc.get("lang_config", nativeLanguage),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = Loc.get("lang_subtitle", nativeLanguage),
                                fontSize = 14.sp,
                                color = textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // App Language
                            DropdownField(
                                label = Loc.get("app_lang", nativeLanguage),
                                selected = appLanguage,
                                options = appLanguages,
                                onSelect = { appLanguage = it }
                            )

                            // Native Language
                            DropdownField(
                                label = Loc.get("native_lang", nativeLanguage),
                                selected = nativeLanguage,
                                options = nativeLanguages,
                                onSelect = { nativeLanguage = it }
                            )

                            // Secondary Language (Optional)
                            DropdownField(
                                label = Loc.get("secondary_lang", nativeLanguage),
                                selected = secondaryLanguage ?: "Nenhum",
                                options = secondaryLanguages,
                                onSelect = { secondaryLanguage = if (it == "Nenhum") null else it }
                            )

                            // Target Language
                            DropdownField(
                                label = Loc.get("study_lang", nativeLanguage),
                                selected = targetLanguage,
                                options = targetLanguages,
                                onSelect = { targetLanguage = it }
                            )
                        }
                    }
                    2 -> {
                        // Step 2: Nível de Proficiência
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = Loc.get("study_level", nativeLanguage),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = String.format(Loc.get("level_desc", nativeLanguage), targetLanguage),
                                fontSize = 14.sp,
                                color = textSecondary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            languageLevels.forEach { (code, label) ->
                                val isSelected = languageLevel == code
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { languageLevel = code }
                                        .testTag("level_option_$code"),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) orangePrimary.copy(alpha = 0.08f) else cardBg
                                    ),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) orangePrimary else cardBorder
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 15.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) orangePrimary else unselectedText
                                        )
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = "Selected",
                                                tint = orangePrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    3 -> {
                        // Step 3: Hobbies / Interesses
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = Loc.get("choose_interests", nativeLanguage),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = Loc.get("interests_desc", nativeLanguage),
                                fontSize = 14.sp,
                                color = textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Box(modifier = Modifier.weight(1f)) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    hobbyCategories.forEach { category ->
                                        Text(
                                            text = category.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = orangePrimary,
                                            modifier = Modifier.padding(top = 8.dp)
                                        )

                                        FlowRow(
                                            modifier = Modifier.fillMaxWidth(),
                                            mainAxisSpacing = 8.dp,
                                            crossAxisSpacing = 8.dp
                                        ) {
                                            category.subcategories.forEach { sub ->
                                                val isSelected = selectedHobbies.contains(sub)
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(20.dp))
                                                        .background(
                                                            if (isSelected) orangePrimary else chipBg
                                                        )
                                                        .clickable {
                                                            if (isSelected) {
                                                                selectedHobbies.remove(sub)
                                                            } else {
                                                                selectedHobbies.add(sub)
                                                            }
                                                        }
                                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                                        .testTag("hobby_$sub")
                                                ) {
                                                    Text(
                                                        text = sub,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.SemiBold,
                                                        color = if (isSelected) Color.White else unselectedText
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
            }

            // Bottom Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (step > 1) {
                    TextButton(
                        onClick = { step -= 1 },
                        colors = ButtonDefaults.textButtonColors(contentColor = textSecondary),
                        modifier = Modifier.testTag("btn_back")
                    ) {
                        Text(Loc.get("back", nativeLanguage), fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Button(
                    onClick = {
                        if (step < 3) {
                            step += 1
                        } else {
                            // Save onboarding data and complete
                            viewModel.completeOnboarding(
                                appLang = appLanguage,
                                nativeLang = nativeLanguage,
                                secondaryLang = secondaryLanguage,
                                targetLang = targetLanguage,
                                level = languageLevel,
                                hobbies = selectedHobbies.toList()
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = orangePrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(50.dp)
                        .padding(horizontal = 8.dp)
                        .testTag("btn_next")
                ) {
                    Text(
                        text = if (step == 3) Loc.get("finish", nativeLanguage) else Loc.get("next", nativeLanguage),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface = MaterialTheme.colorScheme.surface
    val outline = MaterialTheme.colorScheme.outlineVariant

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = onSurface.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                readOnly = true,
                value = selected,
                onValueChange = {},
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = onSurface,
                    unfocusedTextColor = onSurface,
                    disabledTextColor = onSurface,
                    focusedBorderColor = Color(0xFFFF6734),
                    unfocusedBorderColor = outline,
                    disabledBorderColor = outline,
                    focusedContainerColor = surface,
                    unfocusedContainerColor = surface,
                    disabledContainerColor = surface
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .testTag("dropdown_" + label.replace(" ", "_"))
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(surface)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option, color = onSurface) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    mainAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    crossAxisSpacing: androidx.compose.ui.unit.Dp = 0.dp,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.layout.Layout(
        modifier = modifier,
        content = content
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val layoutWidth = constraints.maxWidth
        val lines = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        var currentLine = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentLineWidth = 0

        placeables.forEach { placeable ->
            val spacing = if (currentLine.isEmpty()) 0 else mainAxisSpacing.roundToPx()
            if (currentLineWidth + spacing + placeable.width <= layoutWidth) {
                currentLine.add(placeable)
                currentLineWidth += spacing + placeable.width
            } else {
                lines.add(currentLine)
                currentLine = mutableListOf(placeable)
                currentLineWidth = placeable.width
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        val height = lines.sumOf { line -> line.maxOf { it.height } } +
                ((lines.size - 1).coerceAtLeast(0) * crossAxisSpacing.roundToPx())

        layout(layoutWidth, height) {
            var y = 0
            lines.forEach { line ->
                var x = 0
                val lineHeight = line.maxOf { it.height }
                line.forEach { placeable ->
                    placeable.placeRelative(x, y + (lineHeight - placeable.height) / 2)
                    x += placeable.width + mainAxisSpacing.roundToPx()
                }
                y += lineHeight + crossAxisSpacing.roundToPx()
            }
        }
    }
}

data class HobbyCategory(
    val name: String,
    val subcategories: List<String>
)
