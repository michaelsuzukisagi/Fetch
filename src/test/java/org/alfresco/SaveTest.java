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

import org.alfresco.selenium.SavePageUtil;
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
public  class SaveTest
{
   private static WebDriver driver;
    @BeforeClass
    public static void setup()
    {
        driver = new FirefoxDriver();
        driver.navigate().to(SavePageTest.SHARE_TEST_URL);
    }
    
    @AfterClass
    public static void clean()
    {
        driver.quit();
    }
    
    @Test
    public void saveFile() throws IOException
    {
        WebElement input = driver.findElement(By.name("username"));
        input.sendKeys("admin");
        WebElement password = driver.findElement(By.name("password"));
        password.sendKeys("admin");
        WebElement button = driver.findElement(By.tagName("button"));
        button.click();
        WebElement searchInput = driver.findElement(By.id("HEADER_SEARCHBOX_FORM_FIELD"));
        searchInput.sendKeys("ipsum \r\n");
        findAndWait(By.id("FCTSRCH_RESULTS_COUNT_LABEL"), 3000, 100);
        SavePageUtil.save(driver, "mytest.html");
    }
    
//    @Test
//    public void saveAlfresco() throws IOException
//    {
//        driver.navigate().to(SavePageTest.ALFRESCO_TEST_URL);
//        SavePageUtil.save(driver, "alfresco.html");
//    }
//  
//    @Test
//    public void saveGoogle() throws IOException
//    {
//        driver.navigate().to("http://www.google.com");
//        SavePageUtil.save(driver, "google.html");
//    }
//    @Test
//    public void saveGitHub() throws IOException
//    {
//        driver.navigate().to("http://www.github.com");
//        SavePageUtil.save(driver, "github.html");
//    }
    public WebElement findAndWait(final By by, final long limit, final long interval)
    {
        FluentWait<By> fluentWait = new FluentWait<By>(by);
        fluentWait.pollingEvery(interval, TimeUnit.MILLISECONDS);
        fluentWait.withTimeout(limit, TimeUnit.MILLISECONDS);
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
