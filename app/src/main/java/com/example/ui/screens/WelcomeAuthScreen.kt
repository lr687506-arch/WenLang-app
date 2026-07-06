package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.LangViewModel
import kotlinx.coroutines.launch

enum class AuthScreenMode {
    WELCOME,
    LOGIN,
    SIGNUP,
    CONFIRM_EMAIL,
    CHOOSE_USERNAME
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeAuthScreen(
    viewModel: LangViewModel,
    modifier: Modifier = Modifier
) {
    var screenMode by remember { mutableStateOf(AuthScreenMode.WELCOME) }
    val coroutineScope = rememberCoroutineScope()
    
    // Auth Inputs
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var inputUsername by remember { mutableStateOf("") }
    
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val orangePrimary = Color(0xFFFF6734)
    val lightBg = if (isDarkTheme) MaterialTheme.colorScheme.background else Color(0xFFFDFDFD)
    val cardBg = if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.White
    val textPrimary = if (isDarkTheme) Color.White else Color(0xFF1E293B)
    val textSecondary = if (isDarkTheme) Color.White.copy(alpha = 0.7f) else Color(0xFF64748B)

    // Collect profile to check if guest or existing username needs to be prefilled
    val profile by viewModel.userProfile.collectAsState()

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
            verticalArrangement = Arrangement.Center
        ) {
            
            // Header back button
            if (screenMode != AuthScreenMode.WELCOME) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(
                        onClick = {
                            statusMessage = null
                            if (screenMode == AuthScreenMode.CHOOSE_USERNAME || screenMode == AuthScreenMode.CONFIRM_EMAIL) {
                                screenMode = AuthScreenMode.WELCOME
                            } else {
                                screenMode = AuthScreenMode.WELCOME
                            }
                        },
                        modifier = Modifier.testTag("btn_auth_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = orangePrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            // Logo and Title Block
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.wenlang_icon_clean),
                    contentDescription = "WenLang App Icon",
                    modifier = Modifier
                        .size(120.dp)
                        .testTag("app_logo_welcome"),
                    contentScale = ContentScale.Fit
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "WenLang",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = orangePrimary,
                    letterSpacing = (-1).sp
                )
                
                Text(
                    text = "Your multilingual micro-learning hub",
                    fontSize = 14.sp,
                    color = textSecondary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Status feedback message
            statusMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isError) Color(0xFFFEE2E2) else Color(0xFFECFDF5)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("status_card")
                ) {
                    Text(
                        text = msg,
                        color = if (isError) Color(0xFF991B1B) else Color(0xFF065F46),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    )
                }
            }

            // Interactive screen areas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.TopCenter
            ) {
                when (screenMode) {
                    AuthScreenMode.WELCOME -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Welcome to WenLang",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // Login Button (Orange Primary Theme)
                            Button(
                                onClick = {
                                    statusMessage = null
                                    screenMode = AuthScreenMode.LOGIN
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = orangePrimary),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("btn_welcome_login")
                            ) {
                                Text(
                                    "Log In",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // Guest Button (Neutral Gray)
                            Button(
                                onClick = {
                                    statusMessage = null
                                    viewModel.continueAsGuest()
                                    screenMode = AuthScreenMode.CHOOSE_USERNAME
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B)),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .testTag("btn_welcome_guest")
                            ) {
                                Text(
                                    "Continue as Guest",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Clickable text info at bottom
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        statusMessage = null
                                        screenMode = AuthScreenMode.SIGNUP
                                    }
                                    .padding(8.dp)
                                    .testTag("btn_goto_signup"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Don't have an account? ",
                                    color = textSecondary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Sign Up",
                                    color = orangePrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    AuthScreenMode.LOGIN -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Log In",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_login_email"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = orangePrimary,
                                    focusedLabelColor = orangePrimary
                                )
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle password visibility"
                                        )
                                    }
                                },
                                singleLine = true,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_login_password"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = orangePrimary,
                                    focusedLabelColor = orangePrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = orangePrimary,
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .padding(16.dp)
                                )
                            } else {
                                Button(
                                    onClick = {
                                        if (email.isBlank() || password.isBlank()) {
                                            isError = true
                                            statusMessage = "Please fill in all fields"
                                            return@Button
                                        }
                                        isLoading = true
                                        statusMessage = "Logging in with Supabase..."
                                        isError = false
                                        coroutineScope.launch {
                                            val res = viewModel.signInWithSupabase(email.trim(), password)
                                            isLoading = false
                                            if (res.success) {
                                                isError = false
                                                statusMessage = "Welcome back!"
                                                // Logged in successfully, automatically proceeds if username exists
                                                if (res.username.isNullOrBlank() || res.username == "Guest" || res.username.startsWith("user")) {
                                                    screenMode = AuthScreenMode.CHOOSE_USERNAME
                                                }
                                            } else {
                                                isError = true
                                                statusMessage = res.errorMessage ?: "Failed to sign in."
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = orangePrimary),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .testTag("btn_submit_login")
                                ) {
                                    Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }

                    AuthScreenMode.SIGNUP -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Create Account",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_signup_email"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = orangePrimary,
                                    focusedLabelColor = orangePrimary
                                )
                            )

                            OutlinedTextField(
                                value = inputUsername,
                                onValueChange = { inputUsername = it },
                                label = { Text("Desired Username") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_signup_username"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = orangePrimary,
                                    focusedLabelColor = orangePrimary
                                )
                            )

                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_signup_password"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = orangePrimary,
                                    focusedLabelColor = orangePrimary
                                )
                            )

                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_signup_confirm"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = orangePrimary,
                                    focusedLabelColor = orangePrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = orangePrimary,
                                    modifier = Modifier
                                        .align(Alignment.CenterHorizontally)
                                        .padding(16.dp)
                                )
                            } else {
                                Button(
                                    onClick = {
                                        if (email.isBlank() || password.isBlank() || inputUsername.isBlank()) {
                                            isError = true
                                            statusMessage = "All fields are required"
                                            return@Button
                                        }
                                        if (password.length < 6) {
                                            isError = true
                                            statusMessage = "Password must be at least 6 characters"
                                            return@Button
                                        }
                                        if (password != confirmPassword) {
                                            isError = true
                                            statusMessage = "Passwords do not match"
                                            return@Button
                                        }
                                        isLoading = true
                                        statusMessage = "Creating your account on Supabase..."
                                        isError = false
                                        coroutineScope.launch {
                                            val res = viewModel.signUpWithSupabase(email.trim(), password, inputUsername.trim())
                                            isLoading = false
                                            if (res.success) {
                                                isError = false
                                                statusMessage = "Enviamos um e-mail de confirmação para seu endereço de e-mail. Confirme sua conta antes de continuar."
                                                screenMode = AuthScreenMode.CONFIRM_EMAIL
                                            } else {
                                                isError = true
                                                statusMessage = res.errorMessage ?: "Failed to sign up."
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = orangePrimary),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .testTag("btn_submit_signup")
                                ) {
                                    Text("Register", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }
                    }

                    AuthScreenMode.CONFIRM_EMAIL -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Confirme seu E-mail",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )

                            Text(
                                text = "Enviamos um e-mail de confirmação para:\n${email.trim()}\n\nPor favor, confirme sua conta clicando no link recebido antes de continuar.",
                                fontSize = 14.sp,
                                color = textSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (isLoading) {
                                CircularProgressIndicator(color = orangePrimary)
                            } else {
                                // 1. "Já confirmei meu e-mail" button
                                Button(
                                    onClick = {
                                        isLoading = true
                                        isError = false
                                        statusMessage = "Verificando confirmação do e-mail..."
                                        coroutineScope.launch {
                                            // Attempt sign in to verify if confirmed
                                            val res = viewModel.signInWithSupabase(email.trim(), password)
                                            isLoading = false
                                            if (res.success) {
                                                isError = false
                                                statusMessage = "E-mail confirmado com sucesso!"
                                                screenMode = AuthScreenMode.CHOOSE_USERNAME
                                            } else {
                                                isError = true
                                                val err = res.errorMessage ?: ""
                                                if (err.contains("not confirmed", ignoreCase = true) || err.contains("confirm", ignoreCase = true)) {
                                                    statusMessage = "Seu e-mail ainda não foi confirmado. Não é possível acessar o aplicativo."
                                                } else {
                                                    statusMessage = "Erro ao verificar: $err"
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = orangePrimary),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .testTag("btn_confirm_already_done")
                                ) {
                                    Text("Já confirmei meu e-mail", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }

                                // 2. "Reenviar e-mail" button
                                OutlinedButton(
                                    onClick = {
                                        isLoading = true
                                        isError = false
                                        statusMessage = "Reenviando e-mail de confirmação..."
                                        coroutineScope.launch {
                                            val res = viewModel.resendConfirmationEmail(email.trim())
                                            isLoading = false
                                            if (res.success) {
                                                isError = false
                                                statusMessage = "E-mail de confirmação reenviado com sucesso!"
                                            } else {
                                                isError = true
                                                statusMessage = res.errorMessage ?: "Erro ao reenviar e-mail."
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = orangePrimary),
                                    border = BorderStroke(1.5.dp, orangePrimary),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .testTag("btn_confirm_resend")
                                ) {
                                    Text("Reenviar e-mail", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }

                                // 3. "Voltar ao login" button
                                TextButton(
                                    onClick = {
                                        statusMessage = null
                                        screenMode = AuthScreenMode.LOGIN
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("btn_confirm_back_to_login")
                                ) {
                                    Text(
                                        text = "Voltar ao login",
                                        color = textSecondary,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    AuthScreenMode.CHOOSE_USERNAME -> {
                        var tempUsername by remember { mutableStateOf(profile?.username ?: "Guest") }
                        if (tempUsername == "Guest") tempUsername = ""

                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Choose Your Username",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = textPrimary
                            )
                            
                            Text(
                                text = "This is how you will be identified on the community and profile board.",
                                fontSize = 14.sp,
                                color = textSecondary
                            )

                            OutlinedTextField(
                                value = tempUsername,
                                onValueChange = { tempUsername = it },
                                label = { Text("Username") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_choose_username"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = orangePrimary,
                                    focusedLabelColor = orangePrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    if (tempUsername.trim().isBlank()) {
                                        isError = true
                                        statusMessage = "Username cannot be empty"
                                        return@Button
                                    }
                                    viewModel.completeAuthWithUsername(tempUsername.trim())
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = orangePrimary),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .testTag("btn_submit_username")
                            ) {
                                Text("Continue to Languages", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))
        }
    }
}
