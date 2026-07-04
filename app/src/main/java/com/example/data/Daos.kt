package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)
}

@Dao
interface TextItemDao {
    @Query("SELECT * FROM text_items ORDER BY timestamp DESC")
    fun getAllTextsFlow(): Flow<List<TextItem>>

    @Query("SELECT * FROM text_items WHERE language = :language ORDER BY timestamp DESC")
    fun getTextsByLanguageFlow(language: String): Flow<List<TextItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertText(text: TextItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTexts(texts: List<TextItem>)

    @Query("DELETE FROM text_items WHERE id = :id")
    suspend fun deleteTextById(id: Int)
}

@Dao
interface SavedWordDao {
    @Query("SELECT * FROM saved_words ORDER BY word ASC")
    fun getAllSavedWordsFlow(): Flow<List<SavedWord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: SavedWord)

    @Query("DELETE FROM saved_words WHERE word = :word")
    suspend fun deleteWord(word: String)

    @Query("SELECT * FROM saved_words WHERE word = :word LIMIT 1")
    suspend fun getSavedWord(word: String): SavedWord?
}

@Dao
interface TranslationCacheDao {
    @Query("SELECT * FROM translation_cache WHERE cacheKey = :cacheKey LIMIT 1")
    suspend fun getCache(cacheKey: String): TranslationCache?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCache(cache: TranslationCache)

    @Query("DELETE FROM translation_cache WHERE cacheKey = :cacheKey")
    suspend fun deleteCache(cacheKey: String)

    @Query("DELETE FROM translation_cache")
    suspend fun clearAllCache()
}
