package nz.adjmunro.ast.sre

import nz.adjmunro.ast.AST

public class SreParser {

    public fun parse(input: String): AST.Node {
        // Placeholder for actual parsing logic
        // This should convert the SRE input into an AST.Node structure
        return AST.Node()
    }

    internal fun tokenize(input: String) {
        var i = 0
        while (i < input.length) {
            when (val c0 = input[i++]) {
                '[' -> when(val c1 = input[i]) {
                    ']' -> error("Empty tag at position ${i - 1}")
                    'b' -> when (val c2 = input[i++]) {
                        ']' -> AST.Token(tag = AST.Tag.Bold, type = AST.Token.MarkerType.OPEN, position = (i - 3)until i)
                        '=' -> when (val c3 = input[i++]) {
                            'w' -> AST.Token(tag = AST.Tag.Bold, type = AST.Token.MarkerType.DISCRETE, position = (i - 4)until i)
                            else -> error("Invalid tag at position ${i - 3}")
                        }
                        'g' -> TODO()
                        else -> error("Invalid tag at position ${i - 2}")
                    }
                }
                else -> {

                }
            }
        }
    }

}
