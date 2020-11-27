package com.mesilat.bvp.demo;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.mesilat.vbp.api.DataValidateEvent;
import com.mesilat.vbp.api.Template;
import com.mesilat.vbp.api.TemplateManager;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

public class BaseEventListener implements InitializingBean, DisposableBean {
    public static final Logger LOGGER = LoggerFactory.getLogger("com.mesilat.bvp.demo");

    private final String templateKey;
    private final String endpoint;
    private final EventPublisher eventPublisher;
    private final TemplateManager bvpTemplateManager;
    private final TransactionTemplate transactionTemplate;
    private final ApplicationLinkService appLinkService;
    private final SettingsManager settingsManager;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterPropertiesSet() throws Exception {
        Thread t = new Thread(() -> {
            transactionTemplate.execute(() -> {
                Template template = bvpTemplateManager.get(templateKey);
                if (template == null) { // || template.getValidationMode() == Template.ValidationMode.NONE) {
                    bvpTemplateManager.setValidationMode(templateKey, Template.ValidationMode.WARN);
                }
                return null;
            });
        });
        t.start();
        eventPublisher.register(this);
        LOGGER.debug(String.format("Registered ValidateEvent listener for template %s", templateKey));
    }
    @Override
    public void destroy() throws Exception {
        eventPublisher.unregister(this);
    }
    @EventListener
    public void dataObjectValidateEvent(DataValidateEvent event) {
        if (!templateKey.equals(event.getTemplateKey())) {
            LOGGER.debug(String.format("DataValidateEvent for template=%s; handler is %s, aborting", event.getTemplateKey(), templateKey));
            return;
        } else {
            LOGGER.debug(String.format("DataValidateEvent for template=%s; processing...", event.getTemplateKey()));
        }

        Page page = event.getPage();
        ConfluenceUser user = AuthenticatedUserThreadLocal.get();
        try {
            if (user == null) {
                AuthenticatedUserThreadLocal.set(page.getLastModifier());
            }

            JsonNode node = mapper.readTree(event.getData());
            if (!node.isObject()) {
                throw new RuntimeException("Unexpected data object");
            }
            ObjectNode obj = (ObjectNode)node;
            obj.put("id", page.getId());
            obj.put("title", page.getTitle());

            ApplicationLink jira = appLinkService.getPrimaryApplicationLink(JiraApplicationType.class);
            if (jira == null) {
                LOGGER.warn("No JIRA application link configured");
                event.setValid(false);
                event.addMessage("No JIRA application link configured");
                return;
            }

            ApplicationLinkRequestFactory reqFactory = jira.createAuthenticatedRequestFactory();
            reqFactory
                .createRequest(Request.MethodType.POST, endpoint)
                .addHeader("content-type", "application/json; charset=UTF-8")
                .setRequestBody(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj))
                .executeAndReturn((com.atlassian.sal.api.net.Response response) -> {
                    if (response.getStatusCode() == 201) {
                        return null;
                    } else {
                        int code = response.getStatusCode();
                        switch (code) {
                            case 401:
                                String url = String.format("%s/plugins/servlet/applinks/oauth/login-dance/access?applicationLinkID=%s",
                                    settingsManager.getGlobalSettings().getBaseUrl(),
                                    jira.getId().get()
                                );
                                throw new RuntimeException(String.format(
                                    "User %s is not authorized by JIRA. <a href='%s'>Click to authorize</a>",
                                    AuthenticatedUserThreadLocal.get().getFullName(),
                                    url
                                ));
                            default:
                                throw new RuntimeException(response.getResponseBodyAsString());
                        }
                    }
                });
        } catch (Throwable ex) {
            event.setValid(false);
            event.addMessage("Failed to push changes to JIRA server: " + ex.getMessage());
            LOGGER.warn("Failed to push changes to JIRA server", ex);
        } finally {
            AuthenticatedUserThreadLocal.set(user);
        }
    }

    public BaseEventListener(
        String templateKey,
        String endpoint,
        EventPublisher eventPublisher,
        TemplateManager bvpTemplateManager,
        TransactionTemplate transactionTemplate,
        ApplicationLinkService appLinkService,
        SettingsManager settingsManager
    ){
        this.templateKey = templateKey;
        this.endpoint = endpoint;
        this.eventPublisher = eventPublisher;
        this.bvpTemplateManager = bvpTemplateManager;
        this.transactionTemplate = transactionTemplate;
        this.appLinkService = appLinkService;
        this.settingsManager = settingsManager;
    }
}