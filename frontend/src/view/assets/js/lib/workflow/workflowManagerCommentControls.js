(function($){
    var Popover = AGN.Lib.Popover;

    var MAILING_TYPES = ["mailing", "actionbased_mailing", "datebased_mailing", "followup_mailing"];

    // Workflow comment controls
    var CommentControls = function CommentControls(campaignManager, data) {
        this.manager = campaignManager;
        this.commentPopover = null;
        this.timeoutId = undefined;
        this.isPdf = campaignManager.isPdf;
        this.commentCounter = 1;
        this.mailingThumbnailURL = data.mailingThumbnailURL;
        this.componentURL = data.componentURL;
    };

    CommentControls.popoverOptions = function(controls, node) {
        var template = '<div class="popover popover-wide" role="tooltip" >' +
            '<div class="arrow"></div>' +
            '<div class="popover-content" style="width: auto; height: auto; max-width: 300px; max-height: 350px; overflow: auto;"></div>' +
            '</div>';

        return {
            trigger: 'manual',
            html: true,
            template: template,
            content: function() {
                return controls.generatePopoverContent(node)
            }
        };
    };

    CommentControls.prototype.generatePopoverContent = function(node) {
        var content = "";

        var mailingId = 0;
        if (MAILING_TYPES.includes(node.type)) {
            mailingId = node.data.mailingId;

            var mailingImg = mailingId > 0 ? this.getMailingThumbnail(mailingId) : '';
            if (mailingImg != '') {
                content = "<img src='" + mailingImg.src + "' style='width: 100%; height: 100%;'/>";
            }
        }

        var comment = node.data.iconComment;
        if (comment != null && comment.trim() != '') {
            content += "<div>" + comment + "</div>";
        }

        return content;
    };

    CommentControls.prototype.getMailingThumbnail = _.memoize(function(mailingId) {
        var self = this;
        var img = '';
        if (!self.isPdf) {
            $.ajax({
                type: "POST",
                url: self.mailingThumbnailURL,
                async: false,
                data: {
                    mailingId: mailingId
                },
                success: function (componentId) {
                    if (componentId > 0) {
                        img = new Image;
                        img.src = self.componentURL.replace("{component-id}", componentId);
                    }
                }
            });
        }
        return img;
    });

    CommentControls.prototype.showCommentPopover = function(node) {
        var comment = node.data.iconComment;
        var mailingId = node.data.mailingId || 0;
        if((comment == null || comment.trim() == '') && mailingId <= 0) {
            return;
        }

        var $element = $(node.elementJQ);
        var draggedElements = this.manager.getDraggedElements();
        if (draggedElements) {
            for(var i = 0; i < draggedElements.length; i++) {
                if (draggedElements[i].DOMElement == $element[0]) {
                    return;
                }
            }
        }

        this.updateComments(node);
        this.showPopover();
    };

    CommentControls.prototype.hideCommentPopover = function() {
        this.hidePopover(false);
    };

    CommentControls.prototype.updateComments = function(node) {
        if (this.commentPopover) {
            this.commentPopover.destroy();
        }

        var commentControlsNew = this;
        this.commentPopover = Popover.new(node.elementJQ, CommentControls.popoverOptions(commentControlsNew, node));

        var $tip = this.commentPopover.tip();

        $tip.on('mouseenter', function() {
             commentControlsNew.showPopover(node);
        });

        $tip.on('mouseleave', function() {
            commentControlsNew.hidePopover(true);
        });
    };

    CommentControls.prototype.stopHiding = function() {
        if (this.timeoutId) {
            clearTimeout(this.timeoutId);
            this.timeoutId = undefined;
        }
    };

    CommentControls.prototype.showPopover = function() {
        this.stopHiding();
        this.commentPopover.show();
    };

    CommentControls.prototype.hidePopover = function (immediate) {
        this.stopHiding();

        if (immediate === true) {
            if(this.commentPopover) {
                this.commentPopover.hide();
            }
        } else {
            var self = this;
            this.timeoutId = setTimeout(function() {
                self.hidePopover(true);
            }, 200);
        }
    };

    CommentControls.prototype.showCommentEditDialog = function() {
        var node = this.manager.getCampaignManagerNodes()
            .getNodeById(AGN.Lib.WM.CampaignManagerSelection.getSelected()[0]);

        if (!this.manager.checkActivation()) {
            this.manager.getEditorsHelper().showIconCommentDialog(node);
        }
    };

    CommentControls.prototype.addCommentEllipsis = function(node) {
        var iconComment = node.data.iconComment;
        var self = this;
        if(self.isPdf) {
            if (iconComment != null && iconComment.trim() != '') {
                self.showCommentFootnote(node.elementJQ, iconComment);
            }
        } else {
            if (iconComment != null && iconComment.trim() != '') {
                node.elementJQ.addClass('icon-commented');
            } else {
                node.elementJQ.removeClass('icon-commented');
            }
        }
    };

    CommentControls.prototype.showCommentFootnote = function($icon, commentText) {
        var self = this;
        var footnoteId = "icon-footnote-" + self.commentCounter;
        var $row = $("<tr id=\"" + footnoteId + "\"><td>" + self.commentCounter + ". " + commentText + "</td></tr>");
        var link = $("<a aria-describedby=\"footnote-label\" href=\"#" + footnoteId + "\"><span class='icon-footnote-number badge badge-info'>" + self.commentCounter + "</span>");
        $("#footnotes-container table#comment-footnotes-list").append($row);
        $icon.append(link);

        self.commentCounter++;
    };

    AGN.Lib.WM.CommentControls = CommentControls;
})(jQuery);
