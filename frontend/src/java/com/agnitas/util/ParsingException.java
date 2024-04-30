/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.util.Objects;

public class ParsingException extends Exception {
    private static final long serialVersionUID = 6209070435425158023L;

    private Caret caret;

    public ParsingException(Caret caret, String message, Throwable cause) {
        super(message, cause);
        Objects.requireNonNull(caret);
        this.caret = caret;
    }

    public ParsingException(Caret caret, String message) {
        super(message);
        Objects.requireNonNull(caret);
        this.caret = caret;
    }

    public ParsingException(int line, int column, String message, Throwable cause) {
        super(message, cause);
        this.caret = Caret.at(line, column);
    }

    public ParsingException(int line, int column, String message) {
        super(message);
        this.caret = Caret.at(line, column);
    }

    public ParsingException(String message, Throwable cause) {
        this(1, 1, message, cause);
    }

    public ParsingException(String message) {
        this(1, 1, message);
    }

    public Caret getCaret() {
        return caret;
    }

    public int getLine() {
        return caret.getLine();
    }

    public int getColumn() {
        return caret.getColumn();
    }
}
