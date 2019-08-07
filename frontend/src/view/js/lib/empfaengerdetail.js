$(document).ready(function()
{
    $('#helpbox_container').hide();
    $('.recipient_detail_mailinglist_content').hide();

    $(".recipient_detail_mailinglist_toggle").click(function(event3) {
        $(this).toggleClass('toggle_open');
        $(this).toggleClass('toggle_closed');
        $(this).next().toggle();
        event3.preventDefault();
    });

    var containers = findClass(document, 'recipient_detail_mailinglist_container');
    for (var i = 0; i < containers.length; i++) {
        var container = containers[i];
        var divs = findClass(container, 'recipient_detail_mailinglist_toggle')
        if (divs.length == 1) {
            var toggle = divs[0];
            var inputs = container.getElementsByTagName('input');
            for (var k = 0; k < inputs.length; k++) {
                var input = inputs[k];
                if (input.type == 'checkbox' && input.checked == true) {
                    $(toggle).click();
                    break;
                }
            }
        }
    }


    $(".help_close A").click(
            function(event2) {
                $('#helpbox_container').hide();
            });

})

function findClass(parent, str) {
    var list = new Array();
    var nodes = parent.getElementsByTagName('*');
    for (i = 0; i < nodes.length; i++) {
        if (nodes[i].className.indexOf(str) != -1) {
            list.push(nodes[i]);
        }
    }
    return list;
}