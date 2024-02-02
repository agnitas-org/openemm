/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.exceptions;

import java.util.List;
import java.util.stream.Collectors;

import com.agnitas.messages.Message;
import com.agnitas.web.mvc.Popups;
import org.apache.commons.collections4.CollectionUtils;

public class DependentTrackableLinkException extends Exception {

    private static final long serialVersionUID = -7766396965178587511L;
    
    private final List<String> usedInActiveWorkflowLinks;
    private final List<String> usedInTargetLinks;

    public DependentTrackableLinkException(List<String> usedInActiveWorkflowLinks, List<String> usedInTargetLinks) {
        this.usedInActiveWorkflowLinks = usedInActiveWorkflowLinks;
        this.usedInTargetLinks = usedInTargetLinks;
    }
    
    public void toPopups(Popups popups) {
        if (CollectionUtils.isNotEmpty(usedInActiveWorkflowLinks)) {
            popups.alert("error.mailing.link.used.workflow", linksToHtmlList(usedInActiveWorkflowLinks));
        }
        if (CollectionUtils.isNotEmpty(usedInTargetLinks)) {
            popups.alert("error.mailing.link.used.target", linksToHtmlList(usedInTargetLinks));
        }
    }    
    
    public void toMessages(List<Message> errors) {
        if (CollectionUtils.isNotEmpty(usedInActiveWorkflowLinks)) {
            errors.add(Message.of("error.mailing.link.used.workflow",
                    linksToHtmlList(usedInActiveWorkflowLinks)));
        }
        if (CollectionUtils.isNotEmpty(usedInTargetLinks)) {
            errors.add(Message.of("error.mailing.link.used.target",
                    linksToHtmlList(usedInTargetLinks)));
        }
    }
    
    private static String linksToHtmlList(List<String> usedInActiveWorkflowLinks) {
        return usedInActiveWorkflowLinks.stream()
                .collect(Collectors.joining("</li><li>", "<ul><li>", "</li></ul>"));
    }
}
