package com.saina.amazon;

import java.net.URLDecoder;

import org.apache.commons.lang3.StringUtils;

public class Test {

	@org.junit.Test
	public void test() {

        try{  
        	System.err.println(URLDecoder.decode(URLDecoder.decode("https%253A%252F%252Fwww.amazon.co.uk%252Fgp%252Fproduct","utf-8")));
           String url = "http://%s/product-reviews/%s/ref=cm_cr_arp_d_show_all?reviewerType=all_reviews&showViewpoints=0&sortBy=recent&pageNumber=5";
			int maxpage = Integer.valueOf(url.substring(url.lastIndexOf("pageNumber=") + 11, url.length()));
			System.err.println(url);
			System.err.println(StringUtils.replacePattern(url, "(pageNumber\\=(\\d+)$)", "pageNumber=" + (++maxpage)));
        }catch(Exception ex){  
            ex.printStackTrace();  
        }  
	}

}
