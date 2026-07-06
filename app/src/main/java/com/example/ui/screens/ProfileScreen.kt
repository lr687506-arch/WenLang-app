package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LangViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: LangViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val orangePrimary = Color(0xFFFF6734)
    val lightBg = MaterialTheme.colorScheme.background
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var showAvatarEditor by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            showAvatarEditor = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meu Perfil", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = lightBg)
            )
        },
        containerColor = lightBg,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            profile?.let { p ->
                // Profile Header Card with Cover Photo and Username
                var showCoverPicker by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Column {
                        // Cover Photo Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .background(
                                    if (p.coverPhoto.isNullOrEmpty() || p.coverPhoto == "sunset") {
                                        Brush.linearGradient(colors = listOf(Color(0xFFFF8F5D), Color(0xFFFF6734)))
                                    } else if (p.coverPhoto == "galaxy") {
                                        Brush.linearGradient(colors = listOf(Color(0xFF3B0764), Color(0xFF1E1B4B)))
                                    } else if (p.coverPhoto == "forest") {
                                        Brush.linearGradient(colors = listOf(Color(0xFF064E3B), Color(0xFF065F46)))
                                    } else if (p.coverPhoto == "ocean") {
                                        Brush.linearGradient(colors = listOf(Color(0xFF0C4A6E), Color(0xFF0284C7)))
                                    } else {
                                        Brush.linearGradient(colors = listOf(Color(0xFF475569), Color(0xFF1E293B)))
                                    }
                                )
                        ) {
                            if (!p.coverPhoto.isNullOrEmpty() && !listOf("sunset", "galaxy", "forest", "ocean").contains(p.coverPhoto)) {
                                AsyncImage(
                                    model = p.coverPhoto,
                                    contentDescription = "Cover Photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            // Edit Cover Photo Button
                            IconButton(
                                onClick = { showCoverPicker = true },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                                    .size(36.dp)
                                    .testTag("btn_edit_cover")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Cover",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        // Avatar and Details (overlapping avatar effect)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(top = 40.dp)
                            ) {
                                Text(
                                    text = p.username,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.testTag("profile_username")
                                )

                                Text(
                                    text = "Estudante de ${p.targetLanguage}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = orangePrimary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                Text(
                                    text = "Nível ${p.languageLevel} • Nativo em ${p.nativeLanguage}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                Button(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = orangePrimary.copy(alpha = 0.08f), contentColor = orangePrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .padding(top = 8.dp, bottom = 16.dp)
                                        .testTag("btn_change_avatar")
                                ) {
                                    Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Alterar foto", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Overlapping Avatar Circle
                            Box(
                                modifier = Modifier
                                    .offset(y = (-40).dp)
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .padding(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .clickable { galleryLauncher.launch("image/*") }
                                        .background(orangePrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!p.avatarUrl.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = p.avatarUrl,
                                            contentDescription = "Foto de perfil",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = p.username.take(2).uppercase(),
                                            color = Color.White,
                                            fontSize = 24.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (showCoverPicker) {
                    var customUrl by remember { mutableStateOf(p.coverPhoto ?: "") }
                    if (listOf("sunset", "galaxy", "forest", "ocean").contains(customUrl)) {
                        customUrl = ""
                    }

                    AlertDialog(
                        onDismissRequest = { showCoverPicker = false },
                        title = { Text("Choose Cover Photo", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Select a preset theme or paste any custom image URL to personalize your profile banner.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )

                                // Custom URL Input
                                OutlinedTextField(
                                    value = customUrl,
                                    onValueChange = { customUrl = it },
                                    label = { Text("Custom Image URL", fontSize = 12.sp) },
                                    placeholder = { Text("https://example.com/image.jpg") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = orangePrimary,
                                        focusedLabelColor = orangePrimary
                                    )
                                )

                                if (customUrl.trim().isNotEmpty()) {
                                    Button(
                                        onClick = {
                                            viewModel.updateCoverPhoto(customUrl.trim())
                                            showCoverPicker = false
                                            Toast.makeText(context, "Cover photo updated!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = orangePrimary),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Save Custom URL", fontWeight = FontWeight.Bold)
                                    }
                                }

                                Text("Preset Themes", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                                // Row/Grid of Preset Choices
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val presets = listOf(
                                        "sunset" to Brush.linearGradient(colors = listOf(Color(0xFFFF8F5D), Color(0xFFFF6734))),
                                        "galaxy" to Brush.linearGradient(colors = listOf(Color(0xFF3B0764), Color(0xFF1E1B4B))),
                                        "forest" to Brush.linearGradient(colors = listOf(Color(0xFF064E3B), Color(0xFF065F46))),
                                        "ocean" to Brush.linearGradient(colors = listOf(Color(0xFF0C4A6E), Color(0xFF0284C7)))
                                    )

                                    presets.forEach { (name, brush) ->
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(50.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(brush)
                                                .clickable {
                                                    viewModel.updateCoverPhoto(name)
                                                    showCoverPicker = false
                                                    Toast.makeText(context, "Cover theme updated!", Toast.LENGTH_SHORT).show()
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = name.replaceFirstChar { it.uppercase() },
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showCoverPicker = false }) {
                                Text("Close", color = orangePrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                // Custom Instagram-style Circular Image Editor and Crop View
                if (showAvatarEditor && selectedImageUri != null) {
                    var scale by remember { mutableStateOf(1f) }
                    var offsetX by remember { mutableStateOf(0f) }
                    var offsetY by remember { mutableStateOf(0f) }
                    var isUploading by remember { mutableStateOf(false) }

                    AlertDialog(
                        onDismissRequest = { 
                            showAvatarEditor = false
                            selectedImageUri = null
                        },
                        title = { Text("Ajustar Foto de Perfil", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text(
                                    text = "Arraste para reposicionar e use o controle abaixo para aproximar a foto.",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    textAlign = TextAlign.Center
                                )

                                // Interactive circular preview crop viewport (WYSIWYG)
                                Box(
                                    modifier = Modifier
                                        .size(200.dp)
                                        .clip(CircleShape)
                                        .background(Color.LightGray)
                                        .border(2.dp, orangePrimary, CircleShape)
                                ) {
                                    AsyncImage(
                                        model = selectedImageUri,
                                        contentDescription = "Preview",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer(
                                                scaleX = scale,
                                                scaleY = scale,
                                                translationX = offsetX,
                                                translationY = offsetY
                                            )
                                            .pointerInput(Unit) {
                                                detectDragGestures { change, dragAmount ->
                                                    change.consume()
                                                    offsetX += dragAmount.x
                                                    offsetY += dragAmount.y
                                                }
                                            }
                                    )
                                }

                                // Slider zoom control
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Aproximar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Text("${(scale * 100).toInt()}%", fontSize = 11.sp, color = orangePrimary)
                                    }
                                    Slider(
                                        value = scale,
                                        onValueChange = { scale = it },
                                        valueRange = 1f..3f,
                                        colors = SliderDefaults.colors(
                                            thumbColor = orangePrimary,
                                            activeTrackColor = orangePrimary
                                        )
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            if (isUploading) {
                                CircularProgressIndicator(color = orangePrimary, modifier = Modifier.size(24.dp))
                            } else {
                                Button(
                                    onClick = {
                                        isUploading = true
                                        coroutineScope.launch {
                                            try {
                                                val inputStream = context.contentResolver.openInputStream(selectedImageUri!!)
                                                val bytes = inputStream?.readBytes()
                                                inputStream?.close()

                                                if (bytes != null) {
                                                    val success = viewModel.uploadAndSaveAvatar(bytes, p.username)
                                                    if (success) {
                                                        Toast.makeText(context, "Foto de perfil atualizada!", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        viewModel.saveLocalAvatar(selectedImageUri.toString())
                                                        Toast.makeText(context, "Foto de perfil salva localmente!", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    Toast.makeText(context, "Erro ao processar imagem.", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                viewModel.saveLocalAvatar(selectedImageUri.toString())
                                                Toast.makeText(context, "Foto de perfil salva localmente!", Toast.LENGTH_SHORT).show()
                                            } finally {
                                                isUploading = false
                                                showAvatarEditor = false
                                                selectedImageUri = null
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = orangePrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Salvar", fontWeight = FontWeight.Bold)
                                }
                            }
                        },
                        dismissButton = {
                            if (!isUploading) {
                                TextButton(
                                    onClick = {
                                        showAvatarEditor = false
                                        selectedImageUri = null
                                    }
                                ) {
                                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }
                        },
                        shape = RoundedCornerShape(24.dp)
                    )
                }

                // Bio Card
                var isEditingBio by remember { mutableStateOf(false) }
                var bioText by remember { mutableStateOf(p.bio) }

                // Synchronize bio text when profile changes
                LaunchedEffect(p.bio) {
                    bioText = p.bio
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sobre Mim",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            TextButton(
                                onClick = {
                                    if (isEditingBio) {
                                        viewModel.updateBio(bioText)
                                        Toast.makeText(context, "Biografia salva com sucesso!", Toast.LENGTH_SHORT).show()
                                    }
                                    isEditingBio = !isEditingBio
                                }
                            ) {
                                Text(
                                    text = if (isEditingBio) "Salvar" else "Editar",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = orangePrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (isEditingBio) {
                            OutlinedTextField(
                                value = bioText,
                                onValueChange = { bioText = it },
                                placeholder = { Text("Fale um pouco sobre você, seus objetivos de estudo...", fontSize = 13.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("bio_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = orangePrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface),
                                maxLines = 4
                            )
                        } else {
                            Text(
                                text = if (p.bio.isEmpty()) "Adicione uma biografia clicando em Editar para que outros alunos saibam mais sobre você!" else p.bio,
                                fontSize = 13.sp,
                                color = if (p.bio.isEmpty()) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // Daily Goal Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "META DIÁRIA",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = orangePrimary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "${p.studyTimeMinutes}",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = " / ${p.studyGoalMinutes} min",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                        }

                        // Circular Progress bar matching user's design
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.size(64.dp)
                        ) {
                            CircularProgressIndicator(
                                progress = { p.studyTimeMinutes.toFloat() / p.studyGoalMinutes.toFloat() },
                                modifier = Modifier.size(64.dp),
                                color = orangePrimary,
                                strokeWidth = 6.dp,
                                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = orangePrimary,
                                modifier = Modifier.size(24.dp)
                             )
                        }
                    }
                }

                // Streak & Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.LocalFireDepartment,
                        iconColor = orangePrimary,
                        title = "Ofensiva",
                        value = "${p.streak} dias"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.PublishedWithChanges,
                        iconColor = Color(0xFF15803D),
                        title = "Publicador",
                        value = if (p.approvedLanguagesForPosting.split(",").map { it.trim() }.contains(p.targetLanguage)) "Aprovado" else "Pendente"
                    )
                }

                // User Hobbies Summary & Interactive Manager
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Meus Interesses & Hobbies",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val hobbiesList = p.selectedHobbies.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                        if (hobbiesList.isEmpty()) {
                            Text(
                                text = "Nenhum hobby selecionado ainda. Adicione abaixo!",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        } else {
                            // Scrollable Row of chips with deletion capability
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 4.dp)
                            ) {
                                items(hobbiesList) { hobby ->
                                    Row(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = hobby,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remover",
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { viewModel.removeHobby(hobby) }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Add Hobby input Row
                        var newHobbyText by remember { mutableStateOf("") }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newHobbyText,
                                onValueChange = { newHobbyText = it },
                                placeholder = { Text("Novo interesse/hobby...", fontSize = 12.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("input_new_hobby"),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = orangePrimary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            IconButton(
                                onClick = {
                                    if (newHobbyText.trim().isNotEmpty()) {
                                        viewModel.addHobby(newHobbyText.trim())
                                        newHobbyText = ""
                                        Toast.makeText(context, "Interesse adicionado!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = orangePrimary, contentColor = Color.White),
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_add_hobby")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Adicionar Interesse")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Restart App / Settings Buttons
                Button(
                    onClick = {
                        viewModel.resetOnboarding()
                        Toast.makeText(context, "Configurações reiniciadas!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("btn_reset_app")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Recomeçar Onboarding", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    title: String,
    value: String
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
