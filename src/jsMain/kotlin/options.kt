import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable

fun options() {
    renderComposable(rootElementId = "app") {
        Div(
            attrs = {
                style {
                    width(100.vw)
                    height(100.vh)
                    display(DisplayStyle.Flex)
                    justifyContent(JustifyContent.Center)
                    alignItems(AlignItems.Center)
                }
            }
        ) {
            Text("hello...")
        }
    }
}