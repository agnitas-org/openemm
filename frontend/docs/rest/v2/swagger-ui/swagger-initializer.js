window.onload = function() {
  const THEME_STORAGE_KEY = "emm-rest-swagger-ui-theme";
  const sunIcon = `<svg viewBox="0 0 16 16" width="16" height="16" fill="currentColor"><path d="M8 12a4 4 0 1 1 0-8 4 4 0 0 1 0 8Zm0-1.5a2.5 2.5 0 1 0 0-5 2.5 2.5 0 0 0 0 5Zm5.657-8.157a.75.75 0 0 1 0 1.061l-1.061 1.06a.749.749 0 0 1-1.275-.326.749.749 0 0 1 .215-.734l1.06-1.06a.75.75 0 0 1 1.06 0Zm-9.193 9.193a.75.75 0 0 1 0 1.06l-1.06 1.061a.75.75 0 1 1-1.061-1.06l1.06-1.061a.75.75 0 0 1 1.061 0ZM8 0a.75.75 0 0 1 .75.75v1.5a.75.75 0 0 1-1.5 0V.75A.75.75 0 0 1 8 0ZM3 8a.75.75 0 0 1-.75.75H.75a.75.75 0 0 1 0-1.5h1.5A.75.75 0 0 1 3 8Zm13 0a.75.75 0 0 1-.75.75h-1.5a.75.75 0 0 1 0-1.5h1.5A.75.75 0 0 1 16 8Zm-8 5a.75.75 0 0 1 .75.75v1.5a.75.75 0 0 1-1.5 0v-1.5A.75.75 0 0 1 8 13Zm3.536-1.464a.75.75 0 0 1 1.06 0l1.061 1.06a.75.75 0 0 1-1.06 1.061l-1.061-1.06a.75.75 0 0 1 0-1.061ZM2.343 2.343a.75.75 0 0 1 1.061 0l1.06 1.061a.751.751 0 0 1-.018 1.042.751.751 0 0 1-1.042.018l-1.06-1.06a.75.75 0 0 1 0-1.06Z"/></svg>`;
  const moonIcon = `<svg viewBox="0 0 16 16" width="16" height="16" fill="currentColor"><path d="M9.598 1.591a.749.749 0 0 1 .785-.175 7.001 7.001 0 1 1-8.967 8.967.75.75 0 0 1 .961-.96 5.5 5.5 0 0 0 7.046-7.046.75.75 0 0 1 .175-.786Zm1.616 1.945a7 7 0 0 1-7.678 7.678 5.499 5.499 0 1 0 7.678-7.678Z"/></svg>`;

  const savedTheme = localStorage.getItem(THEME_STORAGE_KEY);
  if (savedTheme === "dark") {
    document.documentElement.classList.add("dark-mode");
  }

  const ThemeTogglePlugin = (system) => ({
    wrapComponents: {
      info: (OriginalComponent, system) => (props) => {
        const { React } = system;

        const [isDark, setIsDark] = React.useState(() =>
          document.documentElement.classList.contains("dark-mode")
        );

        const toggleTheme = () => {
          const nextDark = !isDark;
          document.documentElement.classList.toggle("dark-mode", nextDark);
          localStorage.setItem(THEME_STORAGE_KEY, nextDark ? "dark" : "light");
          setIsDark(nextDark);
        };

        const toggleButton = React.createElement("button", {
          onClick: toggleTheme,
          style: {
            background: "transparent",
            border: "none",
            cursor: "pointer",
            color: "inherit",
            display: "inline-flex",
            alignItems: "center",
            justifyContent: "center",
            padding: "8px",
            borderRadius: "50%",
            transition: "background-color 0.2s"
          },
          title: isDark ? "Switch to light mode" : "Switch to dark mode",
          dangerouslySetInnerHTML: { __html: isDark ? sunIcon : moonIcon }
        });

        return React.createElement("div", { style: { position: "relative" } },
          React.createElement(OriginalComponent, props),
          React.createElement("div", {
            style: {
              position: "absolute",
              top: "24px",
              right: "0",
              zIndex: 10
            }
          }, toggleButton)
        );
      }
    }
  });

  const ExtendedScopeBadgePlugin = (system) => ({
    wrapComponents: {
      OperationSummary: (OriginalComponent, system) => (props) => {
        const { React } = system;
        const specPath = props.specPath?.toJS ? props.specPath.toJS() : props.specPath;

        let isExtended = false;
        if (specPath) {
          isExtended = system.specSelectors.specJson()?.getIn([...specPath, "x-extended-scope"]) === true;
        }
        if (!isExtended) {
          return React.createElement(OriginalComponent, props);
        }
        const badge = React.createElement("span", {
          style: {
            backgroundColor: "#0071B9",
            color: "#ffffff",
            padding: "3px 5px",
            borderRadius: "5px",
            fontSize: "11px",
            fontWeight: "bold",
            boxShadow: "0 1px 3px rgba(0,0,0,0.12)"
          }
        }, "EMM only");

        return React.createElement("div", { style: { position: "relative", width: "100%" } },
          React.createElement(OriginalComponent, props),
          React.createElement("div", {
            style: {
              position: "absolute",
              right: "115px",
              top: "47%",
              transform: "translateY(-50%)",
              pointerEvents: "none"
            }
          }, badge)
        );
      }
    }
  });

  const NON_BLANKABLE_FIELDS = new Set(window.__NON_BLANKABLE_FIELDS__ || []);

  let currentOperationId = null;

  const UncheckSendEmptyValuePlugin = function() {
    return {
      wrapComponents: {
        operation: (Original, { React }) => (props) => {
          currentOperationId = props.operation?.get("operationId");
          return React.createElement(Original, props);
        },
        ParameterIncludeEmpty: (Original, { React }) => (props) => {
          const key = props.isIncludedOptions?.key;
          const compositeKey = `${currentOperationId}::${key}`;
          if (NON_BLANKABLE_FIELDS.has(compositeKey)) {
            return null;
          }
          props.isIncludedOptions.defaultValue = false;
          return React.createElement(Original, props)
        },
        TryItOutButton: (Original, { React }) => (props) => {
          if (props.isOAS3 && props.hasUserEditedBody) { props.hasUserEditedBody = false; }
          return React.createElement(Original, props)
        }
      }
    }
  }

  //<editor-fold desc="Changeable Configuration Block">

  // the following lines will be replaced by docker/configurator, when it runs in a docker-container

  window.ui = SwaggerUIBundle({
    url: 'openapi',
    dom_id: '#swagger-ui',
    docExpansion: "none",
    deepLinking: true,
    presets: [
      SwaggerUIBundle.presets.apis,
      SwaggerUIStandalonePreset
    ],
    plugins: [
      SwaggerUIBundle.plugins.DownloadUrl,
      ExtendedScopeBadgePlugin,
      ThemeTogglePlugin,
      UncheckSendEmptyValuePlugin
    ],
    layout: "BaseLayout",
    queryConfigEnabled: false,
    requestInterceptor: (req) => {
      const accept = req.headers["Accept"] || req.headers["accept"];
      if (accept && !accept.includes("json")) {
        delete req.headers["accept"];
        req.headers["Accept"] = accept + ", application/json;q=0.9, */*;q=0.1";
      }
      return req;
    },
    onComplete: () => {
      const select = document.querySelector('.swagger-ui .servers select');
      if (select && select.options.length <= 1) {
        document.querySelector('.schemes-server-container').style.opacity = '0';
      }
    }
  });

  //</editor-fold>
};
