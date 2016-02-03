/*
 * Copyright (C) 2005-2016 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.selenium;

import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

/**
 * Fetch HttpClient factory, provides an Http client
 * with user session taken from the {@link WebDriver}
 *
 * @author Michael Suzuki
 */
public class FetchHttpClient
{
    public static CloseableHttpClient getHttpClient(final WebDriver driver)
    {
        BasicCookieStore cookieStore = new BasicCookieStore();
        cookieStore.addCookie(generateSessionCookie(driver));
        //Create http client to retrieve the file.
        return HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
    }
    /**
     * Prepare the client cookie based on the authenticated {@link WebDriver} 
     * cookie. 
     * @param driver {@link WebDriver}
     * @return BasicClientCookie with correct credentials.
     */
    public static BasicClientCookie generateSessionCookie(WebDriver driver)
    {
        Cookie originalCookie = driver.manage().getCookieNamed("JSESSIONID");
        if (originalCookie == null) 
        {
            return null;
        }
        // just build new apache-like cookie based on webDriver's one
        String cookieName = originalCookie.getName();
        String cookieValue = originalCookie.getValue();
        BasicClientCookie resultCookie = new BasicClientCookie(cookieName, cookieValue);
        resultCookie.setDomain(originalCookie.getDomain());
        resultCookie.setExpiryDate(originalCookie.getExpiry());
        resultCookie.setPath(originalCookie.getPath());
        return resultCookie;
    }
}
