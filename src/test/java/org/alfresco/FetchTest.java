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
package org.alfresco;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.alfresco.selenium.FetchUtil;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.FluentWait;

import com.google.common.base.Predicate;


/**
 * Tests the functions in SavePage.
 * @author Michael Suzuki
 * 
 */
public  class FetchTest
{
   private static WebDriver driver;
    @BeforeClass
    public static void setup()
    {
        driver = new FirefoxDriver();
        driver.navigate().to(FetchPageTest.SHARE_TEST_URL);
    }
    
    @AfterClass
    public static void clean()
    {
        driver.quit();
    }
//    
//    @Test
//    public void saveFile() throws IOException
//    {
//        WebElement input = driver.findElement(By.name("username"));
//        input.sendKeys("admin");
//        WebElement password = driver.findElement(By.name("password"));
//        password.sendKeys("admin");
//        WebElement button = driver.findElement(By.tagName("button"));
//        button.click();
//        WebElement searchInput = driver.findElement(By.id("HEADER_SEARCHBOX_FORM_FIELD"));
//        FetchUtil.save(driver, "dashboard.html");
//        searchInput.sendKeys("ipsum \r\n");
//        findAndWait(By.id("FCTSRCH_RESULTS_COUNT_LABEL"));
//        FetchUtil.save(driver, "faceted-search.html");
//        driver.navigate().to("http://localhost:8080/share/page/site/swsdp/document-details?nodeRef=workspace://SpacesStore/5515d3e1-bb2a-42ed-833c-52802a367033");
//        findAndWait(By.id("document-version"));
//        FetchUtil.save(driver, "document-details.html");
//        driver.navigate().to("http://localhost:8080/share/page/site/swsdp/documentlibrary");
//        findAndWait(By.id("HEADER_SITE_CONFIGURATION_DROPDOWN"));
//        FetchUtil.save(driver, "document-library.html");
//    }
//    
//    @Test
//    public void saveAlfresco() throws IOException
//    {
//        driver.navigate().to(FetchUtil.ALFRESCO_TEST_URL);
//        FetchUtil.save(driver, "alfresco.html");
//    }
    @Test
    public void saveAlfrescoHomePage() throws IOException
    {
        driver.navigate().to("http://www.alfresco.com");
        FetchUtil.save(driver, "alfresco-com.html");
    }
//    @Test
//    public void saveGoogle() throws IOException
//    {
//        driver.navigate().to("http://www.google.com");
//        FetchUtil.save(driver, "google.html");
//    }
//    @Test
//    public void saveBBC() throws IOException
//    {
//        driver.navigate().to("bbc");
//        FetchUtil.save(driver, "bbc.html");
//    }
//    @Test
//    public void saveGitHub() throws IOException
//    {
//        driver.navigate().to("http://www.github.com");
//        SavePageUtil.save(driver, "github.html");
//    }
    public WebElement findAndWait(final By by)
    {
        FluentWait<By> fluentWait = new FluentWait<By>(by);
        fluentWait.pollingEvery(100, TimeUnit.MILLISECONDS);
        fluentWait.withTimeout(5000, TimeUnit.MILLISECONDS);
        try
        {
            fluentWait.until(new Predicate<By>()
            {
                public boolean apply(By by)
                {
                    try
                    {
                        return driver.findElement(by).isDisplayed();
                    }
                    catch (NoSuchElementException ex)
                    {
                        return false;
                    }
                }
            });
            return driver.findElement(by);
        }
        catch (RuntimeException re)
        {
            throw new TimeoutException("Unable to locate element " + by);
        }
    }
}
