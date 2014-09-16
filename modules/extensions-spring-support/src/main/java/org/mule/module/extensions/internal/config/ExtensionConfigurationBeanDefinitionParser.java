/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.config;

import org.mule.config.spring.parsers.generic.AutoIdUtils;
import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.introspection.api.ExtensionConfiguration;
import org.mule.extensions.introspection.api.ExtensionParameter;
import org.mule.module.extensions.internal.runtime.DefaultObjectBuilder;
import org.mule.module.extensions.internal.runtime.ObjectBuilder;
import org.mule.module.extensions.internal.runtime.resolver.ValueResolver;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * Generic implementation of {@link org.springframework.beans.factory.xml.BeanDefinitionParser}
 * capable of parsing any {@link org.mule.extensions.introspection.api.ExtensionConfiguration}
 * <p/>
 * It supports simple attributes, pojos, lists/sets of simple attributes, list/sets of beans,
 * and maps of simple attributes
 * <p/>
 * It the given config doesn't provide a name, then one will be automatically generated in order to register the config
 * in the {@link org.mule.api.registry.Registry}
 *
 * @since 3.7.0
 */
abstract class ExtensionConfigurationBeanDefinitionParser extends ExtensionBeanDefinitionParser
{

    private final Extension extension;
    private final ExtensionConfiguration configuration;

    public ExtensionConfigurationBeanDefinitionParser(Extension extension, ExtensionConfiguration configuration)
    {
        this.extension = extension;
        this.configuration = configuration;
    }

    @Override
    public final BeanDefinition parse(Element element, ParserContext parserContext)
    {
        parseConfigName(element);
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(getResolverClass());
        builder.setScope(BeanDefinition.SCOPE_SINGLETON);
        applyLifecycle(builder, configuration.getDeclaringClass());

        doParse(element, builder, parserContext);

        BeanDefinition definition = builder.getBeanDefinition();
        setNoRecurseOnDefinition(definition);

        return definition;
    }

    protected abstract Class<? extends ValueResolver> getResolverClass();

    protected abstract void doParse(Element element, BeanDefinitionBuilder builder, ParserContext parserContext);

    protected ObjectBuilder getObjectBuilder(Element element) {
        ObjectBuilder builder = new DefaultObjectBuilder();
        builder.setPrototypeClass(configuration.getDeclaringClass());

        for (ExtensionParameter parameter : configuration.getParameters())
        {
            builder.addProperty(parameter, parseParameter(element, parameter));
        }

        return builder;
    }

    private void parseConfigName(Element element)
    {
        if (hasAttribute(element, "name"))
        {
            element.setAttribute("name", AutoIdUtils.getUniqueName(element, "mule-bean"));
        }
    }
}
