function removeTableResultLists(resultClassName, pagelinksClassName) {
    var elements = document.getElementsByTagName('div');
    for (var i = 0; i < elements.length; i++) {
        var div = elements.item(i);
        if (div.getAttribute("class") == resultClassName || div.getAttribute("className") == resultClassName) {
            div.style.display = 'none';
        }
    }
    var elements = document.getElementsByTagName('span');
    for (var i = 0; i < elements.length; i++) {
        var div = elements.item(i);
        if (div.getAttribute("class") == pagelinksClassName || div.getAttribute("className") == pagelinksClassName) {
            div.style.display = 'none';
        }
    }

}