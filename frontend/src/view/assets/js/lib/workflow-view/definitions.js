(function() {
    // Defines images to be used for icons (depending on their types) and connection anchors positioning.
    var ICONS_CONFIG = {
        actionbased_mailing: {
            icons: {
                inactive: 'icon_actionbased_mailing_g.png',
                active: 'icon_actionbased_mailing_l.png',
                dialog: 'icon_actionbased_mailing_s.png'
            },
            anchors: [
                [0.07, 0.5, -1, 0],
                [0.5, 0.22, 0, -1],
                [0.9, 0.5, 1, 0],
                [0.5, 0.78, 0, 1]
            ]
        },
        archive: {
            icons: {
                inactive: 'icon_archive_g.png',
                active: 'icon_archive_l.png',
                dialog: 'icon_archive_s.png'
            },
            anchors: [
                [0.1, 0.5, -1, 0],
                [0.5, 0.22, 0, -1],
                [0.9, 0.5, 1, 0],
                [0.5, 0.8, 0, 1]
            ]
        },
        datebased_mailing: {
            icons: {
                inactive: 'icon_datebased_mailing_g.png',
                active: 'icon_datebased_mailing_l.png',
                dialog: 'icon_datebased_mailing_s.png'
            },
            anchors: [
                [0.07, 0.5, -1, 0],
                [0.5, 0.22, 0, -1],
                [0.9, 0.5, 1, 0],
                [0.5, 0.78, 0, 1]
            ]
        },
        deadline: {
            icons: {
                inactive: 'icon_deadline_g.png',
                active: 'icon_deadline_l.png',
                dialog: 'icon_deadline_s.png'
            },
            anchors: [
                [0.1, 0.5, -1, 0],
                [0.5, 0.05, 0, -1],
                [0.9, 0.5, 1, 0],
                [0.5, 0.9, 0, 1]
            ]
        },
        decision: {
            icons: {
                inactive: 'icon_decision_g.png',
                active: 'icon_decision_l.png',
                dialog: 'icon_decision_s.png'
            },
            anchors: [
                [0.06, 0.5, -1, 0],
                [0.5, 0.06, 0, -1],
                [0.94, 0.5, 1, 0],
                [0.5, 0.94, 0, 1]
            ]
        },
        export: {
            icons: {
                inactive: 'icon_export_g.png',
                active: 'icon_export_l.png',
                dialog: 'icon_export_s.png'
            },
            anchors: [
                [0.1, 0.5, -1, 0],
                [0.5, 0.1, 0, -1],
                [0.9, 0.5, 1, 0],
                [0.5, 0.9, 0, 1]
            ]
        },
        followup_mailing: {
            icons: {
                inactive: 'icon_followup_mailing_g.png',
                active: 'icon_followup_mailing_l.png',
                dialog: 'icon_followup_mailing_s.png'
            },
            anchors: [
                [0.04, 0.5, -1, 0],
                [0.5, 0.20, 0, -1],
                [0.96, 0.5, 1, 0],
                [0.5, 0.8, 0, 1]
            ]
        },
        form: {
            icons: {
                inactive: 'icon_form_g.png',
                active: 'icon_form_l.png',
                dialog: 'icon_form_s.png'
            },
            anchors: [
                [0.1, 0.5, -1, 0],
                [0.5, 0.22, 0, -1],
                [0.9, 0.5, 1, 0],
                [0.5, 0.78, 0, 1]
            ]
        },
        import: {
            icons: {
                inactive: 'icon_import_g.png',
                active: 'icon_import_l.png',
                dialog: 'icon_import_s.png'
            },
            anchors: [
                [0.1, 0.5, -1, 0],
                [0.5, 0.1, 0, -1],
                [0.9, 0.5, 1, 0],
                [0.5, 0.9, 0, 1]
            ]
        },
        mailing: {
            icons: {
                inactive: 'icon_mailing_g.png',
                active: 'icon_mailing_l.png',
                dialog: 'icon_mailing_s.png'
            },
            anchors: [
                [0.1, 0.5, -1, 0],
                [0.5, 0.22, 0, -1],
                [0.9, 0.5, 1, 0],
                [0.5, 0.78, 0, 1]
            ]
        },
        ownWorkflow: {
            icons: {
                inactive: 'icon_ownWorkflow_g.png',
                active: 'icon_ownWorkflow_l.png',
                dialog: 'icon_ownWorkflow_s.png'
            },
            anchors: [
                [0.1, 0.5, -1, 0],
                [0.5, 0.22, 0, -1],
                [0.9, 0.5, 1, 0],
                [0.5, 0.78, 0, 1]
            ]
        },
        parameter: {
            icons: {
                inactive: 'icon_parameter_g.png',
                active: 'icon_parameter_l.png',
                dialog: 'icon_parameter_s.png'
            },
            anchors: [
                [0.1, 0.5, -1, 0],
                [0.5, 0.1, 0, -1],
                [0.9, 0.5, 1, 0],
                [0.5, 0.9, 0, 1]
            ]
        },
        recipient: {
            icons: {
                inactive: 'icon_recipient_g.png',
                active: 'icon_recipient_l.png',
                dialog: 'icon_recipient_s.png'
            },
            anchors: [
                [0.1, 0.5, -1, 0],
                [0.5, 0.1, 0, -1],
                [0.9, 0.5, 1, 0],
                [0.5, 0.9, 0, 1]
            ]
        },
        report: {
            icons: {
                inactive: 'icon_report_g.png',
                active: 'icon_report_l.png',
                dialog: 'icon_report_s.png'
            },
            anchors: [
                [0.1, 0.5, -1, 0],
                [0.5, 0.22, 0, -1],
                [0.9, 0.5, 1, 0],
                [0.5, 0.78, 0, 1]
            ]
        },
        scABTest: {
            icons: {
                inactive: 'icon_scABTest_g.png',
                active: 'icon_scABTest_l.png',
                dialog: 'icon_scABTest_s.png'
            },
            anchors: [
                [0.1, 0.5, -1, 0],
                [0.5, 0.15, 0, -1],
                [0.9, 0.5, 1, 0],
                [0.5, 0.85, 0, 1]
            ]
        },
        scBirthday: {
            icons: {
                inactive: 'icon_scBirthday_g.png',
                active: 'icon_scBirthday_l.png',
                dialog: 'icon_scBirthday_s.png'
            },
            anchors: [
                [0.1, 0.5, -1, 0],
                [0.5, 0.1, 0, -1],
                [0.9, 0.5, 1, 0],
                [0.5, 0.85, 0, 1]
            ]
        },
        scDOI: {
            icons: {
                inactive: 'icon_scDOI_g.png',
                active: 'icon_scDOI_l.png',
                dialog: 'icon_scDOI_s.png'
            },
            anchors: [
                [0.3, 0.5, -1, 0],
                [0.5, 0.04, 0, -1],
                [0.72, 0.5, 1, 0],
                [0.5, 0.96, 0, 1]
            ]
        },
        start: {
            icons: {
                inactive: 'icon_start_g.png',
                active: 'icon_start_l.png',
                dialog: 'icon_start_s.png'
            },
            anchors: [
                [0, 0.5, -1, 0],
                [0.5, 0.15, 0, -1],
                [1, 0.5, 1, 0],
                [0.5, 0.85, 0, 1]
            ]
        },
        stop: {
            icons: {
                inactive: 'icon_start_g.png',
                active: 'icon_start_l.png',
                dialog: 'icon_start_s.png'
            },
            anchors: [
                [0, 0.5, -1, 0],
                [0.5, 0.15, 0, -1],
                [1, 0.5, 1, 0],
                [0.5, 0.85, 0, 1]
            ]
        }
    };

    // Defines what icons are allowed to be connected (depending on their types).
    var CONNECTION_CONSTRAINTS = {
        '*': {
            deadline: function (source, target) {
                // Make sure that there's no incoming connection from import icon.
                return !target.connections.incoming.some(function (connection) {
                    return connection.source.getType() === 'import';
                });
            },

            mailing: function (source, target) {
                // The second incoming connection to mailing icon is not allowed.
                return target.connections.incoming.length === 0;
            },

            datebased_mailing: function (source, target) {
                // The second incoming connection to mailing icon is not allowed.
                return target.connections.incoming.length === 0;
            }
        },
        start: {
            recipient: true,
            form: true,
            import: true,
            export: true
        },
        stop: false,
        decision: {
            stop: true,
            decision: true,
            parameter: true,
            deadline: true,
            report: true,
            recipient: true,
            form: true,
            archive: true,
            actionbased_mailing: true,
            datebased_mailing: true,
            followup_mailing: true,
            mailing: true
        },
        parameter: {
            stop: true,
            decision: true,
            parameter: true,
            deadline: true,
            report: true,
            recipient: true,
            form: true,
            archive: true,
            actionbased_mailing: true,
            datebased_mailing: true,
            followup_mailing: true,
            mailing: true
        },
        deadline: {
            decision: true,
            parameter: true,
            report: true,
            recipient: true,
            archive: true,
            actionbased_mailing: true,
            datebased_mailing: true,
            followup_mailing: true,
            mailing: true,
            import: true,
            export: true
        },
        report: {
            stop: true,
            decision: true,
            parameter: true,
            deadline: true,
            report: true,
            recipient: true,
            archive: true,
            actionbased_mailing: true,
            datebased_mailing: true,
            followup_mailing: true,
            mailing: true,
            import: true,
            export: true
        },
        recipient: {
            decision: true,
            parameter: true,
            deadline: true,
            report: true,
            recipient: true,
            form: true,
            archive: true,
            actionbased_mailing: true,
            datebased_mailing: true,
            followup_mailing: true,
            mailing: true,
            import: true,
            export: true
        },
        actionbased_mailing: {
            stop: true,
            decision: true,
            parameter: true,
            deadline: true,
            report: true,
            recipient: true,
            form: true,
            archive: true,
            import: true,
            export: true
        },
        datebased_mailing: {
            stop: true,
            decision: true,
            parameter: true,
            deadline: true,
            report: true,
            recipient: true,
            form: true,
            archive: true,
            import: true,
            export: true
        },
        followup_mailing: {
            stop: true,
            decision: true,
            parameter: true,
            deadline: true,
            report: true,
            recipient: true,
            form: true,
            archive: true,
            import: true,
            export: true
        },
        mailing: {
            stop: true,
            decision: true,
            parameter: true,
            deadline: true,
            report: true,
            recipient: true,
            form: true,
            archive: true,
            import: true,
            export: true
        },
        archive: {
            decision: true,
            parameter: true,
            deadline: true,
            report: true,
            recipient: true,
            actionbased_mailing: true,
            datebased_mailing: true,
            followup_mailing: true,
            mailing: true,
            import: true,
            export: true
        },
        form: {
            stop: true,
            decision: true,
            parameter: true,
            deadline: true,
            report: true,
            recipient: true,
            form: true,
            archive: true,
            actionbased_mailing: true,
            datebased_mailing: true,
            followup_mailing: true,
            mailing: true,
            import: true,
            export: true
        },
        import: {
            deadline: function (source /*, target */) {
                return source.connections.outgoing.length === 0;
            }
        },
        export: {
            stop: true,
            decision: true,
            parameter: true,
            deadline: true,
            report: true,
            recipient: true,
            actionbased_mailing: true,
            datebased_mailing: true,
            followup_mailing: true,
            mailing: true,
            import: true,
            export: true,
            archive: true,
            form: true
        }
    };

    AGN.Lib.WM.Definitions = {
        KEY_ENTER: 13,
        KEY_SHIFT: 16,
        KEY_CTRL: 17,
        KEY_SPACE: 32,
        KEY_DELETE: 46,

        MOUSE_BUTTON_LEFT: 0,
        MOUSE_BUTTON_MIDDLE: 1,

        BOTTOM: 'BOTTOM',
        LEFT: 'LEFT',
        RIGHT: 'RIGHT',
        TOP: 'TOP',

        NODE_ANIMATION_DROPPED: 'animate-dropped',
        NODE_ANIMATION_AUTO_DROPPED: 'animate-auto-dropped',

        // Page level.
        Z_INDEX_VIEW_PORT: 10,
        Z_INDEX_ICON_PANEL: 50,  // Icon panel above the view port.

        // Panel level.
        Z_INDEX_DRAGGABLE_BUTTON: 60,

        // Stage level.
        Z_INDEX_ICON_TITLES_CONTAINER: 5,  // Lowest object on the stage.
        Z_INDEX_CONNECTION_OVERLAY: 9,
        Z_INDEX_ICON: 10,
        Z_INDEX_CONNECTION_HOVER: 11,
        Z_INDEX_CONNECTION_OVERLAY_HOVER: 12,
        Z_INDEX_ICON_HOVER: 13,
        Z_INDEX_ICON_TITLE_HOVER: 19,
        Z_INDEX_DRAGGED_ICON_NODE: 20,
        Z_INDEX_JSPLUMB_DRAG_OPTIONS: 25,
        Z_INDEX_NAVIGATOR_GLOBE: 30,
        Z_INDEX_NAVIGATOR_ARROWS: 31,
        Z_INDEX_CONTEXT_MENU: 40,  // Highest object on the stage.

        CANVAS_GRID_SIZE: 20,
        NODE_SIZE: 64,
        NODE_MIN_MARGIN: 0.1,
        TITLE_WIDTH: 94,  // Same as node itself + padding.

        MINIMAP_PADDING: 2,

        AUTO_ALIGN_STEP_X: 5,
        AUTO_ALIGN_EXTRA_STEP_X: 0.5,
        AUTO_ALIGN_STEP_Y: 6,
        AUTO_ALIGN_STEP_BETWEEN_GROUPS: 2,

        // Auto drop is required when a new icon double clicked, not dragged in, so position has to be picked automatically.
        AUTO_DROP_STEP_X: 4,
        AUTO_DROP_STEP_Y: 3,

        MIN_ZOOM: 0.5,
        MAX_ZOOM: 1.5,
        DEFAULT_ZOOM: 1,
        ZOOM_STEP: 0.1,

        NAVIGATION_STEP: 100,

        MAX_UNDO_STEPS: 17,

        CONNECTION_COLOR: '#61B7CF',
        CONNECTION_THICKNESS: 3,
        CONNECTION_OUTLINE_COLOR: 'transparent',
        CONNECTION_OUTLINE_WIDTH: 4,
        CONNECTION_HOVER_COLOR: '#216477',
        CONNECTION_ARROW_SIZE: 10,

        NODE_TYPE_START: 'start',
        NODE_TYPE_STOP: 'stop',
        NODE_TYPE_DECISION: 'decision',
        NODE_TYPE_DEADLINE: 'deadline',
        NODE_TYPE_PARAMETER: 'parameter',
        NODE_TYPE_REPORT: 'report',
        NODE_TYPE_RECIPIENT: 'recipient',
        NODE_TYPE_ARCHIVE: 'archive',
        NODE_TYPE_FORM: 'form',
        NODE_TYPE_MAILING: 'mailing',
        NODE_TYPE_OWN_WORKFLOW: 'ownWorkflow',
        NODE_TYPE_SC_BIRTHDAY: 'scBirthday',
        NODE_TYPE_SC_DOI: 'scDOI',
        NODE_TYPE_SC_ABTEST: 'scABTest',
        NODE_TYPE_ACTION_BASED_MAILING: 'actionbased_mailing',
        NODE_TYPE_DATE_BASED_MAILING: 'datebased_mailing',
        NODE_TYPE_FOLLOWUP_MAILING: 'followup_mailing',
        NODE_TYPE_IMPORT: 'import',
        NODE_TYPE_EXPORT: 'export',

        NODE_TYPES_MAILING: ['mailing', 'followup_mailing', 'actionbased_mailing', 'datebased_mailing'],
        NODE_TYPES_IMPORT_EXPORT: ['import', 'export'],

        NODE_TYPE_START_ID: 0,
        NODE_TYPE_STOP_ID: 1,
        NODE_TYPE_DECISION_ID: 2,
        NODE_TYPE_DEADLINE_ID: 3,
        NODE_TYPE_PARAMETER_ID: 4,
        NODE_TYPE_REPORT_ID: 5,
        NODE_TYPE_RECIPIENT_ID: 6,
        NODE_TYPE_ARCHIVE_ID: 7,
        NODE_TYPE_FORM_ID: 8,
        NODE_TYPE_MAILING_ID: 9,
        NODE_TYPE_OWN_WORKFLOW_ID: 10,
        NODE_TYPE_SC_BIRTHDAY_ID: 11,
        NODE_TYPE_SC_DOI_ID: 12,
        NODE_TYPE_SC_ABTEST_ID: 13,
        NODE_TYPE_ACTION_BASED_MAILING_ID: 14,
        NODE_TYPE_DATE_BASED_MAILING_ID: 15,
        NODE_TYPE_FOLLOWUP_MAILING_ID: 16,
        NODE_TYPE_IMPORT_ID: 17,
        NODE_TYPE_EXPORT_ID: 18,

        MAILING_PARAM_MAILING_LIST: 1,
        MAILING_PARAM_TARGET_GROUPS: 2,
        MAILING_PARAM_ARCHIVE: 3,
        MAILING_PARAM_LIST_SPLIT: 4,
        MAILING_PARAM_PLANNED_DATE: 5,
        MAILING_PARAM_SEND_DATE: 6,

        // Make sure to keep in sync with WorkflowDependencyType enum.
        DEPENDENCY_TYPE_ARCHIVE: 1,
        DEPENDENCY_TYPE_AUTO_EXPORT: 2,
        DEPENDENCY_TYPE_AUTO_IMPORT: 3,
        DEPENDENCY_TYPE_MAILING_DELIVERY: 4,
        DEPENDENCY_TYPE_MAILING_LINK: 5,
        DEPENDENCY_TYPE_MAILING_REFERENCE: 6,
        DEPENDENCY_TYPE_MAILINGLIST: 7,
        DEPENDENCY_TYPE_PROFILE_FIELD: 8,
        DEPENDENCY_TYPE_PROFILE_FIELD_HISTORY: 9,
        DEPENDENCY_TYPE_REPORT: 10,
        DEPENDENCY_TYPE_TARGET_GROUP: 11,
        DEPENDENCY_TYPE_USER_FORM: 12,

        GENDER_PROFILE_FIELD: 'gender',
        FIELD_TYPE_DATE: 'date',

        TITLE_MAX_TARGETS: 2,
        TITLE_MAX_REPORTS: 2,

        PDF_SIZE: 'A4',
        PDF_ORIENTATION: 'landscape',

        PAGE_DIMENSIONS: {
            'A4': {
                width: 800,
                height: 1360
            }
        },

        ICONS_CONFIG: ICONS_CONFIG,

        CONNECTION_CONSTRAINTS: CONNECTION_CONSTRAINTS,

        NODE_POPOVER_TEMPLATE: '<div class="popover popover-wide" role="tooltip" ><div class="arrow"></div><div class="popover-content" style="width: auto; height: auto; max-width: 300px; max-height: 350px; overflow: auto;"></div></div>'
    };
})();