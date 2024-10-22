(function () {
    var Confirm = AGN.Lib.Confirm,
        Page = AGN.Lib.Page,
        RecipientSuccessDeliveryInfoRenderer = function () {};

    function getButtonHref(params) {
        if (params.deliveryHistoryEnabled) {
            return AGN.url('/recipient/' + params.recipientId + '/mailing/' + params.data.mailingId + '/successfulDeliveryHistory.action')
        } else {
            return '';
        }
    }

    // gets called once before the renderer is used
    RecipientSuccessDeliveryInfoRenderer.prototype.init = function(params) {
        this.eGui = document.createElement('div');
        var href = getButtonHref(params);

        if (!href) {
            this.eGui.innerHTML = params.value;
        } else {
            this.eGui.innerHTML = '<button type="button" class="table__btn btn btn-inverse"><i class="icon icon-external-link-alt"></i><span>' + params.value + '</span></button>';

            this.eButton = this.eGui.querySelector('.btn');

            this.eButton.setAttribute('href', href);

            // add event listener to button
            this.eventListener = function (e) {
                var href = getButtonHref(params);
                if (href) {
                    $.get(href).done(function (resp) {
                        var $resp = $(resp),
                            $modal;

                        $modal = $resp.all('.modal');

                        if ($modal.length == 1) {
                            Confirm.create(resp);
                        } else {
                            Page.render(resp);
                        }
                    });

                    e.preventDefault();
                    return false;
                }
            };

            this.eButton.addEventListener('click', this.eventListener);
        }
    };

    // gets called once when grid ready to insert the element
    RecipientSuccessDeliveryInfoRenderer.prototype.getGui = function() {
        return this.eGui;
    };

    // gets called whenever the user gets the cell to refresh
    RecipientSuccessDeliveryInfoRenderer.prototype.refresh = function(params) {
        // set value into cell again
        if (this.eButton) {
            this.eButton.dataset.url = getButtonHref(params);
        }

        // return true to tell the grid we refreshed successfully
        return true;
    };

    // gets called when the cell is removed from the grid
    RecipientSuccessDeliveryInfoRenderer.prototype.destroy = function() {
        // do cleanup, remove event listener from button
        if (this.eButton) {
            this.eButton.removeEventListener('click', this.eventListener);
        }
    };

    AGN.Opt.TableCellRenderers['RecipientSuccessDeliveryInfoRenderer'] = RecipientSuccessDeliveryInfoRenderer;
})();