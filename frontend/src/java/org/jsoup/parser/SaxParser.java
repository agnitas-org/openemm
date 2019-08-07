/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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

    public SaxParser(SaxParsingHandler handler) {
        this(handler, Locale.getDefault());
    }

    public void parse(String content) throws ParsingException {
        CharacterContentReader reader = new CharacterContentReader(content);
        Tokeniser tokeniser = new SaxTokenizer(reader);
        Token token;

        do {
            try {
                token = tokeniser.read();
                process(token);
            } catch (TokenizerException e) {
                throw exception(reader, "error.default.InvalidSyntax", e);
            } catch (SAXException e) {
                throw exception(reader, e);
            }
        } while (token.type != Token.TokenType.EOF);
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
        }
    }

    private void processDoctype(Token.Doctype doctype) throws SAXException {
        handler.onDoctype(doctype.getName(), doctype.getPublicIdentifier(), doctype.getSystemIdentifier(), doctype.isForceQuirks());
    }

    private void processOpeningTag(Token.StartTag tag) throws SAXException {
        handler.onOpeningTag(tag.name(), tag.getAttributes(), tag.isSelfClosing());
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

    private ParsingException exception(CharacterContentReader reader, Exception cause) {
        return new ParsingException(reader.getCaret(), cause.getMessage(), cause);
    }

    private ParsingException exception(CharacterContentReader reader, String key, Exception cause) {
        return new ParsingException(reader.getCaret(), I18nString.getLocaleString(key, locale), cause);
    }

    private static class SaxTokenizer extends Tokeniser {
        public SaxTokenizer(CharacterReader reader) {
            super(reader);
        }

        @Override
        void error(TokeniserState state) {
            super.error(state);
            throw new TokenizerException();
        }

        @Override
        void eofError(TokeniserState state) {
            super.eofError(state);
            throw new TokenizerException();
        }
    }

    private static class CharacterContentReader extends CharacterReader {
        private String content;

        public CharacterContentReader(String content) {
            super(content);
            this.content = toString();
        }

        public Caret getCaret() {
            return Caret.at(content, pos());
        }
    }

    private static class TokenizerException extends RuntimeException {
        private static final long serialVersionUID = 1766667657923635575L;
    }
}
