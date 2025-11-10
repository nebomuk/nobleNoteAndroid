package com.blogspot.noblenoteandroid.editor

import java.io.StringReader
import java.util.*

object TextConverter {

    fun removeHtmlTagsFast(inp : String): String {
        var insideTag = false;
        val builder = StringBuilder();

        for (i in 0 until inp.length) {
            if (!insideTag && inp[i] == '<') {
                insideTag = true
                continue
            }
            if (insideTag && inp[i] == '>') {
                insideTag = false
                continue
            }
            if (!insideTag) {
                builder.append(inp[i])
            }
        }
        return builder.toString();

    }

    @Deprecated("replaced by apache method")
    fun unescapeUnicodefromHtmlString(yourInputString : String) : String
    {
        val p = Properties();
        p.load(StringReader("key="+yourInputString));
        return p.getProperty("key");
    }


    // TODO this can probably replaced by Html.escapeHtml
    fun convertFromPlainText(plain: String): String {
        var col = 0
        val rich = StringBuilder()
        rich.append("<p>")
        var i = 0
        while (i < plain.length) {
            if (plain[i] == '\n') {
                var c = 1
                while (i + 1 < plain.length && plain[i + 1] == '\n') {
                    i++
                    c++
                }
                if (c == 1) rich.append("<br>\n") else {
                    rich.append("</p>\n")
                    while (--c > 1) rich.append("<br>\n")
                    rich.append("<p>")
                }
                col = 0
            } else {
                if (plain[i] == '<') rich.append("&lt;") else if (plain[i] == '>') rich.append("&gt;") else if (plain[i] == '&') rich.append("&amp;") else rich.append(plain[i])
                ++col
            }
            ++i
        }
        if (col != 0) rich.append("</p>")
        return rich.toString()
    }

    // heuristically detects if the text contains html
    fun mightBeRichText(text : String) : Boolean
    {
        if (text.isEmpty())
            return false;
        var start = 0;
        while (start < text.length && text.get(start).isWhitespace())
            ++start;
        // skip a leading <?xml ... ?> as for example with xhtml
        if (text.mid(start, 5).compareTo("<?xml") == 0) {
            while (start < text.length) {
                if (text.get(start) == '?'
                        && start + 2 < text.length
                        && text.get(start + 1) == '>') {
                    start += 2;
                    break;
                }
                ++start;
            }
            while (start < text.length && text.get(start).isWhitespace())
                ++start;
        }
        if (text.mid(start, 5).compareTo("<!doc", ignoreCase = true) == 0)
            return true;
        var open = start;
        while (open < text.length && text.get(open) != '<'
                && text.get(open) != '\n') {
            if (text.get(open) == '&' &&  text.mid(open + 1, 3) == "lt;")
                return true; // support desperate attempt of user to see <...>
            ++open;
        }
        if (open < text.length && text.get(open) == '<') {
            var close = text.indexOf('>', open);
            if (close > -1) {
                var tag = "";
                for (i : Int in open+1 until close) {
                    if (text[i].isDigit() || text[i].isLetter())
                        tag += text[i];
                    else if (!tag.isEmpty() && text[i].isWhitespace())
                        break;
                    else if (!tag.isEmpty() && text[i] == '/' && i + 1 == close)
                        break;
                    else if (!text[i].isWhitespace() && (!tag.isEmpty() || text[i] != '!'))
                        return false; // that's not a tag
                }
                return htmlElements.contains(tag);
            }
        }
        return false;
    }

    @ExperimentalUnsignedTypes
    private fun String.mid(inputPosition : Int, inputLength : Int) : String
    {
        var length = inputLength;
        var position = inputPosition;
        val originalLength = this.length;
        if (position > originalLength)
            return "";
        if (position < 0) {
            if (length < 0 || length + position >= originalLength)
                return this;
            if (length + position <= 0)
                return "";
            length += position;
            position = 0;
        } else if (length.toUInt() > (originalLength - position).toUInt()) {
            length = originalLength - position;
        }
        if (position == 0 && length == originalLength)
            return this;
        return if(length > 0) this.substring(position).take(length)  else "";
    }
    
    private val htmlElements = arrayOf(
         "a",
         "address",
         "b",
         "big",
         "blockquote",
         "body",
         "br",
         "caption",
         "center",
         "cite",
         "code",
         "dd",
         "dfn",
         "div",
         "dl",
         "dt",
         "em",
         "font",
         "h1",
         "h2",
         "h3",
         "h4",
         "h5",
         "h6",
         "head",
         "hr",
         "html",
         "i",
         "img",
         "kbd",
         "li",
         "link",
         "meta",
         "nobr",
         "ol",
         "p",
         "pre",
         "qt",
         "s",
         "samp",
         "script",
         "small",
         "span",
         "strong",
         "style",
         "sub",
         "sup",
         "table",
         "tbody",
         "td",
         "tfoot",
         "th",
         "thead",
         "title",
         "tr",
         "tt",
         "u",
         "ul",
         "var")

}