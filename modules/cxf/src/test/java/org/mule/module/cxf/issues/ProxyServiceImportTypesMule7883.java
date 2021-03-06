/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.cxf.issues;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.tck.junit4.FunctionalTestCase;
import org.mule.tck.junit4.rule.DynamicPort;
import org.mule.transport.NullPayload;

import java.io.StringReader;
import java.util.List;

import org.apache.cxf.helpers.DOMUtils;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class ProxyServiceImportTypesMule7883 extends FunctionalTestCase
{

    @Rule
    public final DynamicPort httpPort = new DynamicPort("port1");

    @Override
    protected String getConfigFile()
    {
        return "issues/proxy-wsdl-import-conf.xml";
    }

    @Test
    public void testWsdlImportType() throws Exception
    {
        testImportType("?wsdl", "?wsdl=TestTypeDefinitions.wsdl", "wsdl", "location");
    }

    @Test
    public void testXsdImportType() throws Exception
    {
        testImportType("?wsdl=TestTypeDefinitions.wsdl", "?xsd=TestSchema.xsd", "xsd", "schemaLocation");
    }

    private void testImportType(String targetUrl, String expectedImportUrl, String importType, String locationAttributeName) throws Exception
    {
        String proxyAddress = "http://localhost:" + httpPort.getNumber() + "/test";
        MuleMessage response = muleContext.getClient().send(proxyAddress + targetUrl, new DefaultMuleMessage(NullPayload.getInstance(), muleContext));

        Document wsdl = XMLUnit.buildTestDocument(new InputSource(new StringReader(response.getPayloadAsString())));
        List<Element> imports = DOMUtils.findAllElementsByTagName(wsdl.getDocumentElement(), importType + ":import");

        assertThat(imports.get(0).getAttribute(locationAttributeName), is(proxyAddress + expectedImportUrl));
    }
}
