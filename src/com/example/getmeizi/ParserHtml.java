package com.example.getmeizi;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.util.Log;

public class ParserHtml {
	public static String TAG = "ParserHtml";

	public static void parser(String html) {
	
		Document document =Jsoup.parse(html);
		Log.d(TAG, "doc:------"+document.toString());
		Element content = document.select(".container.content").first();
        if (content == null) {
            Log.e(TAG, "no content");
           // return null;
        }

        Element meta = content.select("> .ds-thread").first();
        if (meta == null) {
            Log.e(TAG, "meta not found");
           // return null;
        }

        String key = meta.attr("data-thread-key");
     

        String title = meta.attr("data-title");
      

        Log.d(TAG, "title: " + title);

        	String url = meta.attr("data-url");
   
    

      // url = url.substring();

//        if (!"/".equals(article.url) && !article.url.matches("^/\\d{4}/\\d{2}/\\d{2}$")) {
//            Log.e(TAG, "invalid url " + article.url);
//            return null;
//        }

        Log.d(TAG, "url: " + url);

        Elements sibling = content.select("> .row .six.columns p");
    

        Element later = sibling.get(0).select("a").first();
      

        Element earlier = sibling.get(1).select("a").first();
    

        for (Element paragraph : content.select("> .outlink > *")) {
            if (isParagraphHeader(paragraph)) {
                break;
            }

            Element image = paragraph.select("> img").first();
         
        }
	}

	private static boolean isParagraphHeader(Element paragraph) {
		return paragraph.tagName().toLowerCase().equals("h1")
				&& paragraph.hasText()
				&& (paragraph.text().equals("iOS") || paragraph.text().equals(
						"Android"));
	}
}
