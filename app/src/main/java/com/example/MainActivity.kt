package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.MainApp
import com.example.ui.LangViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    handleIntent(intent)
    
    setContent {
      MyApplicationTheme {
        MainApp()
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    handleIntent(intent)
  }

  private fun handleIntent(intent: Intent?) {
    val data: Uri? = intent?.data
    if (data != null && data.scheme == "wenlang" && data.host == "auth-callback") {
      val token = extractAccessToken(data)
      if (token != null) {
        val viewModel = ViewModelProvider(this)[LangViewModel::class.java]
        lifecycleScope.launch {
          val result = viewModel.handleAuthCallbackToken(token)
          if (result.success) {
            Toast.makeText(this@MainActivity, "E-mail confirmado com sucesso! Bem-vindo!", Toast.LENGTH_LONG).show()
          } else {
            Toast.makeText(this@MainActivity, "Erro ao confirmar e-mail: ${result.errorMessage}", Toast.LENGTH_LONG).show()
          }
        }
      }
    }
  }

  private fun extractAccessToken(uri: Uri): String? {
    // 1. Try query parameter
    val queryToken = uri.getQueryParameter("access_token")
    if (!queryToken.isNullOrEmpty()) return queryToken

    // 2. Try fragment (Supabase uses fragment by default on browser redirects)
    val fragment = uri.fragment ?: return null
    val params = fragment.split("&")
    for (param in params) {
      val keyValue = param.split("=")
      if (keyValue.size == 2 && keyValue[0] == "access_token") {
        return keyValue[1]
      }
    }
    return null
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
  MyApplicationTheme { Greeting("Android") }
}
