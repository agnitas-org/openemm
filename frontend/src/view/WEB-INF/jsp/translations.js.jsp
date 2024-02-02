<%@ page contentType="application/javascript" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring"%>

window.I18n = {
    error: {
      workflow: {
        saveActivatedWorkflow: '<mvc:message javaScriptEscape="true" code="error.workflow.SaveActivatedWorkflow"/>',
        editActivatedWorkflow: '<mvc:message javaScriptEscape="true" code="error.workflow.editActivatedWorkflow"/>',
        editPartActivatedWorkflow: '<mvc:message javaScriptEscape="true" code="error.workflow.editPartOfActivatedWorkflow"/>',
        editUsingActivatedWorkflow: '<mvc:message javaScriptEscape="true" code="error.workflow.editUsingActivatedWorkflow"/>',
        wrongMaxRecipientsFormat: '<mvc:message javaScriptEscape="true" code="error.workflow.wrongMaxRecipientsFormat"/>',
        maxRecipientsTooBig: '<mvc:message javaScriptEscape="true" code="error.workflow.maxRecipientsTooBig"/>',
        maxRecipientsLessThanZero: '<mvc:message javaScriptEscape="true" code="error.workflow.maxRecipientsLessThanZero"/>',
        noLinkSelected: '<mvc:message javaScriptEscape="true" code="error.workflow.noLinkSelected"/>',
        noValidThreshold: '<mvc:message javaScriptEscape="true" code="error.workflow.NoValidThreshold"/>',
        autoImportPermission: '<mvc:message javaScriptEscape="true" code="error.workflow.autoImportPermission"/>',
        autoExportPermission: '<mvc:message javaScriptEscape="true" code="error.workflow.autoExportPermission"/>',
        followupPermission: '<mvc:message javaScriptEscape="true" code="error.workflow.followupPermission"/>',
        startDateOmitted: '<mvc:message javaScriptEscape="true" code="error.workflow.StartDateValueOmitted"/>',
        notAddedMailingList: '<mvc:message javaScriptEscape="true" code="error.workflow.notAddedMailingList"/>',
        noMailing: '<mvc:message javaScriptEscape="true" code="error.workflow.noMailing"/>',
        noForm: '<mvc:message javaScriptEscape="true" code="error.workflow.NoForm"/>',
        noImport: '<mvc:message javaScriptEscape="true" code="error.workflow.NoImport"/>',
        noExport: '<mvc:message javaScriptEscape="true" code="error.workflow.NoExport"/>',
        shortName: '<mvc:message javaScriptEscape="true" code="error.name.too.short"/>',
        emptyRecipientList: '<mvc:message javaScriptEscape="true" code="calendar.error.emptyRecipientList"/>',
        autoImportInUse: '<mvc:message javaScriptEscape="true" code="error.workflow.autoImport.used"/>',
        autoExportInUse: '<mvc:message javaScriptEscape="true" code="error.workflow.autoExport.used"/>',
        deadlineIsTooShortForImport: '<mvc:message javaScriptEscape="true" code="error.workflow.autoImport.delay.tooShort" arguments="%s"/>',
        autoOptimizationDecisionForbidden: '<mvc:message javaScriptEscape="true" code="error.workflow.decision.auto.optimisation" />'
      },
      delay: {
        60:'<mvc:message javaScriptEscape="true" code="error.delay.60"/>'
      },
      upload: {
        email: '<mvc:message javaScriptEscape="true" code="error.upload.email"/>',
        file: '<mvc:message javaScriptEscape="true" code="error.upload.file"/>'
      },
      mailing: {
        invalidPriorityCount: '<mvc:message javaScriptEscape="true" code="mailing.priority.maxMails"/>',
        exclusiveLockingFailed: '<mvc:message javaScriptEscape="true" code="error.mailing.locked.advanced" arguments="%s"/>'
      },
      enterEmailAddresses: '<mvc:message javaScriptEscape="true" code="enterEmailAddresses"/>',
      inUse: '<mvc:message javaScriptEscape="true" code="error.email.duplicated"/>',
      content: {
        empty: '<mvc:message javaScriptEscape="true" code="error.content.empty"/>'
      },
      isBlacklisted: '<mvc:message javaScriptEscape="true" code="error.email.blacklisted" arguments="${[null]}"/>',
      grid: {
        noCategorySelected: '<mvc:message javaScriptEscape="true" code="grid.mediapool.image.no.category"/>'
      },
      recipient: {
        restricted: '<mvc:message javaScriptEscape="true" code="error.access.limit.targetgroup" />'
      },
      statistic: {
        period_format: '<mvc:message javaScriptEscape="true" code="error.period.format" />'
      }
    },
    date: {
      firstDayOfWeek: 1,
      format: '<mvc:message javaScriptEscape="true" code="datePicker.dateFormat" />',
      weekdaysShort: ['<mvc:message javaScriptEscape="true" code="weekdayShort.sunday" />', '<mvc:message javaScriptEscape="true" code="weekdayShort.monday" />', '<mvc:message javaScriptEscape="true" code="weekdayShort.tuesday" />', '<mvc:message javaScriptEscape="true" code="weekdayShort.wednesday" />', '<mvc:message javaScriptEscape="true" code="weekdayShort.thursday" />', '<mvc:message javaScriptEscape="true" code="weekdayShort.friday" />', '<mvc:message javaScriptEscape="true" code="weekdayShort.saturday" />'],
      weekdaysFull: ['<mvc:message javaScriptEscape="true" code="calendar.dayOfWeek.1" />', '<mvc:message javaScriptEscape="true" code="calendar.dayOfWeek.2" />', '<mvc:message javaScriptEscape="true" code="calendar.dayOfWeek.3" />', '<mvc:message javaScriptEscape="true" code="calendar.dayOfWeek.4" />', '<mvc:message javaScriptEscape="true" code="calendar.dayOfWeek.5" />', '<mvc:message javaScriptEscape="true" code="calendar.dayOfWeek.6" />', '<mvc:message javaScriptEscape="true" code="calendar.dayOfWeek.7" />'],
      monthsShort: ['<mvc:message javaScriptEscape="true" code="monthShort.january" />', '<mvc:message javaScriptEscape="true" code="monthShort.february" />', '<mvc:message javaScriptEscape="true" code="monthShort.march" />', '<mvc:message javaScriptEscape="true" code="monthShort.april" />', '<mvc:message javaScriptEscape="true" code="monthShort.may" />', '<mvc:message javaScriptEscape="true" code="monthShort.june" />', '<mvc:message javaScriptEscape="true" code="monthShort.july" />', '<mvc:message javaScriptEscape="true" code="monthShort.august" />', '<mvc:message javaScriptEscape="true" code="monthShort.september" />', '<mvc:message javaScriptEscape="true" code="monthShort.october" />', '<mvc:message javaScriptEscape="true" code="monthShort.november" />', '<mvc:message javaScriptEscape="true" code="monthShort.december" />'],
      monthsFull: ['<mvc:message javaScriptEscape="true" code="calendar.month.1" />', '<mvc:message javaScriptEscape="true" code="calendar.month.2" />', '<mvc:message javaScriptEscape="true" code="calendar.month.3" />', '<mvc:message javaScriptEscape="true" code="calendar.month.4" />', '<mvc:message javaScriptEscape="true" code="calendar.month.5" />', '<mvc:message javaScriptEscape="true" code="calendar.month.6" />', '<mvc:message javaScriptEscape="true" code="calendar.month.7" />', '<mvc:message javaScriptEscape="true" code="calendar.month.8" />', '<mvc:message javaScriptEscape="true" code="calendar.month.9" />', '<mvc:message javaScriptEscape="true" code="calendar.month.10" />', '<mvc:message javaScriptEscape="true" code="calendar.month.11" />', '<mvc:message javaScriptEscape="true" code="calendar.month.12" />'],
      nextMonth: '<mvc:message javaScriptEscape="true" code="nextMonth" />',
      prevMonth: '<mvc:message javaScriptEscape="true" code="prevMonth" />',
      selectMonth: '<mvc:message javaScriptEscape="true" code="selectMonth" />',
      selectYear: '<mvc:message javaScriptEscape="true" code="selectYear" />',
      formats: {
        label: '<mvc:message javaScriptEscape="true" code="import.dateFormat"/>',
        'DD.MM.YYYY' : '<mvc:message javaScriptEscape="true" code="date.format.DD.MM.YYYY"/>',
        YYYYMMDD: '<mvc:message javaScriptEscape="true" code="default.date.format.YYYYMMDD"/>',
        DDMM: '<mvc:message javaScriptEscape="true" code="date.format.DDMM"/>',
        DDMMYYYY: '<mvc:message javaScriptEscape="true" code="date.format.DDMMYYYY"/>',
        MMDD: '<mvc:message javaScriptEscape="true" code="date.format.MMDD"/>',
        DD: '<mvc:message javaScriptEscape="true" code="date.format.DD"/>',
        MM: '<mvc:message javaScriptEscape="true" code="date.format.MM"/>',
        YYYY: '<mvc:message javaScriptEscape="true" code="default.date.format.YYYY"/>'
      }
    },
    time: {
      format: 'HH:i',
      min: '<mvc:message javaScriptEscape="true" code="default.time.min"/>',
      interval: '<mvc:message javaScriptEscape="true" code="default.interval"/>',
      60: '<mvc:message javaScriptEscape="true" code="default.minutes.60"/>'
    },
    defaults: {
      delete: '<mvc:message javaScriptEscape="true" code="Delete" />',
      today: '<mvc:message javaScriptEscape="true" code="calendar.today.button" />',
      clear: '<mvc:message javaScriptEscape="true" code="GWUA.datepicker.clear.button" />',
      days: '<mvc:message javaScriptEscape="true" code="days" />',
      entries: '<mvc:message javaScriptEscape="true" code="default.entries" />',
      close: '<mvc:message javaScriptEscape="true" code="close" />',
      warning: '<mvc:message javaScriptEscape="true" code="warning" />',
      success: '<mvc:message javaScriptEscape="true" code="default.Success" />',
      error: '<mvc:message javaScriptEscape="true" code="Error" />',
      saved: '<mvc:message javaScriptEscape="true" code="default.changes_saved" />',
      yes: '<mvc:message javaScriptEscape="true" code="default.Yes" />',
      no: '<mvc:message javaScriptEscape="true" code="default.No" />',
      info: '<mvc:message javaScriptEscape="true" code="Info" />',
      ok: '<mvc:message javaScriptEscape="true" code="OK" />',
      cancel: '<mvc:message javaScriptEscape="true" code="button.Cancel"/>',
      logout: '<mvc:message javaScriptEscape="true" code="default.Logout"/>',
      relogin: '<mvc:message javaScriptEscape="true" code="logout.relogin"/>',
      remember: {
        choice: '<mvc:message javaScriptEscape="true" code="remember.choice"/>'
      },
      andMore: '<mvc:message javaScriptEscape="true" code="error.showNumberOfLeft" arguments="%s"/>',
      previewMode: '<mvc:message javaScriptEscape="true" code="mailing.preview.mode"/>',
      window: {
        popout: '<mvc:message javaScriptEscape="true" code="GWUA.window.popout"/>'
      },
      upload: '<mvc:message javaScriptEscape="true" code="UploadFile"/>',
      files: '<mvc:message javaScriptEscape="true" code="mailing.files"/>',
      name: '<mvc:message javaScriptEscape="true" code="default.Name"/>',
      size: '<mvc:message javaScriptEscape="true" code="default.Size"/>',
      changesNotSaved: '<mvc:message javaScriptEscape="true" code="changes_not_saved"/>'
    },
    selects: {
      noMatches: '<mvc:message javaScriptEscape="true" code="default.noMatchesFor" />',
      matches: '<mvc:message javaScriptEscape="true" code="default.matches" />',
      matchesSing: '<mvc:message javaScriptEscape="true" code="default.matchesSing" />',
      searching: '<mvc:message javaScriptEscape="true" code="default.searching" />',
      loadMore: '<mvc:message javaScriptEscape="true" code="default.loadingResults" />',
      errors: {
        ajax: '<mvc:message javaScriptEscape="true" code="errors.ajax" />',
        inputTooShort: '<mvc:message javaScriptEscape="true" code="errors.inputTooShort" />',
        inputTooLong: '<mvc:message javaScriptEscape="true" code="errors.inputTooLong" />',
        selectionTooBig: '<mvc:message javaScriptEscape="true" code="errors.selectionTooBig" />'
      }
    },
    fields: {
      content: {
        charactersEntered: '<mvc:message javaScriptEscape="true" code="editor.charactersEntered" arguments="%s"/>'
      },
      password: {
        safe: '<mvc:message javaScriptEscape="true" code="secure" />',
        unsafe: '<mvc:message javaScriptEscape="true" code="insecure" />',
        matches: '<mvc:message javaScriptEscape="true" code="matches" />',
        matchesNot: '<mvc:message javaScriptEscape="true" code="matchesNot" />',
        successHtml: '<i class="icon icon-state-success"></i> <span class="text">%s</span>',
        unsuccessHtml: '<i class="icon icon-state-alert"></i> <span class="text">%s</span>',
        errors: {
          notMatching: '<mvc:message javaScriptEscape="true" code="error.password.mismatch"/>',
          unsafe: '<mvc:message javaScriptEscape="true" code="insecure" />'
        }
      },
      required: {
        errors: {
          missing: '<mvc:message javaScriptEscape="true" code="error.default.required"/>'
        }
      },
      name: {
        errors: {
          name_in_use: '<mvc:message javaScriptEscape="true" code="error.name_in_use"/>'
        }
      },
      errors: {
        contentLengthExceedsLimit: '<mvc:message javaScriptEscape="true" code="error.contentLengthExceedsLimit" arguments="%s"/>',
        number_nan: '<mvc:message javaScriptEscape="true" code="querybuilder.error.number_nan"/>',
        number_not_integer: '<mvc:message javaScriptEscape="true" code="querybuilder.error.number_not_integer"/>',
        number_not_double: '<mvc:message javaScriptEscape="true" code="querybuilder.error.number_not_double"/>',
        number_exceed_min: '<mvc:message javaScriptEscape="true" code="querybuilder.error.number_exceed_min" arguments="%s"/>',
        number_exceed_max: '<mvc:message javaScriptEscape="true" code="querybuilder.error.number_exceed_max" arguments="%s"/>',
        phone_number_invalid: '<mvc:message javaScriptEscape="true" code="error.phoneNumber.invalidFormat"/>',
        string_exceed_min_length: '<mvc:message javaScriptEscape="true" code="querybuilder.error.string_exceed_min_length" arguments="%s"/>',
        string_exceed_max_length: '<mvc:message javaScriptEscape="true" code="querybuilder.error.string_exceed_max_length" arguments="%s"/>',
        illegal_script_element: '<mvc:message javaScriptEscape="true" code="error.mailing.content.illegal.script"/>'
      },
      mailing: {
        parameter: '<mvc:message javaScriptEscape="true" code="MailingParameter"/>',
        description: '<mvc:message javaScriptEscape="true" code="Description"/>',
        for_mailing: '<mvc:message javaScriptEscape="true" code="mailing.MailingParameter.forMailing"/>',
        change_date: '<mvc:message javaScriptEscape="true" code="default.changeDate"/>'
      },
      mailinglist: {
        errors:{
          removed: '<mvc:message javaScriptEscape="true" code="default.selection.deleted"/>'
        }
      },
      mediapool: {
        errors: {
          mobile_base_duplicate: '<mvc:message javaScriptEscape="true" code="error.mediapool.mobile.duplicate"/>'
        },
        warnings: {
          overwrite_inactive: '<mvc:message javaScriptEscape="true" code="error.mediapool.overwrite" arguments="%s" />'
        }
      }
    },
    messages: {
      error: {
        headline: '<mvc:message javaScriptEscape="true" code="error.global.headline"/>',
        text: '<mvc:message javaScriptEscape="true" code="error.default.message"/>',
        reload: '<mvc:message javaScriptEscape="true" code="error.reload"/>',
        nothing_selected: '<mvc:message javaScriptEscape="true" code="error.default.nothing_selected"/>'
      },
      permission: {
        denied : {
          title: '<mvc:message javaScriptEscape="true" code="permission.denied.title"/>',
          text: '<mvc:message javaScriptEscape="true" code="permission.denied.message"/>'
        }
      }
    },
    autosave: {
      confirm: {
        title: '<mvc:message javaScriptEscape="true" code="restore.confirm.title"/>',
        question: '<mvc:message javaScriptEscape="true" code="restore.confirm.question" arguments="%s"/>'
      },
      success: {
        title: '<mvc:message javaScriptEscape="true" code="restore.confirm.success.title"/>',
        message: '<mvc:message javaScriptEscape="true" code="restore.confirm.success"/>'
      },
      discard: '<mvc:message javaScriptEscape="true" code="default.No"/>',
      restore: '<mvc:message javaScriptEscape="true" code="default.Yes"/>'
    },
    querybuilder: {
      common: {
        add_rule: '<mvc:message javaScriptEscape="true" code="querybuilder.add_rule"/>',
        add_group: '<mvc:message javaScriptEscape="true" code="querybuilder.add_group"/>',
        delete_rule: '<mvc:message javaScriptEscape="true" code="Delete"/>',
        delete_group: '<mvc:message javaScriptEscape="true" code="Delete"/>',
        invert: '<mvc:message javaScriptEscape="true" code="querybuilder.invent"/>',
        anyLink: '<mvc:message javaScriptEscape="true" code="target.link.any" />',
        include_empty: '<mvc:message javaScriptEscape="true" code="querybuilder.empty.include" />'
      },
      conditions: {
        AND: '<mvc:message javaScriptEscape="true" code="condition.and"/>',
        OR: '<mvc:message javaScriptEscape="true" code="condition.or"/>'
      },
      operators: {
        equal: '<mvc:message javaScriptEscape="true" code="operator.equal"/>',
        not_equal: '<mvc:message javaScriptEscape="true" code="operator.not_equal"/>',
        less: '<mvc:message javaScriptEscape="true" code="operator.less"/>',
        less_or_equal: '<mvc:message javaScriptEscape="true" code="operator.less_or_equal"/>',
        greater: '<mvc:message javaScriptEscape="true" code="operator.greater"/>',
        greater_or_equal: '<mvc:message javaScriptEscape="true" code="operator.greater_or_equal"/>',
        in: '<mvc:message javaScriptEscape="true" code="mailing.searchIn"/>',
        not_in: '<mvc:message javaScriptEscape="true" code="operator.notin"/>',
        between: '<mvc:message javaScriptEscape="true" code="operator.between"/>',
        not_between: '<mvc:message javaScriptEscape="true" code="operator.not_between"/>',
        begins_with: '<mvc:message javaScriptEscape="true" code="operator.begins_with"/>',
        not_begins_with: '<mvc:message javaScriptEscape="true" code="operator.not_begins_with"/>',
        contains: '<mvc:message javaScriptEscape="true" code="target.operator.contains"/>',
        not_contains: '<mvc:message javaScriptEscape="true" code="target.operator.not_contains"/>',
        ends_with: '<mvc:message javaScriptEscape="true" code="operator.ends_with"/>',
        not_ends_with: '<mvc:message javaScriptEscape="true" code="operator.not_ends_with"/>',
        is_empty: '<mvc:message javaScriptEscape="true" code="operator.is_empty"/>',
        is_not_empty: '<mvc:message javaScriptEscape="true" code="operator.is_not_empty"/>',
        is_null: '<mvc:message javaScriptEscape="true" code="operator.is_null"/>',
        is_not_null: '<mvc:message javaScriptEscape="true" code="operator.is_not_null"/>',
        before: '<mvc:message javaScriptEscape="true" code="target.operator.before"/>',
        after: '<mvc:message javaScriptEscape="true" code="target.operator.after"/>'
      },
      errors: {
        general: '<mvc:message javaScriptEscape="true" code="error.target.saving"/>',
        invalid_definition: '<mvc:message javaScriptEscape="true" code="error.target.definition"/>',
        no_rule: '<mvc:message javaScriptEscape="true" code="error.target.norule"/>',
        no_filter: '<mvc:message javaScriptEscape="true" code="querybuilder.error.no_filter"/>',
        empty_group: '<mvc:message javaScriptEscape="true" code="querybuilder.error.empty_group"/>',
        radio_empty: '<mvc:message javaScriptEscape="true" code="querybuilder.error.select_empty"/>',
        checkbox_empty: '<mvc:message javaScriptEscape="true" code="querybuilder.error.select_empty"/>',
        select_empty: '<mvc:message javaScriptEscape="true" code="querybuilder.error.select_empty"/>',
        string_empty: '<mvc:message javaScriptEscape="true" code="querybuilder.error.string_empty"/>',
        string_exceed_min_length: '<mvc:message javaScriptEscape="true" code="querybuilder.error.string_exceed_min_length" arguments="${[null]}"/>',
        string_exceed_max_length: '<mvc:message javaScriptEscape="true" code="querybuilder.error.string_exceed_max_length" arguments="${[null]}"/>',
        string_invalid_format: '<mvc:message javaScriptEscape="true" code="querybuilder.error.string_invalid_format" arguments="${[null]}"/>',
        number_nan: '<mvc:message javaScriptEscape="true" code="querybuilder.error.number_nan"/>',
        number_not_integer: '<mvc:message javaScriptEscape="true" code="querybuilder.error.number_not_integer"/>',
        number_not_double: '<mvc:message javaScriptEscape="true" code="querybuilder.error.number_not_double"/>',
        number_exceed_min: '<mvc:message javaScriptEscape="true" code="querybuilder.error.number_exceed_min" arguments="${[null]}"/>',
        number_exceed_max: '<mvc:message javaScriptEscape="true" code="querybuilder.error.number_exceed_max" arguments="${[null]}"/>',
        number_wrong_step: '<mvc:message javaScriptEscape="true" code="querybuilder.error.number_wrong_step" arguments="${[null]}"/>',
        datetime_empty: '<mvc:message javaScriptEscape="true" code="querybuilder.error.datetime_empty"/>',
        datetime_invalid: '<mvc:message javaScriptEscape="true" code="querybuilder.error.datetime_invalid" arguments="${[null]}"/>',
        datetime_exceed_min: '<mvc:message javaScriptEscape="true" code="querybuilder.error.datetime_exceed_min" arguments="${[null]}"/>',
        datetime_exceed_max: '<mvc:message javaScriptEscape="true" code="querybuilder.error.datetime_exceed_max" arguments="${[null]}"/>',
        boolean_not_valid: '<mvc:message javaScriptEscape="true" code="querybuilder.error.boolean_not_valid"/>',
        operator_not_multiple: '<mvc:message javaScriptEscape="true" code="querybuilder.error.operator_not_multiple" arguments="${[null]}"/>'
      }
    },
    workflow: {
      reaction: {
        title: '<mvc:message javaScriptEscape="true" code="workflow.Reaction"/>',
        opened: '<mvc:message javaScriptEscape="true" code="statistic.opened"/>',
        not_opened: '<mvc:message javaScriptEscape="true" code="workflow.reaction.NotOpened" />',
        clicked: '<mvc:message javaScriptEscape="true" code="default.clicked"/>',
        not_clicked: '<mvc:message javaScriptEscape="true" code="workflow.reaction.NotClicked"/>',
        bought: '<mvc:message javaScriptEscape="true" code="workflow.reaction.Bought"/>',
        not_bought: '<mvc:message javaScriptEscape="true" code="workflow.reaction.NotBought"/>',
        download: '<mvc:message javaScriptEscape="true" code="button.Download"/>',
        change_of_profile: '<mvc:message javaScriptEscape="true" code="workflow.reaction.ChangeOfProfile"/>',
        waiting_for_confirm: '<mvc:message javaScriptEscape="true" code="workflow.reaction.WaitingForConfirm"/>',
        opt_in: '<mvc:message javaScriptEscape="true" code="workflow.reaction.OptIn"/>',
        opt_out: '<mvc:message javaScriptEscape="true" code="workflow.reaction.OptOut"/>',
        clicked_on_link: '<mvc:message javaScriptEscape="true" code="workflow.reaction.ClickedOnLink"/>',
        opened_and_clicked: '<mvc:message javaScriptEscape="true" code="workflow.reaction.OpenedAndClicked"/>',
        opened_or_clicked: '<mvc:message javaScriptEscape="true" code="workflow.reaction.OpenedOrClicked"/>',
        confirmed_opt_in: '<mvc:message javaScriptEscape="true" code="workflow.reaction.ConfirmedOptIn"/>'
      },
      start: {
        title: '<mvc:message javaScriptEscape="true" code="workflow.start.title"/>',
        profile_field: '<mvc:message javaScriptEscape="true" code="workflow.start.ProfileField"/>',
        rule: '<mvc:message javaScriptEscape="true" code="workflow.start.Rule"/>',
        start_date: '<mvc:message javaScriptEscape="true" code="workflow.start.StartDate"/>',
        start_event: '<mvc:message javaScriptEscape="true" code="workflow.start.StartEvent"/>',
        reminder_text: '<mvc:message javaScriptEscape="true" code="workflow.start.reminderText"/>',
        reaction_based: '<mvc:message javaScriptEscape="true" code="workflow.start.ReactionBasedStart"/>',
        date_based: '<mvc:message javaScriptEscape="true" code="workflowlist.dateBased"/>',
        action_based: '<mvc:message javaScriptEscape="true" code="workflowlist.actionBased"/>'
      },
      stop: {
        title: '<mvc:message javaScriptEscape="true" code="workflow.stop.title"/>',
        end_date: '<mvc:message javaScriptEscape="true" code="workflow.stop.EndDate"/>',
        end_event: '<mvc:message javaScriptEscape="true" code="workflow.stop.EndEvent"/>',
        open_end: '<mvc:message javaScriptEscape="true" code="workflow.stop.OpenEnd"/>',
        automatic_end: '<mvc:message javaScriptEscape="true" code="workflow.stop.AutomaticEnd"/>',
        reminder_text: '<mvc:message javaScriptEscape="true" code="workflow.stop.reminderText"/>'
      },
      button: {
        copy: '<mvc:message javaScriptEscape="true" code="button.Copy"/>',
        cancel: '<mvc:message javaScriptEscape="true" code="button.Cancel"/>'
      },
      defaults: {
        error: '<mvc:message javaScriptEscape="true" code="Error"/>',
        edit: '<mvc:message javaScriptEscape="true" code="button.Edit"/>',
        delete: '<mvc:message javaScriptEscape="true" code="button.Delete"/>',
        comment: '<mvc:message javaScriptEscape="true" code="button.Comment"/>',
        delay: '<mvc:message javaScriptEscape="true" code="Delay"/>',
        hour: '<mvc:message javaScriptEscape="true" code="Hour"/>',
        hours: '<mvc:message javaScriptEscape="true" code="Hours"/>',
        day: '<mvc:message javaScriptEscape="true" code="Day"/>',
        days: '<mvc:message javaScriptEscape="true" code="Days"/>',
        week: '<mvc:message javaScriptEscape="true" code="Week"/>',
        weeks: '<mvc:message javaScriptEscape="true" code="default.weeks"/>',
        month: '<mvc:message javaScriptEscape="true" code="Month"/>',
        months: '<mvc:message javaScriptEscape="true" code="default.months"/>',
        report: '<mvc:message javaScriptEscape="true" code="Report"/>',
        no_mailing: '<mvc:message javaScriptEscape="true" code="NoMailing"/>',
        yes: '<mvc:message javaScriptEscape="true" code="default.Yes"/>',
        date: '<mvc:message javaScriptEscape="true" code="Date"/>',
        mailing: '<mvc:message javaScriptEscape="true" code="Mailing"/>',
        ckickrate: '<mvc:message javaScriptEscape="true" code="Clickrate"/>',
        mailtracking_required: '<mvc:message javaScriptEscape="true" code="mailtrackingRequired"/>',
        no: '<mvc:message javaScriptEscape="true" code="birt.No"/>',
        title: '<mvc:message javaScriptEscape="true" code="workflow.settings.overtake.title"/>',
        and: '<mvc:message javaScriptEscape="true" code="default.and"/>',
        or: '<mvc:message javaScriptEscape="true" code="default.or"/>'
      },
      deadline: {
        title: '<mvc:message javaScriptEscape="true" code="workflow.deadline"/>',
        minutes: '<mvc:message javaScriptEscape="true" code="workflow.deadline.Minutes"/>',
        minute: '<mvc:message javaScriptEscape="true" code="workflow.deadline.Minute"/>',
      },
      mailing: {
        new: '<mvc:message javaScriptEscape="true" code="dashboard.mailing.new"/>',
        archive: '<mvc:message javaScriptEscape="true" code="mailing.archive"/>',
        edit_mailing_link: '<mvc:message javaScriptEscape="true" code="workflow.mailing.editMailingLink"/>',
        autooptimization: '<mvc:message javaScriptEscape="true" code="mailing.autooptimization"/>',
        action_based: '<mvc:message javaScriptEscape="true" code="mailing.action.based.mailing"/>',
        date_based: '<mvc:message javaScriptEscape="true" code="mailing.Rulebased_Mailing"/>',
        followup: '<mvc:message javaScriptEscape="true" code="mailing.Followup_Mailing"/>',
        "mediatype_sms": '<mvc:message javaScriptEscape="true" code="UserRight.mediatype.sms"/>',
        "mediatype_post": '<mvc:message javaScriptEscape="true" code="UserRight.mediatype.post"/>'
      },

      mailinglist: {
        short: '<mvc:message javaScriptEscape="true" code="workflow.mailinglist.short"/>'
      },
      target: {
        short: '<mvc:message javaScriptEscape="true" code="workflow.target.short"/>'
      },
      statistic: {
        revenue: '<mvc:message javaScriptEscape="true" code="statistic.revenue"/>'
      },

      activating: {
        title: '<mvc:message javaScriptEscape="true" code="workflow.activating.title"/>',
        unpauseTitle: '<mvc:message javaScriptEscape="true" code="button.continue.workflow"/>',
        auto: '<mvc:message javaScriptEscape="true" code="workflow.pause.reactivation"/>'
      },

      report: {
        error : {
          no_report: '<mvc:message javaScriptEscape="true" code="workflow.report.ErrorNoReport"/>'
        }
      },

      copy: {
        question: '<mvc:message javaScriptEscape="true" code="workflow.copy.question"/>',
        question_with_content: '<mvc:message javaScriptEscape="true" code="workflow.copy.withContentQuestion"/>',
      },
      pdf: {
        save_campaign: '<mvc:message javaScriptEscape="true" code="workflow.pdf.saveCampaign"/>',
        save_new_campaign:'<mvc:message javaScriptEscape="true" code="workflow.pdf.saveNewCampaign"/>',
        save_modified_campaign:'<mvc:message javaScriptEscape="true" code="workflow.pdf.saveModifiedCampaign"/>',
      },
      inactivating: {
        title: '<mvc:message javaScriptEscape="true" code="workflow.inactivating.title"/>'
      },

      ownWorkflow: {
        title: '<mvc:message javaScriptEscape="true" code="workflow.ownCampaign"/>',
        copy_title: '<mvc:message javaScriptEscape="true" code="workflow.ownWorkflow.copyWorkflowTitle"/>'
      },

      connect: '<mvc:message javaScriptEscape="true" code="workflow.connect"/>',
      disconnect: '<mvc:message javaScriptEscape="true" code="workflow.disconnect"/>',
      opening_rate: '<mvc:message javaScriptEscape="true" code="workflow.decision.OpeningRate"/>',
      decision: '<mvc:message javaScriptEscape="true" code="workflow.decision"/>',
      recipient: '<mvc:message javaScriptEscape="true" code="Recipient"/>',
      parameter: '<mvc:message javaScriptEscape="true" code="workflow.parameter"/>',
      single: '<mvc:message javaScriptEscape="true" code="workflow.single"/>',
      undo_history: '<mvc:message javaScriptEscape="true" code="workflow.undo.historyIsEmptyDialog.title"/>',
      status: {
        NONE: 'NONE',
        open: '<mvc:message javaScriptEscape="true" code="workflow.view.status.open"/>',
        active: '<mvc:message javaScriptEscape="true" code="workflow.view.status.active"/>',
        inactive: '<mvc:message javaScriptEscape="true" code="workflow.view.status.inActive"/>',
        completed: '<mvc:message javaScriptEscape="true" code="workflow.view.status.complete"/>',
        testing: '<mvc:message javaScriptEscape="true" code="workflow.view.status.testing"/>',
        tested: '<mvc:message javaScriptEscape="true" code="workflow.view.status.tested"/>',
        failed: 'FAILED',
        testing_failed: '<mvc:message javaScriptEscape="true" code="workflow.view.status.testing.failed"/>',
        paused: '<mvc:message javaScriptEscape="true" code="workflow.view.status.paused"/>'
      }
    },
    logon: {
      info: {
        multiple_tabs:'<mvc:message javaScriptEscape="true" code="logon.tabs.multiple"/>'
      },
      session: {
        notification: '<mvc:message javaScriptEscape="true" code="warning.session.expired"/>',
        expired:'<mvc:message javaScriptEscape="true" code="session.timer.expired"/>',
      }
    },
    messenger: {
      recipients: '<mvc:message javaScriptEscape="true" code="Recipients"/>'
    },
    recipient: {
      duplicate: {
        question: '<mvc:message javaScriptEscape="true" code="recipient.duplicate.question"/>'
      },
      blacklisted: {
        question: '<mvc:message javaScriptEscape="true" code="recipient.blacklisted.question"/>'
      },
      existing: {
        btn: '<mvc:message javaScriptEscape="true" code="recipient.existing.switch"/>'
      }
    },
    calendar: {
      error: {
        empty_comment: '<mvc:message javaScriptEscape="true" code="calendar.error.emptyComment"/>',
        long_comment: '<mvc:message javaScriptEscape="true" code="calendar.error.longComment"/>',
        empty_recipient_list: '<mvc:message javaScriptEscape="true" code="calendar.error.emptyRecipientList"/>',
        long_recipient_list: '<mvc:message javaScriptEscape="true" code="calendar.error.longRecipientList"/>',
        invalid_email: '<mvc:message javaScriptEscape="true" code="calendar.error.invalidEmail"/>'
      },
      title: {
        show_hide_comment: '<mvc:message javaScriptEscape="true" code="calendar.ShowHideComments"/>',
        new_comment: '<mvc:message javaScriptEscape="true" code="calendar.NewComment"/>',
        delete_comment: '<mvc:message javaScriptEscape="true" code="calendar.DeleteComment"/>'
      },
      common: {
        comment: '<mvc:message javaScriptEscape="true" code="calendar.Comment"/>',
        new_comment: '<mvc:message javaScriptEscape="true" code="calendar.NewComment"/>',
        edit_comment: '<mvc:message javaScriptEscape="true" code="calendar.EditComment"/>',
        'weekNumber': '<mvc:message javaScriptEscape="true" code="calendar.WeekNumber"/>'
      }
    },
    userform: {
      error: {
        velocity_not_allowed: '<mvc:message javaScriptEscape="true" code="userform.velocityNotAllowed" />',
        invalid_name: '<mvc:message javaScriptEscape="true" code="error.form.invalid_name" />',
        illegal_directive: '<mvc:message javaScriptEscape="true" code="error.form.illegal_directive" arguments="%s" />',
        invalid_link: '<mvc:message javaScriptEscape="true" code="error.invalid_link" arguments="${[\"%s\", \"%s\"]}" />'
      },
      formBuilder: {
        success:{
          htmlGenerated: '<mvc:message javaScriptEscape="true" code="userform.builder.success" />'
        },
        generateHtml: '<mvc:message javaScriptEscape="true" code="userform.builder.generateHtml" />',
        tooltips: {
          agnCTOKEN: '<mvc:message javaScriptEscape="true" code="userform.builder.tooltip.replace.cid" />',
          agnUID: '<mvc:message javaScriptEscape="true" code="userform.builder.tooltip.agnUID" />'
        },
        mediapoolImage: '<mvc:message javaScriptEscape="true" code="mediapool.image" />',
        imageWidth: '<mvc:message javaScriptEscape="true" code="default.image.width" />',
        imageHeight: '<mvc:message javaScriptEscape="true" code="grid.mediapool.image.sizes.height" />',
        imageAlt: '<mvc:message javaScriptEscape="true" code="default.text.alt" />',
        nextForm: '<mvc:message javaScriptEscape="true" code="userform.builder.next" />',
        formName: '<mvc:message javaScriptEscape="true" code="userform.name" />',
        emmField: '<mvc:message javaScriptEscape="true" code="workflow.start.ProfileField" />',
        template: {
          template: '<mvc:message javaScriptEscape="true" code="Template" />',
          subscribe: '<mvc:message javaScriptEscape="true" code="action.op.SubscribeCustomer" />',
          unsubscribe: '<mvc:message javaScriptEscape="true" code="action.op.UnsubscribeCustomer" />',
          profile_change: '<mvc:message javaScriptEscape="true" code="action.op.UpdateCustomer" />',
          other: '<mvc:message javaScriptEscape="true" code="others" />'
        }
      }
    },

    triggerManager: {
      operation: {
        ActivateDoubleOptIn: '<mvc:message javaScriptEscape="true" code="action.op.ActivateDoubleOptIn" />',
        ContentView: '<mvc:message javaScriptEscape="true" code="action.op.ContentView" />',
        ExecuteScript: '<mvc:message javaScriptEscape="true" code="action.op.ExecuteScript" />',
        GetArchiveList: '<mvc:message javaScriptEscape="true" code="action.op.GetArchiveList" />',
        GetArchiveMailing: '<mvc:message javaScriptEscape="true" code="action.op.GetArchiveMailing" />',
        GetCustomer: '<mvc:message javaScriptEscape="true" code="action.op.GetCustomer" />',
        IdentifyCustomer: '<mvc:message javaScriptEscape="true" code="action.op.IdentifyCustomer" />',
        SendMailing: '<mvc:message javaScriptEscape="true" code="action.op.SendMailing" />',
        ServiceMail: '<mvc:message javaScriptEscape="true" code="action.op.ServiceMail" />',
        SubscribeCustomer: '<mvc:message javaScriptEscape="true" code="action.op.SubscribeCustomer" />',
        UnsubscribeCustomer: '<mvc:message javaScriptEscape="true" code="action.op.UnsubscribeCustomer" />',
        UpdateCustomer: '<mvc:message javaScriptEscape="true" code="action.op.UpdateCustomer" />',
        serviceMail: {
          error: {
            senderAddress: '<mvc:message javaScriptEscape="true" code="error.mailing.sender_adress" />',
            recipientAddress: '<mvc:message javaScriptEscape="true" code="error.mailing.recipient_adress" />',
            replyAddress: '<mvc:message javaScriptEscape="true" code="error.mailing.reply_adress" />',
            subjectToShort: '<mvc:message javaScriptEscape="true" code="error.subjectToShort" />',
            subjectToLong: '<mvc:message javaScriptEscape="true" code="error.subjectToLong" />'
          }
        }
      }
    },
    referenceTables: {
      table: {
        error: {
          name: '<mvc:message javaScriptEscape="true" code="error.referenceTable.name.invalid"/>',
          keyColumn: '<mvc:message javaScriptEscape="true" code="error.referenceTable.keyColumn.invalid"/>'
        }
      }
    },
    import: {
      gender: {
        short: {
          0: '<mvc:message javaScriptEscape="true" code="recipient.gender.0.short"/>',
          1: '<mvc:message javaScriptEscape="true" code="recipient.gender.1.short"/>',
          2: '<mvc:message javaScriptEscape="true" code="recipient.gender.2.short"/>',
          3: '<mvc:message javaScriptEscape="true" code="recipient.gender.3.short"/>',
          4: '<mvc:message javaScriptEscape="true" code="recipient.gender.4.short"/>',
          5: '<mvc:message javaScriptEscape="true" code="recipient.gender.5.short"/>'
        },
        error: {
          duplicate: '<mvc:message javaScriptEscape="true" code="error.import.gender.number.duplicate"/>',
          empty: '<mvc:message javaScriptEscape="true" code="error.import.gender.empty"/>'
        }
      },
      columnMapping: {
        error: {
          duplicate: '<mvc:message javaScriptEscape="true" code="error.import.column.duplicate"/>',
          data: '<mvc:message javaScriptEscape="true" code="error.import.invalidDataForField"/>',
          type: '<mvc:message javaScriptEscape="true" code="error.import.dataType" arguments="%s"/>',
          length: '<mvc:message javaScriptEscape="true" code="error.import.dataLength" arguments="%s"/>',
          dateFormat: '<mvc:message javaScriptEscape="true" code="error.profiledb.invalidDefaultValue" arguments="%s"/>',
          func: '<mvc:message javaScriptEscape="true" code="error.import.functions"/>',
          invalidEmail: '<mvc:message javaScriptEscape="true" code="error.email.invalid"/>'
        }
      }
    },
    export: {
      columnMapping: {
        error: {
          invalidColName: '<mvc:message javaScriptEscape="true" code="error.invalidSimpleName"/>',
          nameToShort: '<mvc:message javaScriptEscape="true" code="error.name.too.short"/>',
          duplicate: '<mvc:message javaScriptEscape="true" code="error.export.column.duplicate"/>',
          exist: '<mvc:message javaScriptEscape="true" code="error.export.column.exist"/>'
        }
      }
    },
    forms: '<mvc:message javaScriptEscape="true" code="workflow.panel.forms"/>',
    report: '<mvc:message javaScriptEscape="true" code="Report"/>',
    auto_export: '<mvc:message javaScriptEscape="true" code="autoExport.autoExport"/>',
    auto_import: '<mvc:message javaScriptEscape="true" code="autoImport.autoImport"/>',
    tables: {
      // for filter panel
      range: '<mvc:message javaScriptEscape="true" code="table.paging.range" arguments="${[\"%s\",\"%s\",\"%s\"]}"/>',
      to: '<mvc:message javaScriptEscape="true" code="default.to"/>',
      next: '<mvc:message javaScriptEscape="true" code="button.Next"/>',
      last: '<mvc:message javaScriptEscape="true" code="default.Last"/>',
      first: '<mvc:message javaScriptEscape="true" code="default.First"/>',
      previous: '<mvc:message javaScriptEscape="true" code="button.Previous"/>',
      loadingOoo: '<mvc:message javaScriptEscape="true" code="default.loading"/>',

      // for set filter
      searchOoo: '<mvc:message javaScriptEscape="true" code="default.search"/>',
      blanks: '',

      // for number filter and text filter
      filterOoo: '<mvc:message javaScriptEscape="true" code="report.mailing.filter"/>',
      applyFilter: '<mvc:message javaScriptEscape="true" code="button.Apply"/>',
      clearFilter: '<mvc:message javaScriptEscape="true" code="filter.reset"/>',
      resetFilter: '<mvc:message javaScriptEscape="true" code="filter.reset"/>',

      // for number filter
      equals: '<mvc:message javaScriptEscape="true" code="target.operator.eq"/>',
      notEqual: '<mvc:message javaScriptEscape="true" code="target.operator.neq"/>',
      lessThan: '<mvc:message javaScriptEscape="true" code="target.operator.lt"/>',
      greaterThan: '<mvc:message javaScriptEscape="true" code="target.operator.gt"/>',
      inRange: '<mvc:message javaScriptEscape="true" code="operator.between"/>',
      lessThanOrEqual: '<mvc:message javaScriptEscape="true" code="target.operator.leq"/>',
      greaterThanOrEqual: '<mvc:message javaScriptEscape="true" code="target.operator.geq"/>',

      // for text filter
      contains: '<mvc:message javaScriptEscape="true" code="target.operator.contains"/>',
      notContains: '<mvc:message javaScriptEscape="true" code="target.operator.not_contains"/>',
      startsWith: '<mvc:message javaScriptEscape="true" code="operator.begins_with"/>',
      endsWith: '<mvc:message javaScriptEscape="true" code="operator.ends_with"/>',

      // for date filter
      from: '<mvc:message javaScriptEscape="true" code="operator.between"/>',
      till: '<mvc:message javaScriptEscape="true" code="default.and"/>',
    },
    mailing: {
      default: {
        target_group_name: '<mvc:message javaScriptEscape="true" code="statistic.all_subscribers"/>',
        interest_group_name: '<mvc:message javaScriptEscape="true" code="nointerestgroup"/>',
        dyn_tag_name: '<mvc:message javaScriptEscape="true" code="statistic.all_subscribers"/>',
        target_group_deleted: '<mvc:message javaScriptEscape="true" code="target.Deleted"/>',
        item_referencetable_warning: '<mvc:message javaScriptEscape="true" code="warning.mailing.items.refencetable.missing"/>',
        targetmode_and: '<mvc:message javaScriptEscape="true" code="mailing.targetmode.and"/>',
        targetmode_or: '<mvc:message javaScriptEscape="true" code="mailing.targetmode.or"/>',
        sender_and_reply_emails_changed: '<mvc:message javaScriptEscape="true" code="warning.mailing.addresses.changed" />',
        sender_email_changed: '<mvc:message javaScriptEscape="true" code="warning.mailing.sender.changed" />',
        reply_email_changed: '<mvc:message javaScriptEscape="true" code="warning.mailing.reply.changed" />'
      },
      remember: {
        choice: '<mvc:message javaScriptEscape="true" code="remember.choice.mailing"/>'
      },
      validation: {
        target_group_duplicated: '<mvc:message javaScriptEscape="true" code="error.mailing.content.target.duplicated"/>',
        all_recipients_not_last: '<mvc:message javaScriptEscape="true" code="error.mailing.content.target.order"/>'
      }
    },
    wysiwyg: {
      dialogs: {
        agn_tags: {
          tooltip: '<mvc:message javaScriptEscape="true" code="htmled.agntagsButtonTooltip"/>'
        }
      }
    },
    facebook : {
      leadAds: {
        renewedPageAccessTokens : '<mvc:message javaScriptEscape="true" code="facebook.leadAds.renewedPageAccessTokens" arguments="%s"/>',
        error : {
          already_bound_to_other_company : '<mvc:message javaScriptEscape="true" code="facebook.leadAds.error.boundToOtherCompany"/>'
        }
      }
    },
    password : {
      error : {
        too_short 					: '<mvc:message javaScriptEscape="true" code="error.password.tooShort" />',
        too_short_min8				: '<mvc:message javaScriptEscape="true" code="error.password.tooShort.min8" />',
        too_short_min12				: '<mvc:message javaScriptEscape="true" code="error.password.tooShort.min12" />',
        too_short_min32				: '<mvc:message javaScriptEscape="true" code="error.password.tooShort.min32" />',
        no_digits 					: '<mvc:message javaScriptEscape="true" code="error.password_no_digits" />',
        no_lower_case 				: '<mvc:message javaScriptEscape="true" code="error.password_no_lowercase_letters" />',
        no_upper_case 				: '<mvc:message javaScriptEscape="true" code="error.password_no_uppercase_letters" />',
        no_special 					: '<mvc:message javaScriptEscape="true" code="error.password_no_special_chars" />',
        generic_error				: '<mvc:message javaScriptEscape="true" code="error.password.general" />'
      }
    },
    clipboard: {
      copied: {
        tooltip: '<mvc:message javaScriptEscape="true" code="tooltip.copy.clipboard"/>'
      }
    },
    contentSource: {
      error: {
        block_not_set: '<mvc:message javaScriptEscape="true" code="error.mailing.contentblock.empty" />'
      }
    },
    birtreport: {
      deactivateAll: '<mvc:message javaScriptEscape="true" code="report.deactivate.all" />',
      deactivateAllQuestion: '<mvc:message javaScriptEscape="true" code="report.deactivate.question" />'
    },
    dashboard: {
      empty: '<mvc:message javaScriptEscape="true" code="info.dashboard.empty"/>',
      tile: {
        empty: '<mvc:message javaScriptEscape="true" code="dashboard.tile.add"/>',
        'add-ons': '<mvc:message javaScriptEscape="true" code="settings.premium.features"/>',
        'imports-exports': '<mvc:message javaScriptEscape="true" code="dashboard.tile.import.export"/>',
        calendar: '<mvc:message javaScriptEscape="true" code="calendar.Calendar"/>',
        news: '<mvc:message javaScriptEscape="true" code="News"/>',
        mailings: '<mvc:message javaScriptEscape="true" code="Mailings"/>',
        statistics: '<mvc:message javaScriptEscape="true" code="Statistics"/>',
        planning: '<mvc:message javaScriptEscape="true" code="dashboard.tile.planning"/>',
        workflows: '<mvc:message javaScriptEscape="true" code="Workflow"/>'
      }
    }
  };
