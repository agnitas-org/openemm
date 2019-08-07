/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans.impl;

import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import com.agnitas.emm.core.workflow.beans.WorkflowConnection;
import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import com.agnitas.emm.core.workflow.service.WorkflowIconTypeSerializer;

public class BaseWorkflowIcon implements WorkflowIcon {
    private int id;
    private int x;
    private int y;
    @JsonSerialize(using = WorkflowIconTypeSerializer.class)
    private int type;
    private boolean filled;
    private String iconTitle;
    private boolean editable = true;
    private String iconComment;
    private List<WorkflowConnection> connections;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public int getType() {
        return type;
    }

    @Override
    public void setType(int type) {
        this.type = type;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public void setX(int x) {
        this.x = x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean isFilled() {
        return filled;
    }

    @Override
    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    @Override
    public String getIconTitle() {
        return iconTitle;
    }

    @Override
    public void setIconTitle(String iconTitle) {
        this.iconTitle = iconTitle;
    }

    @Override
    public boolean isEditable() {
        return editable;
    }

    @Override
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @Override
    public void setConnections(List<WorkflowConnection> connections) {
        this.connections = connections;
    }
	
	@Override
	public String getIconComment() {
		return iconComment;
	}
	
	@Override
	public void setIconComment(String iconComment) {
		this.iconComment = iconComment;
	}
	
	@Override
    public List<WorkflowConnection> getConnections() {
        return connections;
    }
}
