/*doc
---
title: Upload & Dropzone
name: upload-zone
category: Components - Upload & Dropzone
---
*/

/*doc
---
title: Dropzone
name: upload-zone-01
parent: upload-zone
---

An upload zone is a feature that allows user to preview and upload a file or multiple files using either classic file input
or drag'n'drop technology.

Let's have a look at the following sample:

```htmlexample
<form class="tiles-container" style="height: 500px">
    <div class="tile" data-initializer="upload">
        <div class="tile-body d-flex flex-column gap-3">
            <div id="dropzone-styleguide" class="dropzone flex-grow-1" data-upload-dropzone>
                <div class="dropzone-text">
                    <b><i class="icon icon-reply"></i>&nbsp; Drag & drop files here or</b>
                    <span class="btn btn-primary btn-upload">
                        <i class="icon icon-file-upload"></i>
                        <span class="text"> Select files</span>
                        <input data-upload="uploads[].file" type="file" multiple="multiple"/>
                    </span>
                </div>
            </div>

            <div data-upload-progress style="display:none;"></div>
        </div>
    </div>

    <div id="styleguide-preview-tile" class="tile">
        <div class="tile-body d-flex flex-column gap-3"></div>
    </div>
</form>

<script type="text/javascript">
    const detectPreviewUrl = file => {
      if (/^image/.test(file.type)) {
        return URL.createObjectURL(file);
      }

      return '';
    }

    AGN.Lib.Action.new({'upload:add': '#dropzone-styleguide'}, function() {
        const file = this.data.file;

        const $el = AGN.Lib.Template.dom('styleguide-image-preview', {
          previewUrl: detectPreviewUrl(file),
          fileName: file.name,
          index: this.data.index
        });
        $('#styleguide-preview-tile > .tile-body').append($el);
    });
</script>

<script id="styleguide-image-preview" type="text/x-mustache-template">
  <div class="tile tile--xs h-auto flex-none">
      <div class="tile-body d-flex gap-3 align-items-center">
          <div class="flex-center flex-none" style="width: 100px; height: 100px">
            {{ if (previewUrl) { }}
              <img src="{{= previewUrl }}" alt="No preview" class="mw-100 mh-100">
            {{ } else { }}
              <img src="/assets/core/images/facelift/no_preview.svg" class="mw-100 mh-100" />
            {{ } }}
          </div>

          <div class="flex-grow-1">
              <label class="form-label">Name of file ({{- index }})</label>
              <input type="text" class="form-control" value="{{- fileName }}">
          <div>
      </div>
  </div>
</script>
```

To enable upload zone use `data-initializer="upload"` attribute at zone's root element. Within that element you should
place a classic file input (must be marked with `data-upload` attribute) and (optional) a droppable area (must be marked
with `data-upload-dropzone` attribute). In case if you use both make sure to place file input within droppable area.

File input alone:

```htmlexample
<div class="btn btn-primary btn-upload">
    <i class="icon icon-file-upload"></i>
    <span class="text">Select Files</span>
    <input type="file" multiple="multiple" data-upload="images[].file">
</div>
```

Dropzone with file input:

```htmlexample
<div class="tile">
  <div class="tile-body">
     <div id="dropzone" class="dropzone flex-grow-1" data-upload-dropzone>
          <div class="dropzone-text">
              <b><i class="icon icon-reply"></i>&nbsp; Drag & drop files here or</b>
              <span class="btn btn-primary btn-upload">
                  <i class="icon icon-file-upload"></i>
                  <span class="text"> Select files</span>
                  <input data-upload="uploads[].file" type="file" multiple="multiple"/>
              </span>
          </div>
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

The `data-upload` of a file input element is required. Normally it stores name of parameter under which the file will be uploaded, but if it contains
pair of square brackets then the unique index will be generated (starting from 0) and placed within those brackets:

```htmlexample
<div class="btn btn-primary btn-upload">
    <i class="icon icon-file-upload"></i>
    <span class="text">Select Files</span>
    <input type="file" multiple="multiple" data-upload="images[]">
</div>
```

Here `images[0]`, `images[1]`, `images[2]`… will be generated on submission. Even if you use single file mode the brackets will be supplied with index `0`.

The current selection can be reset and there are two ways to do that. The first way is to trigger `upload:reset` event on
the upload zone's element (the one marked with `data-upload-dropzone` attribute). The second way it to click on
any button or link marked with `data-upload-reset` attribute and placed within upload zone.
If button placed outsize the upload zone, then `data-linked-dropzone` attribute can be used. Here you should specify selector for dropzone.
*/

/*doc
---
title: Preview area
name: upload-zone-03
parent: upload-zone
---

The preview area is the UI part responsible for visual representation of the selected files. Also it can be used to accept
additional user input related to selected files. It can be located either directly on the page or in a modal window.

By subscribing to the `upload:add` event, you can build visual blocks and add them wherever you need.

*/

/*doc
---
title: Progress area
name: upload-zone-04
parent: upload-zone
---

The progress area is basically a progress bar. The container where it will be located should be marked with `data-upload-progress` attribute.
If you want to specify custom progress bar template, then you need to add `data-upload-progress-template` attribute and specify template name.

Let's have a look at an example:

```htmlexample
<div data-upload-progress style="display: none" data-upload-progress-template="upload-template-progress"></div>
```

The root element (marked with `data-upload-progress`) is shown during form submission and is populated with an HTML generated out of preferred template once on `form:loadershow` event fired by form (see Submission section)
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

/*doc
---
title: Extra dropzone
name: upload-zone-07
parent: upload-zone
---

In cases where the dropzone is on the page, and file previews are in the modal, it is no longer possible to load new files.
In this case, you can add an extra dropzone and specify `data-upload-dropzone="extra"` attribute.

Here's an example:

```htmlexample
<div class="tile" data-initializer="upload" style="height: 150px">
    <div class="tile-body d-flex flex-column gap-3">
        <div id="dropzone-modal" class="dropzone flex-grow-1" data-upload-dropzone>
            <div class="dropzone-text">
                <b><i class="icon icon-reply"></i>&nbsp; Drag & drop files here or</b>
                <span class="btn btn-primary btn-upload">
                    <i class="icon icon-file-upload"></i>
                    <span class="text"> Select files</span>
                    <input data-upload="uploads[].file" type="file" multiple="multiple"/>
                </span>
            </div>
        </div>

        <div data-upload-progress style="display:none;"></div>
    </div>
</div>

<script type="text/javascript">
  AGN.Lib.Action.new({'upload:add': '#dropzone-modal'}, function() {
      const file = this.data.file;

      const $item = AGN.Lib.Template.dom('styleguide-image-preview', {
        previewUrl: detectPreviewUrl(file),
        fileName: file.name,
        index: this.data.index
      });

      let $modal = $('#upload-modal');
      if (!$modal.exists()) {
        $modal = AGN.Lib.Modal.fromTemplate('styleguide-upload-modal');
      }

      $modal.find('#uploads-container').append($item);
  });
</script>

<script id="styleguide-upload-modal" type="text/x-mustache-template">
    <form id="upload-modal" class="modal" tabindex="-1" enctype="multipart/form-data" data-custom-loader="true" data-initializer="upload" data-linked-dropzone="#dropzone-modal">
        <div class="modal-dialog modal-fullscreen-xl-down modal-xl">
            <div class="modal-content">
                <div class="modal-header">
                    <h1 class="modal-title">Upload</h1>
                    <button type="button" class="btn-close" data-bs-dismiss="modal">
                        <span class="sr-only"><mvc:message code="button.Cancel"/></span>
                    </button>
                </div>

                <div class="modal-body js-scrollable">
                    <div id="dropzone-extra" class="dropzone flex-grow-1" data-upload-dropzone="extra">
                        <div class="dropzone-text">
                            <b><i class="icon icon-reply"></i>&nbsp;Drag & drop additional files here or</b>
                            <span class="btn btn-sm btn-primary btn-upload">
                                <i class="icon icon-file-upload"></i>
                                <span class="text">Select files</span>
                                <input id="upload-files-extra" data-upload type="file" multiple="multiple"/>
                            </span>
                        </div>
                    </div>
                    <div id="uploads-container" class="d-flex flex-column gap-2 mt-2"></div>
                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-primary" data-form-submit>
                        <i class="icon icon-file-upload"></i>
                        <span>Upload</span>
                    </button>
                </div>
            </div>
        </div>
    </mvc:form>
</script>
```
*/

AGN.Lib.DomInitializer.new('upload', function ($e) {

  const Upload = AGN.Lib.Upload;
  const Template = AGN.Lib.Template;

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
    const progressBarTemplate = $progress.data('upload-progress-template') || 'progress';

    exposeSelection();

    $form.on('form:loadershow', () => {
      $progress.html(Template.text(progressBarTemplate,{currentProgress: true}));
      $progress.show();
    });
    $form.on('form:loaderhide', () => $progress.hide());
    $form.on('form:submit', () => setFormValues());
    $form.on('submitted', reset);
    $form.on('form:progress', (e, data) => {
      $progress.html(Template.text(progressBarTemplate,{currentProgress: data.progress}));
    });

    $dropzone.on('upload:reset', reset);
    $dropzone.on('upload:dropped', (e, files) => {
      exposeSelection();
      if ($dropzone.data('upload-dropzone') === 'submit') {
        $dropzone.hide(); // progress shown instead
        form.submit();
      }
    });
  }

  function $findDropzone($el, extra = false) {
    if (extra) {
      return $el.find('[data-upload-dropzone="extra"]').first();
    }

    const $dropzone = $($el.data('linked-dropzone'));
    return $dropzone.exists() ? $dropzone : $el.find('[data-upload-dropzone]').first();
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
    exposeSelection();
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
  }

  function getIndexedUploadName(index) {
    return inputUploadName.replace('[]', `[${index}]`);
  }

  function findProgressArea() {
    const $progress = $dropzone.nextAll('[data-upload-progress]').first();
    return $progress.length ? $progress : $e.find('[data-upload-progress]');
  }

  function exposeSelection() {
    $e.data('upload-selection', getSelection());
  }

  function getSelection() {
    return upload ? upload.getSelection() : [];
  }
});
