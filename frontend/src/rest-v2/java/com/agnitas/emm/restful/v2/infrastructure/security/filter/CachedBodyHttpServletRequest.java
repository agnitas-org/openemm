/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.infrastructure.security.filter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

/**
 * A wrapper for {@link HttpServletRequest} that caches the request body,
 * allowing it to be read multiple times.
 * <p>
 * This is especially useful in filters or interceptors that need to inspect
 * the request <b>body</b> before it reaches a controller, such as for XSS detection.
 * Normally, the input stream of a servlet request can
 * only be read once, but this wrapper stores the body in memory
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private final byte[] cachedBody;
    private final String encoding;

    public CachedBodyHttpServletRequest(HttpServletRequest req) throws IOException {
        super(req);
        try (InputStream is = req.getInputStream()) {
            this.cachedBody = is.readAllBytes();
            this.encoding = Optional.ofNullable(req.getCharacterEncoding()).orElse(StandardCharsets.UTF_8.name());
        }
    }

    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream is = new ByteArrayInputStream(this.cachedBody);
        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return is.available() == 0;
            }
            @Override
            public boolean isReady() {
                return true;
            }
            @Override
            public void setReadListener(ReadListener rl) {
                /* not used */
            }
            @Override
            public int read() {
                return is.read();
            }
        };
    }

    public String getCachedBodyStr() {
        try {
            return new String(cachedBody, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new UncheckedIOException(e);
        }
    }
}

