CKEDITOR.editorConfig = function(config) {
/*-- EDITOR BEHAVIOUR --*/

    //Disable converting 'background' to 'background-color' in tag styles
    config.colorButton_normalizeBackground = false;

    /*true keeps HTML structure after paste from Word*/
    /*FCKConfig.CleanWordKeepsStructure*/
    config.pasteFromWordNumberedHeadingToList = true;

    /*FCKConfig.DocType*/
    config.docType = '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">';

    /*true removes all formatting info*/
    /*FCKConfig.ForcePasteAsPlainText*/
    config.forcePasteAsPlainText = false ;

    /*true inserts html, head and body tags*/
    /*FCKConfig.FullPage*/
    config.fullPage = false ;

    /*cursor focus in editing area*/
    /*FCKConfig.StartupFocus*/
    config.startupFocus = false;

    /*FCKConfig.MaxUndoLevels*/
    config.undoStackSize = 15;

    config.pasteFromWordRemoveFontStyles = true;
    config.pasteFromWordRemoveStyles = true;

    /**
     * Allows all tags, style, attributes and classes.
     *
     * @see <a href="https://docs.ckeditor.com/ckeditor4/latest/guide/dev_allowed_content_rules.html#2">content rules</a>
     */
    config.allowedContent = {
      $1: {
        // Use the ability to specify elements as an object.
        elements: CKEDITOR.dtd,
        attributes: true,
        styles: true,
        classes: true
      }
    };

    /**
     * Prevents a few possible XSS injections.
     *
     * @see <a href="https://www.owasp.org/index.php/Cross-site_Scripting_(XSS)">cross site scripting</a>
     */
    config.disallowedContent = 'script; iframe; *[on*];';

    /**
     * Completely turns off filter rules for next tags and their children.
     */
    config.ignoredTags = ['font'];

    /*-- STYLES --*/

    /*FCKConfig.FontColors*/
    config.colorButton_colors = '000000,993300,333300,003300,003366,000080,333399,333333,800000,FF6600,808000,808080,008080,0000FF,666699,808080,FF0000,FF9900,99CC00,339966,33CCCC,3366FF,800080,999999,FF00FF,FFCC00,FFFF00,00FF00,00FFFF,00CCFF,993366,C0C0C0,FF99CC,FFCC99,FFFF99,CCFFCC,CCFFFF,99CCFF,CC99FF,FFFFFF';

    /*FCKConfig.FontFormats*/
    config.format_tags = 'p;div;pre;address;h1;h2;h3;h4;h5;h6' ;

    /*FCKConfig.FontNames*/
    // Font plugin should provide this values
    //config.font_names = 'Arial;Comic Sans MS;Courier New;Tahoma;Times New Roman;Verdana' ;

    /*FCKConfig.FontSizes*/
    // Font plugin should provide this values
    //config.fontSize_sizes = '6/6px;8/8px;10/10px;12/12px;16/16px;20/20px;24/24px' ;

    config.contentsCss = [
        CKEDITOR.getUrl('contents.css'),
        CKEDITOR.getUrl('emm_fonts.css')
    ];

    var fonts = [
        // Add desired font names in format 'DisplayedFontName/RealFontNameFromEmmFontsCss'
        config.font_names,
        'Helvetica/Helvetica, sans-serif'
    ];

    config.font_names = fonts.join(';');

/*-- HTML OUTPUT --*/

    /*FCKConfig.EnterMode*/
    config.enterMode = CKEDITOR.ENTER_BR;

    /*FCKConfig.ShiftEnterMode*/
    config.shiftEnterMode = CKEDITOR.ENTER_BR;

    /*true fills empty blocks with '&nbsp;'*/
    /*FCKConfig.FillEmptyBlocks*/
    config.fillEmptyBlocks = false;

    /*true outputs '&' instead of '&amp;'*/
    /*FCKConfig.ForceSimpleAmpersand*/
    config.forceSimpleAmpersand = true;

    /*true converts special characters to HTML entities*/
    /*FCKConfig.ProcessHTMLEntities*/
    config.entities = false;

    /*true shows border for tables with border=0*/
    /*FCKConfig.ShowBorders*/
    config.startupShowBorders = true;


    /*number of '&nbsp' for a tab*/
    /*FCKConfig.TabSpaces*/
    config.tabSpaces = 0;

/*USER INTERFACE*/

    /*options of context menu*/
    /*FCKConfig.ContextMenu*/
    config.menu_groups = 'clipboard,link,anchor,image,flash,select,textarea,checkbox,radio,textField,hiddenfield,imagebutton,button,bulletedlist,numberedlist,tablecell,table,form';

    /*do not hide Advanced tab in flash properties window*/
    /*do not hide Link tab in image properties window*/
    /*FCKConfig.FlashDlgHideAdvanced*/
    /*FCKConfig.ImageDlgHideLink*/
    config.removeDialogTabs = 'flash:advanced;image:Link';

    /*do not hide Advanced tab in link dialog window*/
    /*FCKConfig.LinkDlgHideAdvanced*/
    config.removeDialogTabs = 'link:advanced';

    /*FCKConfig.SmileyColumns*/
    config.smiley_columns = 8;

    /*toolbar can be hidden*/
    /*FCKConfig.ToolbarCanCollapse*/
    config.toolbarCanCollapse = true;

    /*toolbar is displayed at startup*/
    /*FCKConfig.ToolbarStartExpanded*/
    config.toolbarStartupExpanded = true;

    /*features available in toolbar "emm"*/
    /*FCKConfig.ToolbarSets["emm"]*/
    config.toolbar = [
	['Cut','Copy','Paste','PasteText','PasteWord'],
    ['Undo','Redo','-','Find','Replace','-','RemoveFormat'],
    ['TextColor','BGColor'],
	['Bold','Italic','Underline','StrikeThrough','-','Subscript','Superscript'],
	['Image','Table','Rule','SpecialChar', 'My_EMM'],
	['OrderedList','UnorderedList','-','Outdent','Indent'],
	['JustifyLeft','JustifyCenter','JustifyRight','JustifyFull'],
	['Link','Unlink','Anchor'],
	['FontName','FontSize']
    ] ;

    /*features available in toolbar "Default"*/
    /*FCKConfig.ToolbarSets["Default"]*/
    config.toolbar_Full = [
    ['Source','DocProps','-','Save','NewPage','Preview','-','Templates'],
    ['Cut','Copy','Paste','PasteText','PasteWord','-','Print','SpellCheck'],
    ['Undo','Redo','-','Find','Replace','-','SelectAll','RemoveFormat'],
    ['Form','Checkbox','Radio','TextField','Textarea','Select','Button','ImageButton','HiddenField'],
    '/',
    ['Bold','Italic','Underline','StrikeThrough','-','Subscript','Superscript'],
    ['OrderedList','UnorderedList','-','Outdent','Indent','Blockquote'],
    ['JustifyLeft','JustifyCenter','JustifyRight','JustifyFull'],
    ['Link','Unlink','Anchor'],
    ['Image','Flash','Table','Rule','Smiley','SpecialChar','PageBreak'],
    '/',
    ['Style','FontFormat','FontName','FontSize'],
    ['TextColor','BGColor'],
    ['FitWindow','ShowBlocks','-','About'] // No comma for the last row.
    ] ;

/*ADVANCED*/

    // It seems like the 'font' plugin is already provided (inlined) by ckeditor.js, but we propose to load it just in case (it wont override inlined version)
    config.extraPlugins = 'emm,image,htmlwriter,table,tabletools,pastefromword';
    config.removePlugins = 'uploadimage';

    config.toolbar_EMM =
        [
            { name: 'clipboard', items : [ 'Cut','Copy','Paste','PasteText','PasteFromWord','-','Undo','Redo' ] },
            { name: 'editing', items : [ 'Find','Replace','-','SelectAll','-','Scayt' ] },
            { name: 'links', items : [ 'Link','Unlink','Anchor' ] },
            { name: 'insert', items : [ 'Image','Table','HorizontalRule','SpecialChar','AGNTag' ] },
            { name: 'styles', items : [ 'Font','FontSize' ] },
            { name: 'format', items : [ 'Styles','Format' ] },
            { name: 'colors', items : [ 'TextColor','BGColor' ] },
            { name: 'basicstyles', items : [ 'Bold','Italic','Underline','Strike','Subscript','Superscript',' - ','RemoveFormat' ] },
            { name: 'justify', items : ['JustifyLeft','JustifyCenter','JustifyRight','JustifyBlock'] },
            { name: 'paragraph', items : [ 'NumberedList','BulletedList','-','Outdent','Indent','-','Blockquote' ] }

        ];

    /*features available in toolbar "EMC"*/
    /*FCKConfig.ToolbarSets["EMC"]*/
    config.toolbar_EMC = [
        ['Cut', 'Copy', 'Paste', 'PasteText', 'PasteFromWord'],
        ['Undo','Redo','-','Find','Replace','-','SelectAll'],
        ['Scayt', '-', 'Link', 'Unlink', '-', 'SpecialChar'],
        ['Bold', 'Italic', 'Underline', '-', 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock']
    ];

    /*features available in toolbar "Classic"*/
    /*FCKConfig.ToolbarSets["Classic"]*/
    config.toolbar_Classic = [
        ['Cut', 'Copy', 'Paste', 'PasteText', 'PasteFromWord'],
        ['Undo', 'Redo', '-', 'Find', 'Replace', '-', 'SelectAll', '-', 'Scayt', '-', 'Link', 'Unlink', '-', 'Image', 'SpecialChar'],
        ['FontSize', '-', 'TextColor', '-', 'Bold', 'Italic', 'Underline', '-', 'JustifyLeft', 'JustifyCenter', 'JustifyRight', 'JustifyBlock'],
        ['NumberedList', 'BulletedList', '-', 'Blockquote']
    ];
};
