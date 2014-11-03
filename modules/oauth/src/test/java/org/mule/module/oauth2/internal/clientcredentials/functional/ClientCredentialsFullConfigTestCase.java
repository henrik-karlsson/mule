/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.oauth2.internal.clientcredentials.functional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

import org.mule.construct.Flow;
import org.mule.module.http.HttpHeaders;
import org.mule.module.http.ParameterMap;
import org.mule.module.oauth2.AbstractOAuthAuthorizationTestCase;
import org.mule.module.oauth2.asserter.OAuthContextFunctionAsserter;
import org.mule.module.oauth2.internal.authorizationcode.OAuthAuthenticationHeader;
import org.mule.tck.junit4.rule.SystemProperty;

import org.junit.Rule;
import org.junit.Test;

public class ClientCredentialsFullConfigTestCase extends AbstractOAuthAuthorizationTestCase
{

    private final String CUSTOM_RESPONSE_PARAMETER1_VALUE = "token-resp-value1";
    private final String CUSTOM_RESPONSE_PARAMETER2_VALUE = "token-resp-value2";
    private static final String RESOURCE_PATH = "/resource";
    private static final String NEW_ACCESS_TOKEN = "abcdefghjkl";
    @Rule
    public SystemProperty tokenUrl = new SystemProperty("token.url", String.format("http://localhost:%d" + TOKEN_PATH, oauthServerPort.getNumber()));
    @Rule
    public SystemProperty customTokenResponseParameter1Name = new SystemProperty("custom.param.extractor1", "token-resp-param1");
    @Rule
    public SystemProperty customTokenResponseParameter2Name = new SystemProperty("custom.param.extractor2", "token-resp-param2");


    @Override
    protected String getConfigFile()
    {
        return "client-credentials/client-credentials-full-config.xml";
    }

    @Override
    protected void doSetUpBeforeMuleContextCreation() throws Exception
    {
        final ParameterMap customTokenResponseParameters = new ParameterMap()
                .putAndReturn(customTokenResponseParameter1Name.getValue(), CUSTOM_RESPONSE_PARAMETER1_VALUE)
                .putAndReturn(customTokenResponseParameter2Name.getValue(), CUSTOM_RESPONSE_PARAMETER2_VALUE);
        configureWireMockToExpectTokenPathRequestForClientCredentialsGrantTypeWithMapResponse(customTokenResponseParameters);
    }

    @Test
    public void authenticationIsDoneOnStartupUsingScope() throws Exception
    {
        verifyRequestDoneToTokenUrlForClientCredentials(scopes.getValue());
    }

    @Test
    public void customTokenResponseParametersAreCaptured() throws Exception
    {
        final OAuthContextFunctionAsserter oauthContextAsserter = OAuthContextFunctionAsserter.createFrom(muleContext.getExpressionLanguage(), "clientCredentialsConfig");
        oauthContextAsserter.assertAccessTokenIs(ACCESS_TOKEN);
        oauthContextAsserter.assertExpiresInIs(EXPIRES_IN);
        oauthContextAsserter.assertContainsCustomTokenResponseParam(customTokenResponseParameter1Name.getValue(), CUSTOM_RESPONSE_PARAMETER1_VALUE);
        oauthContextAsserter.assertContainsCustomTokenResponseParam(customTokenResponseParameter2Name.getValue(), CUSTOM_RESPONSE_PARAMETER2_VALUE);
    }

    @Test
    public void authenticationFailedTriggersRefreshAccessToken() throws Exception
    {
        configureWireMockToExpectTokenPathRequestForClientCredentialsGrantTypeWithMapResponse(NEW_ACCESS_TOKEN);

        wireMockRule.stubFor(post(urlEqualTo(RESOURCE_PATH))
                                     .withHeader(HttpHeaders.Names.AUTHORIZATION, containing(ACCESS_TOKEN))
                                     .willReturn(aResponse()
                                                         .withStatus(500).withHeader(HttpHeaders.Names.WWW_AUTHENTICATE, "Basic realm=\"myRealm\"")));

        wireMockRule.stubFor(post(urlEqualTo(RESOURCE_PATH))
                                     .withHeader(HttpHeaders.Names.AUTHORIZATION, containing(NEW_ACCESS_TOKEN))
                                     .willReturn(aResponse()
                                                         .withBody(TEST_MESSAGE)
                                                         .withStatus(200)));

        Flow testFlow = (Flow) getFlowConstruct("testFlow");
        testFlow.process(getTestEvent(TEST_MESSAGE));

        verifyRequestDoneToTokenUrlForClientCredentials();

        wireMockRule.verify(postRequestedFor(urlEqualTo(RESOURCE_PATH))
                                    .withHeader(HttpHeaders.Names.AUTHORIZATION, equalTo(OAuthAuthenticationHeader.buildAuthorizationHeaderContent(NEW_ACCESS_TOKEN))));
    }
}
