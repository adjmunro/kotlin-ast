package nz.adjmunro.ast

public class AST {

    public data class Node(
        val token: Token? = null,
        val left: Node? = null,
        val right: Node? = null,
    )

    public data class Token(
        val tag: Tag,
        val type: MarkerType,
        val position: IntRange,
        val id: String? = null,
        val selectors: List<String> = emptyList(),
        val matchingToken: Token? = null,
    ) {
        public enum class MarkerType {
            OPEN,
            CLOSE,
            DISCRETE;
        }
    }

    public enum class Tag {
        // Plain tags
        Bold,
        Italic,
        Underline,
        Strikethrough,
        Superscript,
        Subscript,
        Size,
        ForegroundColor,
        BackgroundColor,
        Heading,

        // Handles
        Anonymous,
        Clickable,

        // Whitespace tags
        Space,
        Tab,
        Newline,

        // Structural tags
        HorizontalRule,
        NumberedList,
        UnorderedList,
        ListItem,
    }

    public enum class FontWeight {
        W100,
        W200,
        W300,
        W400,
        W500,
        W600,
        W700,
        W800,
        W900;
    }
}
