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
import org.apache.http.client.utils.HttpClientUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;

/**
 * Utility that captures the page for offline use.
 * An alternative to screen shot which allows further inspection of HTML and css code changes.
 * 
 * The proposed default storage structure is:
 * page-nth.html
 * img directory for all images.
 * css style sheets.
 * js  all javascripts imported by the page.
 * <p>
 * Example usage:
 * FetchUtil.save(driver, name.html)
 *
 * @author Michael Suzuki
 */
public class FetchUtil
{
    private static final Log logger = LogFactory.getLog(FetchUtil.class);
    private static final String GET_BASE_URL_JS_COMMAND = "return document.location.origin;";
    private static final String URL_PATH_SEPARATOR = "/";
    /* regex to locate and extract source of all assets */
    private static final Pattern SRC_PATTERN = Pattern.compile("(?<=src=\")[^\"]*(?<!\")");
    private static final Pattern CSS_PATTERN = Pattern.compile("(?<=url\\(\").*?(?=\"\\))");
    private static final Pattern CSS_LINK_PATTERN = Pattern.compile("<link.*?\\>");
    
    private static final Pattern HREF_PATTERN = Pattern.compile("(?<=href=\").*?(?=\")");
    //Required to handle strange characters on the page.
    private static final String UTF8_HTML = "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">";
    public static final String OUTPUT_DIR = "./target/public/";
    public static final String ASSET_FOLDER =  "content/";
    public static final String ASSET_DIR = OUTPUT_DIR + ASSET_FOLDER;
    
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
        
        //download all assets: js,img and stylesheet.
        List<String> files = extractFiles(sourceHtml);
        List<String> urls = parseURL(files, getHost(driver), currentUrl); 
        getFiles(urls, driver);
        String html = parseHtml(sourceHtml, files);
        File file = new File(OUTPUT_DIR + filename);
        file.delete();
        FileUtils.writeStringToFile(file, html);
    }
    public static String getHost(WebDriver driver)
    {
        return (String)((JavascriptExecutor) driver).executeScript(GET_BASE_URL_JS_COMMAND);
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
            throw new IllegalArgumentException("HTML source input required");
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
    public static List<String> parseURL(List<String> files,final String baseUrl, final String currentURL)
    {
        if(null == baseUrl || baseUrl.isEmpty())
        {
            throw new IllegalArgumentException("Site domain url is required");
        }
        if(null == currentURL || currentURL.isEmpty())
        {
            throw new IllegalArgumentException("Current WebDriver url is required");
        }
        if(files == null || files.isEmpty())
        {
            return Collections.emptyList();
        }
        //Strip tail "/"
        String base = StringUtils.removeEnd(baseUrl, URL_PATH_SEPARATOR);
        List<String> urls = new ArrayList<String>();
        for(String url : files)
        {
            //replace amp&; with &
            url = url.replaceAll("&amp;", "&");
            if(!url.startsWith("http"))
            {
                if(url.startsWith("."))
                {
                    String relativePath = url.startsWith("..") ? url.substring(2) : url.substring(1);
                    urls.add(currentURL + relativePath);
                }
                else
                {
                    //Check if / is needed as we striped / from the base url.
                    String path = url.startsWith(URL_PATH_SEPARATOR) ? base + url : base + URL_PATH_SEPARATOR + url;
                    urls.add(path);
                }
            }
            else
            {
                urls.add(url);
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
    public static void getFiles(List<String> files, WebDriver driver) throws IOException
    {
        if(null == files)
        {
            throw new IllegalArgumentException("Collections of url's are required");
        }
        //Create a client to get content.
        CloseableHttpClient client = FetchHttpClient.getHttpClient(driver);
        try
        {
            for(String source: files)
            {
                File file = getFile(source, client);
                if(source.endsWith("css"))
                {
                    FetchCSS.getCSSFiles(file, driver);
                }
            }
        }
        finally
        {
            HttpClientUtils.closeQuietly(client);
        }
    }
    /**
     * Gets the name from path
     * @param source the path to file
     * @return String name of file
     */
    private static String getName(final String source)
    {
        int index  = source.lastIndexOf(URL_PATH_SEPARATOR);
        return source.substring(index + 1);
    }
    /**
     * Gets the external file using http client.
     * @param source file name
     * @param client {@link CloseableHttpClient}
     * @return {@link File} output.
     * @throws IOException if error
     */
    private static File getFile(final String source, final CloseableHttpClient client) throws IOException
    {
        String name = getName(source);
        //Strip ? as it causes regex problems when its a prefix. 
        if(name.startsWith("?"))
        {
            name = name.replaceFirst("\\?", "");
        }
        File target = new File(ASSET_DIR);
        if(!target.exists()) 
        {
            target.mkdirs();
        }
        File destination = new File(ASSET_DIR + URL_PATH_SEPARATOR + name);
        return retrieveFile(source, client, destination);
    }
    /**
     * Updates the HTML with the new locations of assets.
     * @param html
     * @param files
     * @return update html source
     */
    public static String parseHtml(String html, List<String> files)
    {
        if(html == null || html.isEmpty())
        {
            throw new IllegalArgumentException("Html source code is required");
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
    protected static File retrieveFile(final String resourceUrl,
                                       final CloseableHttpClient client,
                                       final File output) throws IOException 
    {
        
        HttpGet httpGet = new HttpGet(resourceUrl);
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        HttpResponse response = null; 
        try 
        {
            response = client.execute(httpGet);
            HttpEntity entity = response.getEntity();
            bos = new BufferedOutputStream(new FileOutputStream(output));
            bis = new BufferedInputStream(entity.getContent());
            int inByte;
            while((inByte = bis.read()) != -1) bos.write(inByte);
            HttpClientUtils.closeQuietly(response);
        }
        catch(Exception e)
        {
            logger.error("Unable to fetch file " + resourceUrl, e);
        }
        finally
        {
            if(response != null)
            {
                HttpClientUtils.closeQuietly(response);
            }
            if(bis != null)
            {
                bis.close();
            }
            if(bos != null)
            {
                bos.close();
            }
        }
        return output;
    }
}
