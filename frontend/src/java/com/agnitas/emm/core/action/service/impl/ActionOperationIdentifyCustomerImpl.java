/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.agnitas.beans.Recipient;
import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.BeanLookupFactory;
import com.agnitas.beans.Company;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationIdentifyCustomerParameters;
import com.agnitas.emm.core.action.operations.ActionOperationType;
import com.agnitas.emm.core.action.service.EmmActionOperation;
import com.agnitas.emm.core.action.service.EmmActionOperationErrors;
import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.commons.uid.UIDFactory;

public class ActionOperationIdentifyCustomerImpl implements EmmActionOperation {
	
	/** The logger. */
	private static final Logger logger = LogManager.getLogger(ActionOperationIdentifyCustomerImpl.class);

	private ComCompanyDao companyDao;
	private RecipientService recipientService;
	private ExtensibleUIDService uidService;

	private BeanLookupFactory beanLookupFactory;
	
	private ConfigService configService;
	
	@Override
	public boolean execute(AbstractActionOperationParameters operation, Map<String, Object> params, final EmmActionOperationErrors actionOperationErrors) {
		ActionOperationIdentifyCustomerParameters op =(ActionOperationIdentifyCustomerParameters) operation;
		int companyID = op.getCompanyId();
		String keyColumn = op.getKeyColumn();
		String passColumn = op.getPassColumn();
		
        Recipient aCust = beanLookupFactory.getBeanRecipient();
		String keyVal = null;
		String passVal = null;
		@SuppressWarnings("unchecked")
		CaseInsensitiveMap<String, Object> reqParams = new CaseInsensitiveMap<>((Map<String, Object>) params.get("requestParameters"));
        aCust.setCompanyID(companyID);
        aCust.setCustDBStructure(recipientService.getRecipientDBStructure(companyID));

        keyVal=(String) reqParams.get(keyColumn.toUpperCase());

        if(passColumn.equals("none")) {
            recipientService.findByKeyColumn(aCust, keyColumn, keyVal);
        } else {
            passVal=(String) reqParams.get(passColumn);
            aCust.setCustomerID(recipientService.findByUserPassword(companyID, keyColumn, keyVal, passColumn, passVal));
        }

        if(aCust.getCustomerID()!=0) {
            aCust.setCustParameters(recipientService.getCustomerDataFromDb(companyID, aCust.getCustomerID(), aCust.getDateFormat()));

            params.put("customerID", aCust.getCustomerID());
            // generate new agnUID
            try {
            	// Create new-style UID.
                
                Company company = companyDao.getCompany(companyID);
                
                if( company != null) {
                    final ComExtensibleUID uid = UIDFactory.from(configService.getLicenseID(), aCust);
                    
                    String uidString = uidService.buildUIDString(uid);
                    params.put("agnUID", uidString);
                    if(!passColumn.equals("none")) {
                        params.put("authenticated", "1");
                    }
                    HttpSession session = ((HttpServletRequest)params.get("_request")).getSession();
                    @SuppressWarnings("unchecked")
					Map<String, Object> sessPar = (Map<String, Object>) session.getAttribute("agnFormParams");
					if (sessPar == null) {
						sessPar = new HashMap<>();
					}
                    sessPar.put("customerID", aCust.getCustomerID());
                    sessPar.put("agnUID", uidString);
					if (!passColumn.equals("none")) {
						sessPar.put("authenticated", "1");
					}
                    session.setAttribute("agnFormParams", sessPar);
                }
         
            } catch (Exception e) {
            	logger.error("problem generating new UID", e);
            }
        } else {
            if(params.containsKey("authenticated")) {
                return true;
            } else {
                return false;
            }
        }


        return true;
	}

    @Override
    public ActionOperationType processedType() {
        return ActionOperationType.IDENTIFY_CUSTOMER;
    }

    @Required
	public final void setCompanyDao(final ComCompanyDao dao) {
		this.companyDao = Objects.requireNonNull(dao, "Company DAO cannot be null");
	}

	@Required
	public final void setUidService(final ExtensibleUIDService service) {
		this.uidService = Objects.requireNonNull(service, "UID service cannot be null");
	}
	
	@Required
	public final void setBeanLookupFactory(final BeanLookupFactory factory) {
		this.beanLookupFactory = Objects.requireNonNull(factory, "Bean lookup factory cannot be null");
	}

	@Required
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "Config service cannot be null");
	}

	@Required
    public void setRecipientService(RecipientService recipientService) {
        this.recipientService = recipientService;
    }
}
