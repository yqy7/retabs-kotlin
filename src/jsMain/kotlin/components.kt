import androidx.compose.runtime.Composable
import org.jetbrains.compose.web.attributes.AttrsScope
import org.jetbrains.compose.web.css.*
import org.jetbrains.compose.web.dom.Div
import org.w3c.dom.HTMLDivElement

@Composable
fun RowDiv(
    attrs: (AttrsScope<HTMLDivElement>.() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Div(
        attrs = {
            style  {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Row)
            }
            attrs?.invoke(this)
        }
    ) {
        content()
    }
}

@Composable
fun ColumnDiv(
    attrs: (AttrsScope<HTMLDivElement>.() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Div(
        attrs = {
            style  {
                display(DisplayStyle.Flex)
                flexDirection(FlexDirection.Column)
            }
            attrs?.invoke(this)
        }
    ) {
        content()
    }
}

@Composable
fun CenterDiv(
    attrs: (AttrsScope<HTMLDivElement>.() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Div(
        attrs = {
            style {
                display(DisplayStyle.Flex)
                justifyContent(JustifyContent.Center)
                alignItems(AlignItems.Center)
            }
            attrs?.invoke(this)
        }
    ) {
        content()
    }
}
