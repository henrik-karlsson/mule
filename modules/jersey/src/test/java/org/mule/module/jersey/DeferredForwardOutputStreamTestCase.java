/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.jersey;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.tck.size.SmallTest;

import java.io.OutputStream;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@SmallTest
@RunWith(MockitoJUnitRunner.class)
public class DeferredForwardOutputStreamTestCase extends AbstractMuleTestCase
{

    private DeferredForwardOutputStream outputStream;
    private byte[] buffer;
    private int len;
    private int off;

    @Mock
    private OutputStream delegate;

    @Before
    public void before()
    {
        outputStream = new DeferredForwardOutputStream();
        buffer = RandomStringUtils.randomAlphabetic(100).getBytes();
        len = buffer.length;
        off = 0;
    }

    @Test
    public void deferredAfterWrite() throws Exception
    {
        outputStream.write(buffer, off, len);
        outputStream.write(buffer, off, len);

        outputStream.setDelegate(delegate);

        verify(delegate, times(2)).write(buffer, off, len);
    }

    @Test
    public void deferredMixed() throws Exception
    {
        outputStream.write(buffer, off, len);
        outputStream.setDelegate(delegate);
        verify(delegate).write(buffer, off, len);

        reset(delegate);
        outputStream.write(buffer, off, len);
        verify(delegate).write(buffer, off, len);
    }

    @Test
    public void notDeferred() throws Exception
    {
        outputStream.setDelegate(delegate);

        outputStream.write(buffer, off, len);
        outputStream.write(buffer, off, len);

        verify(delegate, times(2)).write(buffer, off, len);
    }

    @Test(expected = IllegalStateException.class)
    public void setDelegateTwice() throws Exception
    {
        outputStream.setDelegate(delegate);
        outputStream.setDelegate(delegate);
    }

    @Test(expected = NullPointerException.class)
    public void nullBuffer() throws Exception
    {
        outputStream.write(null, off, len);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void invalidArgs() throws Exception
    {
        outputStream.write(buffer, len * 2, off);
    }

    @Test
    public void noLength() throws Exception
    {
        buffer = new byte[] {};
        len = 0;
        off = 0;

        outputStream.write(buffer, off, len);
        verify(delegate, never()).write(buffer, off, len);
    }
}
