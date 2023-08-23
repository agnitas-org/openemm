/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import org.agnitas.beans.ColumnMapping;
import org.agnitas.beans.ImportProfile;
import org.agnitas.dao.ImportProfileDao;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.service.ImportProfileService;
import org.agnitas.util.ImportUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

public class ImportProfileServiceImpl implements ImportProfileService {

    private static final Logger logger = LogManager.getLogger(ImportProfileServiceImpl.class);

    private RecipientService recipientService;
    private ImportProfileDao importProfileDao;

    @Override
    @Transactional
    public void saveImportProfile(ImportProfile profile) {
        ImportProfile oldProfile;
        List<ColumnMapping> oldColumnMappings = Collections.emptyList();
        if (profile.getId() != 0) {
            oldProfile = importProfileDao.getImportProfileById(profile.getId());
            oldColumnMappings = oldProfile.getColumnMapping();
        }
        saveImportProfileWithoutColumnMappings(profile);
        List<ColumnMapping> columnMapping = profile.getColumnMapping();
        columnMapping.forEach(item -> item.setProfileId(profile.getId()));
        importProfileDao.deleteColumnMappings(getColumnIdsForRemove(columnMapping, oldColumnMappings));
        importProfileDao.insertColumnMappings(columnMapping.stream().filter(item -> item.getId() == 0).collect(Collectors.toList()));
        importProfileDao.updateColumnMappings(columnMapping.stream().filter(item -> item.getId() != 0).collect(Collectors.toList()));
    }
    @Override
    public ColumnMapping findColumnMappingByDbColumn(String dbColumnName, List<ColumnMapping> mappings) {
        for (ColumnMapping mapping : mappings) {
            if (mapping.getDatabaseColumn().equalsIgnoreCase(dbColumnName)) {
                return mapping;
            }
        }

        return null;
    }

    @Override
    @Transactional
    public void saveColumnsMappings(List<ColumnMapping> columnMappings, int profileId, Admin admin) {
        List<Integer> columnsForRemove = Collections.emptyList();

        if (profileId != 0) {
            ImportProfile profile = importProfileDao.getImportProfileById(profileId);
            columnsForRemove = getColumnIdsForRemove(columnMappings, profile.getColumnMapping());
        }

        List<String> hiddenColumns = ImportUtils.getHiddenColumns(admin);

        for (ColumnMapping mapping : columnMappings) {
            mapping.setProfileId(profileId);

            if (hiddenColumns.contains(mapping.getDatabaseColumn())) {
                mapping.setDatabaseColumn(ColumnMapping.DO_NOT_IMPORT);
            }
        }

        List<ColumnMapping> columnsForInsert = columnMappings.stream()
                .filter(item -> item.getId() == 0)
                .collect(Collectors.toList());
        List<ColumnMapping> columnsForUpdate = columnMappings.stream()
                .filter(item -> item.getId() != 0)
                .collect(Collectors.toList());

        importProfileDao.deleteColumnMappings(columnsForRemove);
        importProfileDao.insertColumnMappings(columnsForInsert);
        importProfileDao.updateColumnMappings(columnsForUpdate);
    }

    @Override
    public void saveImportProfileWithoutColumnMappings(ImportProfile profile) {
        try {
            if (profile.getId() == 0) {
                importProfileDao.insertImportProfile(profile);
            } else {
                importProfileDao.updateImportProfile(profile);
            }
        } catch (Exception e) {
            logger.error("Error saving profile:", e);
        }
    }

    @Override
    public ImportProfile getImportProfileById(int id) {
        return importProfileDao.getImportProfileById(id);
    }

    @Override
    public void deleteImportProfileById(int id) {
        importProfileDao.deleteImportProfileById(id);
    }

    @Override
    public List<ImportProfile> getImportProfilesByCompanyId(int companyId) {
        return importProfileDao.getImportProfilesByCompanyId(companyId);
    }

    @Override
    public List<Integer> getSelectedMailingListIds(int id, int companyId) {
        return importProfileDao.getSelectedMailingListIds(id, companyId);
    }

    private List<Integer> getColumnIdsForRemove(List<ColumnMapping> mappings, List<ColumnMapping> oldMappings) {
        List<Integer> oldIds = oldMappings.stream()
                .map(ColumnMapping::getId)
                .collect(Collectors.toList());

        List<Integer> newIds = mappings.stream()
                .map(ColumnMapping::getId)
                .collect(Collectors.toList());

        oldIds.removeAll(newIds);
        return oldIds;
    }

	@Override
	public Map<String, Integer> getImportProfileGenderMapping(int id) {
		return importProfileDao.getImportProfileGenderMapping(id);
	}

	@Override
	public void saveImportProfileGenderMapping(int id, Map<String, Integer> genderMapping) {
		importProfileDao.saveImportProfileGenderMapping(id, genderMapping);
	}

	@Override
	public boolean addImportProfileGenderMapping(int profileId, String addedGender, int addedGenderInt) {
		Map<String, Integer> genderMapping = getImportProfileGenderMapping(profileId);
		String[] genderTokens = addedGender.split(",");
		boolean alreadyContained = false;
		for (String genderToken : genderTokens) {
			if (StringUtils.isNotBlank(genderToken) && genderMapping.containsKey(genderToken.trim())) {
				alreadyContained = true;
				break;
			}
		}
		if (!alreadyContained) {
			for (String genderToken : genderTokens) {
				if (StringUtils.isNotBlank(genderToken)) {
					genderMapping.put(genderToken.trim(), addedGenderInt);
				}
			}
			saveImportProfileGenderMapping(profileId, genderMapping);
        	return true;
		} else {
			return false;
		}
	}

    @Override
    public boolean isKeyColumnsIndexed(ImportProfile profile) {
        List<String> columnsToCheck = profile.getKeyColumns();
        return CollectionUtils.isEmpty(columnsToCheck)
                || recipientService.isColumnsIndexed(columnsToCheck, profile.getCompanyId());
    }

    @Override
    public boolean isDuplicatedName(String name, int id, int companyId) {
        int foundImportProfile = importProfileDao.findImportProfileIdByName(name, companyId);

        if (foundImportProfile == -1) {
            return false;
        }

        return foundImportProfile != id;
    }

    @Override
    public boolean isColumnWasImported(String columnName, int id) {
        if (id <= 0) {
            return false;
        }

        return importProfileDao.isColumnWasImported(columnName, id);
    }

    @Required
    public void setImportProfileDao(ImportProfileDao importProfileDao) {
        this.importProfileDao = importProfileDao;
    }

    @Required
    public void setRecipientService(RecipientService recipientService) {
        this.recipientService = recipientService;
    }
}
