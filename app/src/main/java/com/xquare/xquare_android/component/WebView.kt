package com.xquare.xquare_android.component

import android.annotation.SuppressLint
import android.os.Build
import android.view.ViewGroup
import android.view.WindowInsets
import android.webkit.*
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.xquare.xquare_android.R
import com.xquare.xquare_android.util.DevicePaddings

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface", "ResourceAsColor")
@Composable
fun WebView(
    modifier: Modifier = Modifier,
    url: String,
    headers: Map<String, String> = mapOf(),
    bridges: Map<String, Any> = mapOf(),
    keyboardCheck: (Boolean) -> Unit = {},
    onCreate: (WebView) -> Unit = {}
) {
    val view = LocalView.current

    val bottomPaddingFalseUrlList = listOf(
        "https://service.xquare.app/feed",
        "https://service.xquare.app/apply",
    )

    val canRefreshList = listOf(
        "https://service.xquare.app/feed",
    )

    val bottomState =
        if (bottomPaddingFalseUrlList.contains(url)) 0.dp
        else DevicePaddings.navigationBarHeightDp.dp
    var bottomPadding by remember { mutableStateOf(bottomState) }

    var scrollState by rememberSaveable { mutableStateOf(0) }

    AndroidView(
        factory = { context ->
            SwipeRefreshLayout(context).apply {
                isEnabled = canRefreshList.contains(url)
                setColorSchemeColors(R.color.purple_200)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                addView(WebView(context).apply {
                    setOnRefreshListener {
                        reload()
                        isRefreshing = false
                    }
                    if (!bottomPaddingFalseUrlList.contains(url)) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            view.rootView.setOnApplyWindowInsetsListener { _, insets ->
                                if (insets.isVisible(WindowInsets.Type.ime())) {
                                    bottomPadding = 0.dp
                                    keyboardCheck(true)
                                } else {
                                    bottomPadding = DevicePaddings.navigationBarHeightDp.dp
                                    keyboardCheck(false)
                                }
                                insets.consumeSystemWindowInsets()
                            }
                        }
                    }

                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            CookieManager.getInstance().flush()
                            scrollTo(0, scrollState)
                        }
                    }
                    // TODO 스크롤 초기화 방지 방안 개선
                    setOnScrollChangeListener { _, _, _, _, _ -> scrollState = scrollY }
                    webChromeClient = WebChromeClient()
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.setSupportZoom(false)
                    settings.builtInZoomControls = false
                    settings.javaScriptEnabled = true
                    settings.javaScriptCanOpenWindowsAutomatically = true
                    settings.setSupportMultipleWindows(false)
                    settings.domStorageEnabled = true
                    settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    bridges.forEach { addJavascriptInterface(it.value, it.key) }
                    loadUrl(url, headers)
                    onCreate(this)
                })
            }
        },
        modifier = modifier
            .imePadding()
            .padding(bottom = bottomPadding)
    )
}
