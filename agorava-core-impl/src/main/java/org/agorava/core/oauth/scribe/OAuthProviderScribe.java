/*
 * Copyright 2013 Agorava
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.agorava.core.oauth.scribe;

import org.agorava.core.api.ApplyQualifier;
import org.agorava.core.api.GenericRoot;
import org.agorava.core.api.Injectable;
import org.agorava.core.api.exception.AgoravaException;
import org.agorava.core.api.oauth.*;
import org.agorava.core.api.rest.RestVerb;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Api;
import org.scribe.model.Token;
import org.scribe.model.Verifier;

import javax.annotation.PostConstruct;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@inheritDoc}
 *
 * @author Antoine Sabot-Durand
 */
@ApplyQualifier
@GenericRoot
public class OAuthProviderScribe implements OAuthProvider {

    private static final String SCRIBE_API_PREFIX = "org.scribe.builder.api.";
    private static final String SCRIBE_API_SUFFIX = "Api";
    private static final String API_CLASS = "apiClass";
    private static Logger logger = Logger.getLogger(OAuthProviderScribe.class.getName());
    @ApplyQualifier
    @Injectable
    OAuthAppSettings settings;
    @ApplyQualifier
    @Injectable
    OAuthApi api;
    private org.scribe.oauth.OAuthService service;

    org.scribe.oauth.OAuthService getService() {
        if (service == null)
            throw new IllegalStateException("OAuthProvider can be used before it is initialized");
        return service;
    }

    @Override
    public OAuthToken getRequestToken() {
        return new OAuthTokenScribe("1.0".equals(getVersion()) ? getService().getRequestToken() : null);
    }

    @Override
    public OAuthToken getAccessToken(OAuthToken requestToken, String verifier) {
        // TODO: catch exception from Scribe if the remote service is unavailabe
        return createToken(getService().getAccessToken(extractToken(requestToken), new Verifier(verifier)));
    }

    @Override
    public void signRequest(OAuthToken accessToken, OAuthRequest request) {
        getService().signRequest(extractToken(accessToken), ((OAuthRequestScribe) request).getDelegate());
    }

    @Override
    public String getVersion() {
        return api.getOAuthVersion();
    }

    @Override
    public String getAuthorizationUrl(OAuthToken tok) {
        return getService().getAuthorizationUrl(extractToken(tok));
    }

    @PostConstruct
    public void init() {
        Class<? extends Api> apiClass = getApiClass(settings.getSocialMediaName());
        ServiceBuilder serviceBuilder = new ServiceBuilder().provider(apiClass).apiKey(settings.getApiKey())
                .apiSecret(settings.getApiSecret());
        if (settings.getCallback() != null && !("".equals(settings.getCallback())))
            serviceBuilder.callback(settings.getCallback());
        if (settings.getScope() != null && !("".equals(settings.getScope()))) {
            serviceBuilder.scope(settings.getScope());
        }
        service = serviceBuilder.build();
    }

    @Override
    public String getVerifierParamName() {
        return "1.0".equals(getVersion()) ? OAuthConstants.VERIFIER : OAuthConstants.CODE;
    }

    /**
     * This methods tries to get Scribe {@link Api} class from Social Media Name
     * It tries first to open a bundle with the provided name to look for an apiClass key.
     * <p/>
     * If it doesn't found this bundle or key it will try to build class name with the scribe package name for API as prefix
     * and Api as suffix.
     * <p/>
     * If none of the above solution works the method throws an exception
     *
     * @param serviceName name of the Social Media
     * @return Scribe API class
     * @throws AgoravaException if the class cannot be found
     */
    @SuppressWarnings("unchecked")
    protected Class<? extends Api> getApiClass(String serviceName) {
        String className;

        try {
            ResourceBundle rb = ResourceBundle.getBundle(serviceName);
            className = rb.getString(API_CLASS);
        } catch (MissingResourceException e) {
            logger.log(Level.INFO, "Found no bundle for service {0}", serviceName);
            className = SCRIBE_API_PREFIX + serviceName + SCRIBE_API_SUFFIX;
        }
        try {
            return (Class<? extends Api>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new AgoravaException("There no such Scribe Api class " + className, e);
        }
    }

    private Token extractToken(OAuthToken tok) {
        return ((OAuthTokenScribe) tok).delegate;
    }

    @Override
    public OAuthRequest requestFactory(RestVerb verb, String uri) {
        return new OAuthRequestScribe(verb, uri);
    }

    @Override
    public OAuthToken tokenFactory(String token, String secret) {
        return new OAuthTokenScribe(token, secret);
    }

    private OAuthToken createToken(Token token) {
        return new OAuthTokenScribe(token);
    }
}
