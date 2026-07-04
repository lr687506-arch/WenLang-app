package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.LangViewModel

@Composable
fun QuizScreen(
    viewModel: LangViewModel,
    modifier: Modifier = Modifier
) {
    val questions by viewModel.quizQuestions.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val selectedIndex by viewModel.selectedAnswerIndex.collectAsState()
    val finished by viewModel.quizFinished.collectAsState()
    val correctCount by viewModel.correctAnswersCount.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    val orangePrimary = Color(0xFFFF6734)
    val lightBg = MaterialTheme.colorScheme.background

    val targetLanguage = profile?.targetLanguage ?: "Japonês"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = lightBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            if (!finished) {
                // Quiz Ongoing
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = orangePrimary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Teste de Proficiência em $targetLanguage",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Acerte 100% (20/20) para desbloquear a publicação de histórias!",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Questão ${currentIndex + 1} de ${questions.size}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "Acertos: $correctCount",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = orangePrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (currentIndex + 1).toFloat() / questions.size.toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .testTag("quiz_progress"),
                        color = orangePrimary,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }

                if (currentIndex < questions.size) {
                    val q = questions[currentIndex]

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Question Card
                        Card(
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = q.question,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(24.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        // Options List
                        q.options.forEachIndexed { optIndex, option ->
                            val isSelected = selectedIndex == optIndex
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectAnswer(optIndex) }
                                    .testTag("option_$optIndex"),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) orangePrimary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) orangePrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .background(
                                                if (isSelected) orangePrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                                RoundedCornerShape(6.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = ('A' + optIndex).toString(),
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = option,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) orangePrimary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Submit Button
                Button(
                    onClick = { viewModel.submitAnswer() },
                    enabled = selectedIndex != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = orangePrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("btn_submit_answer")
                ) {
                    Text(
                        "Confirmar Resposta",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = if (selectedIndex != null) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    )
                }

            } else {
                // Quiz Finished / Results
                val percentage = (correctCount.toFloat() / questions.size.toFloat() * 100).toInt()
                val isSuccess = correctCount == questions.size

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                if (isSuccess) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                                RoundedCornerShape(32.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isSuccess) Icons.Default.LockOpen else Icons.Default.Close,
                            contentDescription = null,
                            tint = if (isSuccess) Color(0xFF15803D) else Color(0xFFB91C1C),
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = if (isSuccess) "Teste Concluído!" else "Não foi dessa vez!",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Você acertou $correctCount de ${questions.size} questões ($percentage%)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSuccess) Color(0xFF15803D) else Color(0xFFB91C1C)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (isSuccess) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF15803D),
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "PUBLICAÇÃO DESBLOQUEADA!",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF15803D)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Incrível! Você acertou todas as questões de proficiência de $targetLanguage e provou ser fluente em sua leitura. Agora você está autorizado a publicar novos textos na plataforma e compartilhá-los com outros estudantes!",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            } else {
                                Text(
                                    text = "Como desbloquear?",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Para manter a qualidade pedagógica dos posts na plataforma, apenas usuários aprovados com 100% no teste de proficiência podem criar histórias. Estude mais os textos da WenLang e tente novamente!",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { viewModel.restartQuiz() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSuccess) MaterialTheme.colorScheme.onBackground else orangePrimary
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("btn_restart_quiz")
                        ) {
                            Text(
                                text = if (isSuccess) "Refazer Teste" else "Tentar Novamente",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = if (isSuccess) MaterialTheme.colorScheme.background else Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
