package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LangViewModel
import com.example.ui.Loc

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishScreen(
    viewModel: LangViewModel,
    onNavigateToQuiz: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val targetLanguage = profile?.targetLanguage ?: "Japonês"
    val nativeLanguage = profile?.nativeLanguage ?: "Português"

    val isApproved = remember(profile) {
        val target = profile?.targetLanguage ?: "Japonês"
        val native = profile?.nativeLanguage ?: ""
        val secondary = profile?.secondaryLanguage ?: ""
        
        target == native || target == secondary || profile?.approvedLanguagesForPosting?.split(",")
            ?.map { it.trim() }?.contains(target) == true
    }

    val orangePrimary = Color(0xFFFF6734)
    val lightBg = MaterialTheme.colorScheme.background
    val context = LocalContext.current

    // Form states
    var isPublishingInNative by remember { mutableStateOf(false) }
    var useTts by remember { mutableStateOf(true) }
    var importedAudioName by remember { mutableStateOf<String?>(null) }
    var showAudioPicker by remember { mutableStateOf(false) }

    var title by remember { mutableStateOf("") }
    var translationTitle by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var translationContent by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tecnologia") }
    var level by remember { mutableStateOf("N3") }
    var tagsInput by remember { mutableStateOf("") }
    var coverUrl by remember { mutableStateOf("") }

    val categories = listOf("Tecnologia", "Arquitetura", "Biologia", "Cultura")
    val levels = listOf("N5", "N4", "N3", "N2", "N1")

    val currentPublishLanguage = if (isPublishingInNative) nativeLanguage else targetLanguage

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = lightBg,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = Loc.get("publish", nativeLanguage), 
                        fontSize = 18.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = MaterialTheme.colorScheme.onBackground
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_to_feed")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = Loc.get("back", nativeLanguage), tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = lightBg)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val showForm = isApproved || isPublishingInNative
            if (!showForm) {
                // Locked screen
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(orangePrimary.copy(alpha = 0.08f), RoundedCornerShape(32.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Bloqueado",
                            tint = orangePrimary,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = Loc.get("publish_locked", nativeLanguage),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = String.format(Loc.get("publish_locked_desc", nativeLanguage), targetLanguage),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = onNavigateToQuiz,
                        colors = ButtonDefaults.buttonColors(containerColor = orangePrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("btn_go_to_quiz")
                    ) {
                        Text(
                            text = Loc.get("take_test", nativeLanguage),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedButton(
                        onClick = {
                            isPublishingInNative = true
                        },
                        border = BorderStroke(1.dp, orangePrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("btn_publish_native")
                    ) {
                        Text(
                            text = "Publicar no meu idioma nativo ($nativeLanguage)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = orangePrimary
                        )
                    }
                }
            } else {
                // Form edit composer
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PostAdd,
                            contentDescription = null,
                            tint = orangePrimary,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = Loc.get("create_story", nativeLanguage),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = String.format(Loc.get("publish_community", nativeLanguage), currentPublishLanguage),
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), modifier = Modifier.padding(vertical = 4.dp))

                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(String.format(Loc.get("orig_title", nativeLanguage), currentPublishLanguage)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("publish_title"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = orangePrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Translation Title
                    OutlinedTextField(
                        value = translationTitle,
                        onValueChange = { translationTitle = it },
                        label = { Text(Loc.get("trans_title", nativeLanguage)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("publish_translation_title"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = orangePrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Content
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text(String.format(Loc.get("story_text", nativeLanguage), currentPublishLanguage)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("publish_content"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = orangePrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 6
                    )

                    // Translation Content
                    OutlinedTextField(
                        value = translationContent,
                        onValueChange = { translationContent = it },
                        label = { Text(Loc.get("story_trans", nativeLanguage)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .testTag("publish_translation_content"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = orangePrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 6
                    )

                    // Level Selector
                    Text(
                        text = Loc.get("diff_level", nativeLanguage),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        levels.forEach { lvl ->
                            val isSelected = level == lvl
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) orangePrimary else MaterialTheme.colorScheme.surface)
                                    .border(1.dp, if (isSelected) orangePrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                    .clickable { level = lvl }
                                    .padding(vertical = 10.dp)
                                    .testTag("publish_level_$lvl"),
                                contentAlignment = Alignment.Center
                            ) {
                                defianceText(lvl, isSelected)
                            }
                        }
                    }

                    // Category Selector
                    Text(
                        text = Loc.get("category", nativeLanguage),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) orangePrimary else MaterialTheme.colorScheme.surface)
                                    .border(1.dp, if (isSelected) orangePrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                    .clickable { selectedCategory = cat }
                                    .padding(vertical = 10.dp)
                                    .testTag("publish_cat_$cat"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Cover URL (optional)
                    OutlinedTextField(
                        value = coverUrl,
                        onValueChange = { coverUrl = it },
                        label = { Text(Loc.get("cover_url", nativeLanguage)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("publish_cover_url"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = orangePrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        leadingIcon = { Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)) },
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Tags Input
                    OutlinedTextField(
                        value = tagsInput,
                        onValueChange = { tagsInput = it },
                        label = { Text(Loc.get("tags", nativeLanguage)) },
                        placeholder = { Text("ex: Japão, Viagem, Cultura") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("publish_tags"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = orangePrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Audio configurations section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Configuração de Áudio",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // TTS option
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { useTts = true },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (useTts) orangePrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(
                                        width = 1.6.dp,
                                        color = if (useTts) orangePrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = null,
                                            tint = if (useTts) orangePrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "TTS Padrão",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (useTts) orangePrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                    }
                                }

                                // Custom audio option
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { useTts = false },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (!useTts) orangePrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                                    ),
                                    border = BorderStroke(
                                        width = 1.6.dp,
                                        color = if (!useTts) orangePrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Audiotrack,
                                            contentDescription = null,
                                            tint = if (!useTts) orangePrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Áudio Próprio",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!useTts) orangePrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }

                            if (!useTts) {
                                Button(
                                    onClick = { showAudioPicker = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurfaceVariant),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth().height(40.dp)
                                ) {
                                    Icon(Icons.Default.Audiotrack, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (importedAudioName != null) "Alterar Áudio" else "Importar Arquivo de Áudio",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }

                                if (importedAudioName != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                            .padding(8.dp)
                                    ) {
                                        Icon(Icons.Default.Audiotrack, contentDescription = null, tint = orangePrimary, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = importedAudioName ?: "",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.weight(1f)
                                        )
                                        IconButton(
                                            onClick = { importedAudioName = null },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Limpar",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (showAudioPicker) {
                        AlertDialog(
                            onDismissRequest = { showAudioPicker = false },
                            title = { Text("Importar Áudio") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Escolha um arquivo de áudio para importar para sua história:", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                                    
                                    val simulatedAudios = listOf("gravacao_minha_historia.mp3", "pronuncia_nativa_aula.wav", "leitura_fluente_audio.m4a")
                                    simulatedAudios.forEach { name ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    importedAudioName = name
                                                    showAudioPicker = false
                                                },
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(Icons.Default.Audiotrack, contentDescription = null, tint = orangePrimary, modifier = Modifier.size(16.dp))
                                                Text(text = name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                            }
                                        }
                                    }
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showAudioPicker = false }) {
                                    Text("Fechar", color = orangePrimary)
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Publish Button
                    Button(
                        onClick = {
                            if (title.isEmpty() || content.isEmpty() || translationTitle.isEmpty() || translationContent.isEmpty()) {
                                Toast.makeText(context, Loc.get("fill_required", nativeLanguage), Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.publishStory(
                                    title = title,
                                    content = content,
                                    translationTitle = translationTitle,
                                    translationContent = translationContent,
                                    category = selectedCategory,
                                    tags = if (tagsInput.isEmpty()) selectedCategory else tagsInput,
                                    level = level,
                                    language = if (isPublishingInNative) nativeLanguage else targetLanguage,
                                    audioUri = importedAudioName,
                                    useTts = useTts
                                )
                                Toast.makeText(context, Loc.get("publish_success", nativeLanguage), Toast.LENGTH_LONG).show()

                                // Reset form fields and navigate back
                                title = ""
                                translationTitle = ""
                                content = ""
                                translationContent = ""
                                tagsInput = ""
                                coverUrl = ""
                                isPublishingInNative = false
                                useTts = true
                                importedAudioName = null
                                onBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = orangePrimary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("btn_publish_post")
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(Loc.get("publish_btn", nativeLanguage), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun defianceText(lvl: String, isSelected: Boolean) {
    Text(
        text = lvl,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    )
}
