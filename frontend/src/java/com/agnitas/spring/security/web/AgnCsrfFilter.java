/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.spring.security.web;

import java.io.IOException;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import com.agnitas.util.AgnUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.log.LogMessage;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class AgnCsrfFilter extends OncePerRequestFilter {

    private static final String HEADER_AND_COOKIE_NAME = "X-XSRF-TOKEN";
    private static final Set<String> ALLOWED_METHODS = Set.of("GET", "HEAD", "TRACE", "OPTIONS");

    private final CsrfTokenRepository tokenRepository;

    private static final Pattern PUSH_API_PATTERN = Pattern.compile("^.*/push-api/.*$");
    private static final Pattern WIDGET__PATTERN = Pattern.compile("^.*/widget/.*$");
    private static final Pattern MAILING_LOCK_PATTERN = Pattern.compile("^.*/mailing/ajax/\\d+/lock\\.action$");
    private static final Pattern SSO_PATTERN = Pattern.compile("^.*/sso(?:Select)?.action$");

    private static final String ERROR_PAGE_URL = "/csrf/error.action";

    public AgnCsrfFilter() {
        this.tokenRepository = createTokenRepository();
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
            logger.warn(LogMessage.of(() -> "Invalid CSRF token found for %s".formatted(getRequestUrl(req))));

            resp.setStatus(HttpStatus.FORBIDDEN.value());
            req.getRequestDispatcher(ERROR_PAGE_URL).forward(req, resp);
        }
    }

    private CsrfToken generateNewToken(HttpServletRequest req, HttpServletResponse resp) {
        CsrfToken csrfToken = tokenRepository.generateToken(req);
        tokenRepository.saveToken(csrfToken, req, resp);
        addSameSitePolicyToCookie(resp);

        return csrfToken;
    }

    private void addSameSitePolicyToCookie(HttpServletResponse resp) {
        resp.getHeaders("Set-Cookie")
                .stream()
                .filter(header -> StringUtils.startsWith(header, HEADER_AND_COOKIE_NAME))
                .findFirst()
                .ifPresent(header -> resp.setHeader("Set-Cookie", header + "; SameSite=Lax"));
    }

    private void addCsrfAttributes(CsrfToken csrfToken, HttpServletRequest req) {
        req.setAttribute(CsrfToken.class.getName(), csrfToken);
        req.setAttribute(csrfToken.getParameterName(), csrfToken);
    }

    private boolean shouldSkipTokenComparison(HttpServletRequest req) {
        return isAllowedHttpMethod(req) || isSentFromUserWebForm(req) || isPushApiRequest(req) || isMailingLockRequest(req)
                || isSsoRequest(req) || isSentFromWidget(req);
    }

    private boolean isSentFromUserWebForm(HttpServletRequest req) {
        return req.getRequestURI().endsWith("form.action") || req.getRequestURI().endsWith("form.do");
    }

    private boolean isSentFromWidget(HttpServletRequest req) {
        return WIDGET__PATTERN.matcher(req.getRequestURI()).matches();
    }

    private boolean isPushApiRequest(final HttpServletRequest req) {
        return PUSH_API_PATTERN.matcher(req.getRequestURI()).matches();
    }

    private boolean isMailingLockRequest(HttpServletRequest req) {
        return MAILING_LOCK_PATTERN.matcher(req.getRequestURI()).matches();
    }

    private boolean isSsoRequest(final HttpServletRequest request) {
        return SSO_PATTERN.matcher(request.getRequestURI()).matches();
    }

    private boolean isAllowedHttpMethod(HttpServletRequest req) {
        return ALLOWED_METHODS.contains(req.getMethod());
    }

    private String getSentToken(HttpServletRequest req, CsrfToken csrfToken) {
        String token = req.getHeader(csrfToken.getHeaderName());
        if (token == null) {
            token = req.getParameter(csrfToken.getParameterName());
        }

        return token;
    }

    private String getRequestUrl(HttpServletRequest request) {
        return AgnUtils.removeJsessionIdFromUrl(UrlUtils.buildFullRequestUrl(request));
    }

}
