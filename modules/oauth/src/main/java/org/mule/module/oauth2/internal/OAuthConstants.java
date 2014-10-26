/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.oauth2.internal;

public interface OAuthConstants
{

    //Parameters
    String GRANT_TYPE_PARAMETER = "grant_type";
    String REDIRECT_URI_PARAMETER = "redirect_uri";
    String CLIENT_SECRET_PARAMETER = "client_secret";
    String CODE_PARAMETER = "code";
    String CLIENT_ID_PARAMETER = "client_id";
    String ACCESS_TOKEN_PARAMETER = "access_token";
    String EXPIRES_IN_PARAMETER = "expires_in";
    String REFRESH_TOKEN_PARAMETER = "refresh_token";
    String STATE_PARAMETER = "state";

    //Values
    String GRANT_TYPE_AUTHENTICATION_CODE = "authorization_code";
    String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

    //Expressions to extract parameters from standard token url response.
    public final static String ACCESS_TOKEN_EXPRESSION = "#[regex('" + ".*\"access_token\"[ ]*:[ ]*\"([^\\\"]*)\".*" + "')]";
    public final static String REFRESH_TOKEN_EXPRESSION = "#[regex('" + ".*\"refresh_token\"[ ]*:[ ]*\"([^\\\"]*)\".*" + "')]";
    public final static String EXPIRATION_TIME_EXPRESSION = "#[regex('" + ".*\"expires_in\"[ ]*:[ ]*([\\\\d]*).*" + "')]";
}
