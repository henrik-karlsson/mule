/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.runtime;

import static org.mule.module.extensions.internal.util.IntrospectionUtils.checkInstantiable;
import static org.mule.module.extensions.internal.util.IntrospectionUtils.getSetter;
import static org.mule.util.ClassUtils.instanciateClass;
import static org.mule.util.Preconditions.checkArgument;
import org.mule.api.MuleEvent;
import org.mule.extensions.introspection.api.ExtensionParameter;
import org.mule.module.extensions.internal.runtime.resolver.ValueResolver;
import org.mule.repackaged.internal.org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class DefaultObjectBuilder implements ObjectBuilder
{

    private Class<?> prototypeClass;
    private final Map<Method, ValueResolver> properties = new HashMap<>();

    @Override
    public ObjectBuilder setPrototypeClass(Class<?> prototypeClass)
    {
        checkInstantiable(prototypeClass);
        this.prototypeClass = prototypeClass;

        return this;
    }

    @Override
    public ObjectBuilder addProperty(ExtensionParameter parameter, ValueResolver resolver)
    {
        checkArgument(parameter != null, "parameter cannot be null");

        return addProperty(getSetter(parameter.getType().getRawType(), parameter), resolver);
    }

    @Override
    public ObjectBuilder addProperty(Method method, ValueResolver resolver)
    {
        checkArgument(method != null, "method cannot be null");
        checkArgument(resolver != null, "resolver cannot be null");

        properties.put(method, resolver);
        return this;
    }

    @Override
    public Object build(MuleEvent event) throws Exception
    {
        Object object = instanciateClass(prototypeClass);

        for (Map.Entry<Method, ValueResolver> entry : properties.entrySet())
        {
            ReflectionUtils.invokeMethod(entry.getKey(), object, entry.getValue().resolve(event));
        }

        return object;
    }
}
