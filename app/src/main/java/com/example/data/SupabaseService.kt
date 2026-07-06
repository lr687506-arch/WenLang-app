package com.example.data

import android.util.Log
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SupabaseService {
    private const val TAG = "SupabaseService"
    private const val SUPABASE_URL = "https://pfkjmhnutwkfzicpclnd.supabase.co"
    private const val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InBma2ptaG51dHdrZnppY3BjbG5kIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODMxNTQxNTYsImV4cCI6MjA5ODczMDE1Nn0.KTCQNfETYTNO5gA84I8Ja9kc0pthFkPW1hqQzXyIh4Y"

    private val client = OkHttpClient()
    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    data class AuthResult(
        val success: Boolean,
        val email: String? = null,
        val userId: String? = null,
        val username: String? = null,
        val token: String? = null,
        val errorMessage: String? = null
    )

    /**
     * Signs up a new user using email, password and custom username.
     */
    suspend fun signUp(email: String, password: String, username: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/auth/v1/signup"
            
            val jsonBody = JSONObject().apply {
                put("email", email)
                put("password", password)
                put("data", JSONObject().apply {
                    put("username", username)
                })
                put("options", JSONObject().apply {
                    put("email_redirect_to", "wenlang://auth-callback")
                })
            }

            val request = Request.Builder()
                .url(url)
                .header("apikey", API_KEY)
                .header("Authorization", "Bearer $API_KEY")
                .header("Content-Type", "application/json")
                .post(jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                val responseStr = response.body?.string() ?: ""
                Log.d(TAG, "SignUp Status: ${response.code}, Body: $responseStr")
                
                if (response.isSuccessful) {
                    val jsonObj = JSONObject(responseStr)
                    val userObj = jsonObj.optJSONObject("user")
                    val userId = userObj?.optString("id") ?: ""
                    val userEmail = userObj?.optString("email") ?: email
                    val userMetadata = userObj?.optJSONObject("user_metadata")
                    val parsedUsername = userMetadata?.optString("username") ?: username
                    val token = jsonObj.optString("access_token", null)

                    // Optional: Try saving to profiles table as well, ignore if fails
                    saveProfileToDb(userId, userEmail, parsedUsername)

                    AuthResult(
                        success = true,
                        email = userEmail,
                        userId = userId,
                        username = parsedUsername,
                        token = token
                    )
                } else {
                    val errorMsg = parseErrorMessage(responseStr)
                    AuthResult(success = false, errorMessage = errorMsg)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "SignUp Exception", e)
            AuthResult(success = false, errorMessage = e.message ?: "Network error. Please try again.")
        }
    }

    /**
     * Resends the confirmation email to a user.
     */
    suspend fun resendConfirmationEmail(email: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/auth/v1/resend"
            
            val jsonBody = JSONObject().apply {
                put("type", "signup")
                put("email", email)
                put("options", JSONObject().apply {
                    put("email_redirect_to", "wenlang://auth-callback")
                })
            }

            val request = Request.Builder()
                .url(url)
                .header("apikey", API_KEY)
                .header("Authorization", "Bearer $API_KEY")
                .header("Content-Type", "application/json")
                .post(jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                val responseStr = response.body?.string() ?: ""
                Log.d(TAG, "Resend Status: ${response.code}, Body: $responseStr")
                
                if (response.isSuccessful) {
                    AuthResult(success = true)
                } else {
                    val errorMsg = parseErrorMessage(responseStr)
                    AuthResult(success = false, errorMessage = errorMsg)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Resend Exception", e)
            AuthResult(success = false, errorMessage = e.message ?: "Network error. Please try again.")
        }
    }

    /**
     * Retrieves user details using a valid access token.
     */
    suspend fun getUserByToken(accessToken: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/auth/v1/user"
            
            val request = Request.Builder()
                .url(url)
                .header("apikey", API_KEY)
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", "application/json")
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                val responseStr = response.body?.string() ?: ""
                Log.d(TAG, "GetUserByToken Status: ${response.code}, Body: $responseStr")

                if (response.isSuccessful) {
                    val jsonObj = JSONObject(responseStr)
                    val userId = jsonObj.optString("id") ?: ""
                    val userEmail = jsonObj.optString("email") ?: ""
                    val userMetadata = jsonObj.optJSONObject("user_metadata")
                    val username = userMetadata?.optString("username") ?: userEmail.substringBefore("@")

                    // Automatically save/upsert the user profile to DB
                    saveProfileToDb(userId, userEmail, username)

                    AuthResult(
                        success = true,
                        email = userEmail,
                        userId = userId,
                        username = username,
                        token = accessToken
                    )
                } else {
                    val errorMsg = parseErrorMessage(responseStr)
                    AuthResult(success = false, errorMessage = errorMsg)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "GetUserByToken Exception", e)
            AuthResult(success = false, errorMessage = e.message ?: "Network error. Please try again.")
        }
    }

    /**
     * Authenticates a user with email and password.
     */
    suspend fun signIn(email: String, password: String): AuthResult = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/auth/v1/token?grant_type=password"
            
            val jsonBody = JSONObject().apply {
                put("email", email)
                put("password", password)
            }

            val request = Request.Builder()
                .url(url)
                .header("apikey", API_KEY)
                .header("Authorization", "Bearer $API_KEY")
                .header("Content-Type", "application/json")
                .post(jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                val responseStr = response.body?.string() ?: ""
                Log.d(TAG, "SignIn Status: ${response.code}, Body: $responseStr")

                if (response.isSuccessful) {
                    val jsonObj = JSONObject(responseStr)
                    val userObj = jsonObj.optJSONObject("user")
                    val userId = userObj?.optString("id") ?: ""
                    val userEmail = userObj?.optString("email") ?: email
                    val userMetadata = userObj?.optJSONObject("user_metadata")
                    val username = userMetadata?.optString("username") ?: email.substringBefore("@")
                    val token = jsonObj.optString("access_token", null)

                    AuthResult(
                        success = true,
                        email = userEmail,
                        userId = userId,
                        username = username,
                        token = token
                    )
                } else {
                    val errorMsg = parseErrorMessage(responseStr)
                    AuthResult(success = false, errorMessage = errorMsg)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "SignIn Exception", e)
            AuthResult(success = false, errorMessage = e.message ?: "Network error. Please try again.")
        }
    }

    /**
     * Uploads user avatar image bytes to Supabase Storage and returns the public URL.
     */
    suspend fun uploadAvatar(userId: String, imageBytes: ByteArray): String? = withContext(Dispatchers.IO) {
        try {
            val bucket = "avatars"
            val objectPath = "user_${userId}_${System.currentTimeMillis()}.png"
            val url = "$SUPABASE_URL/storage/v1/object/$bucket/$objectPath"

            // Using application/octet-stream or image/png
            val requestBody = imageBytes.toRequestBody("image/png".toMediaType())
            val request = Request.Builder()
                .url(url)
                .header("apikey", API_KEY)
                .header("Authorization", "Bearer $API_KEY")
                .header("Content-Type", "image/png")
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyStr = response.body?.string() ?: ""
                Log.d(TAG, "Storage Upload Response: ${response.code}, Body: $bodyStr")
                if (response.isSuccessful || response.code == 200 || response.code == 201) {
                    // Return public URL
                    return@withContext "$SUPABASE_URL/storage/v1/object/public/$bucket/$objectPath"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Storage Upload Exception", e)
        }
        return@withContext null
    }

    /**
     * Patches the avatar URL of the user's profile on Supabase.
     */
    suspend fun updateAvatarUrlInDb(userId: String, avatarUrl: String) = withContext(Dispatchers.IO) {
        try {
            val url = "$SUPABASE_URL/rest/v1/profiles?id=eq.$userId"
            val jsonBody = JSONObject().apply {
                put("avatar_url", avatarUrl)
                put("updated_at", System.currentTimeMillis())
            }

            val request = Request.Builder()
                .url(url)
                .header("apikey", API_KEY)
                .header("Authorization", "Bearer $API_KEY")
                .header("Content-Type", "application/json")
                .patch(jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                Log.d(TAG, "DB profile avatar patch status: ${response.code}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "updateAvatarUrlInDb skipped: ${e.message}")
        }
    }

    /**
     * Safely attempts to save or upsert the user profile to a custom 'profiles' table on Supabase.
     * This is a best-effort call so if the table does not exist or user doesn't have permissions,
     * it won't crash the app.
     */
    private fun saveProfileToDb(userId: String, email: String, username: String) {
        try {
            val url = "$SUPABASE_URL/rest/v1/profiles"
            val jsonBody = JSONObject().apply {
                put("id", userId)
                put("email", email)
                put("username", username)
                put("updated_at", System.currentTimeMillis())
            }

            val request = Request.Builder()
                .url(url)
                .header("apikey", API_KEY)
                .header("Authorization", "Bearer $API_KEY")
                .header("Content-Type", "application/json")
                .header("Prefer", "resolution=merge-duplicates")
                .post(jsonBody.toString().toRequestBody(JSON_MEDIA_TYPE))
                .build()

            client.newCall(request).execute().use { response ->
                Log.d(TAG, "DB profile save status: ${response.code}")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Best-effort saveProfileToDb skipped: ${e.message}")
        }
    }

    private fun parseErrorMessage(responseStr: String): String {
        return try {
            val jsonObj = JSONObject(responseStr)
            jsonObj.optString("error_description", null)
                ?: jsonObj.optString("error", null)
                ?: jsonObj.optString("message", null)
                ?: jsonObj.optString("msg", "Authentication failed.")
        } catch (e: Exception) {
            "Authentication failed."
        }
    }
}
