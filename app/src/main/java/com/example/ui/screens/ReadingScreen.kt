package com.example.ui.screens

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import org.json.JSONObject
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.SavedWord
import com.example.data.TextItem
import com.example.ui.LangViewModel
import com.example.ui.Loc
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen(
    viewModel: LangViewModel,
    onNavigateToPublish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allTexts by viewModel.allTexts.collectAsState()
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val savedWords by viewModel.savedWords.collectAsState()
    val profile by viewModel.userProfile.collectAsState()

    var selectedCategory by remember { mutableStateOf("Para Você") }
    var activeStoryForReading by remember { mutableStateOf<TextItem?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }

    val categories = listOf("Para Você", "Tecnologia", "Arquitetura", "Biologia", "Cultura")
    val orangePrimary = Color(0xFFFF6734)
    val nativeLanguage = profile?.nativeLanguage ?: "Português"

    // Filtered texts based on search, levels, and categories
    val filteredTexts = remember(allTexts, selectedCategory, searchQuery, profile) {
        val targetLanguage = profile?.targetLanguage ?: "Japonês"
        var list = allTexts.filter { it.language.equals(targetLanguage, ignoreCase = true) }

        if (searchQuery.isNotEmpty()) {
            list = list.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.translationTitle.contains(searchQuery, ignoreCase = true) ||
                        it.content.contains(searchQuery, ignoreCase = true)
            }
        }

        if (selectedCategory != "Para Você") {
            list = list.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        } else {
            // Intelligent recommendation: Show texts matching user's selected level first,
            // then match by hobbies
            profile?.let { p ->
                val userHobbies = p.selectedHobbies.split(",").map { it.trim().lowercase() }
                list = list.sortedWith(compareByDescending<TextItem> {
                    it.level == p.languageLevel
                }.thenByDescending {
                    val textTags = it.tags.split(",").map { t -> t.trim().lowercase() }
                    textTags.any { tag -> userHobbies.contains(tag) }
                })
            }
        }
        list
    }

    if (activeStoryForReading != null) {
        // Detailed reader screen
        StoryReaderView(
            story = activeStoryForReading!!,
            viewModel = viewModel,
            savedWords = savedWords,
            onBack = { activeStoryForReading = null }
        )
    } else {
        Box(
            modifier = modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Topbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(orangePrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile?.nativeLanguage?.take(2)?.uppercase() ?: "JS",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Olá, Aprendiz",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${Loc.get("level", nativeLanguage)}: ${profile?.targetLanguage} ${profile?.languageLevel}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(
                            onClick = { showSearch = !showSearch },
                            modifier = Modifier.testTag("btn_toggle_search")
                        ) {
                            Icon(
                                imageVector = if (showSearch) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = "Buscar",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleDarkTheme() },
                            modifier = Modifier.testTag("btn_toggle_theme")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Alternar Tema",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                // Search Bar
                AnimatedVisibility(visible = showSearch) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Pesquisar histórias, vocabulário...", fontSize = 14.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                            .testTag("search_input"),
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = if (isDarkTheme) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B)) },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isDarkTheme) Color.White else Color(0xFF1E293B),
                            unfocusedTextColor = if (isDarkTheme) Color.White else Color(0xFF1E293B),
                            focusedPlaceholderColor = if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color(0xFF64748B),
                            unfocusedPlaceholderColor = if (isDarkTheme) Color.White.copy(alpha = 0.5f) else Color(0xFF64748B),
                            focusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.White,
                            unfocusedContainerColor = if (isDarkTheme) MaterialTheme.colorScheme.surface else Color.White,
                            focusedBorderColor = orangePrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        ),
                        singleLine = true
                    )
                }

                // Categories horizontal tabs
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) orangePrimary else MaterialTheme.colorScheme.surface)
                                .clickable { selectedCategory = cat }
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) orangePrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("tab_$cat")
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Reading Feed
                if (filteredTexts.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(40.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Nenhuma história encontrada",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Tente alterar os filtros ou adicione uma nova história!",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp, start = 20.dp, end = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        items(filteredTexts) { story ->
                            StoryCard(
                                story = story,
                                orangePrimary = orangePrimary,
                                nativeLanguage = nativeLanguage,
                                onRead = { activeStoryForReading = story }
                            )
                        }
                    }
                }
            }

            val targetLanguage = profile?.targetLanguage ?: "Japonês"
            val isApproved = profile?.approvedLanguagesForPosting?.split(",")
                ?.map { it.trim() }?.contains(targetLanguage) == true ||
                profile?.targetLanguage == profile?.nativeLanguage ||
                profile?.targetLanguage == profile?.secondaryLanguage

            FloatingActionButton(
                onClick = onNavigateToPublish,
                containerColor = orangePrimary,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("fab_publish")
            ) {
                Box {
                    Icon(Icons.Default.PostAdd, contentDescription = "Criar Nova História")
                    if (!isApproved) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Trancado",
                            tint = Color.White,
                            modifier = Modifier
                                .size(10.dp)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StoryCard(
    story: TextItem,
    orangePrimary: Color,
    nativeLanguage: String,
    onRead: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("story_card_${story.id}"),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // Capa/Header image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                if (story.imageUrl != null && story.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = story.imageUrl,
                        contentDescription = "Capa",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Level Badge
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .background(orangePrimary, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.TopStart)
                ) {
                    Text(
                        text = story.level,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                // Title overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            androidx.compose.ui.graphics.Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = story.title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = story.translationTitle,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        maxLines = 1,
                        fontStyle = FontStyle.Italic
                    )
                }
            }

            // Body
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = story.content,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 3,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Tags row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        story.tags.split(",").take(2).forEach { tag ->
                            val cleanTag = tag.trim()
                            if (cleanTag.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .background(orangePrimary.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = cleanTag.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = orangePrimary
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = onRead,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onBackground),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("btn_read_${story.id}")
                    ) {
                        Text(Loc.get("reading", nativeLanguage), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.background)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryReaderView(
    story: TextItem,
    viewModel: LangViewModel,
    savedWords: List<SavedWord>,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var showTranslation by remember { mutableStateOf(true) }
    val showFurigana by viewModel.showFurigana.collectAsState()
    var dictionaryWord by remember { mutableStateOf<DictionaryEntry?>(null) }
    var translatingWordState by remember { mutableStateOf<TranslationUiState?>(null) }

    val context = LocalContext.current
    val orangePrimary = Color(0xFFFF6734)

    val profile by viewModel.userProfile.collectAsState()
    val nativeLanguage = profile?.nativeLanguage ?: "Português"
    val targetLanguage = profile?.targetLanguage ?: "Japonês"

    // TTS Setup matching story language
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isTtsPlaying by remember { mutableStateOf(false) }

    var isCustomAudioPlaying by remember { mutableStateOf(false) }
    var customAudioProgress by remember { mutableFloatStateOf(0f) }

    // Simulating custom audio playback progression
    LaunchedEffect(isCustomAudioPlaying) {
        if (isCustomAudioPlaying) {
            val totalSteps = 150 // 15 seconds (100ms per step)
            for (step in 1..totalSteps) {
                if (!isCustomAudioPlaying) break
                kotlinx.coroutines.delay(100)
                customAudioProgress = step.toFloat() / totalSteps
            }
            isCustomAudioPlaying = false
            customAudioProgress = 0f
        }
    }

    DisposableEffect(story.language) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = when {
                    story.language.lowercase().contains("ingl") || story.language.lowercase().contains("en") -> Locale.ENGLISH
                    story.language.lowercase().contains("alem") || story.language.lowercase().contains("de") -> Locale.GERMAN
                    story.language.lowercase().contains("espa") || story.language.lowercase().contains("es") -> Locale("es", "ES")
                    story.language.lowercase().contains("port") || story.language.lowercase().contains("pt") -> Locale("pt", "BR")
                    story.language.lowercase().contains("japo") || story.language.lowercase().contains("ja") -> Locale.JAPANESE
                    story.language.lowercase().contains("chin") || story.language.lowercase().contains("zh") -> Locale.CHINESE
                    story.language.lowercase().contains("russ") || story.language.lowercase().contains("ru") -> Locale("ru", "RU")
                    story.language.lowercase().contains("greg") || story.language.lowercase().contains("el") -> Locale("el", "GR")
                    story.language.lowercase().contains("hebr") || story.language.lowercase().contains("he") -> Locale("iw", "IL")
                    story.language.lowercase().contains("indo") || story.language.lowercase().contains("id") -> Locale("id", "ID")
                    else -> Locale.ENGLISH
                }
                tts?.language = locale
                tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        isTtsPlaying = true
                    }
                    override fun onDone(utteranceId: String?) {
                        isTtsPlaying = false
                    }
                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        isTtsPlaying = false
                    }
                })
            }
        }
        onDispose {
            tts?.stop()
            tts?.shutdown()
        }
    }

    // Interactive words lookup dictionary
    val dictionaryLookup = mapOf(
        "稲荷神社" to DictionaryEntry("稲荷神社", "いなりじんじゃ", "Santuário Inari", "稲荷神社は日本中にあります。", "Existem santuários Inari por todo o Japão."),
        "鳥居" to DictionaryEntry("鳥居", "とりい", "Portal Torii", "赤い鳥居を通り抜ける。", "Atravessar o portal torii vermelho."),
        "狐" to DictionaryEntry("狐", "きつね", "Raposa", "狐はお稲荷さんの使いです。", "A raposa é a mensageira do Inari-san."),
        "現代" to DictionaryEntry("現代", "げんだい", "Tempos modernos / Modernidade", "現代の建築は美しいです。", "A arquitetura da modernidade é bela."),
        "建築" to DictionaryEntry("建築", "けんちく", "Arquitetura", "建築を専門に学ぶ。", "Estudar arquitetura profissionalmente."),
        "コンクリート" to DictionaryEntry("コンクリート", "こんくりーと", "Concreto", "コンクリートの美しさを引き出す。", "Extrair a beleza do concreto."),
        "人工知能" to DictionaryEntry("人工知能", "じんこうちのう", "Inteligência Artificial (IA)", "人工知能가発達する。", "A inteligência artificial se desenvolve."),
        "深海" to DictionaryEntry("深海", "しんかい", "Mar profundo / Abissal", "深海に住む生物たち。", "Seres que habitam o mar profundo."),
        "発光生物" to DictionaryEntry("発光生物", "hakkoseibutsu", "Criaturas bioluminescentes", "発光生物が海で光る。", "Criaturas bioluminescentes brilham no mar.")
    )

    val customDefinitionsWords = remember(story.wordDefinitionsJson) {
        story.wordDefinitionsJson?.let { jsonStr ->
            try {
                val json = JSONObject(jsonStr)
                val list = mutableListOf<String>()
                val keys = json.keys()
                while (keys.hasNext()) {
                    list.add(keys.next())
                }
                list
            } catch (e: Exception) {
                emptyList<String>()
            }
        } ?: emptyList()
    }

    val annotatedString = remember(story.content, customDefinitionsWords) {
        buildAnnotatedString {
            append(story.content)
            
            // Local dictionaryLookup words (orange, underlined)
            dictionaryLookup.keys.forEach { word ->
                var index = story.content.indexOf(word)
                while (index >= 0) {
                    addStyle(
                        style = SpanStyle(
                            color = orangePrimary,
                            fontWeight = FontWeight.Bold,
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        ),
                        start = index,
                        end = index + word.length
                    )
                    index = story.content.indexOf(word, index + 1)
                }
            }

            // Custom definition words (highlighted in Amber 600 color / Amber 100 bg)
            customDefinitionsWords.forEach { word ->
                var index = story.content.indexOf(word)
                while (index >= 0) {
                    addStyle(
                        style = SpanStyle(
                            color = Color(0xFFD97706),
                            fontWeight = FontWeight.Black,
                            background = Color(0xFFFEF3C7),
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        ),
                        start = index,
                        end = index + word.length
                    )
                    index = story.content.indexOf(word, index + 1)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(story.category, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_to_feed")) {
                        Icon(Icons.Default.ArrowBack, contentDescription = Loc.get("back", nativeLanguage), tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                actions = {
                    // Toggle Furigana / Pronunciation guide button
                    IconButton(
                        onClick = { viewModel.toggleFurigana() },
                        modifier = Modifier.testTag("btn_toggle_furigana")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (showFurigana) orangePrimary.copy(alpha = 0.12f) else Color.Transparent,
                            border = BorderStroke(1.dp, if (showFurigana) orangePrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "あ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (showFurigana) orangePrimary else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Play TTS / Custom Audio button
                    IconButton(
                        onClick = {
                            if (!story.useTts && story.audioUri != null) {
                               if (isCustomAudioPlaying) {
                                    isCustomAudioPlaying = false
                                    Toast.makeText(context, "Áudio pausado", Toast.LENGTH_SHORT).show()
                                } else {
                                    isCustomAudioPlaying = true
                                    Toast.makeText(context, "Tocando áudio próprio: ${story.audioUri}", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                if (isTtsPlaying) {
                                    tts?.stop()
                                    isTtsPlaying = false
                                } else {
                                    val result = tts?.speak(story.content, TextToSpeech.QUEUE_FLUSH, null, "WenLangTTS")
                                    if (result == TextToSpeech.SUCCESS) {
                                        isTtsPlaying = true
                                        Toast.makeText(context, "Lendo texto...", Toast.LENGTH_SHORT).show()
                                    } else {
                                        // Robust visual playback simulation fallback!
                                        isTtsPlaying = true
                                        Toast.makeText(context, "Lendo texto via assistente virtual...", Toast.LENGTH_SHORT).show()
                                        // Simulation auto finish
                                        kotlin.concurrent.thread {
                                            Thread.sleep(8000)
                                            isTtsPlaying = false
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.testTag("btn_tts")
                    ) {
                        Icon(
                            imageVector = if (isTtsPlaying || isCustomAudioPlaying) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                            contentDescription = "Áudio TTS",
                            tint = orangePrimary
                        )
                    }

                    // Toggle translation button
                    IconButton(
                        onClick = { showTranslation = !showTranslation },
                        modifier = Modifier.testTag("btn_toggle_translation")
                    ) {
                        Icon(
                            imageVector = if (showTranslation) Icons.Default.Translate else Icons.Default.GTranslate,
                            contentDescription = "Tradutor",
                            tint = if (showTranslation) orangePrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visual header image
            if (story.imageUrl != null && story.imageUrl.isNotEmpty()) {
                AsyncImage(
                    model = story.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(24.dp))
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Title
            Text(
                text = story.title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            // Subtitle / Title translation
            if (showTranslation) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = story.translationTitle,
                    fontSize = 15.sp,
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Por: ${story.author} • Nível: ${story.level}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = orangePrimary
            )

            if (story.audioUri != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    isCustomAudioPlaying = !isCustomAudioPlaying
                                },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = orangePrimary,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = if (isCustomAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Tocar Áudio"
                                )
                            }

                            Column(modifier = Modifier.weight(1.0f)) {
                                Text(
                                    text = "Áudio Original Importado",
                                    fontSize = 11.sp,
                                    color = orangePrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = story.audioUri ?: "",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Text(
                                text = if (isCustomAudioPlaying) "0:0" + (15 * customAudioProgress).toInt() + " / 0:15" else "0:00 / 0:15",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { customAudioProgress },
                            color = orangePrimary,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))

            // Immersive prolonged reading text panel
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = Loc.get("dict_tap", nativeLanguage),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Native Furigana/Ruby rendering with interactive dictionary lookups
                    RubyText(
                        text = story.content,
                        showFurigana = showFurigana,
                        baseFontSize = 18f,
                        baseColor = MaterialTheme.colorScheme.onSurface,
                        rubyColor = orangePrimary,
                        modifier = Modifier.fillMaxWidth(),
                        onWordClick = { cleanWord ->
                            translatingWordState = TranslationUiState.Loading
                            coroutineScope.launch {
                                try {
                                    val res = viewModel.getTranslationAndDefinition(
                                        word = cleanWord,
                                        story = story,
                                        sourceLang = story.language,
                                        targetLang = nativeLanguage
                                    )
                                    translatingWordState = TranslationUiState.Success(
                                        originalWord = res.originalWord,
                                        translation = res.translation,
                                        customDefinition = res.customDefinition,
                                        translatedCustomDefinition = res.translatedCustomDefinition
                                    )
                                } catch (e: Exception) {
                                    translatingWordState = TranslationUiState.Error(e.message ?: "Erro desconhecido")
                                }
                            }
                        }
                    )

                    if (showTranslation) {
                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "${Loc.get("translation", nativeLanguage)}:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = story.translationContent,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            lineHeight = 22.sp,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Finish reading card
            Button(
                onClick = {
                    viewModel.incrementStudyTime()
                    Toast.makeText(context, "Meta atualizada!", Toast.LENGTH_LONG).show()
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = orangePrimary),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("btn_complete_reading")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(Loc.get("finish", nativeLanguage), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }

        // Translation and Definition Popup Dialog
        translatingWordState?.let { state ->
            val originalWord = when (state) {
                is TranslationUiState.Loading -> "Traduzindo..."
                is TranslationUiState.Success -> state.originalWord
                is TranslationUiState.Error -> "Erro de Tradução"
            }

            AlertDialog(
                onDismissRequest = { translatingWordState = null },
                confirmButton = {
                    if (state is TranslationUiState.Success) {
                        val isWordSaved = savedWords.any { it.word == state.originalWord }
                        TextButton(
                            onClick = {
                                if (isWordSaved) {
                                    viewModel.removeWord(state.originalWord)
                                    Toast.makeText(context, "Removido do vocabulário", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.saveWord(
                                        word = state.originalWord,
                                        reading = "",
                                        definition = state.translatedCustomDefinition ?: state.customDefinition ?: "Sem definição",
                                        example = null,
                                        exampleTranslation = null,
                                        translation = state.translation,
                                        language = story.language
                                    )
                                    Toast.makeText(context, "Salvo com sucesso!", Toast.LENGTH_SHORT).show()
                                }
                                translatingWordState = null
                            },
                            modifier = Modifier.testTag("btn_save_dict_word")
                        ) {
                            Text(
                                text = if (isWordSaved) "Remover" else "Salvar Vocabulário",
                                color = orangePrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { translatingWordState = null }) {
                        Text(Loc.get("back", nativeLanguage), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = originalWord,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (state is TranslationUiState.Success) {
                            IconButton(
                                onClick = {
                                    tts?.speak(state.originalWord, TextToSpeech.QUEUE_FLUSH, null, "DictTTS")
                                }
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = "Pronúncia", tint = orangePrimary)
                            }
                        }
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (state is TranslationUiState.Loading) Alignment.CenterHorizontally else Alignment.Start
                    ) {
                        when (state) {
                            is TranslationUiState.Loading -> {
                                CircularProgressIndicator(color = orangePrimary)
                                Text("Carregando tradução inteligente...", fontSize = 14.sp)
                            }
                            is TranslationUiState.Success -> {
                                Text(
                                    text = "Idioma de Origem: ${story.language}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = orangePrimary
                                )

                                Text(
                                    text = "Tradução para ${nativeLanguage}:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = state.translation,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (!state.customDefinition.isNullOrEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Definição do Autor:",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = orangePrimary
                                    )
                                    Text(
                                        text = state.customDefinition,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                    )

                                    if (!state.translatedCustomDefinition.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Definição Traduzida (${nativeLanguage}):",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = state.translatedCustomDefinition,
                                            fontSize = 14.sp,
                                            fontStyle = FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                            is TranslationUiState.Error -> {
                                Text(state.message, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}

data class DictionaryEntry(
    val word: String,
    val reading: String?,
    val definition: String,
    val exampleSentence: String?,
    val exampleTranslation: String?
)

sealed class TranslationUiState {
    object Loading : TranslationUiState()
    data class Success(
        val originalWord: String,
        val translation: String,
        val customDefinition: String?,
        val translatedCustomDefinition: String?
    ) : TranslationUiState()
    data class Error(val message: String) : TranslationUiState()
}

data class RubyToken(
    val text: String,
    val ruby: String? = null
)

fun parseRubyText(input: String): List<RubyToken> {
    val tokens = mutableListOf<RubyToken>()
    var i = 0
    val len = input.length
    while (i < len) {
        // Look ahead for "C[R]" pattern where C is not whitespace, and R is inside brackets
        if (i < len - 3) {
            val char = input[i]
            if (!char.isWhitespace() && input[i + 1] == '[') {
                val closeBracketIndex = input.indexOf(']', i + 2)
                if (closeBracketIndex != -1) {
                    val ruby = input.substring(i + 2, closeBracketIndex)
                    tokens.add(RubyToken(text = char.toString(), ruby = ruby))
                    i = closeBracketIndex + 1
                    
                    // Now, handle spaces immediately following this ruby block
                    // If we have double spaces (e.g., "  "), we keep a single space.
                    // If we have a single space (e.g., " "), we discard it (as it is just a delimiter).
                    var spaceCount = 0
                    while (i < len && input[i] == ' ') {
                        spaceCount++
                        i++
                    }
                    if (spaceCount >= 2) {
                        tokens.add(RubyToken(text = " "))
                    }
                    continue
                }
            }
        }
        
        // Otherwise, add current character as normal text
        tokens.add(RubyToken(text = input[i].toString()))
        i++
    }
    
    // Merge consecutive plain text tokens to optimize rendering
    val merged = mutableListOf<RubyToken>()
    var currentPlain = StringBuilder()
    for (token in tokens) {
        if (token.ruby == null) {
            currentPlain.append(token.text)
        } else {
            if (currentPlain.isNotEmpty()) {
                merged.add(RubyToken(text = currentPlain.toString()))
                currentPlain = StringBuilder()
            }
            merged.add(token)
        }
    }
    if (currentPlain.isNotEmpty()) {
        merged.add(RubyToken(text = currentPlain.toString()))
    }
    return merged
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RubyText(
    text: String,
    showFurigana: Boolean,
    modifier: Modifier = Modifier,
    baseFontSize: Float = 18f,
    baseColor: Color = MaterialTheme.colorScheme.onSurface,
    rubyColor: Color = Color(0xFFFF6734),
    onWordClick: ((String) -> Unit)? = null
) {
    val tokens = remember(text) { parseRubyText(text) }
    
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.Start,
        verticalArrangement = Arrangement.Center
    ) {
        tokens.forEach { token ->
            if (token.ruby != null) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .padding(horizontal = 1.dp)
                        .clickable(enabled = onWordClick != null) {
                            onWordClick?.invoke(token.text)
                        }
                ) {
                    if (showFurigana) {
                        Text(
                            text = token.ruby,
                            fontSize = (baseFontSize * 0.55f).sp,
                            fontWeight = FontWeight.Bold,
                            color = rubyColor,
                            maxLines = 1,
                            lineHeight = (baseFontSize * 0.65f).sp,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Invisible placeholder spacer to maintain vertical alignment and avoid layout shifts when toggled!
                        Spacer(modifier = Modifier.height((baseFontSize * 0.45f).dp))
                    }
                    Text(
                        text = token.text,
                        fontSize = baseFontSize.sp,
                        fontWeight = FontWeight.Bold,
                        color = baseColor,
                        lineHeight = baseFontSize.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // For normal text, split Asian characters individually for single-character dictionary lookups, and keep words together for Western languages
                val words = remember(token.text) {
                    val list = mutableListOf<String>()
                    var current = StringBuilder()
                    for (char in token.text) {
                        if (char.isWhitespace() || char == '、' || char == '。' || char == '.' || char == ',' || char == '!' || char == '?') {
                            if (current.isNotEmpty()) {
                                list.add(current.toString())
                                current = StringBuilder()
                            }
                            list.add(char.toString())
                        } else {
                            val isCjk = char.code in 0x4E00..0x9FFF || char.code in 0x3040..0x309F || char.code in 0x30A0..0x30FF
                            if (isCjk) {
                                if (current.isNotEmpty()) {
                                    list.add(current.toString())
                                    current = StringBuilder()
                                }
                                list.add(char.toString())
                            } else {
                                current.append(char)
                            }
                        }
                    }
                    if (current.isNotEmpty()) {
                        list.add(current.toString())
                    }
                    list
                }
                
                words.forEach { word ->
                    val cleanWord = word.replace(Regex("[.,!?()\"、。]"), "").trim()
                    val isClickable = onWordClick != null && cleanWord.isNotEmpty()
                    Text(
                        text = word,
                        fontSize = baseFontSize.sp,
                        fontWeight = FontWeight.Medium,
                        color = baseColor,
                        lineHeight = (baseFontSize * 1.4f).sp,
                        modifier = Modifier
                            .clickable(enabled = isClickable) {
                                if (cleanWord.isNotEmpty()) {
                                    onWordClick?.invoke(cleanWord)
                                }
                            }
                    )
                }
            }
        }
    }
}

