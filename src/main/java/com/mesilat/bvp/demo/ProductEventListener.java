package com.mesilat.bvp.demo;

import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.transaction.TransactionTemplate;
import com.mesilat.vbp.api.TemplateManager;
import javax.inject.Inject;
import javax.inject.Named;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

@Named
public class ProductEventListener extends BaseEventListener implements InitializingBean, DisposableBean {
    public static final String TEMPLATE_KEY = "com.mesilat.blueprint-validation-demo:bvp-product-template";
    public static final String ENDPOINT = "/rest/confield-demo/1.0/product";

    @Inject
    public ProductEventListener(
        @ComponentImport EventPublisher eventPublisher,
        @ComponentImport TemplateManager bvpTemplateManager,
        @ComponentImport TransactionTemplate transactionTemplate,
        @ComponentImport ApplicationLinkService appLinkService,
        @ComponentImport SettingsManager settingsManager
    ){
        super(TEMPLATE_KEY, ENDPOINT, eventPublisher, bvpTemplateManager, transactionTemplate, appLinkService, settingsManager);
    }
}