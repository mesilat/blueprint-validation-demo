package com.mesilat.bvp.demo;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.confluence.renderer.UserMacroConfig;
import com.atlassian.confluence.renderer.UserMacroLibrary;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.spring.container.ContainerManager;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Named;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

@Named
public class UserMacroInstaller implements InitializingBean {
    public static final Logger LOGGER = LoggerFactory.getLogger("com.mesilat.bvp.demo");

    private final ApplicationLinkService appLinkService;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterPropertiesSet() throws Exception {
        Thread t = new Thread(() -> {
            try (InputStream in = this.getClass().getResourceAsStream("/user-macros.json")) {
                JsonNode json = mapper.readTree(in);
                if (json.isArray()) {
                    ArrayNode arr = (ArrayNode)json;
                    arr.forEach(node -> createUserMacro((ObjectNode)node));
                } else if (json.isObject()) {
                    createUserMacro((ObjectNode)json);
                }
            } catch(IOException ex) {
                LOGGER.error("Failed to install user macros", ex);
            }
        });
        t.start();
    }

    private void createUserMacro(ObjectNode obj) {
        String macroName = null;
        try {
            if (obj.get("name") == null || obj.get("name").isNull()) {
                throw new Exception("Macro name is missing or null");
            }
            macroName = obj.get("name").asText();
            UserMacroLibrary userMacroLibrary = (UserMacroLibrary)ContainerManager.getComponent("userMacroLibrary");
            UserMacroConfig config = userMacroLibrary.getMacro(macroName);
            if (config != null) {
                LOGGER.warn(String.format("User macro with name %s already exists", macroName));
                return;
            }

            config = new UserMacroConfig();
            config.setName(macroName);

            if (obj.has("template") && !obj.get("template").isNull()) {
                String template = obj.get("template").asText();
                ApplicationLink jira = appLinkService.getPrimaryApplicationLink(JiraApplicationType.class);
                if (jira != null) {
                    template = template.replace("{jira-server-id}", jira.getId().get());
                }
                config.setTemplate(template);
            }
            if (obj.has("bodyType") && !obj.get("bodyType").isNull())
                config.setBodyType(obj.get("bodyType").asText());
            if (obj.has("description") && !obj.get("description").isNull())
                config.setDescription(obj.get("description").asText());
            if (obj.has("documentationUrl") && !obj.get("documentationUrl").isNull())
                config.setDocumentationUrl(obj.get("documentationUrl").asText());
            if (obj.has("iconLocation") && !obj.get("iconLocation").isNull())
                config.setIconLocation(obj.get("iconLocation").asText());
            if (obj.has("title") && !obj.get("title").isNull())
                config.setTitle(obj.get("title").asText());
            if (obj.has("hasBody") && !obj.get("hasBody").isNull())
                config.setHasBody(obj.get("hasBody").asBoolean());
            if (obj.has("hidden") && !obj.get("hidden").isNull())
                config.setHidden(obj.get("hidden").asBoolean());
            if (obj.has("categories") && obj.get("categories").isArray()) {
                ArrayNode categories = (ArrayNode)obj.get("categories");
                Set<String> cats = new HashSet<>();
                categories.forEach(a -> {
                    if (a.isTextual())
                        cats.add(a.asText());
                });
                config.setCategories(cats);
            }        
            userMacroLibrary.addUpdateMacro(config);        
        } catch(Throwable ex) {
            LOGGER.error(String.format("Failed to install user macro", macroName), ex);
        }
    }
    
    public UserMacroInstaller(@ComponentImport ApplicationLinkService appLinkService) {
        this.appLinkService = appLinkService;
    }
}
