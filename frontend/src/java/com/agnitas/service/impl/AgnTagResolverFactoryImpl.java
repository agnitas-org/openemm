/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;

import org.agnitas.beans.Recipient;
import org.agnitas.beans.TagDefinition;
import org.agnitas.beans.TagDetails;
import org.agnitas.beans.Title;
import org.agnitas.beans.factory.RecipientFactory;
import org.agnitas.dao.TagDao;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.RequiredInformationMissingException;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.UIDStringBuilderException;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.ComTitleDao;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.commons.uid.UIDFactory;
import com.agnitas.service.AgnTagResolver;
import com.agnitas.service.AgnTagResolverFactory;

public class AgnTagResolverFactoryImpl implements AgnTagResolverFactory {
    private static final String DEFAULT_LANGUAGE = "de";
    private static final String DEFAULT_COUNTRY = "DE";

    private RecipientFactory recipientFactory;

    private ExtensibleUIDService extensibleUidService;

    private ComCompanyDao companyDao;
    private ComRecipientDao recipientDao;
    private ComMailingDao mailingDao;
    private ComTitleDao titleDao;
    private TagDao tagDao;
    private ConfigService configService;

    @Override
    public AgnTagResolver create(@VelocityCheck int companyId, int mailingId, int mailingListId, int customerId) {
        return new AgnTagResolverImpl(companyId, mailingId, mailingListId, customerId);
    }

    @Override
    public AgnTagResolver create(@VelocityCheck int companyId) {
        return new AgnTagResolverImpl(companyId, 0, 0, 0);
    }

    @Required
    public final void setRecipientFactory(final RecipientFactory factory) {
        this.recipientFactory = Objects.requireNonNull(factory, "Recipient factory cannot be null");
    }

    @Required
    public final void setExtensibleUidService(final ExtensibleUIDService service) {
        this.extensibleUidService = Objects.requireNonNull(service, "UID service cannot be null");
    }

    @Required
    public final void setCompanyDao(final ComCompanyDao dao) {
        this.companyDao = Objects.requireNonNull(dao, "Company DAO cannot be null");
    }

    @Required
    public final void setRecipientDao(final ComRecipientDao dao) {
        this.recipientDao = Objects.requireNonNull(dao, "Recipient DAO cannot be null");
    }

    @Required
    public final void setMailingDao(final ComMailingDao dao) {
        this.mailingDao = Objects.requireNonNull(dao, "Mailing DAO cannot be null");
    }

    @Required
    public final void setTitleDao(final ComTitleDao dao) {
        this.titleDao = Objects.requireNonNull(dao, "Title DAO cannot be null");
    }

    @Required
    public final void setTagDao(final TagDao dao) {
        this.tagDao = Objects.requireNonNull(dao, "Tag DAO cannot be null");
    }
    
    @Required
    public final void setConfigService(final ConfigService service) {
    	this.configService = Objects.requireNonNull(service, "Config Service cannot be null");
    }

    private class AgnTagResolverImpl implements AgnTagResolver {
        private final Logger logger = Logger.getLogger(AgnTagResolverImpl.class);

        private int companyId;
        private int mailingId;
        private int mailingListId;
        private int customerId;

        private Map<String, String> options;
        private String name;

        private String redirectDomain;
        private Map<String, Object> customerData;
        private Map<Integer, String> dateFormatCache = new HashMap<>();
        private Map<Integer, Title> titlesCache = new HashMap<>();
        private Map<String, TagDefinition> definitionCache = new HashMap<>();

        public AgnTagResolverImpl(int companyId, int mailingId, int mailingListId, int customerId) {
            this.companyId = companyId;
            this.mailingId = mailingId;
            this.mailingListId = mailingListId;
            this.customerId = customerId;
        }

        @Override
		public String resolve(TagDetails tag) {
            initialize(tag);

            switch (name) {
	            case "agnONEPIXEL":
	                return resolveAgnOnePixel();
	
	            case "agnDATE":
	                return resolveAgnDate();
	
	            case "agnTITLE":
	            case "agnTITLEFIRST":
	            case "agnTITLEFULL":
	                return resolveAgnTitle();
	
	            case "agnPROFILE":
	                return resolveAgnProfile();
	
	            case "agnUNSUBSCRIBE":
	                return resolveAgnUnsubscribe();
	
	            case "agnFORM":
	                return resolveAgnForm();
	                
				default:
					return resolveCustomAgnTag(tag);
            }
        }

        /**
         * Resolve an agn-tag {@code agnDATE}.
         */
        private String resolveAgnDate() {
            return getDateFormat(getOptionType()).format(new Date());
        }

        /**
         * Resolve an agn-tags {@code agnTITLE}, {@code agnTITLEFIRST} and {@code agnTITLEFULL}.
         */
        private String resolveAgnTitle() {
            try {
                return resolveAgnTitle(getOptionType());
            } catch (Exception e) {
                return null;
            }
        }

        /**
         * Resolve an agn-tags {@code agnTITLE}, {@code agnTITLEFIRST} and {@code agnTITLEFULL}.
         */
        private String resolveAgnTitle(int titleId) {
            Map<Integer, String> genderMap = getGenderMap(titleId);
            if (genderMap == null) {
                return null;
            }

            String firstName = StringUtils.trimToEmpty((String) getCustomerValue("firstname"));
            String lastName = StringUtils.trimToEmpty((String) getCustomerValue("lastname"));
            String title = StringUtils.trimToEmpty((String) getCustomerValue("title"));

            int gender = Title.GENDER_UNKNOWN;
            // Generate salutation based on GENDER_UNKNOWN if last name isn't available.
            if (StringUtils.isNotEmpty(lastName)) {
                gender = NumberUtils.toInt((String) getCustomerValue("gender"), gender);
            }

            String value = genderMap.get(gender);
            if (gender == Title.GENDER_UNKNOWN) {
                return value;
            }

            if (!title.isEmpty()) {
                value += " " + title;
            }

            switch (name) {
	            case "agnTITLE":
	                return value + " " + lastName;
	            case "agnTITLEFIRST":
	                return genderMap.get(gender) + " " + firstName;
	            case "agnTITLEFULL":
	                return value + " " + firstName + " " + lastName;
				default:
					return value;
            }
        }

        private String resolveAgnProfile() {
            return resolveAgnForm("profile");
        }

        private String resolveAgnUnsubscribe() {
            return resolveAgnForm("unsubscribe");
        }

        private String resolveAgnForm() {
            return resolveAgnForm(options.get("name"));
        }

        private String resolveAgnForm(String formName) {
            return getRedirectDomain() + "/form.action?agnCI=" + companyId + "&agnFN=" + formName + "&agnUID=##AGNUID##";
        }

        /**
         * Resolve an agn-tag {@code agnONEPIXEL}.
         */
        private String resolveAgnOnePixel() {
            return "";
        }

        private String getUid() {
        	final int licenseID = configService.getLicenseID();
            final ComExtensibleUID uid = UIDFactory.from(licenseID, companyId, customerId, mailingId);

            try {
                return extensibleUidService.buildUIDString(uid);
            } catch (UIDStringBuilderException | RequiredInformationMissingException e) {
                logger.error("UID building error: " + e.getMessage(), e);
                return "";
            }
        }

        private String resolveCustomAgnTag(TagDetails tag) {
            TagDefinition definition = getTagDefinition();
            if (definition == null) {
                return null;
            }

            String value = definition.getSelectValue();

            if (definition.getType() == TagDefinition.TagType.COMPLEX) {
                value = resolveComplexParameters(value);
                if (value == null) {
                    return null;
                }
            }

            if (value.contains("[agnUID]")) {
                value = value.replace("[agnUID]", getUid());
            }

            value = recipientDao.getField(value, customerId, companyId);
            if (value == null) {
                logger.error("Error processing tag " + name + " (" + tag.getFullText() + "). (mailing #" + mailingId + ", customer #" + customerId + ")");
            }

            return value;
        }

        private void initialize(TagDetails tag) {
            name = StringUtils.defaultString(tag.getTagName());

            options = tag.getTagParameters();
            if (options == null) {
                options = Collections.emptyMap();
            }
        }

        private String getOption(String key, String defaultValue) {
            String value = options.get(key);
            if (value == null) {
                return defaultValue;
            }
            return value;
        }

        private int getOptionType() {
            return NumberUtils.toInt(options.get("type"));
        }

        private Locale getLocale() {
            String language = getOption("language", DEFAULT_LANGUAGE);
            String country = getOption("country", DEFAULT_COUNTRY);

            return new Locale(language, country);
        }

        private SimpleDateFormat getDateFormat(int type) {
            String pattern = dateFormatCache.computeIfAbsent(type, mailingDao::getFormat);
            return new SimpleDateFormat(pattern, getLocale());
        }

        private Map<Integer, String> getGenderMap(int titleId) {
            Title title = titlesCache.computeIfAbsent(titleId, id -> titleDao.getTitle(id, companyId));

            if (title == null) {
                return null;
            }

            return title.getTitleGender();
        }

        private Object getCustomerValue(String key) {
            if (customerData == null) {
                Recipient recipient = recipientFactory.newRecipient(companyId);
                recipient.setCustomerID(customerId);
                customerData = recipient.getCustomerDataFromDb();
            }
            return customerData.get(key);
        }

        private String getRedirectDomain() {
            if (redirectDomain == null) {
                redirectDomain = companyDao.getRedirectDomain(companyId);
            }
            return StringUtils.defaultString(redirectDomain);
        }

        private TagDefinition getTagDefinition() {
            return definitionCache.computeIfAbsent(name, nameParameter -> {
                TagDefinition definition = tagDao.getTag(companyId, nameParameter);
                if (definition == null) {
                    logger.error("Unknown tag " + nameParameter + " (no definition available)");
                    return null;
                }

                String value = definition.getSelectValue();

                value = value.replace("[company-id]", Integer.toString(companyId))
                    .replace("[mailinglist-id]", Integer.toString(mailingListId))
                    .replace("[mailing-id]", Integer.toString(mailingId));

                if (value.contains("[rdir-domain]")) {
                    value = value.replace("[rdir-domain]", getRedirectDomain());
                }

                definition.setSelectValue(value);

                return definition;
            });
        }

        private String resolveComplexParameters(String value) {
            StringBuffer sb = new StringBuffer();
            Matcher matcher = TagDefinition.COMPLEX_PARAMETER_PATTERN.matcher(value);

            while (matcher.find()) {
                String parameterName = matcher.group();
                String parameterValue = options.get(parameterName);
                if (parameterValue == null) {
                    return null;
                }

                matcher.appendReplacement(sb, parameterValue.replace("'", "''"));
            }
            matcher.appendTail(sb);

            return sb.toString();
        }
    }
}
