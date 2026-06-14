import kotlinx.browser.window

fun main() {
    when (detectContext()) {
        ExtensionContext.BACKGROUND -> {
        }

        ExtensionContext.CONTENT_SCRIPT -> {
        }

        ExtensionContext.POPUP -> {
            popup()
        }

        ExtensionContext.OPTIONS -> {
            options()
        }

        ExtensionContext.UNKNOWN -> {
        }
    }
}

enum class ExtensionContext {
    BACKGROUND,
    CONTENT_SCRIPT,
    POPUP,
    OPTIONS,
    UNKNOWN
}

fun detectContext(): ExtensionContext {
    val hasWindow = js("typeof window !== 'undefined'") as Boolean
    val hasDocument = js("typeof document !== 'undefined'") as Boolean
    val hasImportScripts = js("typeof importScripts !== 'undefined'") as Boolean

    return when {
        hasImportScripts -> ExtensionContext.BACKGROUND
        hasWindow && hasDocument -> {
            val href = window.location.href
            when {
                href.contains("popup") -> ExtensionContext.POPUP
                href.contains("options") -> ExtensionContext.OPTIONS
                else -> ExtensionContext.CONTENT_SCRIPT
            }
        }
        else -> ExtensionContext.UNKNOWN
    }
}