function editCM(cmId) {
    var editCmLink = document.getElementById("edit-CM-link");
    window.location = editCmLink.href + cmId;
}

function doIframe() {
    var myIframe = document.getElementById('editorFrame');
    resizeIframe('editorFrame');
    addEvent(myIframe, 'load', doIframe);
}

function resizeIframe(frId) {
    var e = document.getElementById(frId);
    if(e.contentDocument) {
        if (e.contentDocument.body.offsetHeight != 50) {
            e.height = e.contentDocument.body.offsetHeight + 35;
        }
        else {
            e.height = e.contentWindow.document.body.scrollHeight;
        }
    }
    else {
        e.height = e.contentWindow.document.body.scrollHeight;
    }
}

function addEvent(obj, evType, fn) {
    if(obj.addEventListener)
    {
        obj.addEventListener(evType, fn, false);
        return true;
    } else if(obj.attachEvent) {
        var r = obj.attachEvent("on" + evType, fn);
        return r;
    } else {
        return false;
    }
}

if(document.getElementById && document.createTextNode) {
    addEvent(window, 'load', doIframe);
}
