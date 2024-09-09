/* Joseph B. Ottinger (C)2024 */
package com.enigmastation.streampack.rules

import com.github.mpe85.grampa.grammar.AbstractGrammar
import com.github.mpe85.grampa.rule.Rule

abstract class ExtendedGrammar<T> : AbstractGrammar<T>() {
    open fun url(): Rule<T> = sequence(choice(string("https://"), string("http://")), word())

    open fun word(): Rule<T> = sequence(oneOrMore(sequence(testNot(whitespace()), anyChar())))
}
