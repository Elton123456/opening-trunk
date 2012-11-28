/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 ForgeRock Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */

package org.forgerock.openig.filter;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.forgerock.openig.handler.GenericHandler;
import org.forgerock.openig.handler.HandlerException;
import org.forgerock.openig.header.LocationHeader;
import org.forgerock.openig.http.Exchange;
import org.forgerock.openig.http.Response;
import org.testng.annotations.Test;

/**
 * Test case for the RedirectFilter
 * 
 * @author Mark de Reeper
 */
public class RedirectFilterTest {

    @Test
    public void caseChangeSchemeHostAndPort() throws HandlerException, IOException, URISyntaxException {


        String expectedResult = "https://proxy.example.com:443/path/to/redirected?a=1&b=2";
        
        URI testRedirectionURI = new URI("http://app.example.com:8080/path/to/redirected?a=1&b=2");

        RedirectFilter filter = new RedirectFilter();
        filter.baseURI = new URI("https://proxy.example.com:443/");

        callFilter(filter, testRedirectionURI, expectedResult);
    }

    @Test
    public void caseChangeHost() throws HandlerException, IOException, URISyntaxException {


        String expectedResult = "http://proxy.example.com:8080/path/to/redirected?a=1&b=2";

        URI testRedirectionURI = new URI("http://app.example.com:8080/path/to/redirected?a=1&b=2");

        RedirectFilter filter = new RedirectFilter();
        filter.baseURI = new URI("http://proxy.example.com:8080/");

        callFilter(filter, testRedirectionURI, expectedResult);
    }

    @Test
    public void caseChangePort() throws HandlerException, IOException, URISyntaxException {


        String expectedResult = "http://app.example.com:9090/path/to/redirected?a=1&b=2";

        URI testRedirectionURI = new URI("http://app.example.com:8080/path/to/redirected?a=1&b=2");

        RedirectFilter filter = new RedirectFilter();
        filter.baseURI = new URI("http://app.example.com:9090/");

        callFilter(filter, testRedirectionURI, expectedResult);
    }

    @Test
    public void caseChangeScheme() throws HandlerException, IOException, URISyntaxException {


        String expectedResult = "https://app.example.com/path/to/redirected?a=1&b=2";

        URI testRedirectionURI = new URI("http://app.example.com/path/to/redirected?a=1&b=2");

        RedirectFilter filter = new RedirectFilter();
        filter.baseURI = new URI("https://app.example.com/");

        callFilter(filter, testRedirectionURI, expectedResult);
    }

    @Test
    public void caseNoChange() throws HandlerException, IOException, URISyntaxException {

        String expectedResult = "http://app.example.com:8080/path/to/redirected?a=1&b=2";

        URI testRedirectionURI = new URI("http://app.example.com:8080/path/to/redirected?a=1&b=2");

        RedirectFilter filter = new RedirectFilter();
        filter.baseURI = new URI("http://app.example.com:8080/");
        
        callFilter(filter, testRedirectionURI, expectedResult);
    }

    private void callFilter(RedirectFilter filter, URI testRedirectionURI, String expectedResult) throws IOException, HandlerException {

        Exchange exchange = new Exchange();
        exchange.response = new Response();
        exchange.response.headers.add(LocationHeader.NAME, testRedirectionURI.toString());
        exchange.response.status = RedirectFilter.REDIRECT_STATUS_302;

        DummyHander handler = new DummyHander();

        filter.filter(exchange, handler);

        LocationHeader header = new LocationHeader(exchange.response);
        org.fest.assertions.Assertions.assertThat(header.toString()).isNotNull();
        org.fest.assertions.Assertions.assertThat(expectedResult.equals(header.toString())).isTrue();
    }

    private class DummyHander extends GenericHandler {

        @Override
        public void handle(Exchange exchange) throws HandlerException, IOException {
        }
    }
}
