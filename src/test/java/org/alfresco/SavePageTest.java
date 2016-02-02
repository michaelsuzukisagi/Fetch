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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.alfresco.selenium.SavePageUtil;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;


/**
 * Tests the save function against different web sites.
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
            +"<img class=\"alfresco-renderers-Thumbnail__image\" data-dojo-attach-point=\"imgNode\" id=\"workspace://SpacesStore/99cb2789-f67e-41ff-bea9-505c138a6b23\" src=\"http://localhost:8080/share/proxy/alfresco/api/node/workspace/SpacesStore/99cb2789-f67e-41ff-bea9-505c138a6b23/content/thumbnails/doclib/?c=queue&amp;ph=true&amp;lastModified=2011-03-03T10:31:31.651Z\" alt=\".ppt\" title=\"Project Overview.ppt\" data-dojo-attach-event=\"onload:getNaturalImageSize\">"
            + "</div></body>";
            
    protected final static String ALFRESCO_TEST_URL = "http://localhost:8080/alfresco";
    protected final static String SHARE_TEST_URL = "http://localhost:8080/share";
    
    @BeforeClass
    public static void setup()
    {
        driver = new FirefoxDriver();
        driver.navigate().to(SHARE_TEST_URL);
    }
    
    @AfterClass
    public static void clean()
    {
        driver.quit();
    }
    
    @Test
    public void extractNoContent() throws IOException
    {
        String test = "<head></head>";
        List<String> list = SavePageUtil.extractFiles(test);
        Assert.assertEquals(0, list.size());
    }
    
    @Test(expected = RuntimeException.class)
    public void extractNullFiles() throws IOException
    {
        SavePageUtil.extractFiles(null);
    }
    
    @Test(expected = RuntimeException.class)
    public void extractEmptyFiles() throws IOException
    {
        SavePageUtil.extractFiles("");
    }
    
    @Test
    public void extractFiles() throws IOException
    {
        List<String> list = SavePageUtil.extractFiles(test);
        Assert.assertEquals(7, list.size());
        Assert.assertEquals("/share/res/js/surf/c8f13f59e45dc75460cd02ba9aab801e.js", list.get(0));
        Assert.assertEquals("/test.js", list.get(1));
        Assert.assertEquals("/share/res/components/images/lightbox/loading.gif", list.get(2));
        Assert.assertEquals("http://localhost:8080/share/proxy/alfresco/api/node/workspace/SpacesStore/99cb2789-f67e-41ff-bea9-505c138a6b23/content/thumbnails/doclib/?c=queue&amp;ph=true&amp;lastModified=2011-03-03T10:31:31.651Z", list.get(3));
        Assert.assertEquals("/share/res/css/yui-fonts-grids_fe8fbe97553ea9e004731970a95a499b.css", list.get(4));
        Assert.assertEquals("/share/res/yui/columnbrowser/assets/columnbrowser_a6ca750d53c6b6c201614545f6c33ee7.css", list.get(5));
        Assert.assertEquals("/css/alfresco.css", list.get(6));
    } 
    
    @Test
    public void parseNullURL() 
    {
        List<URL> nullList = SavePageUtil.parseURL(null, "http://localhost:8080/main", "http://localhost:8080");
        Assert.assertEquals(0, nullList.size());
    }
    
    @Test
    public void parseEmptyURL() 
    {
        List<URL> nolists = SavePageUtil.parseURL(Collections.emptyList(), "http://localhost:8080/main", "http://localhost:8080");
        Assert.assertEquals(0, nolists.size());
    }
    
    @Test
    public void parseURL() 
    {
        List<String> files = SavePageUtil.extractFiles(test);
        List<URL> urls = SavePageUtil.parseURL(files, "http://localhost:8080/", "http://localhost:8080");
        Assert.assertEquals(7, urls.size());
        Assert.assertEquals("http://localhost:8080/share/proxy/alfresco/api/node/workspace/SpacesStore/99cb2789-f67e-41ff-bea9-505c138a6b23/content/thumbnails/doclib/?c=queue&ph=true&lastModified=2011-03-03T10:31:31.651Z",
                urls.get(3).toString());

        List<URL> urls2 = SavePageUtil.parseURL(files, "http://localhost:8080/main", "http://localhost:8080");
        Assert.assertEquals(7, urls2.size());
        
        List<URL> nullList = SavePageUtil.parseURL(null, "http://localhost:8080/main", "http://localhost:8080");
        Assert.assertEquals(0, nullList.size());
    }
    
    @Test(expected = RuntimeException.class)
    public void parseNullDomain()
    {
        SavePageUtil.parseURL(null, null,"http://localhost:8080");
    }
    
    @Test(expected = RuntimeException.class)
    public void parseNullCurrentURL()
    {
        SavePageUtil.parseURL(null, "http://localhost:8080", null);
    }
    
    @Test(expected = RuntimeException.class)
    public void parseEmptyDomain()
    {
        SavePageUtil.parseURL(null, "","http://localhost:8080");
    }
    
    @Test(expected = RuntimeException.class)
    public void parseEmptyCurrentUrl()
    {
        SavePageUtil.parseURL(null, "http://localhost:8080", "");
    }
    
    @Test(expected = RuntimeException.class)
    public void getFilesNull() throws IOException
    {
        SavePageUtil.getFiles(null, driver);
    }
    
    @Test
    public void getFilesEmpty() throws IOException
    {
        SavePageUtil.getFiles(Collections.emptyList(), driver);
    }
    
    @Test
    public void getFiles() throws IOException
    {
        File folder = new File(SavePageUtil.ASSET_DIR);
        FileUtils.deleteDirectory(folder);
        folder.mkdirs();
        File[] filesInFolder = folder.listFiles();
        int start = filesInFolder.length;
        
        List<String> files = SavePageUtil.extractFiles(test);
        List<URL> urls = SavePageUtil.parseURL(files, "http://localhost:8080/", "http://localhost:8080/");
        SavePageUtil.getFiles(urls, driver);
        int end = folder.listFiles().length;
        Assert.assertNotEquals(start, end);
        Assert.assertEquals(7, end);
    }
    
    @Test
    public void getCDN() throws IOException
    {
        File folder = new File(SavePageUtil.ASSET_DIR);
        FileUtils.deleteDirectory(folder);
        folder.mkdirs();
        File[] filesInFolder = folder.listFiles();
        int start = filesInFolder.length;
        
        List<URL> files = new ArrayList<URL>();
        files.add(new URL("https://cdn-www.alfresco.com/sites/www.alfresco.com/files//advagg_css/css__YZMmyCjxADNsxWJVyzxskiYBiPsGboww8DDJoAv1iVA__PqGVjSeXe3e-YM4xspxCavDlyydtEB28TRpZPTEwV5I__-BEWX4mtkwr1skpja3HlI8KN54EGjkcptZCT0YQ6Cjw.css"));
        SavePageUtil.getFiles(files, driver);
        
        int end = folder.listFiles().length;
        Assert.assertNotEquals(start, end);
        Assert.assertEquals(1, end);
    }
    
    @Test(expected = RuntimeException.class)
    public void parseHTMLNull()
    {
        SavePageUtil.extractFiles(null);
    }
    
    @Test(expected = RuntimeException.class)
    public void parseHTMLEmpty()
    {
        SavePageUtil.extractFiles("");
    }
    
    @Test
    public void parseHTML()
    {
        List<String> files = SavePageUtil.extractFiles(test);
        String html = SavePageUtil.parseHtml(test, files);
        Assert.assertNotEquals(test, html);
    }
    
}
