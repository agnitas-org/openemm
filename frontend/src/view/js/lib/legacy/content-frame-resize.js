
function doIframe() {
    var myIframe = document.getElementById('editorFrame');
    resizeIframe('editorFrame');
    addEvent(myIframe, 'load', doIframe);
}

function resizeIframe(frId) {
    var e = document.getElementById(frId);
    var maxPreviewHeight = document.getElementById('maxPreviewHeight');    
    if(e.contentDocument && e.contentDocument.body.offsetHeight > 400) {
        var offsetHeight = e.contentDocument.body.offsetHeight;
        var newHeight = offsetHeight + 50;
        if(offsetHeight > maxPreviewHeight)
            newHeight = maxPreviewHeight;
        e.height = newHeight;
    }
    else {
//        e.height = e.contentWindow.document.body.scrollHeight;
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
