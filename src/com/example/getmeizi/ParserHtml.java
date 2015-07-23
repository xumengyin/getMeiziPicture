package com.example.getmeizi;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

public class ParserHtml {
	public static String TAG = "ParserHtml";

	public static Pic parser(String html) {
	
		Pic pic =new Pic();
		Document document =Jsoup.parse(html);
		//Log.d(TAG, "doc:------"+document.toString());
		//Element content11=document.getElementsByClass("typo").;
		Element content = document.select(".container.content").first();
		//Element columns=content.select(".six.columns").first();
        if (content == null) {
            Log.e(TAG, "no content");
           // return null;
        }

        Elements sibling1 = content.select("> .row .six.columns p");
    
        if (sibling1.size() != 4) {
            Log.e(TAG, "sibling count not 4");
            return null;
        }

        Element later = sibling1.get(0).select("a").first();
      
        if(later!=null)
        {
        	pic.setNextUrl(later.attr("href"));
        }

        Element earlier = sibling1.get(1).select("a").first();
        if(earlier!=null)
        {
        	pic.setPreUrl(earlier.attr("href"));
        }
    
        for (Element paragraph : content.select("> .outlink > *")) {
            if (isParagraphHeader(paragraph)) {
                break;
            }

            Element image = paragraph.select("> img").first();
            if(image!=null)
            {
            	String url=image.attr("src");
            	if(url!=null)
            	{
            		pic.setUrl(url);
            		//Log.d(TAG, url);
            	}
            }
            
         
        }
        return pic;
	}

	private static boolean isParagraphHeader(Element paragraph) {
		return paragraph.tagName().toLowerCase().equals("h1")
				&& paragraph.hasText()
				&& (paragraph.text().equals("iOS") || paragraph.text().equals(
						"Android"));
	}
}
