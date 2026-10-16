package rocks.livinginthefuture.hiriganatouchtypingtutor

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.MotionEvent
import android.view.View
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech

    @SuppressLint("SetJavaScriptEnabled", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Native Android Text-to-Speech
        tts = TextToSpeech(this, this)

        val webView: WebView = findViewById(R.id.webView)

        // Enable required WebView settings
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false

        // Custom WebViewClient to open web links in standard browser
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url.toString()

                // If it's an external link, open the default system browser
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    val intent =
                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(intent)
                    return true
                }

                // Allow the WebView to load local files normally
                return false
            }
        }

        webView.webChromeClient = WebChromeClient()

        // Bind the Kotlin TTS engine to a JavaScript object named "AndroidApp"
        webView.addJavascriptInterface(WebAppInterface(), "AndroidApp")

        // Load the HTML game
        webView.loadUrl("file:///android_asset/index.html")

        // Force WebView to accept input focus so the soft keyboard works
        webView.requestFocus(View.FOCUS_DOWN)
        webView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP -> {
                    if (!v.hasFocus()) {
                        v.requestFocus()
                    }
                }
            }
            false
        }
    }

    // Set TTS language to Japanese once it finishes initializing
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.JAPAN
        }
    }

    // This interface allows the HTML/JS file to call the native Android TTS
    inner class WebAppInterface {
        @JavascriptInterface
        fun speak(text: String) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    // Clean up memory and TTS engine when the app is closed
    override fun onDestroy() {
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}
