/**
 *
 */
package com.blogspot.noblenoteandroid.filesystem

/**
 * @author Taiko-G780
 *
 * matches file system allowed chars
 */
object FileNameValidator {

    val disallowedCharacters : Array<Char> get() = arrayOf('\\','^','/','?','<','>',':','*','|','\"')

    fun containsDisallowedCharacter(input : CharSequence) : Boolean
    {
       return input.any { !isAllowedChar(it) }
    }

    private fun isAllowedChar(c: Char): Boolean { //Matcher m = p.matcher(CharBuffer.wrap(new char[]{c}));
//return !m.matches();
// this is probably faster
        return !(c == '\\' || c == '^' || c == '/' || c == '?' || c == '<' || c == '>' || c == ':' || c == '*' || c == '|' || c == '\"')
    }
}