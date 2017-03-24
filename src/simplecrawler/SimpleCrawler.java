/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simplecrawler;

import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Mr T.Khine
 */
public class SimpleCrawler
{

    Queue< String> urlList = new LinkedList<>();
    ArrayList< String> downloadedList = new ArrayList< String>();

    SimpleCrawler(ArrayList<String> seed)
    {
        for(String url : seed)
        {
            this.urlList.add(url);
        }
    }
    public void getAllURLFromPage(String page)
    {
        URL pageUrl = null;
        StringBuffer buffer = new StringBuffer();
        try
        {
            pageUrl = new URL(page);

        } catch (Exception e)
        {
            //System.out.println ("In retirebing ");
            e.printStackTrace();
        }
        String s = "";
        try
        {
            DataInputStream din = new DataInputStream(pageUrl.openStream());
            while ((s = din.readLine()) != null)
            {
                buffer.append(s);
            }
            din.close();
        } catch (IOException e)
        {
        }

        Pattern p = Pattern.compile("<a\\s+href\\s*=\\s*\"?(.*?)[\"|>]",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(buffer);

        String link = "";

        while (m.find())
        {

            link = m.group(1).trim();
            ///System.out.println ("link name "+ link);
            if (link.length() < 1)
            {
                continue;
            }

            if (link.charAt(0) == '#')
            {
                continue;
            }

            if (link.indexOf("mailto:") != -1)
            {
                continue;
            }

            if (link.toLowerCase().indexOf("javascript") != -1)
            {
                continue;
            }
            if (link.toLowerCase().indexOf("vbscript:") != -1)
            {
                continue;
            }
            // Prefix absolute and relative URLs if necessary.
            if (link.indexOf("://") == -1)
            {
                // Handle absolute URLs.
                if (link.charAt(0) == '/')
                {

                    link = "http://" + pageUrl.getHost() + link;
     //System.out.println ("This case ADD URL "+ link);
                    // Handle relative URLs.
                } else
                {
                    String file = pageUrl.getFile();

                    if (file.indexOf('\\') == -1)
                    {

                        String path = file.substring(0, file.lastIndexOf('/') + 1);
                        link = "http://" + pageUrl.getHost() + path + link;
                        //System.out.println ("Adding This to Link "+ link);
                    } else
                    {

                        String path = file.substring(0, file.lastIndexOf('\\') + 1);

                        link = "http://" + pageUrl.getHost() + path + link;
                    }
                }
            }
            // Remove anchors from link.
            int index = link.indexOf('#');
            if (index != -1)
            {
                link = link.substring(0, index);
            }
            // Remove leading "www" from URL's host if present.
            link = removeWwwFromUrl(link);

            //link = link.replace("\\","%5C");
           
            if (!this.downloadedList.contains(link))
            {
                System.out.println ("Adding link to url queue "+ link);
                this.urlList.add(link);
            }

        }

    }

    private static String removeWwwFromUrl(String url)
    {
        int index = url.indexOf("://www.");
        if (index != -1)
        {
            return url.substring(0, index + 3) + url.substring(index + 7);
        }
        return (url);
    }

    URL downloadPage(String url)
    {
        URL downloadPage = null;
        try
        {
            downloadPage = new URL(url);
            return downloadPage;

        } catch (Exception e)
        {
            System.out.println("Enable to connect " + url);
        }
        return downloadPage;

    }
    String urlToFileName(String url) throws UnsupportedEncodingException
    {
        return URLEncoder.encode(url, "UTF-8")+".txt";
        
    }
    public void crawl()
    {
        while(!urlList.isEmpty())
        {
            String page = urlList.remove();
           
            try
            {
                System.out.println("Crawling "+page);
                URL downloadedPage = this.downloadPage(page);

                this.getAllURLFromPage(page);
                String fileName = urlToFileName(page);
                ReadableByteChannel rbc = Channels.newChannel(downloadedPage.openStream());
                FileOutputStream fos = new FileOutputStream(fileName);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                fos.flush();
                fos.close();
                rbc.close();
                System.out.println("Save to "+fileName);
                this.downloadedList.add(page);
            }
            catch(Exception e )
            {
                e.printStackTrace();
            }
        }	
    }
    public static void main(String[] args)
    {
       ArrayList<String> seed = new ArrayList<String>();
       seed.add("https://www.sitepoint.com");
       
       SimpleCrawler crawler = new SimpleCrawler(seed);
       crawler.crawl();
    }

}
