package com.mesilat.bvp.demo;

import com.atlassian.confluence.plugins.createcontent.api.contextproviders.AbstractBlueprintContextProvider;
import com.atlassian.confluence.plugins.createcontent.api.contextproviders.BlueprintContext;
import com.atlassian.plugin.spring.scanner.annotation.component.Scanned;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.message.I18nResolver;
import javax.inject.Inject;

@Scanned
public class ProductContextProvider extends AbstractBlueprintContextProvider {
    private final I18nResolver resolver;

    @Override
    protected BlueprintContext updateBlueprintContext(BlueprintContext blueprintContext) {
        blueprintContext.setTitle(resolver.getText("com.mesilat.bvp.demo.product-template.caption"));
        return blueprintContext;
    }
    @Inject
    public ProductContextProvider(final @ComponentImport  I18nResolver resolver) {
        this.resolver = resolver;
    }
}