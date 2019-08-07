// Corrects iframe height according to its content

var iframeid = "cm_preview";

var getFFVersion = navigator.userAgent.substring(navigator.userAgent.indexOf("Firefox")).split("/")[1];
var FFextraHeight = getFFVersion >= 0.1 ? 16 : 0; //extra height in px to add to iframe in FireFox 1.0+ browsers

function startdyncode() {
    dyniframesize();
}

function dyniframesize() {
    if(document.getElementById) { //begin resizing iframe procedure
        var dyniframe = document.getElementById(iframeid);
        if(dyniframe && !window.opera) {
            dyniframe.style.display = "block";
            if(dyniframe.contentDocument && dyniframe.contentDocument.body.offsetHeight) {//ns6 syntax
                dyniframe.height = dyniframe.contentDocument.body.offsetHeight + FFextraHeight + 35;
            } else if(dyniframe.Document && dyniframe.Document.body.scrollHeight) {//ie5+ syntax
                dyniframe.height = dyniframe.Document.body.scrollHeight + 30;
            }
        }
    }
}

if(window.addEventListener) {
    window.addEventListener("load", startdyncode, false);
} else if(window.attachEvent) {
    window.attachEvent("onload", startdyncode);
}
