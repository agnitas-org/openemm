/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.util;


import com.agnitas.dao.LicenseDao;
import com.agnitas.util.XmlUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class LicenseParser {

    public static class PackageDefinitions {
        public String permissions;
        public Map<String, Map<Integer, String>> configValues = new LinkedHashMap<>();
        public String toString(){ return permissions+", configValues="+configValues; }
    }
    protected LicenseDao licenseDao;
    private static final Logger logger = LogManager.getLogger(LicenseParser.class);

    public LicenseParser(LicenseDao licenseDao) {
        this.licenseDao = licenseDao;
    }

    public  Map<String, Map<Integer, String>> parse() throws Exception {
        byte[] licenseDataArray = this.licenseDao.getLicenseData();

        Map<String, Map<Integer, String>> licenseData = new HashMap<>();
        Document licenseDocument = XmlUtilities.parseXMLDataAndXSDVerifyByDOM(licenseDataArray, "UTF-8", null);
        Element root = (Element) licenseDocument.getElementsByTagName("emm.license").item(0);

        Map<String, Map<Integer, String>> result = new LinkedHashMap<>();

        result.putAll(parseConfigElement(0, root));

        NodeList companies = root.getElementsByTagName("company");
        for (int i=0; i<companies.getLength();i++) {
            Element company = (Element) companies.item(i);
            String companyID = company.getAttribute("id");
            int id = companyID.isEmpty() ? 0 : Integer.parseInt(companyID);
            Map<String, Map<Integer, String>> companyResult = parseConfigElement(id, company);
            companyResult.forEach((key, inner) ->
                    result.merge(key, new HashMap<>(inner), (existing, incoming) -> {
                        Map<Integer,String> existingConfigValue = new HashMap<>(existing);
                        existingConfigValue.putAll(incoming);
                        return existingConfigValue;
                    }));


        }
        // Handle different names for license id
        Map<Integer, String> licenseID = result.remove("licenseID");
        if (licenseID != null) {
            result.put(ConfigValue.System_Licence.toString(), licenseID);
        }
        return result;
    }
    public void setLicenseDao(LicenseDao licenseDao) {
        this.licenseDao = licenseDao;
    }

    private static  Map<String, Map<Integer, String>> parseConfigElement(int id, Element element) {
        Map<String, Map<Integer, String>> map = new LinkedHashMap<>();
        NodeList children = element.getChildNodes();
        for (int i=0; i<children.getLength();i++) {
            Node item = children.item(i);
            if (item.getNodeType() != Node.ELEMENT_NODE) continue;

            Element child = (Element) item;
            String tag = child.getTagName();
            if ("allowedPremiumFeatures".equals(tag)) {
                PackageDefinitions packageDefinitions = parsePermissions(id, child);
                map.put("allowedPremiumFeatures", Map.of(id, packageDefinitions.permissions));
                map.putAll(packageDefinitions.configValues);
            } else if ("companies".equals(tag)) {
                continue;
            } else {
                String value = child.getTextContent().trim();
                map.put(tag, Map.of(id,value));
            }
        }
        return map;
    }

    private static PackageDefinitions parsePermissions(int id, Element permissionsElement) {
        PackageDefinitions packageDefinitions = new PackageDefinitions();
        StringBuilder textParts = new StringBuilder();

        NodeList children = permissionsElement.getChildNodes();
        for (int i=0; i<children.getLength(); i++) {
            Node childElement = children.item(i);
            if (childElement.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) childElement;
                if ("ConfigValue".equals(element.getTagName())) {
                    String name = element.getAttribute("name");
                    String value = element.getTextContent().trim();
                    packageDefinitions.configValues.put(name, Map.of(id, value));
                } else {
                    textParts.append(element.getTextContent());
                }
            }
            else if (childElement.getNodeType() == Node.TEXT_NODE) {
                String textContent = childElement.getTextContent();
                if (textContent != null) textParts.append(textContent);

            }
        }

        packageDefinitions.permissions = textParts.toString().replaceAll("\\s+", "\n").trim();
        return packageDefinitions;
    }

}
