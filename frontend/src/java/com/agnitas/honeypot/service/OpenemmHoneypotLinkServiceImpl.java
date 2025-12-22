package com.agnitas.honeypot.service;

import jakarta.servlet.http.HttpServletRequest;

public class OpenemmHoneypotLinkServiceImpl implements HoneypotLinkService {

    @Override
    public boolean isShowHoneypotLinkRequestIntermediatePage(String targetUrl, HttpServletRequest request, int companyId) {
        return false;
    }

}
