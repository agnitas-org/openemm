/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.util.Objects;

public final class Caret {
    private final int line;
    private final int column;

    public static Caret at(int line, int column) {
        if (line < 1 || column < 1) {
            throw new IllegalArgumentException("line < 1 || column < 1");
        }
        return new Caret(line, column);
    }

    public static Caret at(String string, int index) {
        Objects.requireNonNull(string);

        int caretLine = 1;
        int caretColumn = 1;

        int errorPosition = Math.min(string.length(), index);
        for (int i = 0; i < errorPosition; i++) {
            switch (string.charAt(i)) {
                case '\n':
                    caretLine++;
                    caretColumn = 1;
                    break;

                case '\r':
                    break;

                default:
                    caretColumn++;
                    break;
            }
        }

        return new Caret(caretLine, caretColumn);
    }

    private Caret(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public int hashCode() {
        return (line + ":" + column).hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Caret) {
            Caret caret = (Caret) object;
            return line == caret.line && column == caret.column;
        }
        return false;
    }

    @Override
    public String toString() {
        return line + ":" + column;
    }
}
