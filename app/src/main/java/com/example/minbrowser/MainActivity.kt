package com.example.minbrowser

import android.os.Bundle
import android.webkit.*
import androidx.activity.ComponentActivity
import androidx.activity.compose.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.javascriptengine.JavaScriptSandbox

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var input by remember { mutableStateOf("https://google.com") }
            var webView: WebView? by remember { mutableStateOf(null) }
            var focusMode by remember { mutableStateOf(true) }

            BackHandler(enabled = webView?.canGoBack() == true) { webView?.goBack() }

            Scaffold(
                topBar = {
                    Surface(tonalElevation = 3.dp) {
                        Row(Modifier.padding(8.dp).statusBarsPadding().fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            TextField(
                                value = input,
                                onValueChange = { input = it },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(24.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                                colors = TextFieldDefaults.colors(focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent),
                                leadingIcon = { Icon(Icons.Default.Search, null) }
                            )
                            IconButton(onClick = { 
                                val trimmed = input.trim()
                                when {
                                    // JS execution
                                    trimmed.startsWith("js:") -> {
                                        val code = trimmed.removePrefix("js:")
                                        JavaScriptSandbox.createConnectedInstanceAsync(this@MainActivity).get().use { 
                                            val res = it.createIsolate().evaluateJavaScriptAsync(code).get()
                                            webView?.loadData("<html><body><h1>Result</h1><p>$res</p></body></html>", "text/html", "UTF-8")
                                        }
                                    }
                                    // URL/Search
                                    else -> {
                                        val url = if (trimmed.contains(".")) (if (trimmed.startsWith("http")) trimmed else "https://$trimmed")
                                                 else "https://google.com/search?q=$trimmed"
                                        webView?.loadUrl(url)
                                    }
                                }
                            }) { Icon(Icons.Default.PlayArrow, null) }
                        }
                    }
                }
            ) { padding ->
                AndroidView(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    if (focusMode) {
                                        val css = "header,nav,footer,aside,.ads{display:none!important;}body{font-family:sans-serif!important;}"
                                        view?.evaluateJavascript("var s=document.createElement('style');s.innerHTML='$css';document.head.appendChild(s);", null)
                                    }
                                }
                            }
                            webView = this
                            loadUrl("https://google.com")
                        }
                    }
                )
            }
        }
    }
}
