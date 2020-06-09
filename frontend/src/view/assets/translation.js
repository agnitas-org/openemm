/* <%@ page contentType="application/javascript" %> */
/* <%@ taglib uri="https://emm.agnitas.de/jsp/jstl/tags" prefix="agn" %> */

/* The parameter translation.js?lang=de or translation.js?lang=en is not evaluated, but prevents the browser caching of the file while the admin changed his language*/

window.I18n = {
    error: {
        workflow: {
            saveActivatedWorkflow: '<agn:agnMessage key="error.workflow.SaveActivatedWorkflow"/>',
            editActivatedWorkflow: '<agn:agnMessage key="error.workflow.editActivatedWorkflow"/>',
            editPartActivatedWorkflow: '<agn:agnMessage key="error.workflow.editPartOfActivatedWorkflow"/>',
            editUsingActivatedWorkflow: '<agn:agnMessage key="error.workflow.editUsingActivatedWorkflow"/>',
            wrongMaxRecipientsFormat: '<agn:agnMessage key="error.workflow.wrongMaxRecipientsFormat"/>',
            maxRecipientsTooBig: '<agn:agnMessage key="error.workflow.maxRecipientsTooBig"/>',
            maxRecipientsLessThanZero: '<agn:agnMessage key="error.workflow.maxRecipientsLessThanZero"/>',
            noLinkSelected: '<agn:agnMessage key="error.workflow.noLinkSelected"/>',
            noValidThreshold: '<agn:agnMessage key="error.workflow.NoValidThreshold"/>',
            autoImportPermission: '<agn:agnMessage key="error.workflow.autoImportPermission"/>',
            autoExportPermission: '<agn:agnMessage key="error.workflow.autoExportPermission"/>',
            followupPermission: '<agn:agnMessage key="error.workflow.followupPermission"/>',
            startDateOmitted: '<agn:agnMessage key="error.workflow.StartDateValueOmitted"/>',
            notAddedMailingList: '<agn:agnMessage key="error.workflow.notAddedMailingList"/>',
            noMailing: '<agn:agnMessage key="error.workflow.noMailing"/>',
            noForm: '<agn:agnMessage key="error.workflow.NoForm"/>',
            noImport: '<agn:agnMessage key="error.workflow.NoImport"/>',
            noExport: '<agn:agnMessage key="error.workflow.NoExport"/>',
            shortName: '<agn:agnMessage key="error.name.too.short"/>',
            emptyRecipientList: '<agn:agnMessage key="calendar.error.emptyRecipientList"/>',
            autoImportInUse: '<agn:agnMessage key="error.workflow.autoImport.used"/>',
            autoExportInUse: '<agn:agnMessage key="error.workflow.autoExport.used"/>',
            deadlineIsTooShortForImport: '<agn:agnMessage key="error.workflow.autoImport.delay.tooShort" arg0="%s"/>'
        },
        delay: {
            60:'<agn:agnMessage key="error.delay.60"/>'
        },
        upload: {
            email: '<agn:agnMessage key="error.upload.email"/>',
            file: '<agn:agnMessage key="error.upload.file"/>'
        },
        mailing: {
            invalidPriorityCount: '<agn:agnMessage key="mailing.priority.maxMails"/>',
            exclusiveLockingFailed: '<agn:agnMessage key="error.mailing.locked"/>'
        },
        enterEmailAddresses: '<agn:agnMessage key="enterEmailAddresses"/>',
        inUse: '<agn:agnMessage key="error.email.duplicated"/>',
        content: {
            empty: '<agn:agnMessage key="error.content.empty"/>'
        },
        isBlacklisted: '<agn:agnMessage key="error.email.blacklisted"/>',
        grid: {
            noCategorySelected: '<agn:agnMessage key="grid.mediapool.image.no.category"/>'
        }
    },
    date: {
        firstDayOfWeek: 1,
        format: '<agn:agnMessage key="datePicker.dateFormat" />',
        weekdaysShort: ['<agn:agnMessage key="weekdayShort.sunday" />', '<agn:agnMessage key="weekdayShort.monday" />', '<agn:agnMessage key="weekdayShort.tuesday" />', '<agn:agnMessage key="weekdayShort.wednesday" />', '<agn:agnMessage key="weekdayShort.thursday" />', '<agn:agnMessage key="weekdayShort.friday" />', '<agn:agnMessage key="weekdayShort.saturday" />'],
        weekdaysFull: ['<agn:agnMessage key="calendar.dayOfWeek.1" />', '<agn:agnMessage key="calendar.dayOfWeek.2" />', '<agn:agnMessage key="calendar.dayOfWeek.3" />', '<agn:agnMessage key="calendar.dayOfWeek.4" />', '<agn:agnMessage key="calendar.dayOfWeek.5" />', '<agn:agnMessage key="calendar.dayOfWeek.6" />', '<agn:agnMessage key="calendar.dayOfWeek.7" />'],
        monthsShort: ['<agn:agnMessage key="monthShort.january" />', '<agn:agnMessage key="monthShort.february" />', '<agn:agnMessage key="monthShort.march" />', '<agn:agnMessage key="monthShort.april" />', '<agn:agnMessage key="monthShort.may" />', '<agn:agnMessage key="monthShort.june" />', '<agn:agnMessage key="monthShort.july" />', '<agn:agnMessage key="monthShort.august" />', '<agn:agnMessage key="monthShort.september" />', '<agn:agnMessage key="monthShort.october" />', '<agn:agnMessage key="monthShort.november" />', '<agn:agnMessage key="monthShort.december" />'],
        monthsFull: ['<agn:agnMessage key="calendar.month.1" />', '<agn:agnMessage key="calendar.month.2" />', '<agn:agnMessage key="calendar.month.3" />', '<agn:agnMessage key="calendar.month.4" />', '<agn:agnMessage key="calendar.month.5" />', '<agn:agnMessage key="calendar.month.6" />', '<agn:agnMessage key="calendar.month.7" />', '<agn:agnMessage key="calendar.month.8" />', '<agn:agnMessage key="calendar.month.9" />', '<agn:agnMessage key="calendar.month.10" />', '<agn:agnMessage key="calendar.month.11" />', '<agn:agnMessage key="calendar.month.12" />'],
        nextMonth: '<agn:agnMessage key="nextMonth" />',
        prevMonth: '<agn:agnMessage key="prevMonth" />',
        selectMonth: '<agn:agnMessage key="selectMonth" />',
        selectYear: '<agn:agnMessage key="selectYear" />',
        formats: {
            label: '<agn:agnMessage key="import.dateFormat"/>',
            'DD.MM.YYYY' : '<agn:agnMessage key="date.format.DD.MM.YYYY"/>',
            YYYYMMDD: '<agn:agnMessage key="default.date.format.YYYYMMDD"/>',
            DDMM: '<agn:agnMessage key="date.format.DDMM"/>',
            DDMMYYYY: '<agn:agnMessage key="date.format.DDMMYYYY"/>',
            MMDD: '<agn:agnMessage key="date.format.MMDD"/>',
            DD: '<agn:agnMessage key="date.format.DD"/>',
            MM: '<agn:agnMessage key="date.format.MM"/>',
            YYYY: '<agn:agnMessage key="default.date.format.YYYY"/>'
        }
    },
    time: {
        format: 'HH:i',
        min: '<agn:agnMessage key="default.time.min"/>',
        interval: '<agn:agnMessage key="default.interval"/>',
        60: '<agn:agnMessage key="default.minutes.60"/>'
    },
    defaults: {
        delete: '<agn:agnMessage key="Delete" />',
        today: '<agn:agnMessage key="calendar.today.button" />',
        days: '<agn:agnMessage key="days" />',
        close: '<agn:agnMessage key="close" />',
        warning: '<agn:agnMessage key="warning" />',
        success: '<agn:agnMessage key="default.Success" />',
        error: '<agn:agnMessage key="Error" />',
        saved: '<agn:agnMessage key="default.changes_saved" />',
        yes: '<agn:agnMessage key="default.Yes" />',
        no: '<agn:agnMessage key="default.No" />',
        info: '<agn:agnMessage key="Info" />',
        ok: '<agn:agnMessage key="OK" />',
        cancel: '<agn:agnMessage key="button.Cancel"/>',
        logout: '<agn:agnMessage key="default.Logout"/>',
        relogin: '<agn:agnMessage key="logout.relogin"/>',
        remember: {
            choice: '<agn:agnMessage key="remember.choice"/>'
        }
    },
    selects: {
        noMatches: '<agn:agnMessage key="default.noMatchesFor" />',
        matches: '<agn:agnMessage key="default.matches" />',
        matchesSing: '<agn:agnMessage key="default.matchesSing" />',
        searching: '<agn:agnMessage key="default.searching" />',
        loadMore: '<agn:agnMessage key="default.loadingResults" />',
        errors: {
            ajax: '<agn:agnMessage key="errors.ajax" />',
            inputTooShort: '<agn:agnMessage key="errors.inputTooShort" />',
            inputTooLong: '<agn:agnMessage key="errors.inputTooLong" />',
            selectionTooBig: '<agn:agnMessage key="errors.selectionTooBig" />'
        }
    },
    fields: {
        content: {
            charactersEntered: '<agn:agnMessage key="editor.charactersEntered" arg0="%s"/>'
        },
        password: {
            safe: '<agn:agnMessage key="secure" />',
            unsafe: '<agn:agnMessage key="insecure" />',
            matches: '<agn:agnMessage key="matches" />',
            matchesNot: '<agn:agnMessage key="matchesNot" />',
            successHtml: '<i class="icon icon-state-success"></i> <span class="text">%s</span>',
            unsuccessHtml: '<i class="icon icon-state-alert"></i> <span class="text">%s</span>',
            errors: {
                notMatching: '<agn:agnMessage key="error.password.mismatch"/>',
                unsafe: '<agn:agnMessage key="insecure" />'
            }
        },
        required: {
            errors: {
                missing: '<agn:agnMessage key="error.default.required"/>'
            }
        },
        name: {
            errors: {
                name_in_use: '<agn:agnMessage key="error.name_in_use"/>'
            }
        },
        errors: {
            contentLengthExceedsLimit: '<agn:agnMessage key="error.contentLengthExceedsLimit" arg0="%s"/>',
            number_nan: '<agn:agnMessage key="querybuilder.error.number_nan"/>',
            number_not_integer: '<agn:agnMessage key="querybuilder.error.number_not_integer"/>',
            number_not_double: '<agn:agnMessage key="querybuilder.error.number_not_double"/>',
            number_exceed_min: '<agn:agnMessage key="querybuilder.error.number_exceed_min" arg0="%s"/>',
            number_exceed_max: '<agn:agnMessage key="querybuilder.error.number_exceed_max" arg0="%s"/>',
            phone_number_invalid: '<agn:agnMessage key="error.phoneNumber.invalidFormat"/>',
            string_exceed_min_length: '<agn:agnMessage key="querybuilder.error.string_exceed_min_length" arg0="%s"/>',
            string_exceed_max_length: '<agn:agnMessage key="querybuilder.error.string_exceed_max_length" arg0="%s"/>'
        },
        mailing: {
            parameter: '<agn:agnMessage key="MailingParameter"/>',
            description: '<agn:agnMessage key="Description"/>',
            for_mailing: '<agn:agnMessage key="mailing.MailingParameter.forMailing"/>',
            change_date: '<agn:agnMessage key="default.changeDate"/>'
        }
    },
    messages: {
        error: {
            headline: '<agn:agnMessage key="error.global.headline"/>',
            text: '<agn:agnMessage key="error.default.message"/>',
            reload: '<agn:agnMessage key="error.reload"/>',
            nothing_selected: '<agn:agnMessage key="error.default.nothing_selected"/>'
        },
        permission: {
            denied : {
                title: '<agn:agnMessage key="permission.denied.title"/>',
                text: '<agn:agnMessage key="permission.denied.message"/>'
            }
        }
    },
    autosave: {
        confirm: {
            title: '<agn:agnMessage key="restore.confirm.title"/>',
            question: '<agn:agnMessage key="restore.confirm.question" arg0="%s"/>'
        },
        success: {
            title: '<agn:agnMessage key="restore.confirm.success.title"/>',
            message: '<agn:agnMessage key="restore.confirm.success"/>'
        },
        discard: '<agn:agnMessage key="default.No"/>',
        restore: '<agn:agnMessage key="default.Yes"/>'
    },
    querybuilder: {
        common: {
            add_rule: '<agn:agnMessage key="querybuilder.add_rule"/>',
            add_group: '<agn:agnMessage key="querybuilder.add_group"/>',
            delete_rule: '<agn:agnMessage key="Delete"/>',
            delete_group: '<agn:agnMessage key="Delete"/>',
            invert: '<agn:agnMessage key="querybuilder.invent"/>',
            anyLink: '<agn:agnMessage key="target.link.any" />'
        },
        conditions: {
            AND: '<agn:agnMessage key="condition.and"/>',
            OR: '<agn:agnMessage key="condition.or"/>'
        },
        operators: {
            equal: '<agn:agnMessage key="operator.equal"/>',
            not_equal: '<agn:agnMessage key="operator.not_equal"/>',
            less: '<agn:agnMessage key="operator.less"/>',
            less_or_equal: '<agn:agnMessage key="operator.less_or_equal"/>',
            greater: '<agn:agnMessage key="operator.greater"/>',
            greater_or_equal: '<agn:agnMessage key="operator.greater_or_equal"/>',
            in: '<agn:agnMessage key="mailing.searchIn"/>',
            not_in: '<agn:agnMessage key="operator.notin"/>',
            between: '<agn:agnMessage key="operator.between"/>',
            not_between: '<agn:agnMessage key="operator.not_between"/>',
            begins_with: '<agn:agnMessage key="operator.begins_with"/>',
            not_begins_with: '<agn:agnMessage key="operator.not_begins_with"/>',
            contains: '<agn:agnMessage key="target.operator.contains"/>',
            not_contains: '<agn:agnMessage key="target.operator.not_contains"/>',
            ends_with: '<agn:agnMessage key="operator.ends_with"/>',
            not_ends_with: '<agn:agnMessage key="operator.not_ends_with"/>',
            is_empty: '<agn:agnMessage key="operator.is_empty"/>',
            is_not_empty: '<agn:agnMessage key="operator.is_not_empty"/>',
            is_null: '<agn:agnMessage key="operator.is_null"/>',
            is_not_null: '<agn:agnMessage key="operator.is_not_null"/>',
            before: '<agn:agnMessage key="target.operator.before"/>',
            after: '<agn:agnMessage key="target.operator.after"/>'
        },
        errors: {
            general: '<agn:agnMessage key="error.target.saving"/>',
            no_filter: '<agn:agnMessage key="querybuilder.error.no_filter"/>',
            empty_group: '<agn:agnMessage key="querybuilder.error.empty_group"/>',
            radio_empty: '<agn:agnMessage key="querybuilder.error.select_empty"/>',
            checkbox_empty: '<agn:agnMessage key="querybuilder.error.select_empty"/>',
            select_empty: '<agn:agnMessage key="querybuilder.error.select_empty"/>',
            string_empty: '<agn:agnMessage key="querybuilder.error.string_empty"/>',
            string_exceed_min_length: '<agn:agnMessage key="querybuilder.error.string_exceed_min_length"/>',
            string_exceed_max_length: '<agn:agnMessage key="querybuilder.error.string_exceed_max_length"/>',
            string_invalid_format: '<agn:agnMessage key="querybuilder.error.string_invalid_format"/>',
            number_nan: '<agn:agnMessage key="querybuilder.error.number_nan"/>',
            number_not_integer: '<agn:agnMessage key="querybuilder.error.number_not_integer"/>',
            number_not_double: '<agn:agnMessage key="querybuilder.error.number_not_double"/>',
            number_exceed_min: '<agn:agnMessage key="querybuilder.error.number_exceed_min"/>',
            number_exceed_max: '<agn:agnMessage key="querybuilder.error.number_exceed_max"/>',
            number_wrong_step: '<agn:agnMessage key="querybuilder.error.number_wrong_step"/>',
            datetime_empty: '<agn:agnMessage key="querybuilder.error.datetime_empty"/>',
            datetime_invalid: '<agn:agnMessage key="querybuilder.error.datetime_invalid"/>',
            datetime_exceed_min: '<agn:agnMessage key="querybuilder.error.datetime_exceed_min"/>',
            datetime_exceed_max: '<agn:agnMessage key="querybuilder.error.datetime_exceed_max"/>',
            boolean_not_valid: '<agn:agnMessage key="querybuilder.error.boolean_not_valid"/>',
            operator_not_multiple: '<agn:agnMessage key="querybuilder.error.operator_not_multiple"/>'
        }
    },
    workflow: {
        reaction: {
            title: '<agn:agnMessage key="workflow.Reaction"/>',
            opened: '<agn:agnMessage key="statistic.opened"/>',
            not_opened: '<agn:agnMessage key="workflow.reaction.NotOpened" />',
            clicked: '<agn:agnMessage key="default.clicked"/>',
            not_clicked: '<agn:agnMessage key="workflow.reaction.NotClicked"/>',
            bought: '<agn:agnMessage key="workflow.reaction.Bought"/>',
            not_bought: '<agn:agnMessage key="workflow.reaction.NotBought"/>',
            download: '<agn:agnMessage key="button.Download"/>',
            change_of_profile: '<agn:agnMessage key="workflow.reaction.ChangeOfProfile"/>',
            waiting_for_confirm: '<agn:agnMessage key="workflow.reaction.WaitingForConfirm"/>',
            opt_in: '<agn:agnMessage key="workflow.reaction.OptIn"/>',
            opt_out: '<agn:agnMessage key="workflow.reaction.OptOut"/>',
            clicked_on_link: '<agn:agnMessage key="workflow.reaction.ClickedOnLink"/>',
            opened_and_clicked: '<agn:agnMessage key="workflow.reaction.OpenedAndClicked"/>',
            opened_or_clicked: '<agn:agnMessage key="workflow.reaction.OpenedOrClicked"/>',
            confirmed_opt_in: '<agn:agnMessage key="workflow.reaction.ConfirmedOptIn"/>'
        },
        start: {
            title: '<agn:agnMessage key="workflow.start.title"/>',
            profile_field: '<agn:agnMessage key="workflow.start.ProfileField"/>',
            rule: '<agn:agnMessage key="workflow.start.Rule"/>',
            start_date: '<agn:agnMessage key="workflow.start.StartDate"/>',
            start_event: '<agn:agnMessage key="workflow.start.StartEvent"/>',
            reminder_text: '<agn:agnMessage key="workflow.start.reminderText"/>',
            reaction_based: '<agn:agnMessage key="workflow.start.ReactionBasedStart"/>',
            date_based: '<agn:agnMessage key="mailing.date"/>',
            action_based: '<agn:agnMessage key="workflowlist.actionBased"/>'
        },
        stop: {
            title: '<agn:agnMessage key="workflow.stop.title"/>',
            end_date: '<agn:agnMessage key="workflow.stop.EndDate"/>',
            end_event: '<agn:agnMessage key="workflow.stop.EndEvent"/>',
            open_end: '<agn:agnMessage key="workflow.stop.OpenEnd"/>',
            automatic_end: '<agn:agnMessage key="workflow.stop.AutomaticEnd"/>',
            reminder_text: '<agn:agnMessage key="workflow.stop.reminderText"/>'
        },
        button: {
            copy: '<agn:agnMessage key="button.Copy"/>',
            cancel: '<agn:agnMessage key="button.Cancel"/>'
        },
        defaults: {
            error: '<agn:agnMessage key="Error"/>',
            edit: '<agn:agnMessage key="button.Edit"/>',
            delete: '<agn:agnMessage key="button.Delete"/>',
            comment: '<agn:agnMessage key="button.Comment"/>',
            delay: '<agn:agnMessage key="Delay"/>',
            hour: '<agn:agnMessage key="Hour"/>',
            hours: '<agn:agnMessage key="Hours"/>',
            day: '<agn:agnMessage key="Day"/>',
            days: '<agn:agnMessage key="Days"/>',
            week: '<agn:agnMessage key="Week"/>',
            weeks: '<agn:agnMessage key="default.weeks"/>',
            month: '<agn:agnMessage key="Month"/>',
            months: '<agn:agnMessage key="default.months"/>',
            report: '<agn:agnMessage key="Report"/>',
            no_mailing: '<agn:agnMessage key="NoMailing"/>',
            yes: '<agn:agnMessage key="default.Yes"/>',
            date: '<agn:agnMessage key="Date"/>',
            mailing: '<agn:agnMessage key="Mailing"/>',
            ckickrate: '<agn:agnMessage key="Clickrate"/>',
            mailtracking_required: '<agn:agnMessage key="mailtrackingRequired"/>',
            no: '<agn:agnMessage key="birt.No"/>',
            title: '<agn:agnMessage key="workflow.settings.overtake.title"/>',
            and: '<agn:agnMessage key="default.and"/>',
            or: '<agn:agnMessage key="default.or"/>'
        },
        deadline: {
            title: '<agn:agnMessage key="workflow.deadline"/>',
            minutes: '<agn:agnMessage key="workflow.deadline.Minutes"/>',
            minute: '<agn:agnMessage key="workflow.deadline.Minute"/>',
        },
        mailing: {
            tip: '<agn:agnMessage key="workflow.mailing.Tip"/>',
            new: '<agn:agnMessage key="dashboard.mailing.new"/>',
            archive: '<agn:agnMessage key="mailing.archive"/>',
            edit_mailing_link: '<agn:agnMessage key="workflow.mailing.editMailingLink"/>',
            autooptimization: '<agn:agnMessage key="mailing.autooptimization"/>',
            action_based: '<agn:agnMessage key="mailing.action.based.mailing"/>',
            date_based: '<agn:agnMessage key="mailing.Rulebased_Mailing"/>',
            followup: '<agn:agnMessage key="mailing.Followup_Mailing"/>'
        },

        mailinglist: {
            short: '<agn:agnMessage key="workflow.mailinglist.short"/>'
        },
        target: {
            short: '<agn:agnMessage key="workflow.target.short"/>'
        },
        statistic: {
            revenue: '<agn:agnMessage key="statistic.revenue"/>'
        },

        activating: {
            title: '<agn:agnMessage key="workflow.activating.title"/>'
        },

        report: {
            error : {
              no_report: '<agn:agnMessage key="workflow.report.ErrorNoReport"/>'
            }
        },

        copy: {
            question: '<agn:agnMessage key="workflow.copy.question"/>',
            question_with_content: '<agn:agnMessage key="workflow.copy.withContentQuestion"/>',
        },
        pdf: {
            save_campaign: '<agn:agnMessage key="workflow.pdf.saveCampaign"/>',
            save_new_campaign:'<agn:agnMessage key="workflow.pdf.saveNewCampaign"/>',
            save_modified_campaign:'<agn:agnMessage key="workflow.pdf.saveModifiedCampaign"/>',
        },
        inactivating: {
            title: '<agn:agnMessage key="workflow.inactivating.title"/>'
        },

        ownWorkflow: {
            title: '<agn:agnMessage key="workflow.ownCampaign"/>',
            copy_title: '<agn:agnMessage key="workflow.ownWorkflow.copyWorkflowTitle"/>'
        },

        connect: '<agn:agnMessage key="workflow.connect"/>',
        disconnect: '<agn:agnMessage key="workflow.disconnect"/>',
        opening_rate: '<agn:agnMessage key="workflow.decision.OpeningRate"/>',
        decision: '<agn:agnMessage key="workflow.decision"/>',
        fade_in_statistics: '<agn:agnMessage key="workflow.fadeInStatistics"/>',
        fade_out_statistics: '<agn:agnMessage key="workflow.fadeOutStatistics"/>',
        recipient: '<agn:agnMessage key="Recipient"/>',
        parameter: '<agn:agnMessage key="workflow.parameter"/>',
        single: '<agn:agnMessage key="workflow.single"/>',
        undo_history: '<agn:agnMessage key="workflow.undo.historyIsEmptyDialog.title"/>',
        status: {
            NONE: 'NONE',
            open: '<agn:agnMessage key="workflow.view.status.open"/>',
            active: '<agn:agnMessage key="default.status.active"/>',
            inactive: '<agn:agnMessage key="workflow.view.status.inActive"/>',
            completed: '<agn:agnMessage key="workflow.view.status.complete"/>',
            testing: '<agn:agnMessage key="workflow.view.status.testing"/>',
            tested: '<agn:agnMessage key="workflow.view.status.tested"/>',
            failed: 'FAILED',
            testing_failed: 'TESTING_FAILED'
        }
    },
    logon: {
        info: {
            multiple_tabs:'<agn:agnMessage key="logon.tabs.multiple"/>'
        },
        session: {
            notification: '<agn:agnMessage key="warning.session.expired"/>',
            expired:'<agn:agnMessage key="session.timer.expired"/>',
        }
    },
    messenger: {
        recipients: '<agn:agnMessage key="Recipients"/>'
    },
    recipient: {
        duplicate: {
            question: '<agn:agnMessage key="recipient.duplicate.question"/>'
        },
        blacklisted: {
            question: '<agn:agnMessage key="recipient.blacklisted.question"/>'
        },
        existing: {
            btn: '<agn:agnMessage key="recipient.existing.switch"/>'
        }
    },
    calendar: {
        error: {
            empty_comment: '<agn:agnMessage key="calendar.error.emptyComment"/>',
            long_comment: '<agn:agnMessage key="calendar.error.longComment"/>',
            empty_recipient_list: '<agn:agnMessage key="calendar.error.emptyRecipientList"/>',
            long_recipient_list: '<agn:agnMessage key="calendar.error.longRecipientList"/>',
            invalid_email: '<agn:agnMessage key="calendar.error.invalidEmail"/>'
        },
        title: {
            show_hide_comment: '<agn:agnMessage key="calendar.ShowHideComments"/>',
            new_comment: '<agn:agnMessage key="calendar.NewComment"/>',
            delete_comment: '<agn:agnMessage key="calendar.DeleteComment"/>'
        },
        common: {
            comment: '<agn:agnMessage key="calendar.Comment"/>',
            new_comment: '<agn:agnMessage key="calendar.NewComment"/>',
            edit_comment: '<agn:agnMessage key="calendar.EditComment"/>'
        }
    },
    userform: {
        error: {
            velocity_not_allowed: '<agn:agnMessage key="userform.velocityNotAllowed" />'
        }
    },

    forms: '<agn:agnMessage key="workflow.panel.forms"/>',
    report: '<agn:agnMessage key="Report"/>',
    auto_export: '<agn:agnMessage key="autoExport.autoExport"/>',
    auto_import: '<agn:agnMessage key="autoImport.autoImport"/>',
    tables: {
        // for filter panel
        range: '<agn:agnMessage key="table.paging.range" arg0="%s" arg1="%s" arg2="%s"/>',
        to: '<agn:agnMessage key="default.to"/>',
        next: '<agn:agnMessage key="button.Next"/>',
        last: '<agn:agnMessage key="default.Last"/>',
        first: '<agn:agnMessage key="default.First"/>',
        previous: '<agn:agnMessage key="button.Previous"/>',
        loadingOoo: '<agn:agnMessage key="default.loading"/>',

        // for set filter
        searchOoo: '<agn:agnMessage key="default.search"/>',
        blanks: '',

        // for number filter and text filter
        filterOoo: '<agn:agnMessage key="report.mailing.filter"/>',
        applyFilter: '<agn:agnMessage key="button.Apply"/>',
        clearFilter: '<agn:agnMessage key="filter.reset"/>',
        resetFilter: '<agn:agnMessage key="filter.reset"/>',

        // for number filter
        equals: '<agn:agnMessage key="target.operator.eq"/>',
        notEqual: '<agn:agnMessage key="target.operator.neq"/>',
        lessThan: '<agn:agnMessage key="target.operator.lt"/>',
        greaterThan: '<agn:agnMessage key="target.operator.gt"/>',
        inRange: '<agn:agnMessage key="operator.between"/>',
        lessThanOrEqual: '<agn:agnMessage key="target.operator.leq"/>',
        greaterThanOrEqual: '<agn:agnMessage key="target.operator.geq"/>',

        // for text filter
        contains: '<agn:agnMessage key="target.operator.contains"/>',
        notContains: '<agn:agnMessage key="target.operator.not_contains"/>',
        startsWith: '<agn:agnMessage key="operator.begins_with"/>',
        endsWith: '<agn:agnMessage key="operator.ends_with"/>',

        // for date filter
        from: '<agn:agnMessage key="operator.between"/>',
        till: '<agn:agnMessage key="default.and"/>',
    },
    mailing: {
        default: {
            target_group_name: '<agn:agnMessage key="statistic.all_subscribers"/>',
            interest_group_name: '<agn:agnMessage key="nointerestgroup"/>',
            dyn_tag_name: '<agn:agnMessage key="statistic.all_subscribers"/>',
            target_group_deleted: '<agn:agnMessage key="target.Deleted"/>',
            item_referencetable_warning: '<agn:agnMessage key="warning.mailing.items.refencetable.missing"/>'
        },
        remember: {
          choice: '<agn:agnMessage key="remember.choice.mailing"/>'
        },
        validation: {
            target_group_duplicated: '<agn:agnMessage key="error.mailing.content.target.duplicated"/>',
            all_recipients_not_last: '<agn:agnMessage key="error.mailing.content.target.order"/>'
        }
    },
    wysiwyg: {
        dialogs: {
            agn_tags: {
                tooltip: '<agn:agnMessage key="htmled.agntagsButtonTooltip"/>'
            }
        }
    },
    facebook : {
    	leadAds: {
    		renewedPageAccessTokens : '<agn:agnMessage key="facebook.leadAds.renewedPageAccessTokens" arg0="%s"/>'
    	}
    }
};
