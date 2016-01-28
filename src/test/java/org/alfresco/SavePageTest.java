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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.selenium.PageCaptureException;
import org.alfresco.selenium.SavePageUtil;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;


/**
 * Tests the functions in SavePage.
 * @author Michael Suzuki
 * 
 */
public  class SavePageTest
{
    private static WebDriver driver;
    String test = "<head>"
            + "<script type=\"text/javascript\" charset=\"utf-8\" src=\"/share/res/js/surf/c8f13f59e45dc75460cd02ba9aab801e.js\"></script>"
            + "<script type=\"text/javascript\" charset=\"utf-8\" src=\"/test.js\"></script>"
            + "<style type=\"text/css\" media=\"screen\">"
            + "@import url(\"/share/res/css/yui-fonts-grids_fe8fbe97553ea9e004731970a95a499b.css\");"
            + "@import url(\"/share/res/yui/columnbrowser/assets/columnbrowser_a6ca750d53c6b6c201614545f6c33ee7.css\");"
            + "</style>"
            + "<link rel=\"stylesheet\" type=\"text/css\" href=\"/css/alfresco.css\">"
            + "</head>"
            + "<body> <div>"
            + "<a href=\"link/home.html\"></a>"
            + "<img src=\"/share/res/components/images/lightbox/loading.gif\">"
            + "</div></body>";
            
    protected final static String TEST_URL = "http://localhost:8080/share";
    @BeforeClass
    public static void setup()
    {
        driver = new FirefoxDriver();
        driver.navigate().to(TEST_URL);
        WebElement input = driver.findElement(By.name("username"));
        input.sendKeys("admin");
        WebElement password = driver.findElement(By.name("password"));
        password.sendKeys("admin");
        WebElement button = driver.findElement(By.tagName("button"));
        button.click();
        WebElement searchInput = driver.findElement(By.id("HEADER_SEARCHBOX_FORM_FIELD"));
        searchInput.sendKeys("ipsum \r\n");
        
    }
    @AfterClass
    public static void clean()
    {
        driver.quit();
    }
    @Test
    public void extractNoContent() throws PageCaptureException, IOException
    {
        String test = "<head></head>";
        List<String> list = SavePageUtil.extractFiles(test);
        Assert.assertEquals(0, list.size());
    }
    @Test
    public void extractCSS() throws PageCaptureException, IOException
    {
        List<String> list = SavePageUtil.extractFiles(test);
        Assert.assertEquals(6, list.size());
        Assert.assertEquals("/share/res/js/surf/c8f13f59e45dc75460cd02ba9aab801e.js", list.get(0));
        Assert.assertEquals("/test.js", list.get(1));
        Assert.assertEquals("/share/res/components/images/lightbox/loading.gif", list.get(2));
        Assert.assertEquals("/share/res/css/yui-fonts-grids_fe8fbe97553ea9e004731970a95a499b.css", list.get(3));
        Assert.assertEquals("/share/res/yui/columnbrowser/assets/columnbrowser_a6ca750d53c6b6c201614545f6c33ee7.css", list.get(4));
        Assert.assertEquals("/css/alfresco.css", list.get(5));
    }
    @Test
    public void parseURL() 
    {
        List<String> files = SavePageUtil.extractFiles(test);
        List<URL> urls = SavePageUtil.parseURL(files, "http://localhost:8080/");
        Assert.assertEquals(6, urls.size());
        List<URL> urls2 = SavePageUtil.parseURL(files, "http://localhost:8080/main");
        Assert.assertEquals(6, urls2.size());
        List<URL> urlsWithNoBase = SavePageUtil.parseURL(files, "");
        Assert.assertEquals(0, urlsWithNoBase.size());
        List<URL> urlsWithull = SavePageUtil.parseURL(files, null);
        Assert.assertEquals(0, urlsWithull.size());
        List<URL> nolists = SavePageUtil.parseURL(Collections.emptyList(), "http://localhost:8080/main");
        Assert.assertEquals(0, nolists.size());
        List<URL> nullList = SavePageUtil.parseURL(null, "http://localhost:8080/main");
        Assert.assertEquals(0, nullList.size());
    }
    @Test
    public void getFile() throws MalformedURLException
    {
        new File("./target/content/").mkdirs();
        
        List<URL> files = new ArrayList<URL>();
        files.add(new URL("https://cdn-www.alfresco.com/sites/www.alfresco.com/files//advagg_css/css__YZMmyCjxADNsxWJVyzxskiYBiPsGboww8DDJoAv1iVA__PqGVjSeXe3e-YM4xspxCavDlyydtEB28TRpZPTEwV5I__-BEWX4mtkwr1skpja3HlI8KN54EGjkcptZCT0YQ6Cjw.css"));
        SavePageUtil.getFiles(files, "./target/content/");
    }
    @Test
    public void parseHTML()
    {
        List<String> files = SavePageUtil.extractFiles(test);
        String html = SavePageUtil.parseHtml(test, files);
        Assert.assertNotEquals(test, html);
    }
    
    @Test
    public void saveFile() throws PageCaptureException, IOException
    {
        
        SavePageUtil.save(driver, "mytest.html");
    }
}
