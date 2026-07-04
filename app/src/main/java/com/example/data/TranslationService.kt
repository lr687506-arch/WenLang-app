package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object TranslationService {
    private const val TAG = "TranslationService"

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    private var repository: AppRepository? = null

    // API Key fallback logic to support both build configuration and hardcoded key securely
    private val apiKey: String
        get() {
            val buildConfigKey = try {
                BuildConfig.DEEPL_API_KEY
            } catch (e: Throwable) {
                ""
            }
            return if (buildConfigKey.isNotBlank() && buildConfigKey != "DEEPL_API_KEY_PLACEHOLDER") {
                buildConfigKey
            } else {
                "c729f8d7-d58c-4bd9-a2e1-8b613de9725f:fx"
            }
        }

    fun initialize(repo: AppRepository) {
        this.repository = repo
    }

    /**
     * Maps user-facing language name or code to a standard DeepL target language.
     * DeepL expects uppercase target language codes.
     */
    fun mapToDeepLTargetLanguage(language: String): String {
        return when (language.trim().lowercase()) {
            "japonês", "japanese", "ja", "日本語" -> "JA"
            "português", "portuguese", "pt", "português (brasil)", "pt-br" -> "PT-BR"
            "português (portugal)", "pt-pt" -> "PT-PT"
            "alemão", "german", "de", "deutsch" -> "DE"
            "espanhol", "spanish", "es", "español" -> "ES"
            "grego", "greek", "el", "ελληνικά" -> "EL"
            "indonésio", "indonesian", "id", "bahasa indonesia" -> "ID"
            "inglês", "english", "en", "inglês (eua)", "en-us" -> "EN-US"
            "inglês (reino unido)", "en-gb" -> "EN-GB"
            "mandarim", "chinese", "zh", "中文" -> "ZH"
            "russo", "russian", "ru", "русский" -> "RU"
            "francês", "french", "fr", "français" -> "FR"
            "coreano", "korean", "ko", "한국어" -> "KO"
            "italiano", "italian", "it" -> "IT"
            else -> "EN-US" // Default fallback
        }
    }

    /**
     * Maps user-facing language name or code to a standard DeepL source language.
     * Returns null if language is unknown to let DeepL auto-detect.
     */
    fun mapToDeepLSourceLanguage(language: String): String? {
        return when (language.trim().lowercase()) {
            "japonês", "japanese", "ja", "日本語" -> "JA"
            "português", "portuguese", "pt", "português (brasil)", "pt-br" -> "PT"
            "alemão", "german", "de", "deutsch" -> "DE"
            "espanhol", "spanish", "es", "español" -> "ES"
            "grego", "greek", "el", "ελληνικά" -> "EL"
            "indonésio", "indonesian", "id", "bahasa indonesia" -> "ID"
            "inglês", "english", "en" -> "EN"
            "mandarim", "chinese", "zh", "中文" -> "ZH"
            "russo", "russian", "ru", "русский" -> "RU"
            "francês", "french", "fr", "français" -> "FR"
            "coreano", "korean", "ko", "한국어" -> "KO"
            "italiano", "italian", "it" -> "IT"
            else -> null // Auto-detection
        }
    }

    /**
     * Translates a single word using the DeepL API, utilizing the database cache.
     */
    suspend fun translateWord(word: String, sourceLang: String, targetLang: String): String = withContext(Dispatchers.IO) {
        if (word.isBlank()) return@withContext ""

        val cacheKey = "$word|$sourceLang|$targetLang"
        val repo = repository

        if (repo != null) {
            try {
                val cached = repo.getCache(cacheKey)
                if (cached != null) {
                    // Cache hit: update lastUsedTimestamp and return cached translation
                    repo.saveCache(cached.copy(lastUsedTimestamp = System.currentTimeMillis()))
                    return@withContext cached.translatedWord
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking cache for word '$word': ${e.message}", e)
            }
        }

        // Cache miss: perform API call
        val translated = try {
            translateViaDeepL(word, sourceLang, targetLang)
        } catch (e: Exception) {
            Log.e(TAG, "API call failed for word '$word': ${e.message}", e)
            throw e
        }

        if (translated.isNotBlank() && repo != null) {
            try {
                val newCache = TranslationCache(
                    cacheKey = cacheKey,
                    originalWord = word,
                    sourceLanguage = sourceLang,
                    targetLanguage = targetLang,
                    translatedWord = translated,
                    translatedDefinition = null,
                    createdTimestamp = System.currentTimeMillis(),
                    lastUsedTimestamp = System.currentTimeMillis()
                )
                repo.saveCache(newCache)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving cache for word '$word': ${e.message}", e)
            }
        }

        return@withContext translated
    }

    /**
     * Translates a custom definition, utilizing the database cache.
     */
    suspend fun translateDefinition(word: String, definition: String, sourceLang: String, targetLang: String): String = withContext(Dispatchers.IO) {
        if (definition.isBlank()) return@withContext ""

        val cacheKey = "$word|$sourceLang|$targetLang"
        val repo = repository

        if (repo != null) {
            try {
                val cached = repo.getCache(cacheKey)
                if (cached != null && !cached.translatedDefinition.isNullOrEmpty()) {
                    // Cache hit for definition: update lastUsedTimestamp and return
                    repo.saveCache(cached.copy(lastUsedTimestamp = System.currentTimeMillis()))
                    return@withContext cached.translatedDefinition
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking cache for definition of '$word': ${e.message}", e)
            }
        }

        // Cache miss: perform API call
        val translatedDef = try {
            translateViaDeepL(definition, sourceLang, targetLang)
        } catch (e: Exception) {
            Log.e(TAG, "API call failed for definition of '$word': ${e.message}", e)
            throw e
        }

        if (translatedDef.isNotBlank() && repo != null) {
            try {
                val cached = repo.getCache(cacheKey)
                if (cached != null) {
                    val updatedCache = cached.copy(
                        translatedDefinition = translatedDef,
                        lastUsedTimestamp = System.currentTimeMillis()
                    )
                    repo.saveCache(updatedCache)
                } else {
                    val newCache = TranslationCache(
                        cacheKey = cacheKey,
                        originalWord = word,
                        sourceLanguage = sourceLang,
                        targetLanguage = targetLang,
                        translatedWord = "", // empty placeholder until word is translated
                        translatedDefinition = translatedDef,
                        createdTimestamp = System.currentTimeMillis(),
                        lastUsedTimestamp = System.currentTimeMillis()
                    )
                    repo.saveCache(newCache)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error saving cache for definition of '$word': ${e.message}", e)
            }
        }

        return@withContext translatedDef
    }

    /**
     * Generic translate method (for phrases, general text).
     */
    suspend fun translate(text: String, sourceLang: String, targetLang: String): String = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext ""
        
        // Check cache if possible (using text as cacheKey)
        val cacheKey = "$text|$sourceLang|$targetLang"
        val repo = repository
        if (repo != null) {
            try {
                val cached = repo.getCache(cacheKey)
                if (cached != null) {
                    repo.saveCache(cached.copy(lastUsedTimestamp = System.currentTimeMillis()))
                    return@withContext cached.translatedWord
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking cache for text: ${e.message}", e)
            }
        }

        val translated = try {
            translateViaDeepL(text, sourceLang, targetLang)
        } catch (e: Exception) {
            Log.e(TAG, "API call failed for general text: ${e.message}", e)
            throw e
        }

        if (translated.isNotBlank() && repo != null) {
            try {
                val newCache = TranslationCache(
                    cacheKey = cacheKey,
                    originalWord = text,
                    sourceLanguage = sourceLang,
                    targetLanguage = targetLang,
                    translatedWord = translated,
                    translatedDefinition = null,
                    createdTimestamp = System.currentTimeMillis(),
                    lastUsedTimestamp = System.currentTimeMillis()
                )
                repo.saveCache(newCache)
            } catch (e: Exception) {
                Log.e(TAG, "Error saving cache for text: ${e.message}", e)
            }
        }

        return@withContext translated
    }

    private fun logD(tag: String, msg: String) {
        try {
            Log.d(tag, msg)
        } catch (e: Throwable) {
            println("[$tag] D: $msg")
        }
    }

    private fun logE(tag: String, msg: String, tr: Throwable? = null) {
        try {
            Log.e(tag, msg, tr)
        } catch (e: Throwable) {
            System.err.println("[$tag] E: $msg")
            tr?.printStackTrace()
        }
    }

    fun testDeepLConnection(): String {
        val resultBuilder = java.lang.StringBuilder()
        
        val text = "Hello"
        val sourceLang = "EN"
        val targetLang = "PT-BR"
        
        val targetCode = mapToDeepLTargetLanguage(targetLang)
        val sourceCode = mapToDeepLSourceLanguage(sourceLang)

        val key = apiKey
        val isFreeKey = key.endsWith(":fx")
        val url = if (isFreeKey) "https://api-free.deepl.com/v2/translate" else "https://api.deepl.com/v2/translate"

        val json = JSONObject().apply {
            put("text", JSONArray().apply { put(text) })
            put("target_lang", targetCode)
            if (sourceCode != null) {
                put("source_lang", sourceCode)
            }
        }

        val requestBodyString = json.toString()
        val body = requestBodyString.toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "DeepL-Auth-Key $key")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        val obscuredKey = if (key.length > 4) "..." + key.substring(key.length - 4) else key
        val requestHeadersLog = "Authorization: DeepL-Auth-Key $obscuredKey\nContent-Type: application/json"

        resultBuilder.append("URL utilizada: $url\n")
        resultBuilder.append("Headers enviados:\n$requestHeadersLog\n")
        resultBuilder.append("Body enviado: $requestBodyString\n")

        try {
            client.newCall(request).execute().use { response ->
                val statusCode = response.code
                val bodyString = response.body?.string() ?: ""

                resultBuilder.append("Status Code: $statusCode\n")
                resultBuilder.append("Response Body: $bodyString\n")
                
                if (response.isSuccessful) {
                    val jsonResponse = JSONObject(bodyString)
                    if (jsonResponse.has("translations")) {
                        val translationsArray = jsonResponse.getJSONArray("translations")
                        if (translationsArray.length() > 0) {
                            val translatedText = translationsArray.getJSONObject(0).getString("text")
                            resultBuilder.append("Tradução com sucesso: $translatedText\n")
                        }
                    }
                } else {
                    resultBuilder.append("Erro na requisição. Código de status mal sucedido.\n")
                }
            }
        } catch (e: Exception) {
            resultBuilder.append("Exception completa: ${e.javaClass.name}: ${e.message}\n")
            val sw = java.io.StringWriter()
            val pw = java.io.PrintWriter(sw)
            e.printStackTrace(pw)
            resultBuilder.append("StackTrace completo:\n$sw\n")
        }

        return resultBuilder.toString()
    }

    /**
     * Calls the official DeepL API.
     */
    private fun translateViaDeepL(text: String, sourceLang: String, targetLang: String): String {
        val targetCode = mapToDeepLTargetLanguage(targetLang)
        val sourceCode = mapToDeepLSourceLanguage(sourceLang)

        val key = apiKey
        logD(TAG, "API KEY LENGTH = ${key.length}")
        logD(TAG, "API KEY LAST = ${key.takeLast(4)}")
        val isFreeKey = key.endsWith(":fx")
        val url = if (isFreeKey) "https://api-free.deepl.com/v2/translate" else "https://api.deepl.com/v2/translate"

        val json = JSONObject().apply {
            put("text", JSONArray().apply { put(text) })
            put("target_lang", targetCode)
            if (sourceCode != null) {
                put("source_lang", sourceCode)
            }
        }

        val requestBodyString = json.toString()
        val body = requestBodyString.toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "DeepL-Auth-Key $key")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        val obscuredKey = if (key.length > 4) "..." + key.substring(key.length - 4) else key
        val requestHeadersLog = "Authorization: DeepL-Auth-Key $obscuredKey\nContent-Type: application/json"

        logD(TAG, "=== DEEPL REQUEST ===")
        logD(TAG, "URL: $url")
        logD(TAG, "Method: POST")
        logD(TAG, "Headers:\n$requestHeadersLog")
        logD(TAG, "Body: $requestBodyString")

        try {
            client.newCall(request).execute().use { response ->
                val statusCode = response.code
                val bodyString = response.body?.string() ?: ""

                logD(TAG, "=== DEEPL RESPONSE ===")
                logD(TAG, "Status Code: $statusCode")
                logD(TAG, "Response Body: $bodyString")

                if (!response.isSuccessful) {
                    val detailedError = "DeepL API returned unsuccessful code: $statusCode\nResponse Body: $bodyString\nURL: $url\nBody: $requestBodyString"
                    logE(TAG, detailedError)
                    throw Exception(detailedError)
                }

                val jsonResponse = JSONObject(bodyString)
                if (jsonResponse.has("translations")) {
                    val translationsArray = jsonResponse.getJSONArray("translations")
                    if (translationsArray.length() > 0) {
                        return translationsArray.getJSONObject(0).getString("text")
                    }
                }
                return ""
            }
        } catch (e: Exception) {
            logE(TAG, "=== DEEPL ERROR ===")
            logE(TAG, "URL: $url")
            logE(TAG, "Method: POST")
            logE(TAG, "Headers:\n$requestHeadersLog")
            logE(TAG, "Body: $requestBodyString")
            logE(TAG, "Exception message: ${e.message}", e)
            throw e
        }
    }
}
