/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.bounce.form;

public class BounceFilterForm {

    private int id;
    private String shortName;
    private String description;
    private String filterEmail;
    private String forwardEmail;

    private boolean ownForwardEmailSelected;

    private boolean doForward;

    private boolean doSubscribe;

    private int mailingListId;

    private int userFormId;

    private boolean doAutoRespond;

    private int arMailingId;

    private String securityToken;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilterEmail() {
        return filterEmail;
    }

    public void setFilterEmail(String filterEmail) {
        this.filterEmail = filterEmail;
    }

    public String getForwardEmail() {
        return forwardEmail;
    }

    public void setForwardEmail(String forwardEmail) {
        this.forwardEmail = forwardEmail;
    }

    public boolean isOwnForwardEmailSelected() {
        return ownForwardEmailSelected;
    }

    public void setOwnForwardEmailSelected(boolean ownForwardEmailSelected) {
        this.ownForwardEmailSelected = ownForwardEmailSelected;
    }

    public boolean isDoForward() {
        return doForward;
    }

    public void setDoForward(boolean doForward) {
        this.doForward = doForward;
    }

    public boolean isDoSubscribe() {
        return doSubscribe;
    }

    public void setDoSubscribe(boolean doSubscribe) {
        this.doSubscribe = doSubscribe;
    }

    public int getMailingListId() {
        return mailingListId;
    }

    public void setMailingListId(int mailingListId) {
        this.mailingListId = mailingListId;
    }

    public int getUserFormId() {
        return userFormId;
    }

    public void setUserFormId(int userFormId) {
        this.userFormId = userFormId;
    }

    public boolean isDoAutoRespond() {
        return doAutoRespond;
    }

    public void setDoAutoRespond(boolean doAutoRespond) {
        this.doAutoRespond = doAutoRespond;
    }

    public int getArMailingId() {
        return arMailingId;
    }

    public void setArMailingId(int arMailingId) {
        this.arMailingId = arMailingId;
    }

    public String getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(String securityToken) {
        this.securityToken = securityToken;
    }
}
