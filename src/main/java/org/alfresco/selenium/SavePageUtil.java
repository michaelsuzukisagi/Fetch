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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * Utility that captures the page for offline use.
 * An alternative to screen shot which allows further inspection of code changes.
 * The implementation class is responsible for storing the html along with 
 * images, js and css.
 * 
 * The proposed default storage structure is:
 * page-nth.html
 * img directory for all images.
 * css style sheets.
 * js  all javascripts imported by the page.
 * <p>
 * Example usage:
 * SavePageUtil.save(driver, name.html)
 * <pre>
 *
 * boolean saved = SavePageUtil.save(driver);
 * </pre>
 *
 * @author Michael Suzuki
 */
public class SavePageUtil
{
    private final static Log logger = LogFactory.getLog(SavePageUtil.class);
    private static final String GET_BASE_URL_JS_COMMAND = "return document.location.origin;";
    private static final String URL_PATH_SEPARATOR = "/";
    public static final String OUTPUT_DIR = "./target/public/";
    public static final String ASSET_FOLDER =  "content/";
    public static final String ASSET_DIR = OUTPUT_DIR + ASSET_FOLDER;
    /* regex to locate and extract source of all assets */
    private static final Pattern SRC_PATTERN = Pattern.compile("(?<=src=\")[^\"]*(?<!\")");
    private static final Pattern CSS_PATTERN = Pattern.compile("(?<=url\\(\").*?(?=\"\\))");
    private static final Pattern CSS_LINK_PATTERN = Pattern.compile("<link.*?\\>");
    private static final Pattern HREF_PATTERN = Pattern.compile("(?<=href=\").*?(?=\")");
    //Required to handle strange characters on the page.
    private static final String UTF8_HTML = "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">";
    /**
     * Saves the current page as seen by the WebDriver
     * @param driver {@link WebDriver}
     * @param filename name of the HTML file output
     * @throws PageCaptureException if error
     * @throws IOException
     */
    public static void save(final WebDriver driver,final String filename) throws IOException 
    {
        String sourceHtml = driver.getPageSource();
        String currentUrl = driver.getCurrentUrl();
        String host = (String)((JavascriptExecutor) driver).executeScript(GET_BASE_URL_JS_COMMAND);
        //download all assets: js,img and stylesheet.
        List<String> files = extractFiles(sourceHtml);
        List<URL> urls = parseURL(files, host, currentUrl); 
        getFiles(urls, driver);
        String html = parseHtml(sourceHtml, files);
        File file = new File(OUTPUT_DIR + filename);
        file.delete();
        FileUtils.writeStringToFile(file, html);
    }
    
    /**
     * Extract source location of all files related to HTML.
     * @param html
     * @return List of all source locations relating to js, css and images
     */
    public static List<String> extractFiles(final String html)
    {
        if(html == null || html.isEmpty())
        {
            throw new RuntimeException("HTML source input required");
        }
        List<String> list = new ArrayList<String>();
        //Find all src=
        Matcher matchSrc = SRC_PATTERN.matcher(html);
        while (matchSrc.find()) 
        {
            String i = matchSrc.group(0);
            list.add(i);
        } 
        //find all url('')
        Matcher css = CSS_PATTERN.matcher(html);
        while (css.find()) 
        {
            list.add(css.group(0));
        } 
        //Find all <link href="">
        Matcher css2 = CSS_LINK_PATTERN.matcher(html);
        while (css2.find()) 
        {
            Matcher href = HREF_PATTERN.matcher(css2.group(0));
            if (href.find())
            {
                list.add(href.group(0));
            }
        } 
        return list;
    }
    
    /**
     * Parses file paths to URLs.
     * @param files collection of paths
     * @param baseUrl Site domain URL, http://localhost:8080
     * @param currentURL the driver.getCurrentUrl() value 
     * @return Collection of URL
     */
    public static List<URL> parseURL(List<String> files,final String baseUrl, final String currentURL)
    {
        if(null == baseUrl || baseUrl.isEmpty())
        {
            throw new RuntimeException("Site domain url is required");
        }
        if(null == currentURL || currentURL.isEmpty())
        {
            throw new RuntimeException("Current WebDriver url is required");
        }
        if(files == null || files.isEmpty())
        {
            return Collections.emptyList();
        }
        //Strip tail "/"
        String base = StringUtils.removeEnd(baseUrl, URL_PATH_SEPARATOR);
        List<URL> urls = new ArrayList<URL>();
        for(String url : files)
        {
            //replace amp&; with &
            url = url.replaceAll("&amp;", "&");
            try
            {
                if(!url.startsWith("http"))
                {
                    if(url.startsWith("."))
                    {
                        String relativePath = url.startsWith("..") ? url.substring(2) : url.substring(1);
                        urls.add(new URL(currentURL + relativePath));
                    }
                    else
                    {
                        //Check if / is needed as we striped / from the base url.
                        String p = url.startsWith(URL_PATH_SEPARATOR) ? base + url : base + URL_PATH_SEPARATOR + url;
                        urls.add(new URL(p));
                    }
                }
                else
                {
                    urls.add(new URL(url));
                }
            }
            catch(MalformedURLException e)
            {
                logger.error("Unable to parse url", e);
            }
        } 
        return urls;
    }
    /**
     * Download all external files to local directory.
     * @param files collection to download
     * @param driver {@link WebDriver}
     * @throws IOException 
     */
    public static void getFiles(List<URL> files, WebDriver driver) throws IOException
    {
        if(null == files)
        {
            throw new RuntimeException("Collections of url's are required");
        }
        for(URL source: files)
        {
            int index  = source.toString().lastIndexOf(URL_PATH_SEPARATOR);
            String name = source.toString().substring(index + 1);
            //Strip ? as it causes problems when its a prefix. 
            if(name.startsWith("?"))
            {
                name = name.replaceFirst("\\?", "");
            }
            File destination = new File(ASSET_DIR + URL_PATH_SEPARATOR + name);
            try
            {
                FileUtils.copyURLToFile(source, destination);
            } 
            catch (Exception e)
            {
                logger.error(e);
                //Try with HttpClient
                retrieveFile(source.toString(), driver, destination);
            } 
        }
    }
    /**
     * Updates the HTML with the new locations of assets.
     * @param html
     * @param files
     * @return
     */
    public static String parseHtml(String html, List<String> files)
    {
        if(html == null || html.isEmpty())
        {
            throw new RuntimeException("Html source code is required");
        }
        String value = html.substring(0);
        value = value.replaceFirst("<head>", "<head>\n" + UTF8_HTML);
        if(files != null)
        {
            for(String file : files)
            {
                //Get the name of the asset.
                int index = file.lastIndexOf(URL_PATH_SEPARATOR);
                String name = file.substring(index + 1);
                if(name.startsWith("?"))
                {
                    try
                    {
                        //Java Regex cant handle ?[a-z]=
                        String t[] = file.split("\\?");
                        String prefix = t[0];
                        String rest = t[1];
                        //Remove ? from new filename.
                        String updatedName = name.replaceFirst("\\?", ""); 
                        value = value.replaceFirst(prefix + "\\?" + rest, "./" + ASSET_FOLDER + updatedName);
                    }
                    catch(Exception e)
                    {
                        logger.error("Unable to parse new url",e);
                    }
                }
                else
                {
                    value = value.replaceFirst(file, "./" + ASSET_FOLDER + name);
                }
            }
        }
        return value;
    }
    /**
     * Retrieve file with authentication, using {@link WebDriver} cookie we
     * keep the session and use HttpClient to download files that requires
     * user to be authenticated. 
     * @param resourceUrl path to file to download
     * @param driver {@link WebDriver}
     * @param output path to output the file
     * @throws IOException if error
     */
    protected static void retrieveFile(final String resourceUrl,
                                       final WebDriver driver,
                                       final File output) throws IOException 
    {
        BasicCookieStore cookieStore = new BasicCookieStore();
        cookieStore.addCookie(getSessionCookie(driver));
        //Create http client to retrieve the file.
        CloseableHttpClient client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
        HttpGet httpGet = new HttpGet(resourceUrl);
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        try 
        {
            HttpResponse httpResponse = client.execute(httpGet);
            HttpEntity entity = httpResponse.getEntity();
            bos = new BufferedOutputStream(new FileOutputStream(output));
            bis = new BufferedInputStream(entity.getContent());
            int inByte;
            while((inByte = bis.read()) != -1) bos.write(inByte);
        }
        catch(Exception e)
        {
            logger.error(e);
        }
        finally
        {
            if(bis != null)
            {
                bis.close();
            }
            if(bos != null)
            {
                bos.close();
            }
        }
    }

    /**
     * Prepare the client cookie based on the authenticated {@link WebDriver} 
     * cookie. 
     * @param driver {@link WebDriver}
     * @return BasicClientCookie with correct credentials.
     */
    protected static BasicClientCookie getSessionCookie(WebDriver driver)
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
