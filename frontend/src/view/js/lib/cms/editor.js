function moveDown(cmId) {
    var cmPhNameElement = document.getElementById('cm.' + cmId + ".ph_name");
    var cmPhOrderElement = document.getElementById('cm.' + cmId + ".order_in_ph");
    var phPanel = document.getElementById('placeholder.' + cmPhNameElement.value);
    var nextIndex = parseInt(cmPhOrderElement.value) + 1;
    var nextCmRowName = 'tr.' + cmPhNameElement.value + "." + nextIndex;
    var nextCmRow = document.getElementById(nextCmRowName);
    if(nextCmRow != null) {
        var thisCmRow = document.getElementById('tr.' + cmPhNameElement.value + "." + cmPhOrderElement.value);
        phPanel.removeChild(thisCmRow);
        thisCmRow.id = "tr." + cmPhNameElement.value + "." + nextIndex;
        nextCmRow.id = "tr." + cmPhNameElement.value + "." + cmPhOrderElement.value;
        var nextNextIndex = nextIndex + 1;
        var nextNextCmRow = document.getElementById('tr.' + cmPhNameElement.value + "." + nextNextIndex);
        if(nextNextCmRow != null) {
            phPanel.insertBefore(thisCmRow, nextNextCmRow);
        }
        else {
            phPanel.appendChild(thisCmRow);
        }
        var prevCmId = getCmIdInPlaceholder(cmPhNameElement.value, cmPhOrderElement.value);
        cmPhOrderElement.value = nextIndex;
        var prevCmPhOrder = document.getElementById('cm.' + prevCmId + ".order_in_ph");
        prevCmPhOrder.value = nextIndex - 1;
    }
}

function moveUp(cmId) {
    var cmPhNameElement = document.getElementById('cm.' + cmId + ".ph_name");
    var cmPhOrderElement = document.getElementById('cm.' + cmId + ".order_in_ph");
    var phPanel = document.getElementById('placeholder.' + cmPhNameElement.value);
    var prevIndex = parseInt(cmPhOrderElement.value) - 1;
    var prevCmRowName = 'tr.' + cmPhNameElement.value + "." + prevIndex;
    var prevCmRow = document.getElementById(prevCmRowName);
    if(prevCmRow != null) {
        var thisCmRow = document.getElementById('tr.' + cmPhNameElement.value + "." + cmPhOrderElement.value);
        phPanel.removeChild(thisCmRow);
        thisCmRow.id = "tr." + cmPhNameElement.value + "." + prevIndex;
        prevCmRow.id = "tr." + cmPhNameElement.value + "." + cmPhOrderElement.value;
        phPanel.insertBefore(thisCmRow, prevCmRow);
        var prevCmId = getCmIdInPlaceholder(cmPhNameElement.value, cmPhOrderElement.value);
        cmPhOrderElement.value = prevIndex;
        var prevCmPhOrder = document.getElementById('cm.' + prevCmId + ".order_in_ph");
        prevCmPhOrder.value = prevIndex + 1;
    }
}

function toNextPlaceholder(cmId) {
    var cmPhNameElement = document.getElementById('cm.' + cmId + ".ph_name");
    var oldPhName = cmPhNameElement.value;
    var oldPhIndex = document.getElementById('ph.' + oldPhName);
    var newPhIndex = parseInt(oldPhIndex.value) + 1;
    var newPhNameElement = document.getElementById('ph_name.' + newPhIndex);
    if(newPhNameElement == null) {
        if(newPhIndex > 1) {
            newPhNameElement = document.getElementById('ph_name.0');
        }
        else {
            return;
        }
    }
    var newPhName = newPhNameElement.value;
    var destPhCms = getAllCmIdInPlaceholderAfterIndex(newPhName, 0);
    moveToPlaceholder(cmId, newPhName);
    // swap positions of CMs (used for version when Ph can contain only one CM)
    if(destPhCms != null) {
        if(destPhCms.length > 0) {
            var destPhCm = destPhCms[0];
            moveToPlaceholder(destPhCm, oldPhName);
        }
        else {
            showPlaceholder(oldPhName);
            hidePlaceholder(newPhName);
        }
    } else {
        showPlaceholder(oldPhName);
        hidePlaceholder(newPhName);
    }
}

function toPrevPlaceholder(cmId) {
    var cmPhNameElement = document.getElementById('cm.' + cmId + ".ph_name");
    var oldPhName = cmPhNameElement.value;
    var oldPhIndex = document.getElementById('ph.' + oldPhName);
    var newPhIndex = parseInt(oldPhIndex.value) - 1;
    var newPhNameElement = document.getElementById('ph_name.' + newPhIndex);
    if(newPhNameElement == null) {
        var phMax = parseInt(document.getElementById('ph_max').value);
        if(phMax <= 0) {
            return;
        }
        else {
            newPhNameElement = document.getElementById('ph_name.' + phMax);
        }
    }
    var newPhName = newPhNameElement.value;
    var destPhCms = getAllCmIdInPlaceholderAfterIndex(newPhName, 0);
    moveToPlaceholder(cmId, newPhName);
    // swap positions of CMs (used for version when Ph can contain only one CM)
    if(destPhCms != null) {
        if(destPhCms.length > 0) {
            var destPhCm = destPhCms[0];
            moveToPlaceholder(destPhCm, oldPhName);
        }
        else {
            showPlaceholder(oldPhName);
            hidePlaceholder(newPhName);
        }
    } else {
        showPlaceholder(oldPhName);
        hidePlaceholder(newPhName);
    }
}

function hidePlaceholder(phName) {
    document.getElementById('name.placeholder.' + phName).innerHTML = '';
    document.getElementById('table.placeholder.' + phName).className = "placeholderEmpty";
}

function showPlaceholder(phName) {
    document.getElementById('name.placeholder.' + phName).innerHTML = phName;
    document.getElementById('table.placeholder.' + phName).className = "placeholder";
}

function moveToPlaceholder(cmId, newPhName) {
    var cmPhNameElement = document.getElementById('cm.' + cmId + ".ph_name");
    var oldPhName = cmPhNameElement.value;
    var cmPhOrderElement = document.getElementById('cm.' + cmId + ".order_in_ph");
    var oldPhOrder = parseInt(cmPhOrderElement.value);
    var cmElement = document.getElementById('tr.' + oldPhName + '.' + oldPhOrder);
    // fix positions of CMs in old PH
    fixPositionsOfCms(oldPhName, oldPhOrder);
    // insert CM into new PH
    var newPh = document.getElementById('placeholder.' + newPhName);
    var allCmInNewPh = getAllCmIdInPlaceholderAfterIndex(newPhName, 0);
    var newCmOrder = allCmInNewPh.length + 1;
    cmElement.id = 'tr.' + newPhName + '.' + newCmOrder;
    cmPhNameElement.value = newPhName;
    cmPhOrderElement.value = newCmOrder;
    newPh.appendChild(cmElement);

    var cmPanelPhName = document.getElementById('cmPanel.phName.' + cmId);
    cmPanelPhName.innerHTML = newPhName;

    // resize iframe that contains editor-page
    parent.resizeIframe("editorFrame");
}

function editCM(cmId) {
    parent.editCM(cmId);
}

function fixPositionsOfCms(phName, phOrder) {
    var cmsInOldPhAfterCurrent = getAllCmIdInPlaceholderAfterIndex(phName, phOrder);
    for(var i = 0; i < cmsInOldPhAfterCurrent.length; i++) {
        var cmId = cmsInOldPhAfterCurrent[i];
        var thisCmRow = document.getElementById('tr.' + phName + '.' + (phOrder + i + 1));
        thisCmRow.id = "tr." + phName + "." + (phOrder + i);
        var thisCmOrderElement = document.getElementById('cm.' + cmId + '.order_in_ph');
        var thisCmOrder = parseInt(thisCmOrderElement.value);
        thisCmOrderElement.value = thisCmOrder - 1;
    }
}

function getCmIdInPlaceholder(phName, index) {
    var cmRow = document.getElementById('tr.' + phName + '.' + index);
    if(cmRow == null) {
        return 0;
    }
    var tables = cmRow.getElementsByTagName('table');
    for(var i = 0; i < tables.length; i++) {
        var curTable = tables[i];
        if(curTable.id != null && curTable.id.length > 3) {
            if(curTable.id.substr(0, 3) == 'cm.') {
                var cmId = curTable.id.substring(3, curTable.id.length);
                return cmId;
            }
        }
    }
    return 0;
}

function getAllCmIdInPlaceholderAfterIndex(phName, index) {
    var result = new Array();
    var i = 0;
    var curIndex = index + 1;
    var curCmId = 1;
    while(curCmId != 0) {
        curCmId = getCmIdInPlaceholder(phName, curIndex);
        if(curCmId != 0) {
            result[i] = curCmId;
        }
        curIndex++;
        i++;
    }
    return result;
}

function initCmPositions() {
    var phIndex = 0;
    while(true) {
        var phNameElement = document.getElementById('ph_name.' + phIndex);
        if(phNameElement != null) {
            var curPhName = phNameElement.value;
            var curPh = document.getElementById('placeholder.' + curPhName);
            var cmIndex = 1;
            while(true) {
                var cmElement = document.getElementById('tr.' + curPhName + '.' + cmIndex);
                if(cmElement != null) {
                    curPh.appendChild(cmElement);
                    hidePlaceholder(curPhName);
                } else {
                    break;
                }
                cmIndex++;
            }
        } else {
            break;
        }
        phIndex++;
    }
}
