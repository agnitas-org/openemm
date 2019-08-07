function togglePreview(idElem2Hide, idElem2Show) {
    var imgPreview = document.getElementById(idElem2Hide);
    imgPreview.style.display = "none";
    Effect.toggle(idElem2Show, 'appear');
    return false;
}

function togglePreview(idElem2Show, idElem2Hide1, idElem2Hide2) {
        var toHide = document.getElementById(idElem2Hide1);
        toHide.style.display = "none";
        toHide = document.getElementById(idElem2Hide2);
        toHide.style.display = "none";
        Effect.toggle(idElem2Show, 'appear');
        return false;
}