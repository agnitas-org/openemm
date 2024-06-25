/*doc
---
title: Upload
name: upload-zone
category: Javascripts - Upload
---
*/

/*doc
---
title: Markup
name: upload-zone-01
parent: upload-zone
---

An upload zone is a feature that allows user to preview and upload a file or multiple files using either classic file input
or drag'n'drop technology.

Let's have a look at the following sample:

```htmlexample
<div data-initializer="upload">
    <mvc:form servletRelativeAction="/upload.action"
                  enctype="multipart/form-data"
                  data-form="resource"
                  data-custom-loader="">

        <div class="tile-content-forms">
            <div class="dropzone" data-upload-dropzone="">
                <div class="dropzone-text">
                    <strong>
                        <i class="icon icon-reply"></i>&nbsp;Drag and drop files here
                    </strong>
                    <span class="btn btn-regular btn-primary btn-upload">
                        <i class="icon icon-cloud-upload"></i>
                        <span class="text">Select Files</span>
                        <input type="file" name="images[].file" multiple="multiple" data-upload="">
                    </span>
                </div>
            </div>
        </div>

        <div class="hidden" data-upload-add="">
            <div class="actions actions-top">
                <div class="action-left">
                    <button type="button" class="btn btn-regular" data-upload-reset="">
                        <i class="icon icon-times"></i>
                        <span class="text"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>
                <div class="action-right">
                    <button type="button" class="btn btn-regular btn-primary" data-form-submit="">
                        <i class="icon icon-cloud-upload"></i>
                        <span class="text"><mvc:message code="button.Upload"/></span>
                    </button>
                </div>
            </div>
            <table class="table table-bordered table-striped">
                <thead>
                    <tr>
                        <th class="squeeze-column"><mvc:message code="mailing.Graphics_Component"/></th>
                        <th><mvc:message code="default.settings"/></th>
                    </tr>
                </thead>
                <tbody data-upload-add-template="upload-images-template-add"></tbody>
            </table>
            <div class="actions">
                <div class="action-left">
                    <button type="button" class="btn btn-regular" data-upload-reset="">
                        <i class="icon icon-times"></i>
                        <span class="text">
                        <mvc:message code="button.Cancel"/>
                    </span>
                    </button>
                </div>
                <div class="action-right">
                    <button type="button" class="btn btn-regular btn-primary" data-form-submit="">
                        <i class="icon icon-cloud-upload"></i>
                        <span class="text"><mvc:message code="button.Upload"/></span>
                    </button>
                </div>
            </div>
        </div>

        <div class="hidden" data-upload-progress="">
            <div class="actions actions-top actions-bottom">
                <div class="action-right">
                    <button type="button" class="btn btn-regular" data-form-abort="">
                        <i class="icon icon-times"></i>
                        <span class="text"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>
            </div>
            <div class="progress-wrapper" data-upload-progress-template="upload-template-progress"></div>
            <div class="actions actions-top">
                <div class="action-right">
                    <button type="button" class="btn btn-regular" data-form-abort="">
                        <i class="icon icon-times"></i>
                        <span class="text"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>
            </div>
        </div>
    </mvc:form>
</div>
```

To enable upload zone use `data-initializer="upload"` attribute at zone's root element. Within that element you should
place a classic file input (must be marked with `data-upload` attribute) and (optional) a droppable area (must be marked
with `data-upload-dropzone` attribute). In case if you use both make sure to place file input within droppable area.

File input alone:

```htmlexample
<div class="tile-content-forms">
  <div class="btn btn-regular btn-primary btn-upload">
      <i class="icon icon-cloud-upload"></i>
      <span class="text">Select Files</span>
      <input type="file" name="images[].file" multiple="multiple" data-upload="">
  </div>
</div>
```

Dropzone with file input:

```htmlexample
<div class="tile-content-forms">
    <div class="dropzone" data-upload-dropzone="">
        <div class="dropzone-text">
            <strong>
                <i class="icon icon-reply"></i>&nbsp;Drag and drop files here
            </strong>
            <span class="btn btn-regular btn-primary btn-upload">
                <i class="icon icon-cloud-upload"></i>
                <span class="text">Select Files</span>
                <input type="file" name="images[].file" multiple="multiple" data-upload="">
            </span>
        </div>
    </div>
</div>
```
*/

/*doc
---
title: Configuration
name: upload-zone-02
parent: upload-zone
---

The file input element *will not* be used for data uploading anyway so it's only required to enable file browser dialog
and configure upload area. If it has `multiple` attribute then upload area will support multiple files at once. Otherwise
it's restricted to manage a single file at a time. A droppable area then works same way — every new dropped file will replace
the previous one (if any).

The `name` attribute of a file input element is required. Normally it will be used "as is" without changes but if it contains
pair of square brackets then the unique index will be generated (starting from 1) and placed within those brackets:

```htmlexample
<div class="tile-content-forms">
  <div class="btn btn-regular btn-primary btn-upload">
      <i class="icon icon-cloud-upload"></i>
      <span class="text">Select Files</span>
      <input type="file" name="images[]" multiple="multiple" data-upload="">
  </div>
</div>
```

Here `images[1]`, `images[2]`, `images[3]`… will be generated on submission. Even if you use single file mode the brackets will be supplied with index `1`.

The current selection can be reset and there are two ways to do that. The first way is to trigger `upload:reset` event on
the upload zone's root element (the one marked with `data-initializer="upload"` attribute). The second way it to click on
any button or link marked with `data-upload-reset` attribute and placed within upload zone.
*/

/*doc
---
title: Preview area
name: upload-zone-03
parent: upload-zone
---

The preview area is the UI part responsible for visual representation of the selected files. Also it can be used to accept
additional user input related to selected files. The basic structure is very simple:

```htmlexample
<div class="hidden" data-upload-add="">
    <div data-upload-add-template="upload-images-template-add"></div>
</div>
```

The root element (marked with `data-upload-add`) is shown or hidden depending on available selection and form submission.
The container element (marked with `data-upload-add-template`) is populated with entries generated out of preferred template
(see `AGN.Lib.Template` and `AGN.Opt.Templates`) as soon as you select files.
*/

/*doc
---
title: Progress area
name: upload-zone-04
parent: upload-zone
---

The progress area is basically a progress bar. Usually it also contains a button for submission abortion.

Let's have a look at an example:

```htmlexample
<div class="hidden" data-upload-progress="">
    <div class="actions actions-top actions-bottom">
        <div class="action-right">
            <button type="button" class="btn btn-regular" data-form-abort="">
                <i class="icon icon-times"></i>
                <span class="text">Cancel</span>
            </button>
        </div>
    </div>

    <div class="progress-wrapper" data-upload-progress-template="upload-template-progress"></div>
</div>
```

The root element (marked with `data-upload-progress`) is shown during form submission. The container element (marked with `data-upload-progress-template`)
is populated with an HTML generated out of preferred template once on `form:loadershow` event fired by form (see Submission section)
and then re-populated every time on `form:progress` event.
*/

/*doc
---
title: Validation
name: upload-zone-05
parent: upload-zone
---

In order to validate files on selection you can listen to `upload:add` event fired at upload zone's root element.
It fires for each file being added. Call `event.preventDefault()` inside listener to reject that file.
*/

/*doc
---
title: Submission
name: upload-zone-06
parent: upload-zone
---

The form is required and once you select files they are scheduled for submission via `AGN.Lib.Form.setValueOnce`. Basically
you can use form of any type but first make sure that the given form supports file uploading.

When selection is reset then all managed files are excluded and will not be uploaded on form submission.

Here are the events of the form element that the upload zone is listening to: `form:loadershow`, `form:loaderhide`, `form:progress`,
`form:submit`, `submitted` and `form:abort`. During form submission the preview area is hidden and progress area is shown.
On `form:abort` event the selection is restored. On `submitted` event the selection is reset.

Please notice that the `form:loadershow`, `form:loaderhide` and `form:progress` events are only enabled when form element has `data-custom-loader` attribute.
*/
AGN.Lib.DomInitializer.new('upload', function ($e) {
  const Upload = AGN.Lib.Upload;

  const $dropzone = $findDropzone($e);
  const $extraDropzone = $findDropzone($e, true);
  const $file = ($dropzone.exists() ? $dropzone : $e).find('[data-upload]');

  if (!$file.exists() && !$dropzone.exists()) {
    return;
  }

  let upload = Upload.get($dropzone, $file);
  if (!upload) {
    upload = new Upload($dropzone, $file);
  }

  if ($extraDropzone.exists()) {
    new Upload(
      $extraDropzone,
      $extraDropzone.find('[data-upload]'),
      upload
    );
  }

  const inputUploadName = $file.data('upload') || '';
  const isInputIndexed = inputUploadName.includes('[]');

  const form = AGN.Lib.Form.get($e);
  const $form = form.get$();
  if ($form.exists()) {
    const $progress = findProgressArea();
    const makeProgressBar = AGN.Lib.Template.prepare($progress.data('upload-progress-template') || 'progress');

    exposeSelection($form);

    $form.on('form:loadershow', () => {
      $progress.html(makeProgressBar({currentProgress: true}));
      $progress.show();
    });
    $form.on('form:loaderhide', () => $progress.hide());
    $form.on('form:submit', () => {
    });
    $form.on('submitted', reset);
    $form.on('form:abort', () => {
      if (getSelection().length) {
        setFormValues();
      }
    });
    $form.on('form:progress', (e, data) => {
      $progress.html(makeProgressBar({currentProgress: data.progress}));
    });

    $dropzone.on('upload:reset', reset);
    $dropzone.on('upload:dropped', () => {
      setFormValues();
      exposeSelection($form);
      if ($dropzone.data('upload-dropzone') === 'submit') {
        $dropzone.hide(); // progress shown instead
        form.submit();
      }
    });
    $e.on('click', 'a[data-upload-reset], button[data-upload-reset]', reset);
  }

  function $findDropzone($el, extra = false) {
    if (extra) {
      return $el.find('[data-upload-dropzone="extra"]').first();
    }

    const $dropzone = $($el.data('linked-dropzone'));
    return $dropzone.length ? $dropzone : $el.find('[data-upload-dropzone]').first();
  }

  function reset() {
    if (isInputIndexed) {
      getSelection().forEach((s, index) => {
        form.setValueOnce(getIndexedUploadName(index), undefined);
      });
    } else {
      form.setValueOnce(inputUploadName, undefined);
    }

    upload.reset();
    exposeSelection($form);
    $dropzone.show(); // may be hidden on progress
  }

  function setFormValues() {
    const selection = getSelection();

    if (isInputIndexed) {
      selection.forEach((s, index) => {
        form.setValueOnce(getIndexedUploadName(index), s);
      });
    } else {
      form.setValueOnce(inputUploadName, selection);
    }

    form.get$().trigger('upload:filesSet', [selection]);
  }

  function getIndexedUploadName(index) {
    return inputUploadName.replace('[]', `[${index}]`);
  }

  function findProgressArea() {
    const $progress = $dropzone.nextAll('[data-upload-progress]').first();
    return $progress.length ? $progress : $e.find('[data-upload-progress]');
  }

  function exposeSelection($el) {
    $e.data('upload-selection', getSelection());
  }

  function getSelection() {
    return upload ? upload.getSelection() : [];
  }
});
