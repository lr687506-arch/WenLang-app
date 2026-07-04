package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.SavedWord
import com.example.data.TextItem
import com.example.data.UserProfile
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONObject

class LangViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppRepository

    val userProfile: StateFlow<UserProfile?>
    val allTexts: StateFlow<List<TextItem>>
    val savedWords: StateFlow<List<SavedWord>>

    // Quiz State
    private val _quizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val quizQuestions: StateFlow<List<QuizQuestion>> = _quizQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _selectedAnswerIndex = MutableStateFlow<Int?>(null)
    val selectedAnswerIndex: StateFlow<Int?> = _selectedAnswerIndex.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleDarkTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    private val _correctAnswersCount = MutableStateFlow(0)
    val correctAnswersCount: StateFlow<Int> = _correctAnswersCount.asStateFlow()

    private val _quizFinished = MutableStateFlow(false)
    val quizFinished: StateFlow<Boolean> = _quizFinished.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = AppRepository(database)
        com.example.data.TranslationService.initialize(repository)

        userProfile = repository.userProfileFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        allTexts = repository.allTextsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        savedWords = repository.allSavedWordsFlow.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.populateDefaultTextsIfNeeded()
            // If profile is empty, create a default unsaved one
            val currentProfile = repository.getProfile()
            if (currentProfile == null) {
                repository.saveProfile(UserProfile())
            }
            // Collect userProfile to update quiz questions automatically when studied language changes
            userProfile.collect { profile ->
                val targetLang = profile?.targetLanguage ?: "Japonês"
                generateQuestionsForLanguage(targetLang)
            }
        }
    }

    fun completeOnboarding(
        appLang: String,
        nativeLang: String,
        secondaryLang: String?,
        targetLang: String,
        level: String,
        hobbies: List<String>
    ) {
        viewModelScope.launch {
            val hobbiesString = hobbies.joinToString(",")
            val current = repository.getProfile() ?: UserProfile()
            val updated = current.copy(
                isOnboarded = true,
                appLanguage = appLang,
                nativeLanguage = nativeLang,
                secondaryLanguage = secondaryLang,
                targetLanguage = targetLang,
                languageLevel = level,
                selectedHobbies = hobbiesString
            )
            repository.saveProfile(updated)
        }
    }

    fun resetOnboarding() {
        viewModelScope.launch {
            val current = repository.getProfile() ?: UserProfile()
            val reset = current.copy(
                isOnboarded = false,
                approvedLanguagesForPosting = "" // Lock posting again
            )
            repository.saveProfile(reset)
        }
    }

    private val _isTranslating = MutableStateFlow(false)
    val isTranslating: StateFlow<Boolean> = _isTranslating.asStateFlow()

    data class TranslationUiResult(
        val originalWord: String,
        val translation: String,
        val customDefinition: String? = null,
        val translatedCustomDefinition: String? = null
    )

    suspend fun getTranslationAndDefinition(
        word: String,
        story: TextItem,
        sourceLang: String,
        targetLang: String
    ): TranslationUiResult {
        _isTranslating.value = true
        try {
            // Get custom definition from story if available
            val customDef = story.wordDefinitionsJson?.let { jsonStr ->
                try {
                    val json = JSONObject(jsonStr)
                    if (json.has(word)) json.getString(word) else null
                } catch (e: Exception) {
                    null
                }
            }

            // Translate word using TranslationService (which handles cache internally)
            val translatedWord = com.example.data.TranslationService.translateWord(word, sourceLang, targetLang)

            // Translate definition using TranslationService if available (also cached internally)
            var translatedDef: String? = null
            if (customDef != null) {
                try {
                    translatedDef = com.example.data.TranslationService.translateDefinition(word, customDef, sourceLang, targetLang)
                } catch (e: Exception) {
                    Log.e("LangViewModel", "Failed to translate custom definition: ${e.message}")
                    // Don't fail the whole word translation if only the author's definition translation failed
                }
            }

            _isTranslating.value = false
            return TranslationUiResult(
                originalWord = word,
                translation = translatedWord,
                customDefinition = customDef,
                translatedCustomDefinition = translatedDef
            )
        } catch (e: Exception) {
            _isTranslating.value = false

            // Fallback: search database cache for any partial match
            val cached = repository.getCache("$word|$sourceLang|$targetLang")
            val customDef = story.wordDefinitionsJson?.let { jsonStr ->
                try {
                    val json = JSONObject(jsonStr)
                    if (json.has(word)) json.getString(word) else null
                } catch (ex: Exception) {
                    null
                }
            }

            if (cached != null) {
                return TranslationUiResult(
                    originalWord = word,
                    translation = cached.translatedWord,
                    customDefinition = customDef,
                    translatedCustomDefinition = cached.translatedDefinition
                )
            }

            // Rethrow user-friendly exception if no cache exists to display a friendly message in the UI
            throw Exception(e.message ?: "Falha ao traduzir. Verifique sua conexão com a internet.")
        }
    }

    fun saveWord(word: String, reading: String?, definition: String, example: String?, exampleTranslation: String?, translation: String? = null, language: String? = null) {
        viewModelScope.launch {
            val saved = SavedWord(
                word = word,
                reading = reading,
                definition = definition,
                exampleSentence = example,
                exampleTranslation = exampleTranslation,
                language = language ?: userProfile.value?.targetLanguage ?: "Japonês",
                translation = translation,
                savedDate = System.currentTimeMillis()
            )
            repository.saveWord(saved)
        }
    }

    fun removeWord(word: String) {
        viewModelScope.launch {
            repository.deleteWord(word)
        }
    }

    fun publishStory(
        title: String,
        content: String,
        translationTitle: String,
        translationContent: String,
        category: String,
        tags: String,
        level: String,
        language: String? = null,
        audioUri: String? = null,
        useTts: Boolean = true,
        wordDefinitions: Map<String, String>? = null
    ) {
        viewModelScope.launch {
            val definitionsJson = wordDefinitions?.let { JSONObject(it).toString() }
            val textItem = TextItem(
                title = title,
                translationTitle = translationTitle,
                content = content,
                translationContent = translationContent,
                level = level,
                language = language ?: userProfile.value?.targetLanguage ?: "Japonês",
                category = category,
                tags = tags,
                isPublished = true,
                audioUri = audioUri,
                useTts = useTts,
                wordDefinitionsJson = definitionsJson
            )
            repository.insertText(textItem)
        }
    }

    fun updateBio(bio: String) {
        viewModelScope.launch {
            val current = repository.getProfile()
            if (current != null) {
                val updated = current.copy(bio = bio)
                repository.saveProfile(updated)
            }
        }
    }

    fun addHobby(hobby: String) {
        viewModelScope.launch {
            val current = repository.getProfile()
            if (current != null) {
                val list = current.selectedHobbies.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                if (!list.contains(hobby)) {
                    list.add(hobby)
                    val updated = current.copy(selectedHobbies = list.joinToString(","))
                    repository.saveProfile(updated)
                }
            }
        }
    }

    fun removeHobby(hobby: String) {
        viewModelScope.launch {
            val current = repository.getProfile()
            if (current != null) {
                val list = current.selectedHobbies.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                if (list.contains(hobby)) {
                    list.remove(hobby)
                    val updated = current.copy(selectedHobbies = list.joinToString(","))
                    repository.saveProfile(updated)
                }
            }
        }
    }

    fun deleteStory(id: Int) {
        viewModelScope.launch {
            repository.deleteTextById(id)
        }
    }

    // --- Quiz Logic ---
    fun generateQuestions() {
        val target = userProfile.value?.targetLanguage ?: "Japonês"
        generateQuestionsForLanguage(target)
    }

    private fun generateQuestionsForLanguage(targetLang: String) {
        val questions = when (targetLang) {
            "Mandarim" -> listOf(
                QuizQuestion(
                    id = 1,
                    question = "O que significa '你好' (Nǐ hǎo)?",
                    options = listOf("Bom dia", "Boa noite", "Olá", "Obrigado"),
                    correctIndex = 2
                ),
                QuizQuestion(
                    id = 2,
                    question = "Como se diz 'Obrigado' em mandarim?",
                    options = listOf("再见 (Zàijiàn)", "谢谢 (Xièxiè)", "对不起 (Duìbùqǐ)", "没关系 (Méiguānxi)"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = 3,
                    question = "Qual destas palavras significa 'China'?",
                    options = listOf("日本 (Rìběn)", "美国 (Měiguó)", "中国 (Zhōngguó)", "英国 (Yīngguó)"),
                    correctIndex = 2
                ),
                QuizQuestion(
                    id = 4,
                    question = "O que significa '再见' (Zàijiàn)?",
                    options = listOf("Olá", "Tchau / Adeus", "Desculpe", "Por favor"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = 5,
                    question = "Qual destas palavras significa 'Gato'?",
                    options = listOf("猫 (Māo)", "狗 (Gǒu)", "鸟 (Niǎo)", "鱼 (Yú)"),
                    correctIndex = 0
                )
            )
            "Inglês" -> listOf(
                QuizQuestion(
                    id = 1,
                    question = "O que significa 'Hello'?",
                    options = listOf("Tchau", "Obrigado", "Olá", "Por favor"),
                    correctIndex = 2
                ),
                QuizQuestion(
                    id = 2,
                    question = "Como se diz 'Obrigado' em inglês?",
                    options = listOf("Please", "Thank you", "Sorry", "Welcome"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = 3,
                    question = "Qual destas palavras significa 'Livro'?",
                    options = listOf("Book", "House", "Car", "Dog"),
                    correctIndex = 0
                ),
                QuizQuestion(
                    id = 4,
                    question = "O que significa 'Goodbye'?",
                    options = listOf("Olá", "Adeus / Tchau", "Por favor", "Desculpe"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = 5,
                    question = "Qual destas palavras significa 'Gato'?",
                    options = listOf("Dog", "Cat", "Bird", "Fish"),
                    correctIndex = 1
                )
            )
            "Espanhol" -> listOf(
                QuizQuestion(
                    id = 1,
                    question = "O que significa 'Hola'?",
                    options = listOf("Tchau", "Olá", "Obrigado", "Por favor"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = 2,
                    question = "Como se diz 'Obrigado' em espanhol?",
                    options = listOf("Gracias", "Por favor", "De nada", "Lo siento"),
                    correctIndex = 0
                ),
                QuizQuestion(
                    id = 3,
                    question = "Qual destas palavras significa 'Livro'?",
                    options = listOf("Libro", "Casa", "Coche", "Perro"),
                    correctIndex = 0
                ),
                QuizQuestion(
                    id = 4,
                    question = "O que significa 'Adiós'?",
                    options = listOf("Olá", "Adeus / Tchau", "Por favor", "Desculpe"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = 5,
                    question = "Qual destas palavras significa 'Gato'?",
                    options = listOf("Perro", "Gato", "Pájaro", "Pez"),
                    correctIndex = 1
                )
            )
            "Alemão" -> listOf(
                QuizQuestion(
                    id = 1,
                    question = "O que significa 'Hallo'?",
                    options = listOf("Tchau", "Obrigado", "Olá", "Por favor"),
                    correctIndex = 2
                ),
                QuizQuestion(
                    id = 2,
                    question = "Como se diz 'Obrigado' em alemão?",
                    options = listOf("Bitte", "Danke", "Entschuldigung", "Guten Tag"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = 3,
                    question = "Qual destas palavras significa 'Livro'?",
                    options = listOf("Buch", "Haus", "Auto", "Hund"),
                    correctIndex = 0
                ),
                QuizQuestion(
                    id = 4,
                    question = "O que significa 'Tschüss'?",
                    options = listOf("Olá", "Adeus / Tchau", "Por favor", "Desculpe"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = 5,
                    question = "Qual destas palavras significa 'Gato'?",
                    options = listOf("Hund", "Katze", "Vogel", "Fisch"),
                    correctIndex = 1
                )
            )
            "Português" -> listOf(
                QuizQuestion(
                    id = 1,
                    question = "O que significa 'Olá'?",
                    options = listOf("Tchau", "Obrigado", "Olá", "Por favor"),
                    correctIndex = 2
                ),
                QuizQuestion(
                    id = 2,
                    question = "Como se diz 'Obrigado' em português?",
                    options = listOf("Por favor", "Obrigado", "Desculpe", "De nada"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = 3,
                    question = "Qual destas palavras significa 'Livro'?",
                    options = listOf("Livro", "Casa", "Carro", "Cachorro"),
                    correctIndex = 0
                ),
                QuizQuestion(
                    id = 4,
                    question = "O que significa 'Adeus'?",
                    options = listOf("Olá", "Adeus / Tchau", "Por favor", "Desculpe"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = 5,
                    question = "Qual destas palavras significa 'Gato'?",
                    options = listOf("Cachorro", "Gato", "Pássaro", "Peixe"),
                    correctIndex = 1
                )
            )
            "Russo" -> listOf(
                QuizQuestion(
                    id = 1,
                    question = "O que significa 'Привет' (Privet)?",
                    options = listOf("Tchau", "Obrigado", "Olá", "Por favor"),
                    correctIndex = 2
                ),
                QuizQuestion(
                    id = 2,
                    question = "Como se diz 'Obrigado' em russo?",
                    options = listOf("Пожалуйста (Pozhaluysta)", "Спасибо (Spasibo)", "Извините (Izvinite)", "Привет (Privet)"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = 3,
                    question = "Qual destas palavras significa 'Livro'?",
                    options = listOf("Книга (Kniga)", "Дом (Dom)", "Машина (Mashina)", "Собака (Sobaka)"),
                    correctIndex = 0
                ),
                QuizQuestion(
                    id = 4,
                    question = "O que significa 'Пока' (Poka)?",
                    options = listOf("Olá", "Adeus / Tchau", "Por favor", "Desculpe"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = 5,
                    question = "Qual destas palavras significa 'Gato'?",
                    options = listOf("Собака (Sobaka)", "Кошка (Koshka)", "Птица (Ptitsa)", "Рыба (Ryba)"),
                    correctIndex = 1
                )
            )
            "Grego" -> listOf(
                QuizQuestion(
                    id = 1,
                    question = "O que significa 'Γεια σου' (Geia sou)?",
                    options = listOf("Tchau", "Obrigado", "Olá", "Por favor"),
                    correctIndex = 2
                ),
                QuizQuestion(
                    id = 2,
                    question = "Como se diz 'Obrigado' em grego?",
                    options = listOf("Παρακαλώ (Parakalo)", "Ευχαριστώ (Efcharisto)", "Συγγνώμη (Sygnomi)", "Γεια (Geia)"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = 3,
                    question = "Qual destas palavras significa 'Livro'?",
                    options = listOf("Βιβλίο (Vivlio)", "Σπίτι (Spiti)", "Αυτοκίνητο (Aftokinito)", "Σκύλος (Skylos)"),
                    correctIndex = 0
                ),
                QuizQuestion(
                    id = 4,
                    question = "O que significa 'Αντίο' (Antio)?",
                    options = listOf("Olá", "Adeus / Tchau", "Por favor", "Desculpe"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = 5,
                    question = "Qual destas palavras significa 'Gato'?",
                    options = listOf("Σκύλος (Skylos)", "Γάτα (Gata)", "Πουλί (Pouli)", "Ψάρι (Psari)"),
                    correctIndex = 1
                )
            )
            "Hebraico" -> listOf(
                QuizQuestion(
                    id = 1,
                    question = "O que significa 'שלום' (Shalom)?",
                    options = listOf("Tchau", "Obrigado", "Olá / Paz / Adeus", "Por favor"),
                    correctIndex = 2
                ),
                QuizQuestion(
                    id = 2,
                    question = "Como se diz 'Obrigado' em hebraico?",
                    options = listOf("בבקשה (Bevakasha)", "תודה (Toda)", "סליחה (Slicha)", "שלום (Shalom)"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = 3,
                    question = "Qual destas palavras significa 'Livro'?",
                    options = listOf("ספר (Sefer)", "בית (Bayit)", "מכונית (Mechonit)", "כלב (Kelev)"),
                    correctIndex = 0
                ),
                QuizQuestion(
                    id = 4,
                    question = "O que significa 'להתראות' (Lehitra'ot)?",
                    options = listOf("Olá", "Adeus / Até logo", "Por favor", "Desculpe"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = 5,
                    question = "Qual destas palavras significa 'Gato'?",
                    options = listOf("כלב (Kelev)", "חתুল (Chatul)", "ציפור (Tzipor)", "דag (Dag)"),
                    correctIndex = 1
                )
            )
            "Indonésio" -> listOf(
                QuizQuestion(
                    id = 1,
                    question = "O que significa 'Halo'?",
                    options = listOf("Tchau", "Obrigado", "Olá", "Por favor"),
                    correctIndex = 2
                ),
                QuizQuestion(
                    id = 2,
                    question = "Como se diz 'Obrigado' em indonésio?",
                    options = listOf("Sama-sama", "Terima kasih", "Maaf", "Silakan"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = 3,
                    question = "Qual destas palavras significa 'Buku'?",
                    options = listOf("Livro", "Casa", "Carro", "Cachorro"),
                    correctIndex = 0
                ),
                QuizQuestion(
                    id = 4,
                    question = "O que significa 'Selamat tinggal'?",
                    options = listOf("Olá", "Adeus / Tchau", "Por favor", "Desculpe"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = 5,
                    question = "Qual destas palavras significa 'Kucing'?",
                    options = listOf("Cachorro", "Gato", "Pássaro", "Peixe"),
                    correctIndex = 1
                )
            )
            else -> listOf( // "Japonês" default
                QuizQuestion(
                    id = 1,
                    question = "O que significa 'こんにちは' (Konnichiwa)?",
                    options = listOf("Bom dia", "Boa noite", "Olá / Boa tarde", "Obrigado"),
                    correctIndex = 2
                ),
                QuizQuestion(
                    id = 2,
                    question = "Qual é o caractere para a vogal 'A' em Hiragana?",
                    options = listOf("あ", "い", "う", "え"),
                    correctIndex = 0
                ),
                QuizQuestion(
                    id = 3,
                    question = "Como se diz 'Obrigado' formalmente em japonês?",
                    options = listOf("ありがとう (Arigatou)", "どういたしまして (Douitashimashite)", "ありがとうございます (Arigatou gozaimasu)", "すみません (Sumimasen)"),
                    correctIndex = 2
                ),
                QuizQuestion(
                    id = 4,
                    question = "O que significa o Kanji '水' (mizu)?",
                    options = listOf("Fogo", "Água", "Vento", "Terra"),
                    correctIndex = 1
                ),
                QuizQuestion(
                    id = 5,
                    question = "Qual o significado de 'ねこ' (neko)?",
                    options = listOf("Cachorro", "Pássaro", "Gato", "Peixe"),
                    correctIndex = 2
                )
            )
        }
        _quizQuestions.value = questions
    }

    fun selectAnswer(optionIndex: Int) {
        _selectedAnswerIndex.value = optionIndex
    }

    fun submitAnswer() {
        val currentIdx = _currentQuestionIndex.value
        val questions = _quizQuestions.value
        val selected = _selectedAnswerIndex.value

        if (currentIdx < questions.size && selected != null) {
            val q = questions[currentIdx]
            if (selected == q.correctIndex) {
                _correctAnswersCount.value += 1
            }

            // Move to next question or finish
            if (currentIdx + 1 < questions.size) {
                _currentQuestionIndex.value += 1
                _selectedAnswerIndex.value = null
            } else {
                _quizFinished.value = true
                checkAndUnlockPosting()
            }
        }
    }

    fun restartQuiz() {
        _currentQuestionIndex.value = 0
        _selectedAnswerIndex.value = null
        _correctAnswersCount.value = 0
        _quizFinished.value = false
    }

    private fun checkAndUnlockPosting() {
        val total = _quizQuestions.value.size
        val correct = _correctAnswersCount.value
        if (correct == total) {
            viewModelScope.launch {
                val current = repository.getProfile()
                if (current != null) {
                    val target = current.targetLanguage
                    val approvedList = current.approvedLanguagesForPosting.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
                    if (!approvedList.contains(target)) {
                        approvedList.add(target)
                    }
                    val updated = current.copy(approvedLanguagesForPosting = approvedList.joinToString(","))
                    repository.saveProfile(updated)
                }
            }
        }
    }

    fun incrementStudyTime() {
        viewModelScope.launch {
            val current = repository.getProfile()
            if (current != null) {
                val updated = current.copy(studyTimeMinutes = current.studyTimeMinutes + 1)
                repository.saveProfile(updated)
            }
        }
    }
}

data class QuizQuestion(
    val id: Int,
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)
