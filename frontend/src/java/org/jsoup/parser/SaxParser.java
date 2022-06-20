/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.jsoup.parser;

import java.util.Locale;
import java.util.Objects;

import org.xml.sax.SAXException;

import com.agnitas.messages.I18nString;
import com.agnitas.util.Caret;
import com.agnitas.util.ParsingException;

public class SaxParser {
    private SaxParsingHandler handler;
    private Locale locale;

    public SaxParser(SaxParsingHandler handler, Locale locale) {
        Objects.requireNonNull(handler);
        this.handler = handler;

        Objects.requireNonNull(locale);
        this.locale = locale;
    }

    public void parse(String content) throws ParsingException {
        CharacterReader reader = new CharacterReader(content);
        ParseErrorList errors = new ParseErrorList(1, 1);
        Tokeniser tokeniser = new Tokeniser(reader, errors);
        Token token;

        do {
            try {
                token = tokeniser.read();

                if (errors.size() > 0) {
                    ParseError e = errors.get(0);
                    throw exception(Caret.at(content, e.getPosition()), "error.default.InvalidSyntax");
                }

                process(token);
            } catch (SAXException e) {
                throw exception(Caret.at(content, reader.pos()), e);
            }
        } while (!token.isEOF());
    }

    private void process(Token token) throws SAXException {
        switch (token.type) {
            case Doctype:
                processDoctype(token.asDoctype());
                break;
            case StartTag:
                processOpeningTag(token.asStartTag());
                break;
            case EndTag:
                processClosingTag(token.asEndTag());
                break;
            case Comment:
                processComment(token.asComment());
                break;
            case Character:
                processCharacter(token.asCharacter());
                break;
            case EOF:
                processEnd();
                break;
			default:
				break;
        }
    }

    private void processDoctype(Token.Doctype doctype) throws SAXException {
        handler.onDoctype(doctype.getName(), doctype.getPublicIdentifier(), doctype.getSystemIdentifier(), doctype.isForceQuirks());
    }

    private void processOpeningTag(Token.StartTag tag) throws SAXException {
        handler.onOpeningTag(tag.name(), tag.attributes, tag.isSelfClosing());
    }

    private void processClosingTag(Token.EndTag tag) throws SAXException {
        handler.onClosingTag(tag.name());
    }

    private void processComment(Token.Comment comment) throws SAXException {
        handler.onComment(comment.getData());
    }

    private void processCharacter(Token.Character character) throws SAXException {
        handler.onCharacter(character.getData());
    }

    private void processEnd() throws SAXException {
        handler.onEnd();
    }

    private ParsingException exception(Caret caret, Exception cause) {
        return new ParsingException(caret, cause.getMessage(), cause);
    }

    private ParsingException exception(Caret caret, String key) {
        return new ParsingException(caret, I18nString.getLocaleString(key, locale));
    }
}
