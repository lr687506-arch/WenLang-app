package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val isOnboarded: Boolean = false,
    val isAuthCompleted: Boolean = false,
    val appLanguage: String = "Português",
    val nativeLanguage: String = "Português",
    val secondaryLanguage: String? = null,
    val targetLanguage: String = "Japonês",
    val languageLevel: String = "N4",
    val selectedHobbies: String = "", // Comma-separated list of selected hobbies
    val approvedLanguagesForPosting: String = "", // Comma-separated list of approved languages
    val studyTimeMinutes: Int = 12,
    val studyGoalMinutes: Int = 30,
    val streak: Int = 3,
    val bio: String = "",
    val username: String = "Guest",
    val coverPhoto: String? = null,
    val avatarUrl: String? = null
)

@Entity(tableName = "text_items")
data class TextItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val translationTitle: String,
    val content: String,
    val translationContent: String,
    val level: String, // N5, N4, N3, etc.
    val language: String, // e.g. "Japonês", "Inglês"
    val category: String, // e.g. "Tecnologia", "Arquitetura", "Biologia", "Cultura", "Folclore"
    val tags: String, // Comma-separated tags
    val imageUrl: String? = null,
    val author: String = "WenLang",
    val isPublished: Boolean = false, // True if user published this post
    val timestamp: Long = System.currentTimeMillis(),
    val audioUri: String? = null,
    val useTts: Boolean = true,
    val wordDefinitionsJson: String? = null // Map of word -> custom author definition in JSON format
)

@Entity(tableName = "saved_words")
data class SavedWord(
    @PrimaryKey val word: String,
    val reading: String? = null,
    val definition: String,
    val exampleSentence: String? = null,
    val exampleTranslation: String? = null,
    val language: String = "Japonês",
    val translation: String? = null,
    val savedDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "translation_cache")
data class TranslationCache(
    @PrimaryKey val cacheKey: String, // format: "originalWord|sourceLanguage|targetLanguage"
    val originalWord: String,
    val sourceLanguage: String,
    val targetLanguage: String,
    val translatedWord: String,
    val translatedDefinition: String? = null,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val lastUsedTimestamp: Long = System.currentTimeMillis()
)
