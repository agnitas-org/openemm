<%@ page contentType="application/javascript" %>
<%@ taglib prefix="mvc" uri="https://emm.agnitas.de/jsp/jsp/spring"%>

window.I18n = {
    error: {
      workflow: {
        saveActivatedWorkflow: '<mvc:message javaScriptEscape="true" code="error.workflow.SaveActivatedWorkflow"/>',
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
        noImport: '<mvc:message javaScriptEscape="true" code="error.workflow.NoImport"/>',
        noExport: '<mvc:message javaScriptEscape="true" code="error.workflow.NoExport"/>',
        shortName: '<mvc:message javaScriptEscape="true" code="error.name.too.short"/>',
        emptyRecipientList: '<mvc:message javaScriptEscape="true" code="calendar.error.emptyRecipientList"/>',
        autoImportInUse: '<mvc:message javaScriptEscape="true" code="error.workflow.autoImport.used"/>',
        autoExportInUse: '<mvc:message javaScriptEscape="true" code="error.workflow.autoExport.used"/>',
        deadlineIsTooShortForImport: '<mvc:message javaScriptEscape="true" code="error.workflow.autoImport.delay.tooShort" arguments="%s"/>',
        autoOptimizationDecisionForbidden: '<mvc:message javaScriptEscape="true" code="error.workflow.decision.auto.optimisation" />',
        emptyEmail: '<mvc:message javaScriptEscape="true" code="error.email.empty" />',
        wrongEmail: '<mvc:message javaScriptEscape="true" code="error.email.wrong" />',
        notPositiveNumber: '<mvc:message javaScriptEscape="true" code="grid.errors.wrong.int" arguments="%s" />',
        cantAddInvalidEmailToTheList: '<mvc:message javaScriptEscape="true" code="GWUA.warning.email.cantBeAddedToTheList" arguments="%s" />'
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
        exclusiveLockingFailed: '<mvc:message javaScriptEscape="true" code="error.mailing.locked.advanced" arguments="%s"/>',
        smsSymbolsProhibited: '<mvc:message javaScriptEscape="true" code="error.mailing.sms.char.invalid" arguments="%s" />'
      },
      enterEmailAddresses: '<mvc:message javaScriptEscape="true" code="enterEmailAddresses"/>',
      isBlacklisted: '<mvc:message javaScriptEscape="true" code="error.email.blacklisted" arguments="${[null]}"/>',
      grid: {
        noCategorySelected: '<mvc:message javaScriptEscape="true" code="grid.mediapool.image.no.category"/>'
      },
      statistic: {
        period_format: '<mvc:message javaScriptEscape="true" code="error.period.format" />'
      }
    },
    date: {
      weekdaysShort: ['<mvc:message javaScriptEscape="true" code="weekdayShort.sunday" />', '<mvc:message javaScriptEscape="true" code="weekdayShort.monday" />', '<mvc:message javaScriptEscape="true" code="weekdayShort.tuesday" />', '<mvc:message javaScriptEscape="true" code="weekdayShort.wednesday" />', '<mvc:message javaScriptEscape="true" code="weekdayShort.thursday" />', '<mvc:message javaScriptEscape="true" code="weekdayShort.friday" />', '<mvc:message javaScriptEscape="true" code="weekdayShort.saturday" />'],
      monthsFull: ['<mvc:message javaScriptEscape="true" code="calendar.month.1" />', '<mvc:message javaScriptEscape="true" code="calendar.month.2" />', '<mvc:message javaScriptEscape="true" code="calendar.month.3" />', '<mvc:message javaScriptEscape="true" code="calendar.month.4" />', '<mvc:message javaScriptEscape="true" code="calendar.month.5" />', '<mvc:message javaScriptEscape="true" code="calendar.month.6" />', '<mvc:message javaScriptEscape="true" code="calendar.month.7" />', '<mvc:message javaScriptEscape="true" code="calendar.month.8" />', '<mvc:message javaScriptEscape="true" code="calendar.month.9" />', '<mvc:message javaScriptEscape="true" code="calendar.month.10" />', '<mvc:message javaScriptEscape="true" code="calendar.month.11" />', '<mvc:message javaScriptEscape="true" code="calendar.month.12" />'],
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
    defaults: {
      apply: '<mvc:message javaScriptEscape="true" code="button.Apply"/>',
      download: '<mvc:message javaScriptEscape="true" code="button.Download"/>',
      reset: '<mvc:message javaScriptEscape="true" code="button.Reset" />',
      today: '<mvc:message javaScriptEscape="true" code="calendar.today.button" />',
      clear: '<mvc:message javaScriptEscape="true" code="button.Delete" />',
      add: '<mvc:message javaScriptEscape="true" code="button.Add" />',
      Days: '<mvc:message javaScriptEscape="true" code="Days" />',
      entries: '<mvc:message javaScriptEscape="true" code="default.entries" />',
      enlargeEditor: '<mvc:message javaScriptEscape="true" code="editor.enlargeEditor" />',
      warning: '<mvc:message javaScriptEscape="true" code="warning" />',
      success: '<mvc:message javaScriptEscape="true" code="default.Success" />',
      error: '<mvc:message javaScriptEscape="true" code="default.error" />',
      saved: '<mvc:message javaScriptEscape="true" code="default.changes_saved" />',
      yes: '<mvc:message javaScriptEscape="true" code="default.Yes" />',
      no: '<mvc:message javaScriptEscape="true" code="default.No" />',
      info: '<mvc:message javaScriptEscape="true" code="Info" />',
      invalidEmail: '<mvc:message javaScriptEscape="true" code="error.invalid.email" />',
      ok: '<mvc:message javaScriptEscape="true" code="OK" />',
      cancel: '<mvc:message javaScriptEscape="true" code="button.Cancel"/>',
      relogin: '<mvc:message javaScriptEscape="true" code="logout.relogin"/>',
      andMore: '<mvc:message javaScriptEscape="true" code="error.showNumberOfLeft" arguments="%s"/>',
      name: '<mvc:message javaScriptEscape="true" code="default.Name"/>',
      size: '<mvc:message javaScriptEscape="true" code="default.Size"/>',
      changesNotSaved: '<mvc:message javaScriptEscape="true" code="changes_not_saved"/>',
      leaveQuestion: '<mvc:message javaScriptEscape="true" code="grid.layout.leaveQuestion"/>',
      rowsToDisplay: '<mvc:message javaScriptEscape="true" code="default.list.display.rows"/>',
      toggleTruncation: '<mvc:message javaScriptEscape="true" code="view.truncation.toggle"/>',
      value: '<mvc:message javaScriptEscape="true" code="Value"/>',
      table: {
        empty: '<mvc:message javaScriptEscape="true" code="noResultsFound"/>',
        editColumns: '<mvc:message  javaScriptEscape="true" code="list.columns.edit" />',
        saveColumns: '<mvc:message  javaScriptEscape="true" code="list.columns.save" />',
        maxColumnsSelected: '<mvc:message javaScriptEscape="true" code="error.maximum.recipient.columns"/>'
      }
    },
    selects: {
      noMatches: '<mvc:message javaScriptEscape="true" code="default.noMatchesFor" />'
    },
    fields: {
      content: {
        charactersEntered: '<mvc:message javaScriptEscape="true" code="editor.charactersEntered" arguments="%s"/>'
      },
      password: {
        safe: '<mvc:message javaScriptEscape="true" code="secure" />',
        matches: '<mvc:message javaScriptEscape="true" code="matches" />',
        matchesNot: '<mvc:message javaScriptEscape="true" code="matchesNot" />',
        errors: {
          notMatching: '<mvc:message javaScriptEscape="true" code="error.password.mismatch"/>'
        }
      },
      dateTime: {
        errors: {
          incompleteDate: '<mvc:message javaScriptEscape="true" code="error.date.invalid" />'
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
      mailinglist: {
        errors:{
          removed: '<mvc:message javaScriptEscape="true" code="error.mailing.mailinglist.deleted"/>'
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
      }
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
      campaign: '<mvc:message javaScriptEscape="true" code="workflow.single"/>',
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
        copy: '<mvc:message javaScriptEscape="true" code="button.Copy"/>'
      },
      defaults: {
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
        no_mailing: '<mvc:message javaScriptEscape="true" code="NoMailing"/>',
        date: '<mvc:message javaScriptEscape="true" code="Date"/>',
        mailing: '<mvc:message javaScriptEscape="true" code="Mailing"/>',
        ckickrate: '<mvc:message javaScriptEscape="true" code="Clickrate"/>',
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
        edit: '<mvc:message javaScriptEscape="true" code="mailing.MailingEdit"/>',
        archive: '<mvc:message javaScriptEscape="true" code="mailing.archive"/>',
        copyQuestion: '<mvc:message javaScriptEscape="true" code="workflow.mailing.copyQuestion"/>',
        autooptimization: '<mvc:message javaScriptEscape="true" code="mailing.autooptimization"/>',
        action_based: '<mvc:message javaScriptEscape="true" code="mailing.action.based.mailing"/>',
        date_based: '<mvc:message javaScriptEscape="true" code="mailing.Rulebased_Mailing"/>',
        followup: '<mvc:message javaScriptEscape="true" code="mailing.Followup_Mailing"/>',
        "mediatype_sms": '<mvc:message javaScriptEscape="true" code="UserRight.mediatype.sms"/>',
        "mediatype_post": '<mvc:message javaScriptEscape="true" code="UserRight.mediatype.post"/>',
        typeChanged: '<mvc:message javaScriptEscape="true" code="info.workflow.start.mailing.type.change"/>'
      },

      mailinglist: {
        short: '<mvc:message javaScriptEscape="true" code="workflow.mailinglist.short"/>',
        onlyOne: '<mvc:message javaScriptEscape="true" code="workflow.mailing.oneMailinglistWarning"/>'
      },
      target: {
        short: '<mvc:message javaScriptEscape="true" code="workflow.target.short"/>'
      },
      statistic: {
        revenue: '<mvc:message javaScriptEscape="true" code="statistic.revenue"/>'
      },

      activating: {
        auto: '<mvc:message javaScriptEscape="true" code="workflow.pause.reactivation"/>'
      },

      inactivating: {
        title: '<mvc:message javaScriptEscape="true" code="workflow.inactivating.title"/>',
        question: '<mvc:message javaScriptEscape="true" code="workflow.inactivating.question"/>'
      },

      connect: '<mvc:message javaScriptEscape="true" code="workflow.connect"/>',
      disconnect: '<mvc:message javaScriptEscape="true" code="workflow.disconnect"/>',
      opening_rate: '<mvc:message javaScriptEscape="true" code="workflow.decision.OpeningRate"/>',
      decision: '<mvc:message javaScriptEscape="true" code="workflow.decision"/>',
      recipient: '<mvc:message javaScriptEscape="true" code="Recipient"/>',
      parameter: '<mvc:message javaScriptEscape="true" code="workflow.parameter"/>',
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
      },
      dialog: {
        connectionNotAllowedTitle: '<mvc:message javaScriptEscape="true" code="error.workflow.connection.deactivated"/>',
        connectionNotAllowedMessage: '<mvc:message javaScriptEscape="true" code="error.workflow.connection.notAllowed"/>'    
      }
    },
    schedule: {
      weekdays: {
        0: '<mvc:message javaScriptEscape="true" code="default.every.day"/>',
        1: '<mvc:message javaScriptEscape="true" code="calendar.dayOfWeek.1"/>',
        2: '<mvc:message javaScriptEscape="true" code="calendar.dayOfWeek.2"/>',
        3: '<mvc:message javaScriptEscape="true" code="calendar.dayOfWeek.3"/>',
        4: '<mvc:message javaScriptEscape="true" code="calendar.dayOfWeek.4"/>',
        5: '<mvc:message javaScriptEscape="true" code="calendar.dayOfWeek.5"/>',
        6: '<mvc:message javaScriptEscape="true" code="calendar.dayOfWeek.6"/>',
        7: '<mvc:message javaScriptEscape="true" code="calendar.dayOfWeek.7"/>'
      },
      intervals: {
        1: '<mvc:message javaScriptEscape="true" code="default.every.hour"/>',
        2: '<mvc:message javaScriptEscape="true" code="default.every.hour.2"/>',
        3: '<mvc:message javaScriptEscape="true" code="default.every.hour.3"/>',
        4: '<mvc:message javaScriptEscape="true" code="default.every.hour.4"/>',
        6: '<mvc:message javaScriptEscape="true" code="default.every.hour.6"/>',
        12: '<mvc:message javaScriptEscape="true" code="default.every.hour.12"/>'
      },
      everyXMonth: {
        1:  '<mvc:message javaScriptEscape="true" code="default.every1Month" />',
        2:  '<mvc:message javaScriptEscape="true" code="default.every2Month" />',
        3:  '<mvc:message javaScriptEscape="true" code="default.every3Month" />',
        4:  '<mvc:message javaScriptEscape="true" code="default.every4Month" />',
        5:  '<mvc:message javaScriptEscape="true" code="default.every5Month" />',
        6:  '<mvc:message javaScriptEscape="true" code="default.every6Month" />',
        7:  '<mvc:message javaScriptEscape="true" code="default.every7Month" />',
        8:  '<mvc:message javaScriptEscape="true" code="default.every8Month" />',
        9:  '<mvc:message javaScriptEscape="true" code="default.every9Month" />',
        10: '<mvc:message javaScriptEscape="true" code="default.every10Month" />',
        11: '<mvc:message javaScriptEscape="true" code="default.every11Month" />',
        12: '<mvc:message javaScriptEscape="true" code="default.every12Month" />'
      },
      weekDayOrdinal: {
        1: '<mvc:message code="mailing.interval.weekdayOrdinal.1"/>',
        2: '<mvc:message code="mailing.interval.weekdayOrdinal.2"/>',
        3: '<mvc:message code="mailing.interval.weekdayOrdinal.3"/>',
        4: '<mvc:message code="mailing.interval.weekdayOrdinal.4"/>',
        5: '<mvc:message code="mailing.interval.weekdayOrdinal.5"/>'
      },
      defineTime: '<mvc:message javaScriptEscape="true" code="default.time.define" />',
      interval: '<mvc:message javaScriptEscape="true" code="default.interval" />',
      interval_type: {
        weekdays: '<mvc:message javaScriptEscape="true" code="report.autosend.days" />',
        monthly: '<mvc:message javaScriptEscape="true" code="Interval.monthly" />',
        bi_weekly: '<mvc:message javaScriptEscape="true" code="Interval.2weekly" />',
        weekly: '<mvc:message javaScriptEscape="true" code="Interval.weekly" />'
      },
      daysAndTime: '<mvc:message javaScriptEscape="true" code="GWUA.schedule.daysAndTime" />',
      monthAndTime: '<mvc:message javaScriptEscape="true" code="GWUA.schedule.monthAndTime" />',
      firstDayOfMonth: '<mvc:message javaScriptEscape="true" code="firstDayOfMonth" />',
      lastDayOfMonth: '<mvc:message javaScriptEscape="true" code="ultimoDayOfMonth" />'
    },
    logon: {
      info: {
        multiple_tabs:'<mvc:message javaScriptEscape="true" code="logon.tabs.multiple"/>'
      },
      session: {
        notification: '<mvc:message javaScriptEscape="true" code="warning.session.expired"/>',
        expired:'<mvc:message javaScriptEscape="true" code="session.timer.expired"/>'
      }
    },
    recipient: {
      duplicate: {
        question: '<mvc:message javaScriptEscape="true" code="recipient.duplicate.question"/>'
      },
      blacklisted: {
        question: '<mvc:message javaScriptEscape="true" code="recipient.blacklisted.question"/>'
      },
      hide: {
        question: '<mvc:message javaScriptEscape="true" code="recipient.hide.question"/>'
      }
    },
    calendar: {
      error: {
        empty_comment: '<mvc:message javaScriptEscape="true" code="calendar.error.emptyComment"/>',
        long_comment: '<mvc:message javaScriptEscape="true" code="calendar.error.longComment"/>',
        empty_recipient_list: '<mvc:message javaScriptEscape="true" code="calendar.error.emptyRecipientList"/>',
        long_recipient_list: '<mvc:message javaScriptEscape="true" code="calendar.error.longRecipientList"/>',
        reminderInPast: '<mvc:message javaScriptEscape="true" code="error.workflow.reminderDateInPast"/>',
        invalid_email: '<mvc:message javaScriptEscape="true" code="calendar.error.invalidEmail"/>'
      },
      common: {
        new_comment: '<mvc:message javaScriptEscape="true" code="calendar.NewComment"/>',
        edit_comment: '<mvc:message javaScriptEscape="true" code="calendar.EditComment"/>',
        weekNumber: '<mvc:message javaScriptEscape="true" code="calendar.WeekNumber"/>'
      }
    },
    userform: {
      error: {
        velocity_not_allowed: '<mvc:message javaScriptEscape="true" code="userform.velocityNotAllowed" />',
        invalid_name: '<mvc:message javaScriptEscape="true" code="error.form.invalid_name" />',
        illegal_directive: '<mvc:message javaScriptEscape="true" code="error.form.illegal_directive" arguments="%s" />',
        invalid_successUrl: '<mvc:message javaScriptEscape="true" code="error.form.invalid_successUrl" arguments="%s" />',
        invalid_link: '<mvc:message javaScriptEscape="true" code="error.invalid_link" arguments="${[\"%s\", \"%s\"]}" />',
        invalid_successHtml: '<mvc:message javaScriptEscape="true" code="error.userform.success.html.missing" />',
        invalid_successUrl: '<mvc:message javaScriptEscape="true" code="error.userform.success.url.missing" />',
        invalid_errorHtml: '<mvc:message javaScriptEscape="true" code="error.userform.error.html.missing" />',
        invalid_errorUrl: '<mvc:message javaScriptEscape="true" code="error.userform.error.url.missing" />'
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
        SendLastNewsletter: '<mvc:message javaScriptEscape="true" code="action.op.SendLastNewsletter" />',
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
          type: '<mvc:message javaScriptEscape="true" code="error.import.dataType" arguments="%s"/>',
          length: '<mvc:message javaScriptEscape="true" code="error.import.dataLength" arguments="%s"/>',
          dateFormat: '<mvc:message javaScriptEscape="true" code="error.profiledb.invalidDefaultValue" arguments="%s"/>',
          invalidEmail: '<mvc:message javaScriptEscape="true" code="error.email.invalid"/>'
        }
      }
    },
    export: {
      columnMapping: {
        error: {
          invalidColName: '<mvc:message javaScriptEscape="true" code="error.invalidSimpleName"/>',
          nameEmpty: '<mvc:message javaScriptEscape="true" code="error.export.column.empty"/>',
          nameTooShort: '<mvc:message javaScriptEscape="true" code="error.name.too.short"/>',
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
      till: '<mvc:message javaScriptEscape="true" code="default.and"/>'
    },
    mailing: {
      default: {
        editWithCampaign: '<mvc:message javaScriptEscape="true" code="mailing.EditWithCampaignManager"/>',
        target_group_name: '<mvc:message javaScriptEscape="true" code="statistic.all_subscribers"/>',
        interest_group_name: '<mvc:message javaScriptEscape="true" code="nointerestgroup"/>',
        targetmode_and: '<mvc:message javaScriptEscape="true" code="mailing.targetmode.and"/>',
        targetmode_or: '<mvc:message javaScriptEscape="true" code="mailing.targetmode.or"/>',
        sender_and_reply_emails_changed: '<mvc:message javaScriptEscape="true" code="warning.mailing.addresses.changed" />',
        sender_email_changed: '<mvc:message javaScriptEscape="true" code="warning.mailing.sender.changed" />',
        reply_email_changed: '<mvc:message javaScriptEscape="true" code="warning.mailing.reply.changed" />',
        grid: '<mvc:message javaScriptEscape="true" code="mailing.grid.GridMailing" />'
      },
      validation: {
        target_group_duplicated: '<mvc:message javaScriptEscape="true" code="error.mailing.content.target.duplicated"/>',
        all_recipients_not_last: '<mvc:message javaScriptEscape="true" code="error.mailing.content.target.order"/>'
      },
      status: {
        active: '<mvc:message javaScriptEscape="true" code="mailing.status.active" />',
        admin: '<mvc:message javaScriptEscape="true" code="mailing.status.admin" />',
        canceled: '<mvc:message javaScriptEscape="true" code="mailing.status.canceled" />',
        canceledAndCopied: '<mvc:message javaScriptEscape="true" code="mailing.status.canceledAndCopied" />',
        disable: '<mvc:message javaScriptEscape="true" code="mailing.status.disable" />',
        edit: '<mvc:message javaScriptEscape="true" code="mailing.status.edit" />',
        'generation-finished': '<mvc:message javaScriptEscape="true" code="mailing.status.generation-finished" />',
        inGeneration: '<mvc:message javaScriptEscape="true" code="mailing.status.in-generation" />',
        'insufficient-vouchers': '<mvc:message javaScriptEscape="true" code="mailing.status.insufficient-vouchers" />',
        new: '<mvc:message javaScriptEscape="true" code="mailing.status.new" />',
        norecipients: '<mvc:message javaScriptEscape="true" code="mailing.status.norecipients" />',
        ready: '<mvc:message javaScriptEscape="true" code="mailing.status.ready" />',
        scheduled: '<mvc:message javaScriptEscape="true" code="mailing.status.scheduled" />',
        sending: '<mvc:message javaScriptEscape="true" code="mailing.status.sending" />',
        sent: '<mvc:message javaScriptEscape="true" code="mailing.status.sent" />',
        test: '<mvc:message javaScriptEscape="true" code="mailing.status.test" />'
      },
      mediatype: {
        email: '<mvc:message javaScriptEscape="true" code="mailing.MediaType.email" />',
        post: '<mvc:message javaScriptEscape="true" code="mailing.MediaType.post" />',
        sms: '<mvc:message javaScriptEscape="true" code="mailing.MediaType.sms" />'
      }
    },
    wysiwyg: {
      dialogs: {
        agn_tags: {
          tooltip: '<mvc:message javaScriptEscape="true" code="htmled.agntagsButtonTooltip"/>'
        },
        emoji: {
          groups: {
            people: '<mvc:message javaScriptEscape="true" code="editor.wysiwyg.emoji.people"/>',
            nature: '<mvc:message javaScriptEscape="true" code="editor.wysiwyg.emoji.nature"/>',
            food: '<mvc:message javaScriptEscape="true" code="editor.wysiwyg.emoji.food"/>',
            travel: '<mvc:message javaScriptEscape="true" code="editor.wysiwyg.emoji.travel"/>',
            activities: '<mvc:message javaScriptEscape="true" code="editor.wysiwyg.emoji.activities"/>',
            objects: '<mvc:message javaScriptEscape="true" code="editor.wysiwyg.emoji.objects"/>',
            symbols: '<mvc:message javaScriptEscape="true" code="editor.wysiwyg.emoji.symbols"/>',
            flags: '<mvc:message javaScriptEscape="true" code="editor.wysiwyg.emoji.flags"/>'
          }
        },
        link: {
          type: '<mvc:message javaScriptEscape="true" code="GWUA.jodit.linkType"/>',
          email_type: '<mvc:message javaScriptEscape="true" code="mailing.MediaType.0"/>',
          phone_type: '<mvc:message javaScriptEscape="true" code="upload.view.phone"/>',
          phone_number: '<mvc:message javaScriptEscape="true" code="GWUA.jodit.phoneNumber"/>',
          email_address: '<mvc:message javaScriptEscape="true" code="settings.Admin.email"/>',
          subject: '<mvc:message javaScriptEscape="true" code="mailing.Subject"/>',
          content: '<mvc:message javaScriptEscape="true" code="default.Content"/>'
        }
      },
      emoji: '<mvc:message javaScriptEscape="true" code="editor.wysiwyg.emoji" />',
      wrapTextInP: '<mvc:message javaScriptEscape="true" code="GWUA.editor.wysiwyg.paragraph.wrapText" />'
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
      mailingContentSelect: '<mvc:message javaScriptEscape="true" code="mailing.content.select" />'
    },
    login: {
      error: {
        capslock: '<mvc:message javaScriptEscape="true" code="error.login.capslock" />'
      }
    },
    dashboard: {
      empty: '<mvc:message javaScriptEscape="true" code="info.dashboard.empty"/>',
      tile: {
        empty: '<mvc:message javaScriptEscape="true" code="dashboard.tile.add"/>',
        'add-ons': '<mvc:message javaScriptEscape="true" code="settings.premium.features"/>',
        'imports-exports': '<mvc:message javaScriptEscape="true" code="dashboard.tile.import.export"/>',
        calendar: '<mvc:message javaScriptEscape="true" code="calendar.small"/>',
        'week-calendar': '<mvc:message javaScriptEscape="true" code="calendar.week"/>',
        news: '<mvc:message javaScriptEscape="true" code="News"/>',
        mailings: '<mvc:message javaScriptEscape="true" code="Mailings"/>',
        statistics: '<mvc:message javaScriptEscape="true" code="Statistics"/>',
        planning: '<mvc:message javaScriptEscape="true" code="dashboard.tile.planning"/>',
        workflows: '<mvc:message javaScriptEscape="true" code="Workflow"/>',
        clickers: '<mvc:message javaScriptEscape="true" code="statistic.clicker"/>',
        analysis: '<mvc:message javaScriptEscape="true" code="default.Analysis"/>',
        openers: '<mvc:message javaScriptEscape="true" code="statistic.opener"/>'
      }
    },
    editableView: {
      edit: '<mvc:message javaScriptEscape="true" code="default.view.edit" />',
      save: '<mvc:message javaScriptEscape="true" code="default.view.save" />',
      saved: '<mvc:message javaScriptEscape="true" code="default.saved.view"/>',
      tile: {
        state: {
          visible: '<mvc:message javaScriptEscape="true" code="default.tile.hide" />',
          hidden: '<mvc:message javaScriptEscape="true" code="default.tile.show" />',
          main: '<mvc:message javaScriptEscape="true" code="default.tile.main" />'
        },
        error: {
          cantRemove: '<mvc:message javaScriptEscape="true" code="error.tile.remove" />'
        }
      }
    },
    mediapool: {
      imageGeneration: {
        limitExceeded: '<mvc:message javaScriptEscape="true" code="image.ai.quota.exceeded"/>',
        remainingGenerations: '<mvc:message javaScriptEscape="true" code="image.ai.quota" arguments="%s"/>'
      }
    },
    ai: {
      regenerateText: '<mvc:message javaScriptEscape="true" code="button.Regenerate" />'
    },
    url: {
        invalid: '<mvc:message javaScriptEscape="true" code="error.linkUrlWrong"/>'
    },

    split: {
      mailing: '<mvc:message javaScriptEscape="true" code="mailing.listsplit"/>',
      deleteWorkflowParameterIconWarn: '<mvc:message javaScriptEscape="true" code="warning.workflow.mailing.split.delete"/>',
      ratio: {
        "050505050575": '<mvc:message javaScriptEscape="true" code="listsplit.050505050575"/>',
        "0505050580": '<mvc:message javaScriptEscape="true" code="listsplit.0505050580"/>',
        "05050585": '<mvc:message javaScriptEscape="true" code="listsplit.05050585"/>',
        "050590": '<mvc:message javaScriptEscape="true" code="listsplit.050590"/>',
        "101010101050": '<mvc:message javaScriptEscape="true" code="listsplit.101010101050"/>',
        "1010101060": '<mvc:message javaScriptEscape="true" code="listsplit.1010101060"/>',
        "10101070": '<mvc:message javaScriptEscape="true" code="listsplit.10101070"/>',
        "101080": '<mvc:message javaScriptEscape="true" code="listsplit.101080"/>',
        "1090": '<mvc:message javaScriptEscape="true" code="listsplit.1090"/>',
        "151570": '<mvc:message javaScriptEscape="true" code="listsplit.151570"/>',
        "2080": '<mvc:message javaScriptEscape="true" code="listsplit.2080"/>',
        "25252525": '<mvc:message javaScriptEscape="true" code="listsplit.25252525"/>',
        "252550": '<mvc:message javaScriptEscape="true" code="listsplit.252550"/>',
        "3070": '<mvc:message javaScriptEscape="true" code="listsplit.3070"/>',
        "333333": '<mvc:message javaScriptEscape="true" code="listsplit.333333"/>',
        "4060": '<mvc:message javaScriptEscape="true" code="listsplit.4060"/>',
        "5050": '<mvc:message javaScriptEscape="true" code="listsplit.5050"/>'
      }
    },

    statistic: {
      benchmark: {
        openrate: '<mvc:message javaScriptEscape="true" code="benchmark.openrate"/>',
        clickrate: '<mvc:message javaScriptEscape="true" code="benchmark.clickrate"/>',
        optoutrate: '<mvc:message javaScriptEscape="true" code="benchmark.optoutrate"/>',
        bouncerate: '<mvc:message javaScriptEscape="true" code="benchmark.bouncerate"/>'
      },
      waitProcessing: '<mvc:message javaScriptEscape="true" code="statistic.viewer.progressbar.prompt"/>',
      cancelProcessing: '<mvc:message javaScriptEscape="true" code="button.Cancel"/>'
    },

    status: {
      push: '<mvc:message javaScriptEscape="true" code="PushNotification" />'
    },

    inboxPreview: {
      canceled: '<mvc:message javaScriptEscape="true" code="info.predelivery.canceled"/>'
    }
};
