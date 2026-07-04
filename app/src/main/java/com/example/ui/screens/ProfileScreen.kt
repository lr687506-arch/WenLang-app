package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LangViewModel

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
                // Profile Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(orangePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = p.targetLanguage.take(1).uppercase() + p.languageLevel,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Estudante de ${p.targetLanguage}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Nível ${p.languageLevel} • Nativo em ${p.nativeLanguage}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
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
