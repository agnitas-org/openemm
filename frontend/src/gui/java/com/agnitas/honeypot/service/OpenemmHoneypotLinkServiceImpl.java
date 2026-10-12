package com.agnitas.honeypot.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class OpenemmHoneypotLinkServiceImpl implements HoneypotLinkService {

    @Override
    public boolean isShowHoneypotLinkRequestIntermediatePage(String targetUrl, HttpServletRequest request, int companyId) {
        return false;
    }

    @Override
    public String getTokenForUrl(String url) {
        throw new UnsupportedOperationException("Not supported.");
    }
}
