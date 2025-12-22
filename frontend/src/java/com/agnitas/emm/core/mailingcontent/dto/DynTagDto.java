/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailingcontent.dto;

import java.util.List;
import java.util.Objects;

public class DynTagDto {

    private int id;
    private int mailingId;
    private int companyId;
    private int templateId;
    private String interestGroup;
    private String name;
    private List<DynContentDto> contentBlocks;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMailingId() {
        return mailingId;
    }

    public void setMailingId(int mailingId) {
        this.mailingId = mailingId;
    }

    public String getInterestGroup() {
        return interestGroup;
    }

    public void setInterestGroup(String interestGroup) {
        this.interestGroup = interestGroup;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DynContentDto> getContentBlocks() {
        return contentBlocks;
    }

    public void setContentBlocks(List<DynContentDto> contentBlocks) {
        this.contentBlocks = contentBlocks;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DynTagDto dynTagDto = (DynTagDto) o;
        return id == dynTagDto.id && mailingId == dynTagDto.mailingId && companyId == dynTagDto.companyId && Objects.equals(name, dynTagDto.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, mailingId, companyId, name);
    }
}
