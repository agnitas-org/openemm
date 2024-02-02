/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.spring.security.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.log.LogMessage;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.InvalidCsrfTokenException;
import org.springframework.security.web.csrf.MissingCsrfTokenException;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class AgnCsrfFilter extends OncePerRequestFilter {

    private static final String HEADER_AND_COOKIE_NAME = "X-XSRF-TOKEN";
    private static final Set<String> ALLOWED_METHODS = Set.of("GET", "HEAD", "TRACE", "OPTIONS");

    private final CsrfTokenRepository tokenRepository;
    private final AccessDeniedHandlerImpl accessDeniedHandler;

    private static final Pattern PUSH_API_PATTERN = Pattern.compile("^.*/push-api/.*$");
    private static final Pattern MAILING_LOCK_PATTERN = Pattern.compile("^.*/mailing/ajax/\\d+/lock\\.action$");

    public AgnCsrfFilter() {
        this.tokenRepository = createTokenRepository();
        this.accessDeniedHandler = new AccessDeniedHandlerImpl();
    }

    private static CsrfTokenRepository createTokenRepository() {
        CookieCsrfTokenRepository cookieTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();

        cookieTokenRepository.setHeaderName(HEADER_AND_COOKIE_NAME);
        cookieTokenRepository.setCookieName(HEADER_AND_COOKIE_NAME);

        return cookieTokenRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse resp, FilterChain filterChain) throws ServletException, IOException {
        CsrfFilter.skipRequest(req);

        CsrfToken csrfToken = tokenRepository.loadToken(req);
        boolean missingToken = csrfToken == null;
        if (csrfToken == null) {
            csrfToken = generateNewToken(req, resp);
        }

        addCsrfAttributes(csrfToken, req);

        if (shouldSkipTokenComparison(req)) {
            filterChain.doFilter(req, resp);
            return;
        }

        String sentToken = getSentToken(req, csrfToken);

        if (Objects.equals(csrfToken.getToken(), sentToken)) {
            CsrfToken newCsrfToken = generateNewToken(req, resp);
            addCsrfAttributes(newCsrfToken, req);

            filterChain.doFilter(req, resp);
        } else {
            logger.debug(LogMessage.of(() -> "Invalid CSRF token found for " + UrlUtils.buildFullRequestUrl(req)));

            AccessDeniedException exception = !missingToken
                    ? new InvalidCsrfTokenException(csrfToken, sentToken)
                    : new MissingCsrfTokenException(sentToken);

            accessDeniedHandler.handle(req, resp, exception);
        }
    }

    private CsrfToken generateNewToken(HttpServletRequest req, HttpServletResponse resp) {
        CsrfToken csrfToken = tokenRepository.generateToken(req);
        tokenRepository.saveToken(csrfToken, req, resp);

        return csrfToken;
    }

    private static void addCsrfAttributes(CsrfToken csrfToken, HttpServletRequest req) {
        req.setAttribute(CsrfToken.class.getName(), csrfToken);
        req.setAttribute(csrfToken.getParameterName(), csrfToken);
    }

    private static boolean shouldSkipTokenComparison(HttpServletRequest req) {
        return isAllowedHttpMethod(req) || isSentFromUserWebForm(req) || isPushApiRequest(req) || isMailingLockRequest(req);
    }

    private static boolean isSentFromUserWebForm(HttpServletRequest req) {
        return req.getRequestURI().endsWith("form.action") || req.getRequestURI().endsWith("form.do");
    }

    private static boolean isPushApiRequest(final HttpServletRequest req) {
        return PUSH_API_PATTERN.matcher(req.getRequestURI()).matches();
    }

    private static boolean isMailingLockRequest(HttpServletRequest req) {
        return MAILING_LOCK_PATTERN.matcher(req.getRequestURI()).matches();
    }

    private static boolean isAllowedHttpMethod(HttpServletRequest req) {
        return ALLOWED_METHODS.contains(req.getMethod());
    }

    private static String getSentToken(HttpServletRequest req, CsrfToken csrfToken) {
        String token = req.getHeader(csrfToken.getHeaderName());
        if (token == null) {
            token = req.getParameter(csrfToken.getParameterName());
        }

        return token;
    }
}
