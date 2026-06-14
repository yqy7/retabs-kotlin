import androidx.compose.runtime.*
import androidx.compose.web.events.SyntheticMouseEvent
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.web.attributes.InputType
import org.jetbrains.compose.web.attributes.placeholder
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.*
import org.jetbrains.compose.web.events.SyntheticChangeEvent
import org.jetbrains.compose.web.renderComposable
import org.w3c.dom.HTMLInputElement

fun popup() {
    renderComposable(rootElementId = "app") {
        Style(PopupStyles)

        val viewModelStore = remember { ViewModelStore() }
        val viewModelStoreOwner = object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = viewModelStore
        }

        CompositionLocalProvider(LocalViewModelStoreOwner provides viewModelStoreOwner) {
            val viewModel = viewModel {
                PopupViewModel()
            }

            val searchText by viewModel.searchText.collectAsState()
            val showSession by viewModel.showSession.collectAsState()
            val showHistory by viewModel.showHistory.collectAsState()

            MainContent {
                SearchBar {
                    viewModel.setSearchText(it.value)
                }

                ActionPanel()

                if (searchText.isBlank() && showSession) {
                    RecentlyClosedSessionList()
                }

                if (showHistory) {
                    HistoryList()
                }
            }
        }
    }
}

object PopupStyles : StyleSheet() {
    val borderColor = Color.lightgray
    val hoverColor = rgb(198, 226, 255)
    val linkColor = rgb(94, 172, 255)
    val whiteColor = rgb(255, 255, 255)

    init {
        "a" {
            color(linkColor)
        }
    }

    val actionPanel by style {
        position(Position.Fixed)
        right(8.px)
        top(60.px)
        border(1.px, LineStyle.Solid, borderColor)
        borderRadius(5.px)
        overflow("hidden")
        display(DisplayStyle.Flex)
        flexDirection(FlexDirection.Row)
    }

    val listItemWrapper by style {
        border(1.px, LineStyle.Solid, Color.lightgray)
    }
    val listItem by style {
        alignItems(AlignItems.Center)
        padding(5.px)

        flex(1)

        whiteSpace("nowrap")
        property("text-overflow", "ellipsis")
        overflow("hidden")
        fontSize(0.8.em)

        cursor("pointer")

        (self + hover).style {
            backgroundColor(hoverColor)
        }
    }

    val copyButton by style {
        padding(15.px)
        cursor("pointer")
        (self + hover).style {
            backgroundColor(hoverColor)
        }
        (self + active).style {
            opacity(0.9)
            backgroundColor(hoverColor)
        }
    }
}

@Composable
fun Title(title: String) {
    Div({
        style {
            padding(5.px, 10.px)
            fontSize(1.cssRem)
        }
    }) {
        Text(title)
    }
}

@Composable
fun MainContent(content: @Composable () -> Unit) {
    Div({
        style {
            width(400.px)
            minHeight(200.px)
            padding(5.px, 0.px, 10.px, 0.px)
        }
    }) {
        content()
    }
}

@Composable
fun SearchBar(onChange: (SyntheticChangeEvent<String, HTMLInputElement>) -> Unit) {
    Div({
        style {
            display(DisplayStyle.Flex)
            padding(8.px, 8.px)
        }
    }) {
        Input(type = InputType.Text) {
            style {
                flex(1)
                height(40.px)
                lineHeight(40.px)
                padding(0.px, 8.px)
                fontSize(1.2.em)
            }
            placeholder(i18n("searchHistory"))
            onChange {
                onChange(it)
            }
        }
    }
}

@Composable
fun ActionPanel() {
    fun StyleScope.buttonStyle() {
        width(30.px)
        height(30.px)
        cursor("pointer")
        backgroundColor(Color.white)
    }

    @Composable
    fun ToggleButton(iconPath: String, selected: Boolean = false, title: String = "", onClick: () -> Unit) {
        CenterDiv({
            title(title)
            style {
                buttonStyle()
                backgroundColor(
                    if (selected) PopupStyles.whiteColor
                    else PopupStyles.hoverColor
                )
                color(
                    if (selected) PopupStyles.hoverColor
                    else PopupStyles.whiteColor
                )
            }
            onClick { onClick() }
        }) {
            Img(src = getUrl(iconPath))
        }
    }

    @Composable
    fun ActionButton(iconPath: String, title: String = "", onClick: () -> Unit) {
        CenterDiv({
            title(title)
            style {
                buttonStyle()
            }
            onClick { onClick() }
        }) {
            Img(src = getUrl(iconPath))
        }
    }

    val viewModel: PopupViewModel = viewModel()
    val scope = rememberCoroutineScope()
    val showSession by viewModel.showSession.collectAsState()
    val showHistory by viewModel.showHistory.collectAsState()

    Div({ classes(PopupStyles.actionPanel) }) {
        ToggleButton("svg/files.svg", showSession, "Session") {
            viewModel.toggleShowSession()
        }
        ToggleButton("svg/clock-history.svg",showHistory, "History") {
            viewModel.toggleShowHistory()
        }
        ActionButton("svg/gear.svg", "Settings") {
            scope.launch { openSettings() }
        }
    }
}


@Composable
fun RecentlyClosedSessionList(viewModel: PopupViewModel = viewModel()) {
    val sessions by viewModel.sessions.collectAsState()
    val showMoreSession by viewModel.showMoreSession.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadChromeSessions()
    }

    Title(i18n("recentlyClosedWindowsAndTabs"))

    Div {
        var count = 0
        for (session in sessions) {
            count++
            if (session.window) {
                WindowSessionItem(session)
            } else {
                val tab = session.tab
                TabSessionItem(tab.title, tab.url, session.lastModified * 1000, tab.sessionId)
            }
            if (count >= 5 && !showMoreSession) break
        }
        CenterDiv({
            style {
                color(PopupStyles.linkColor)
                cursor("pointer")
                lineHeight(2.em)
            }
            onClick { viewModel.toggleShowMoreSession() }
        }) {
            if (showMoreSession) {
                Text(i18n("showLess"))
            } else {
                Text(i18n("showMore"))
            }
        }

    }
}

external class Session {
    val window: dynamic
    val tab: dynamic
    val lastModified: dynamic
}

@Composable
fun WindowSessionItem(session: Session) {
    val scope = rememberCoroutineScope()
    val win = session.window

    Div({
        style {
            border(1.px, LineStyle.Solid, Color.lightgray)
        }
    }) {
        Div({
            style {
                padding(5.px, 10.px, 0.px, 10.px)
                cursor("pointer")
                textAlign("center")
            }
            onClick {
                scope.launch { reopenSession(win.sessionId) }
            }
        }) {
            Img(src = getUrl("svg/window.svg")) {
                style {
                    property("vertical-align", "middle")
                }
            }

            Span({
                style {
                    marginLeft(5.px)
                    fontSize(0.8.em)
                }
            }) {
                Text(i18n("openInNewWindow"))
            }
        }

        Div({
            style {
                border(1.px, LineStyle.Solid, Color.lightgray)
                margin(5.px)
            }
        }) {
            for (tab in win.tabs) {
                TabSessionItem(tab.title, tab.url, session.lastModified * 1000, tab.sessionId)
            }
        }
    }
}

@Composable
fun TabSessionItem(title: String, url: String, time: Long?, sessionId: String) {
    val scope = rememberCoroutineScope()
    ListItem(title, url, time) { event ->
        event.preventDefault()
        scope.launch { reopenSession(sessionId) }
    }
}

@Composable
fun HistoryList(viewModel: PopupViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    Title(i18n("history"))

    val historyItems by viewModel.historyList.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadHistory()
    }

    Div {
        for (item in historyItems) {
            HistoryItem(item.title, item.url, item.lastVisitTime)
        }
        CenterDiv({
            style {
                color(PopupStyles.linkColor)
                cursor("pointer")
                lineHeight(2.em)
            }
            onClick { scope.launch { openChromeHistory() } }
        }) {
            Text(i18n("moreHistory"))
        }
    }
}

@Composable
fun HistoryItem(title: String, url: String, time: Long?) {
    val scope = rememberCoroutineScope()

    ListItem(title, url, time) { event ->
        event.preventDefault()
        scope.launch { openUrl(url) }
    }
}

@Composable
fun ListItem(title: String, url: String, time: Long?, onItemClick: (event: SyntheticMouseEvent) -> Unit) {
    val favIcon = getFavIcon(url)
    RowDiv({ classes(PopupStyles.listItemWrapper) }) {
        RowDiv({
            classes(PopupStyles.listItem)
            onClick { onItemClick(it) }
        }) {
            Img(src = favIcon) {
                style {
                    width(32.px)
                    height(32.px)
                    marginRight(10.px)
                }
            }

            ColumnDiv {
                Div { Text(title) }
                Div {
                    A(href = url) {
                        Text(url)
                    }
                }
                Div { Text(timeFormat(time)) }
            }
        }

        CenterDiv({
            title(i18n("copyLink"))
            classes(PopupStyles.copyButton)
        }) {
            Img(src = getUrl("svg/link.svg")) {
                onClick { copyLink(url) }
            }
        }
    }
}