/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.recipient.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.agnitas.beans.ProfileField;
import org.agnitas.beans.Recipient;
import org.agnitas.beans.factory.RecipientFactory;
import org.agnitas.emm.core.recipient.service.RecipientBulkService;
import org.agnitas.emm.core.recipient.service.RecipientModel;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.emm.core.service.impl.AbstractBulkServiceImpl;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.core.validator.ModelValidator;
import org.agnitas.emm.springws.endpoint.Utils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.Tuple;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.ComRecipientDao;

public final class RecipientBulkServiceImpl extends AbstractBulkServiceImpl<RecipientModel> implements RecipientBulkService {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(RecipientBulkServiceImpl.class);

	private ComRecipientDao recipientDao;
	private RecipientService recipientService;
	private ModelValidator validator;
	private RecipientFactory recipientFactory;

	private int validateModels(String annotation, final List<RecipientModel> models, boolean ignoreErrors, Object dummyResult, List<Object> results) {
		int invalidCnt = 0;
		results.clear();
		for (RecipientModel recipientModel : models) {
			try {
				validator.validate(annotation, recipientModel);
				results.add(dummyResult);
			} catch (Exception e) {
				results.add(e);
				invalidCnt++;
				if (!ignoreErrors) {
					break;
				}
			}
		}
		return invalidCnt;
	}
	
	private List<Object> validatedOperation(final List<RecipientModel> models, final boolean ignoreErrors, 
			String annotation, Object dummyResult, ProcessRecipientModels todo) {
		
		List<Object> results = new ArrayList<>();
		// Validate each model in list with required 'annotation' rule
		int invalids = validateModels(annotation, models, ignoreErrors, dummyResult, results);
		if (!ignoreErrors && invalids > 0) {
			return results;
		}
		
		// Filter valid models
		final List<RecipientModel> validModels = new ArrayList<>();
		for (int i = 0; i < results.size(); i++) {
			if (results.get(i) == dummyResult) {
				validModels.add(models.get(i));
			}
		}
		
		// Run required operation for valid models only
		List<Object> validResults = todo.exec(validModels);
		
		// Merge validation and operation results 
		int k = 0;
		for (int i = 0; i < results.size(); i++) {
			if (k >= validResults.size()) {
				break;
			}
			if (results.get(i) == dummyResult) {
				results.set(i, validResults.get(k++));
			}
		}
		return results;
	}

	@Override
	public List<Object> addSubscriber(final List<RecipientModel> models, final boolean ignoreErrors, String username, final int companyID, List<UserAction> userActions) {
		int defaultDataSourceID = recipientDao.getDefaultDatasourceID(Utils.getUserName(), Utils.getUserCompany());
		return validatedOperation(models, ignoreErrors, "addSubscriber", 0 /*RecipientBulkNotAppliedException()*/, validModels -> {
			if (ignoreErrors) {
				return addCustomersIgnoreErrors(validModels, defaultDataSourceID, username, companyID, userActions);
			} else {
				return transaction(status -> {
					List<Object> result = addCustomers(validModels, defaultDataSourceID, userActions);
					if (result.isEmpty()) {
						status.setRollbackOnly();
					}
					return result;
				});
			}
		});
	}
	
	private List<Object> processCustomersIgnoreErrors(List<RecipientModel> models, boolean lock, 
			final ProcessRecipientModels todo, ProcessRecipientModels todoSafe) {
		
		final int PACK_SIZE = 200;
		List<Object> result = new ArrayList<>();

		int startPosition = 0;
		while (startPosition < models.size()) {
			int endPosition = startPosition + PACK_SIZE;
			if (endPosition > models.size()) {
				endPosition = models.size();
			}
			final List<RecipientModel> modelsSublist = models.subList(startPosition, endPosition);
			
			// attempt to add with batch
			List<Object> res = transaction(status -> {
				if (lock) {
					int companyId = modelsSublist.get(0).getCompanyId(); // Assume that all models has same companyId
					recipientDao.lockCustomers(companyId, getIDs(modelsSublist));
				}
				List<Object> result1 = todo.exec(modelsSublist);
				if (result1.isEmpty()) {
					status.setRollbackOnly();
				}
				return result1;
			});
			
			// in case of batch fault - add by one
			if (res.isEmpty()) {
				res = transaction(status -> {
					if (lock) {
						int companyId = modelsSublist.get(0).getCompanyId(); // Assume that all models has same companyId
						recipientDao.lockCustomers(companyId, getIDs(modelsSublist));
					}
					return todoSafe.exec(modelsSublist);
				});
			}
			result.addAll(res);
			startPosition = endPosition;
		}
		return result;
	}
	
	private List<Object> addCustomersIgnoreErrors(List<RecipientModel> models, int defaultDataSourceID, String username, final int companyID, List<UserAction> userActions) {
		return processCustomersIgnoreErrors(models, false, subList -> addCustomersChunkIgnoreErrors(subList, defaultDataSourceID, userActions), subList -> processBulk(subList, true, new BulkOperation() {
			@Override
			public Object run(RecipientModel model) throws Exception {
				return recipientService.addSubscriber(model, username, companyID, userActions);
			}
		}));
	}
	
	private List<Object> updateCustomersIgnoreErrors(List<RecipientModel> models, String username, List<UserAction> userActions) {
		return processCustomersIgnoreErrors(models, true, subList -> updateCustomersChunk(subList, userActions), subList -> processBulk(subList, true, new BulkOperation() {
			@Override
			public Object run(RecipientModel model) throws Exception {
				return recipientService.updateSubscriber(model, username);
			}
		}));
	}

	private List<Object> addCustomersChunkIgnoreErrors(List<RecipientModel> models, int defaultDataSourceID, List<UserAction> userActions) {
		List<Recipient> toInsert = new ArrayList<>();
		List<Boolean> doubleCheck = new ArrayList<>();
		List<Boolean> overwrite = new ArrayList<>();
		List<String> keyFields = new ArrayList<>();
		
		int companyID = -1;
		for (RecipientModel model : models) {
			if (model.getCompanyId() == 0) {
				logger.error("addCustomersChunckIgnoreErrors: model.getCompanyId() == 0 for model " + model.toString());
				return Collections.emptyList();
			}
			if (companyID == -1) {
				companyID = model.getCompanyId();
			} else if (model.getCompanyId() != companyID) {
				logger.error("addCustomersChunckIgnoreErrors: model.getCompanyId differs for model " + model.toString());
				return Collections.emptyList();
			}
			model.setEmail(model.getEmail().toLowerCase());
			
			final Recipient aCust = this.recipientFactory.newRecipient(companyID);
	        Map<String, String> customerFieldTypes = aCust.getCustDBStructure();
	        for (Entry<String, Object> entry : model.getParameters().entrySet()) {
				String name = entry.getKey();
				String value = (String) entry.getValue();

				if ("DATE".equalsIgnoreCase(customerFieldTypes.get(name))) {
					try {
						Date newValue = DateUtilities.parseIso8601DateTimeString(value);
						if (newValue != null) {
							Calendar calendar = new GregorianCalendar();
							calendar.setTime(newValue);
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, Integer.toString(calendar.get(Calendar.YEAR)));
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, Integer.toString(calendar.get(Calendar.MONTH) + 1));
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)));
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR, Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)));
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE, Integer.toString(calendar.get(Calendar.MINUTE)));
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND, Integer.toString(calendar.get(Calendar.SECOND)));
						} else {
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, "");
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, "");
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, "");
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR, "");
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE, "");
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND, "");
						}
					} catch (Exception e) {
						logger.error("Invalid date value: " + e.getMessage(), e);
						throw new RuntimeException("Invalid date value: " + e.getMessage(), e);
					}
				} else {
					aCust.setCustParameters(name, value);
				}
	        }
	        recipientService.supplySourceID(aCust, defaultDataSourceID);
        	toInsert.add(aCust);
        	doubleCheck.add(model.isDoubleCheck());
        	overwrite.add(model.isOverwrite());
        	keyFields.add(model.getKeyColumn());
		}
		
		List<Object> results = recipientDao.insertCustomers(companyID, toInsert, doubleCheck, overwrite, keyFields);

        StringBuilder description = new StringBuilder("Recipient");
        if (results.size() > 1) {
            description.append("s");
        }

        description
				.append(" ")
				.append(StringUtils.join(results, ", "))
				.append(" created");

        userActions.add(new UserAction("create recipient", description.toString()));

        return results;
	}

	private interface ProcessRecipientModels {
		List<Object> exec(List<RecipientModel> models);
	}

	private List<Object> processCustomers(List<RecipientModel> models, ProcessRecipientModels todo) {
		final int PACK_SIZE = 1000;
		List<Object> result = new ArrayList<>();

		int startPosition = 0;
		while (startPosition < models.size()) {
			int endPosition = startPosition + PACK_SIZE;
			if (endPosition > models.size()) {
				endPosition = models.size();
			}
			final List<RecipientModel> modelsSublist = models.subList(startPosition, endPosition);
			List<Object> res;
			res = todo.exec(modelsSublist);
			result.addAll(res);
			startPosition = endPosition;
		}
		return result;
	}
	
	private List<Object> addCustomers(final List<RecipientModel> models, int defaultDataSourceID, List<UserAction> userActions) {
		return  addCustomersChunk(models, defaultDataSourceID, userActions);
	}

	/**
	 * For bulk insert/update of customers.
	 * If recipients' CompanyId differs for any recipients, or companyID == 0 - it's assumed as error.
	 * 
	 * @param models CompanyId should be the same for all models in list.
	 * @return list of recipient ID's or empty list in case of errors
	 */
	private List<Object> addCustomersChunk(List<RecipientModel> models, int defaultDataSourceID, List<UserAction> userActions) {
		List<Recipient> toInsert = new ArrayList<>();
		List<Boolean> doubleCheck = new ArrayList<>();
		List<Boolean> overwrite = new ArrayList<>();
		List<String> keyFields = new ArrayList<>();

		int companyID = -1;
		for (RecipientModel model : models) {
			if (model.getCompanyId() == 0) {
				logger.error("addSubscriberInternal: model.getCompanyId() == 0 for model " + model.toString());
				return Collections.emptyList();
			}
			if (companyID == -1) {
				companyID = model.getCompanyId();
			} else if (model.getCompanyId() != companyID) {
				logger.error("addSubscriberInternal: model.getCompanyId differs for model " + model.toString());
				return Collections.emptyList();
			}
			model.setEmail(model.getEmail().toLowerCase());
			
			final Recipient aCust = this.recipientFactory.newRecipient(companyID);
	        
	        Map<String, String> customerFieldTypes = aCust.getCustDBStructure();
	        for (Entry<String, Object> entry : model.getParameters().entrySet()) {
				String name = entry.getKey();
				String value = (String) entry.getValue();

				if ("DATE".equalsIgnoreCase(customerFieldTypes.get(name))) {
					try {
						Date newValue = DateUtilities.parseIso8601DateTimeString(value);
						if (newValue != null) {
							Calendar calendar = new GregorianCalendar();
							calendar.setTime(newValue);
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, Integer.toString(calendar.get(Calendar.YEAR)));
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, Integer.toString(calendar.get(Calendar.MONTH) + 1));
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)));
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR, Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)));
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE, Integer.toString(calendar.get(Calendar.MINUTE)));
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND, Integer.toString(calendar.get(Calendar.SECOND)));
						} else {
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, "");
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, "");
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, "");
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR, "");
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE, "");
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND, "");
						}
					} catch (Exception e) {
						logger.error("Invalid date value: " + e.getMessage(), e);
						throw new RuntimeException("Invalid date value: " + e.getMessage(), e);
					}
				} else {
					aCust.setCustParameters(name, value);
				}
	        }
	        recipientService.supplySourceID(aCust, defaultDataSourceID);

        	toInsert.add(aCust);
			doubleCheck.add(model.isDoubleCheck());
			overwrite.add(model.isOverwrite());
			keyFields.add(model.getKeyColumn());
		}
		List<Integer> customerIds = toInsert.stream().map(Recipient::getCustomerID).collect(Collectors.toList());
		recipientDao.lockCustomers(companyID, customerIds);
        List<Object> results = recipientDao.insertCustomers(companyID, toInsert, doubleCheck, overwrite, keyFields);

        StringBuilder description = new StringBuilder("Recipient");
        if (results.size() > 1) {
            description.append("s");
        }
        description
				.append(" ")
				.append(StringUtils.join(results, ", "))
				.append(" created");
        userActions.add(new UserAction("create recipient", description.toString()));

        return results;
	}


	private List<Object> updateCustomers(List<RecipientModel> models, List<UserAction> userActions) {
		return processCustomers(models, subList -> updateCustomersChunk(subList, userActions));
	}

	/**
	 * For bulk update of recipients. 
	 * isDoubleCheck & isOverwrite don't matter.
	 * If recipients' CompanyId differs for any recipients, or companyID == 0 - it's assumed as error. 
	 * 
	 * @param models CompanyId should be the same for all models in list.
	 * @return successful updated flags list
	 */
	private List<Object> updateCustomersChunk(List<RecipientModel> models, List<UserAction> userActions) {
		List<Recipient> toUpdate = new ArrayList<>();

		StringBuilder description = new StringBuilder("Recipient");
        if (models.size() > 1) {
            description.append("s");
        }
        description.append(" ");

		int companyID = -1;
		boolean isFirst = true;
        for (RecipientModel model : models) {
        	if (isFirst) {
        		isFirst = false;
        	} else {
                description.append(", ");
            }
			if (model.getCompanyId() == 0) {
				logger.error("updateCustomersChunk: model.getCompanyId() == 0 for model " + model.toString());
				return Collections.emptyList();
			}
			if (companyID == -1) {
				companyID = model.getCompanyId();
			} else if (model.getCompanyId() != companyID) {
				logger.error("updateCustomersChunk: model.getCompanyId differs for model " + model.toString());
				return Collections.emptyList();
			}
			
			String email = model.getEmail();
			if (email != null) {
				model.setEmail(email.toLowerCase());
			}
			
			final Recipient aCust = this.recipientFactory.newRecipient(companyID);
	        aCust.setCustomerID(model.getCustomerId());

	        Map<String, String> customerFieldTypes = aCust.getCustDBStructure();
	        for (Entry<String, Object> entry : model.getParameters().entrySet()) {
				String name = entry.getKey();
				String value = (String) entry.getValue();

				if ("DATE".equalsIgnoreCase(customerFieldTypes.get(name))) {
					try {
						Date newValue = DateUtilities.parseIso8601DateTimeString(value);
						if (newValue != null) {
								Calendar calendar = new GregorianCalendar();
								calendar.setTime(newValue);
								aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, Integer.toString(calendar.get(Calendar.YEAR)));
								aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, Integer.toString(calendar.get(Calendar.MONTH) + 1));
								aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)));
								aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR, Integer.toString(calendar.get(Calendar.HOUR_OF_DAY)));
								aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE, Integer.toString(calendar.get(Calendar.MINUTE)));
								aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND, Integer.toString(calendar.get(Calendar.SECOND)));
						} else {
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, "");
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, "");
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, "");
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR, "");
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE, "");
							aCust.setCustParameters(name + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND, "");
						}
					} catch (Exception e) {
						logger.error("Invalid date value: " + e.getMessage(), e);
						throw new RuntimeException("Invalid date value: " + e.getMessage(), e);
					}
				} else {
					aCust.setCustParameters(name, value);
				}
			}

	       	toUpdate.add(aCust);

            description.append(model.getCustomerId());
		}

        description.append(" updated");
        userActions.add(new UserAction("edit recipient", description.toString()));

        return recipientDao.updateCustomers(companyID, toUpdate);
	}

	@Override
	public List<Object> updateSubscriber(final List<RecipientModel> models, final boolean ignoreErrors, String username, List<UserAction> userActions) {
		return validatedOperation(models, ignoreErrors, "updateSubscriber",
			Boolean.FALSE, validModels -> {
				if (ignoreErrors) {
					return updateCustomersIgnoreErrors(validModels, username, userActions);
				} else {
					return transaction(status -> {
						if (validModels.isEmpty()) {
							return Collections.emptyList();
						}
						int companyId = validModels.get(0).getCompanyId(); // Assume that all models has same companyId
						recipientDao.lockCustomers(companyId, getIDs(validModels));
						List<Object> result = updateCustomers(validModels, userActions);
						if (result.isEmpty()) {
							status.setRollbackOnly();
						}
						return result;
					});
				}
			});
	}

	private List<Integer> getIDs(List<RecipientModel> models) {
		List<Integer> ids = new ArrayList<>();
		for (RecipientModel recipientModel : models) {
			ids.add(recipientModel.getCustomerId());
		}
		return ids;
	}

//	@Override
//	public List<Object> deleteSubscriber(List<RecipientModel> models, boolean ignoreErrors) {
//		return processBulk(models, ignoreErrors, new BulkOperationWithoutResult() {
//			@Override
//			protected void runWithoutResult(RecipientModel model) {
//				recipientService.deleteSubscriber(model);
//			}
//		});
//	}
	@Override
	public List<Object> deleteSubscriber(final List<RecipientModel> models, boolean ignoreErrors) {
		return validatedOperation(models, ignoreErrors, "deleteSubscriber", null, validModels -> transaction(status -> processCustomers(models, subList -> {
			if (subList.isEmpty()) {
				return Collections.emptyList();
			}
			int companyId = subList.get(0).getCompanyId(); // Assume that all models has same companyId
			recipientDao.deleteRecipients(companyId, getIDs(models));
			return Collections.nCopies(subList.size(), null);
		})));
	}

	@Override
	public List<Object> getSubscriber(final List<RecipientModel> models) {
		return validatedOperation(models, true, "deleteSubscriber", null, validModels -> transaction(status -> processCustomers(models, this::getCustomers)));
	}
	
	private List<Object> getCustomers(List<RecipientModel> models) {
		List<Object> results = new ArrayList<>();
		List<Integer> customerIDs = new ArrayList<>();

		boolean selective = false;
		int companyID = -1;

		for (RecipientModel model : models) {
			if (companyID == -1) {
				companyID = model.getCompanyId();
			} else if (model.getCompanyId() != companyID) {
				logger.error("getCustomers: model.getCompanyId differs for model " + model.toString());
				return Collections.emptyList();
			}

			if (CollectionUtils.isNotEmpty(model.getColumns())) {
				selective = true;
			}
			customerIDs.add(model.getCustomerId());
		}

		try {
			if (selective) {
				// Retrieve a requested columns only
				CaseInsensitiveMap<String, ProfileField> availableProfileFields = recipientDao.getAvailableProfileFields(companyID);

				for (RecipientModel model : models) {
					Object result = null;
					for (String column : model.getColumns()) {
						if (!availableProfileFields.containsKey(column)) {
							result = new ProfileFieldNotExistException(column);
							break;
						}
					}

					if (result == null) {
						result = recipientDao.getCustomerDataFromDb(companyID, model.getCustomerId(), model.getColumns());
					}
					results.add(new Tuple<>(model, result));
				}
			} else {
				// Retrieve a whole set of columns
				Map<Integer, Object> resultsMap = new HashMap<>();

				for (CaseInsensitiveMap<String, Object> res : recipientDao.getCustomers(customerIDs, companyID)) {
					try {
						int id = Integer.parseInt((String) res.get("customer_id"));
						resultsMap.put(id, res);
					} catch (ClassCastException | NumberFormatException e) {
						// Do nothing
					}
				}

				for (RecipientModel model : models) {
					results.add(new Tuple<>(model, resultsMap.get(model.getCustomerId())));
				}
			}
		} catch (Exception e) {
			if (e instanceof RuntimeException) {
				throw (RuntimeException) e;
			} else {
				throw new RuntimeException(e);
			}
		}

		return results;
	}

	public void setRecipientDao(ComRecipientDao recipientDao) {
		this.recipientDao = recipientDao;
	}

	public void setRecipientService(RecipientService recipientService) {
		this.recipientService = recipientService;
	}

	public void setValidator(ModelValidator validator) {
		this.validator = validator;
	}
	
	@Required
	public final void setRecipientFactory(final RecipientFactory factory) {
		this.recipientFactory = factory;
	}
}
