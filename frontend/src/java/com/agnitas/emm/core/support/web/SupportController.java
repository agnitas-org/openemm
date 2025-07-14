/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.support.web;

import static com.agnitas.util.Const.Mvc.MESSAGES_VIEW;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.messages.I18nString;
import com.agnitas.web.UserFormSupportForm;
import com.agnitas.web.dto.DataResponseDto;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.Anonymous;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

public class SupportController implements XssCheckAware {

    private static final Logger logger = LogManager.getLogger(SupportController.class);

    protected final JavaMailService javaMailService;
    protected final ConfigService configService;
    private final String formNotFoundEmailTemplate;
    private final String formNotFoundUrlParameterTemplate;

    public SupportController(JavaMailService javaMailService, ConfigService configService,
                             String formNotFoundEmailTemplate,
                             String formNotFoundUrlParameterTemplate) {
        this.javaMailService = javaMailService;
        this.configService = configService;
        this.formNotFoundEmailTemplate = formNotFoundEmailTemplate;
        this.formNotFoundUrlParameterTemplate = formNotFoundUrlParameterTemplate;
    }

    @GetMapping("/help-center.action")
    public String helpCenter(@RequestParam(name = "helpKey", required = false) String helpKey, Model model) {
        model.addAttribute("helpKey", helpKey);
        addHelpCenterAttrs(model);
        return "help_center";
    }

    protected void addHelpCenterAttrs(Model model) {
        // empty for OpenEMM
    }

    @PostMapping("/sendMessage.action")
    public ResponseEntity<DataResponseDto<String>> sendSupportMessage(@RequestParam String content, Admin admin, Popups popups) {
        String answer = RandomStringUtils.randomAlphabetic(5, 250);
        return ResponseEntity.ok(new DataResponseDto<>(answer, popups, true));
    }

    @Anonymous
    @PostMapping("/sendFormReport.action")
    public String sendFormReport(UserFormSupportForm supportForm, Popups popups) {
        String supportAddress = configService.getValue(ConfigValue.Mailaddress_Support);
        Map<String, List<String>> params = supportForm.getParams();

        if (isParamValueEmpty(params, "agnCI") || isParamValueEmpty(params, "agnFN")) {
            logger.warn("formSupport: couldn't send error report");
            popups.alert("FormNotFoundSendMissingInformation");
        } else {
            trySendErrorReportEmail(supportForm, popups, supportAddress);
        }
        return MESSAGES_VIEW;
    }

    private void trySendErrorReportEmail(UserFormSupportForm supportForm, Popups popups, String supportAddress) {
        String messageBody = buildMessageBody(supportForm);
        if (javaMailService.sendEmail(0, supportAddress, I18nString.getLocaleString("FormNotFoundTitle", "de"), messageBody, messageBody)) {
            popups.success("FormNotFoundSent");
        } else {
            logger.warn("formSupport: couldn't send error report");
            popups.alert("FormNotFoundSendFailed");
        }
    }

    private String buildMessageBody(UserFormSupportForm supportForm) {
        return formNotFoundEmailTemplate
                .replace("%URL%", supportForm.getUrl())
                .replace("%PARAMLIST%", buildParameterList(supportForm.getParams()));
    }

    private String buildParameterList(Map<String, List<String>> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> param : params.entrySet()) {
            List<String> paramValues = ListUtils.emptyIfNull(param.getValue());
            if (paramValues.size() == 1) {
                sb.append(formNotFoundUrlParameterTemplate
                        .replace("%PARAM%", param.getKey())
                        .replace("%VALUE%", paramValues.get(0)));
            } else {
                paramValues.forEach(value -> sb.append(formNotFoundUrlParameterTemplate
                        .replace("%PARAM%", param.getKey())
                        .replace("%VALUE%", value)));
            }
        }
        return sb.toString();
    }

    private boolean isParamValueEmpty(Map<String, List<String>> params, String paramName) {
        return StringUtils.isBlank(params.getOrDefault(paramName, Collections.singletonList("")).get(0));
    }
}
