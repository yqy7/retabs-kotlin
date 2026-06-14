import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.w3c.dom.url.URL
import kotlin.js.Date
import kotlin.js.Promise
import kotlin.js.json

val chrome: dynamic = js("chrome")

suspend fun getChromeSessions(): dynamic {
    return chrome.sessions.getRecentlyClosed().unsafeCast<Promise<dynamic>>().await()
}

@OptIn(ExperimentalWasmJsInterop::class)
suspend fun getChromeHistory(searchText: String, maxResults: Int = 20): dynamic {
    return chrome.history.search(
        json(
            "text" to searchText,
            "maxResults" to maxResults,
            "startTime" to 0
        )
    ).unsafeCast<Promise<dynamic>>().await()
}

fun getFavIcon(pageUrl: String): String {
    val url = URL(getUrl("/_favicon/"))
    url.searchParams.set("pageUrl", pageUrl)
    url.searchParams.set("size", "32")
    return url.toString()
}

fun getUrl(path: String) = chrome.runtime.getURL(path).toString()

fun i18n(key: String): String {
    return chrome.i18n.getMessage(key)
}

suspend fun openUrl(url: String) {
    val tabs = chrome.tabs.query(
        json(
            "url" to url
        )
    ).unsafeCast<Promise<dynamic>>().await()

    if (tabs && tabs.length > 0) {
        chrome.tabs.update(
            tabs[0].id, json(
                "active" to true
            )
        )
    } else {
        chrome.tabs.create(
            json(
                "url" to url,
            )
        )
    }
}

suspend fun openSettings() {
    val url = getUrl("options.html")
    openUrl(url)
    window.close()
}

suspend fun openChromeHistory() {
    openUrl("chrome://history/")
}

suspend fun reopenSession(sessionId: String) {
    val restoredSession = chrome.sessions.restore(sessionId)
        .unsafeCast<Promise<dynamic>>().await()
    console.log("restoredSession", restoredSession)
}

fun timeFormat(timpstamp: Long?): String =
    timpstamp?.let { Date(it).toLocaleString() } ?: ""

fun copyToClipboard(text: String): Boolean {
    val textarea: dynamic = document.createElement("textarea")
    document.body?.appendChild(textarea)
    textarea.innerText = text
    textarea.select()
    val rs = document.execCommand("copy")
    document.body?.removeChild(textarea)
    return rs
}

fun copyLink(url: String) {
    if (copyToClipboard(url)) {
        console.log("Copy link to clipboard.")
    } else {
        console.log("Copy link failed.")
    }
}