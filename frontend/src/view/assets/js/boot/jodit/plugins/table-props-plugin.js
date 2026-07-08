(() => {

  if (!window.Jodit) {
    return;
  }

  const MODAL_HTML = String.raw`
    <div class="modal" tabindex="-1">
        <div class="modal-dialog modal-lg">
            <div class="modal-content" data-controller="wysiwyg-agn-tags">
                <div class="modal-header">
                    <h1 class="modal-title">${t('wysiwyg.dialogs.table.attributes')}</h1>
                    <button type="button" class="btn-close js-confirm-negative" data-bs-dismiss="modal">
                        <span class="sr-only">${t('defaults.cancel')}</span>
                    </button>
                </div>
    
                <div class="modal-body row g-3">
                    <div class="col-6">
                        <label for="jodit-table-rows" class="form-label">${t('wysiwyg.dialogs.table.rows')}</label>
                        <input type="text" id="jodit-table-rows" class="form-control"
                               oninput="this.value = !!this.value && Math.abs(this.value) >= 1 ? Math.abs(this.value) : null"
                               min="1" pattern="\d+" placeholder="3" value="3"
                        />
                    </div>
    
                    <div class="col-6">
                        <label for="jodit-table-cols" class="form-label">${t('wysiwyg.dialogs.table.columns')}</label>
                        <input type="text" id="jodit-table-cols" class="form-control"
                               oninput="this.value = !!this.value && Math.abs(this.value) >= 1 ? Math.abs(this.value) : null"
                               min="1" pattern="\d+" placeholder="2" value="2"
                        />
                    </div>
    
                    <div class="col-6">
                        <label for="jodit-table-width" class="form-label">${t('wysiwyg.dialogs.table.width')}</label>
                        <input type="text" id="jodit-table-width" class="form-control"
                               oninput="this.value = this.value.replace(/[^0-9%]/g,'').replace(/(%.+)$/, '%')"
                               placeholder="e.g. 100% or 400" value="500"
                        />
                    </div>
    
                    <div class="col-6">
                        <label for="jodit-table-height" class="form-label">${t('wysiwyg.dialogs.table.height')}</label>
                        <input type="text" id="jodit-table-height" class="form-control"
                               oninput="this.value = this.value.replace(/[^0-9%]/g,'').replace(/(%.+)$/, '%')"
                               placeholder="e.g. 100% or 400" value=""
                        />
                    </div>
    
                    <div class="col-4">
                        <label for="jodit-table-headers" class="form-label">${t('wysiwyg.dialogs.table.headers')}</label>
                        <select id="jodit-table-headers" class="form-control">
                            <option value="none">${t('defaults.none')}</option>
                            <option value="top">${t('wysiwyg.dialogs.table.top')}</option>
                            <option value="left">${t('wysiwyg.dialogs.table.left')}</option>
                            <option value="both">${t('wysiwyg.dialogs.table.topAndLeft')}</option>
                        </select>
                    </div>
    
                    <div class="col-4">
                        <label for="jodit-table-border" class="form-label">${t('wysiwyg.dialogs.table.border')}</label>
                        <input id="jodit-table-border" class="form-control" type="number"
                               oninput="this.value = !!this.value && Math.abs(this.value) >= 0 ? Math.abs(this.value) : null"
                               min="0" pattern="\d+" placeholder="0" value="1"
                        />
                    </div>
    
                    <div class="col-4">
                        <label for="jodit-table-align" class="form-label">${t('wysiwyg.dialogs.table.alignment')}</label>
                        <select id="jodit-table-align" class="form-control">
                            <option value="none">${t('defaults.none')}</option>
                            <option value="left">${t('wysiwyg.dialogs.table.align_left')}</option>
                            <option value="Center">${t('wysiwyg.dialogs.table.center')}</option>
                            <option value="Right">${t('wysiwyg.dialogs.table.align_right')}</option>
                        </select>
                    </div>
    
                    <div class="col-6">
                        <label for="jodit-table-caption" class="form-label">${t('wysiwyg.dialogs.table.caption')}</label>
                        <input type="text" id="jodit-table-caption" class="form-control" placeholder="${t('wysiwyg.dialogs.table.caption')}"/>
                    </div>
    
                    <div class="col-6">
                        <label for="jodit-table-summary" class="form-label">${t('wysiwyg.dialogs.table.summary')}</label>
                        <input type="text" id="jodit-table-summary" class="form-control" placeholder="${t('wysiwyg.dialogs.table.summary')}"/>
                    </div>
    
                    <div class="col-6">
                        <label for="jodit-table-cellpadding" class="form-label">${t('wysiwyg.dialogs.table.cellpadding')}</label>
                        <input type="number" id="jodit-table-cellpadding" class="form-control"
                               oninput="this.value = !!this.value && Math.abs(this.value) >= 0 ? Math.abs(this.value) : null"
                               min="0" pattern="\d+" placeholder="0" value="1"
                        />
                    </div>
    
                    <div class="col-6">
                        <label for="jodit-table-cellspacing" class="form-label">${t('wysiwyg.dialogs.table.cellspacing')}</label>
                        <input type="number" id="jodit-table-cellspacing" class="form-control"
                               oninput="this.value = !!this.value && Math.abs(this.value) >= 0 ? Math.abs(this.value) : null"
                               min="0" pattern="\d+" placeholder="0" value="1"
                        />
                    </div>
                </div>
    
                <div class="modal-footer">
                    <button type="button" class="btn btn-icon btn-danger js-confirm-negative">
                        <i class="icon icon-times"></i>
                        <span class="text">${t('defaults.cancel')}</span>
                    </button>
                    <button type="button" class="btn btn-icon btn-primary js-confirm-positive">
                        <i class="icon icon-plus"></i>
                        <span class="text">${t('wysiwyg.dialogs.table.insert')}</span>
                    </button>
                </div>
            </div>
        </div>
    </div>
`

  Jodit.modules.Icon.set('table-props', Jodit.modules.Icon.get('table'));

  Jodit.plugins.add('table-props', editor => {

    editor.options.controls['table-props'] = {
      icon: 'table-props',
      tooltip: 'Insert table',
      exec: editor => {
        AGN.Lib.Confirm
          .create(MODAL_HTML)
          .done(() => insertTable(editor));
      }
    };
  });

  const escapeHtml = (s = '') => String(s).replace(/[&<>"']/g, m => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;'
  })[m]);

  const buildTableAttrs = (params = {}) => {
    const attrs = [];
    if (params.width) {
      attrs.push(`width="${params.width}"`);
    }
    if (params.height) {
      attrs.push(`height="${params.height}"`);
    }
    if (params.border != null && params.border !== '') {
      attrs.push(`border="${params.border}"`);
    }
    if (params.cellpadding != null && params.cellpadding !== '') {
      attrs.push(`cellpadding="${params.cellpadding}"`);
    }
    if (params.cellspacing != null && params.cellspacing !== '') {
      attrs.push(`cellspacing="${params.cellspacing}"`);
    }
    if (params.summary) {
      attrs.push(`summary="${escapeHtml(params.summary)}"`);
    }
    if (params.align && String(params.align).toLowerCase() !== 'none') {
      attrs.push(`align="${params.align}"`);
    }
    if (attrs.length) {
      return ' ' + attrs.join(' ');
    }
    return '';
  };

  function insertTable(editor) {
    const params = {
      rows: Number.parseInt($('#jodit-table-rows').val(), 10) || 1,
      cols: Number.parseInt($('#jodit-table-cols').val(), 10) || 1,
      width: $('#jodit-table-width').val().trim(),
      height: $('#jodit-table-height').val().trim(),
      headers: $('#jodit-table-headers').val(),
      border: $('#jodit-table-border').val(),
      align: $('#jodit-table-align').val(),
      caption: $('#jodit-table-caption').val().trim(),
      summary: $('#jodit-table-summary').val().trim(),
      cellpadding: $('#jodit-table-cellpadding').val(),
      cellspacing: Number.parseInt($('#jodit-table-cellspacing').val(), 10) || 0,
    };

    const html = buildTableHtml(params);
    insertIntoEditor(editor, html);
  }

  function buildThead(hasFirstRow, cols) {
    if (!hasFirstRow) {
      return '';
    }
    let ths = '';
    for (let c = 0; c < cols; c++) {
      ths += `      <th scope="col"><br></th>\n`;
    }
    return `  <thead>\n    <tr>\n${ths}    </tr>\n  </thead>\n`;
  }

  function buildTbody(hasFirstCol, rows, cols, hasFirstRow) {
    const bodyRowCount = Math.max(0, rows - (hasFirstRow ? 1 : 0));
    let tbodyRowsHtml = '';
    for (let r = 0; r < bodyRowCount; r++) {
      tbodyRowsHtml += '    <tr>\n';
      for (let c = 0; c < cols; c++) {
        if (hasFirstCol && c === 0) {
          tbodyRowsHtml += '      <th scope="row"><br></th>\n';
        } else {
          tbodyRowsHtml += '      <td><br></td>\n';
        }
      }
      tbodyRowsHtml += '    </tr>\n';
    }
    return tbodyRowsHtml;
  }

  function buildTableHtml(params = {}) {
    const rows = Math.max(1, Number(params.rows) || 1);
    const cols = Math.max(1, Number(params.cols) || 1);
    const attrStr = buildTableAttrs(params);

    let captionHtml = '';
    if (params.caption) {
      const content = params.caption.trim() === '' ? '<br>' : escapeHtml(params.caption);
      captionHtml = `  <caption style="min-height: 1em; padding: 5px">${content}</caption>\n`;
    }

    const hdr = String(params.headers || '').trim().toLowerCase();
    const isBoth = hdr === 'both';
    const hasFirstRow = isBoth || hdr === 'top';
    const hasFirstCol = isBoth || hdr === 'left';

    const theadHtml = buildThead(hasFirstRow, cols);
    const tbodyRowsHtml = buildTbody(hasFirstCol, rows, cols, hasFirstRow);

    return `<table${attrStr}>\n${captionHtml}${theadHtml}  <tbody>\n${tbodyRowsHtml}  </tbody>\n</table>\n`;
  }

  const insertIntoEditor = (editor, html) => {
    const inserter =
      editor?.selection?.insertHTML?.bind(editor.selection) ??
      editor?.s?.insertHTML?.bind(editor.s) ??
      editor?.insertHTML?.bind(editor) ??
      (h => editor?.editor?.insertAdjacentHTML?.('beforeend', h));

    inserter?.(html);
    editor?.events?.fire?.('change');
  };
})();
