/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.service.impl;

import com.agnitas.dao.MailingComponentDao;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.target.beans.TargetGroupDependencyType;
import com.agnitas.emm.core.target.beans.TargetGroupDependentEntry;
import com.agnitas.emm.core.target.component.TargetDependentDynTagsCollector;
import com.agnitas.emm.core.target.component.TargetDependentExportProfilesCollector;
import com.agnitas.emm.core.target.component.TargetDependentMaildropsCollector;
import com.agnitas.emm.core.target.component.TargetDependentMailingComponentsCollector;
import com.agnitas.emm.core.target.component.TargetDependentMailingsCollector;
import com.agnitas.emm.core.target.component.TargetGroupDependenciesCollector;
import com.agnitas.emm.core.target.service.TargetGroupDependencyService;
import com.agnitas.messages.Message;
import com.agnitas.service.ExportPredefService;
import com.agnitas.beans.DynamicTagContent;
import com.agnitas.beans.MailingComponent;
import com.agnitas.emm.core.dyncontent.service.DynamicTagContentService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class TargetGroupDependencyServiceImpl implements TargetGroupDependencyService {

    protected final Set<TargetGroupDependenciesCollector> dependencyCollectors;
    protected final MailingService mailingService;

    private final ExportPredefService exportPredefService;
    private final MailingComponentDao componentDao;
    private final DynamicTagContentService dynamicTagContentService;

    @Autowired
    public TargetGroupDependencyServiceImpl(TargetDependentExportProfilesCollector exportProfilesCollector, TargetDependentMaildropsCollector maildropsCollector,
                                            TargetDependentMailingComponentsCollector mailingComponentsCollector, TargetDependentDynTagsCollector dynTagCollector,
                                            TargetDependentMailingsCollector mailingsCollector, ExportPredefService exportPredefService, MailingService mailingService,
                                            MailingComponentDao componentDao, DynamicTagContentService dynamicTagContentService) {
        this.exportPredefService = exportPredefService;
        this.mailingService = mailingService;
        this.componentDao = componentDao;
        this.dynamicTagContentService = dynamicTagContentService;

        dependencyCollectors = new HashSet<>(Set.of(
                mailingsCollector,
                dynTagCollector,
                mailingComponentsCollector,
                maildropsCollector,
                exportProfilesCollector
        ));
    }

    @Override
    public boolean exists(int id, int companyId) {
        return !findDependencies(id, companyId).isEmpty();
    }

    @Override
    public Optional<TargetGroupDependentEntry> findAnyActualDependency(int targetId, int companyId) {
        return this.findDependencies(targetId, companyId)
                .stream()
                .filter(TargetGroupDependentEntry::isActual)
                .findAny();
    }

    protected List<TargetGroupDependentEntry> findDependencies(int targetGroupId, int companyId) {
        List<TargetGroupDependentEntry> dependencies = new ArrayList<>();

        for (TargetGroupDependenciesCollector collector : dependencyCollectors) {
            collector.collect(dependencies, targetGroupId, companyId);
        }

        return dependencies;
    }

    @Override
    public Message buildErrorMessage(TargetGroupDependentEntry dependency, String targetName) {
        if (TargetGroupDependencyType.EXPORT_PROFILE.equals(dependency.getType())) {
            String exportProfileName = exportPredefService.findName(dependency.getId(), dependency.getCompanyId());
            String linkToExport = "/export/" + dependency.getId() + "/view.action";

            return Message.of("error.target.delete.export", targetName, linkToExport, exportProfileName);
        }

        if (TargetGroupDependencyType.MAILING.equals(dependency.getType())) {
            String mailingName = mailingService.getMailingName(dependency.getId(), dependency.getCompanyId());
            String linkToMailing = "/mailing/" + dependency.getId() + "/settings.action";
            
            return Message.of("error.target.delete.mailing", targetName, linkToMailing, mailingName);
        }

        if (TargetGroupDependencyType.MAILING_COMPONENT.equals(dependency.getType())) {
            MailingComponent component = componentDao.findComponent(dependency.getId(), dependency.getCompanyId());
            String mailingName = mailingService.getMailingName(component.getMailingID(), dependency.getCompanyId());
            String linkToAttachments = "/mailing/" + component.getMailingID() + "/attachment/list.action";

            return Message.of("error.target.delete.component", targetName, component.getComponentName(), linkToAttachments, mailingName);
        }

        if (TargetGroupDependencyType.DYN_TAG.equals(dependency.getType())) {
            DynamicTagContent content = dynamicTagContentService.getContent(dependency.getCompanyId(), dependency.getId());
            String mailingName = mailingService.getMailingName(content.getMailingID(), dependency.getCompanyId());
            String linkToContentTab = "/mailing/content/" + dependency.getId() + "/view.action";

            return Message.of("error.target.delete.dynTag", targetName, content.getDynName(), linkToContentTab, mailingName);
        }

        return Message.of("error.target.in_use");
    }
}
