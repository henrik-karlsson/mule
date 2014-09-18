/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.extensions.internal.resources;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertSame;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mule.api.config.ServiceRegistry;
import org.mule.extensions.introspection.api.Extension;
import org.mule.extensions.resources.api.GenerableResource;
import org.mule.extensions.resources.api.ResourcesGenerator;
import org.mule.extensions.resources.spi.GenerableResourceContributor;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

@SmallTest
public abstract class ResourcesGeneratorContractTestCase extends AbstractMuleTestCase
{

    protected ResourcesGenerator generator;

    @Before
    public void before()
    {
        generator = buildGenerator();
    }

    protected abstract ResourcesGenerator buildGenerator();

    @Test
    public void getOrCreateResource()
    {
        final String filepath = "path";
        GenerableResource resource = generator.getOrCreateResource(filepath);

        assertNotNull(resource);
        assertEquals(filepath, resource.getFilePath());
        assertNotNull(resource.getContentBuilder());

        assertSame(resource, generator.getOrCreateResource(filepath));
    }

    @Test
    public void generate()
    {
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        GenerableResourceContributor contributor = mock(GenerableResourceContributor.class);
        when(serviceRegistry.lookupProviders(same(GenerableResourceContributor.class), any(ClassLoader.class)))
                .thenReturn(Arrays.asList(contributor).iterator());

        Extension extension = mock(Extension.class);

        generator.setServiceRegistry(serviceRegistry);
        generator.generateFor(extension);

        verify(contributor).contribute(extension, generator);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullServiceRegistry()
    {
        generator.setServiceRegistry(null);
    }
}