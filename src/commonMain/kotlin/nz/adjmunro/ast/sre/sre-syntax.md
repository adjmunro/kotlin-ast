# SRE Syntax
*Lit. String Resource Embedded Syntax*

> SRE Syntax draws on BBCode, HTML, and Markdown as inspiration. It's most similar to BBCode, but is tailored for a more modern and simpler ecosystem. The exact standard is outlined below for reference should any team wish to port it to other platforms for embedded server messages.

## Why?
- Creating `AnnotatedString` objects in Android is extremely obtuse and cumbersome.
- `AnnotatedString` works best for hardcoded strings, but not for remote content or Android string resources. 
  - Doing so would involve searching the string for matching keywords, and using the index to style to content, which is tedious. 
  - Not only is this approach prone to bugs (especially with nested styling), but it immediately breaks down when faced with multilingual strings.
- Compose offers a `fromHtml` function, but a full HTML parser is heavier and is only suitable for remote strings sent from the server.
- Android string resources are cleaned of all XML/HTML-like tags when loaded (unless `CDATA` is used, however that is not necessarily more conventient for binding `onClick` callbacks).
  - Angle brackets `<tag>text</tag>` are not allowed in Android string resources, so we use square brackets instead.
  - Similar issues arise with apostrophes and speech marks, which are not allowed in Android string resources without escaping `\'` & `\"`.
- SRE is not in conflict with markdown. 
  - Markdown is easier to write & use for simple styling.
  - SRE aims to provide a more powerful and flexible syntax for complex and/or nested styling, along with other features that markdown does not typically support, such as colours or reference handles for code.

# Specification:
> This standard does not support embedded images, since these are for `String` *and that would be weird*. 
> Emoji support depends on platform/encoding support for emojis inside of strings.

> If an opening tag includes an `=` sign (except for `[click]`), it must also have a closing tag with the same `=` sign. This is to ensure proper parsing of nested scopes.
## Inline Text Styles

| Syntax                                  | Description                            |
|-----------------------------------------|----------------------------------------|
| `[b]text[/b]` / `[b=w100]text[/b=w100]` | Bold (W700 default or W100-W900)       |
| `[i]text[/i]`                           | Italic text                            |
| `[u]text[/u]`                           | Underlined text                        |
| `[s]text[/s]`                           | Strikethrough text                     |
| `[sub]text[/sub]`                       | Subscript text                         |
| `[sup]text[/sup]`                       | Superscript text                       |
| `[size=10]text[/size=10]`               | Text Size (10sp)                       |
| `[fg=#FF0000]text[/fg=#FF0000]`         | Coloured text foreground (hexadecimal) |
| `[bg=#FF0000]text[/bg=#FF0000]`         | Coloured text background (hexadecimal) |

> ### Font Weight
> The `w` in `[b=w100]` refers to a font weight enum, which are discreet multiples of 100.

> ### Font Size
> Font size *only* supports density-scaled `sp` units, as this is the standard for text in Android & Compose.
> *Other platform implementations may differ.*

> ### Inline Colours
> Hexadecimal colours may be any of: `#RGB`, `#ARGB`, `#RRGGBB`, or `#AARRGGBB`.
> Colours and other styles can also be applied via `$identifier` and `@selectors` programmatically.

## Handles
> Handles provide important "hooks" for code into the String at specific indices or ranges, allowing further manipulations.
> > Accessing a hook in code will enable metadata to be retrieved, such as the `#identifier`, `@selectors`, or tag indices used.

| Syntax                                                  | Description                                                                                                                                       | Example                                                             |
|---------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------|
| `[]`                                                    | Anonymous tag. A reference to a specific index, or range. <br/>*If enclosing a range, ***must*** use an `$identifier` or `@selector`.*            | Cannot be used by itself.                                           |
| `$identifier`                                           | Unique identifier. Can only be used once. A tag cannot have more than one `$identifier`.                                                          | `[$id]` / `[$blue100]text[/$blue100]`                               |
| `@selector`                                             | Multi-selector, similar to CSS classes. <br/>The same `@selector` can be used on multiple tags. <br/>A single tag can have multiple `@selectors`. | `[@1@3]text[/@1]text[/@3]` / <br/>`[$id @style1]text[/$id @style1]` |
| `[click]text[/click]` / `[click=hyperlink]text[/click]` | Bind text to an `onClick` callback                                                                                                                | `[click $id]text[/click]`                                           |

> ### Anonymous Selectors
> An anonymous `@selector` (with no `$identifier` or tag name) must have a closing `[/@selector]` tag!

> ### Click Tags
> - Each `[click]` tags is bound to an `onClick` listener in code.
> - All click tags are assigned their own click index based on position.
> - Metadata handles, such as the `#identifier` and `@selectors` can also be used to locate and bind a click listener.
> - Or a hyperlink will be bound to a callback automatically.
> - The callback will receive the tag metadata when invoked.
> - Nested `[click]` tags are not supported.


## Whitespace
> Whitespace tags have no closing tag. These focus on non-collapsing whitespace, which is useful for formatting text in a way that is not affected by the layout engine.

| Syntax                                                    | Description                                        |
|-----------------------------------------------------------|----------------------------------------------------|
| `[space=2]`                                               | Non-collapsing space characters (2+)               |
| `[tab]` / `[tab=2]`                                       | Non-collapsing tab character (1 or 2+)             |
| `[newline]` / `[newline=2]`                               | Non-collapsing line break (1 or 2+)                |

## Constructs
> Constructs are little extras to provide structured content, such as lists or horizontal rules.

| Syntax                   | Description                                  |
|--------------------------|----------------------------------------------|
| `[numlist]...[/numlist]` | Defines an ordered list. May be nested.      |
| `[list]...[/list]`       | Defines an unordered list. May be nested.    |
| `[li]item1[li]item2`     | List item (each delimited by the next item). | 

# TODO:
- write TDD - make start with the hardest part first?
- actually, first start by tokenizing all tags into open/close objects with indexes in the clean string.
- then construct the AST from the tokens (if AST even makes sense - how to handle out of order tags?)
- add a `[[$id]]` replace by id name tag? something easier to work with than format? or is it, might just end up being the same thing.
- Or perhaps some variant of `\[`, `\]`, `[[]]` to display inline square brackets
- Perhaps support using `@body1` etc selector tags & creating a function to bind certain selectors to specific compose styles to replace design-system specific styles
- Add `Text<T>` interface
- Perhaps `[tag /]` should mean "apply this style until the end of the string".
