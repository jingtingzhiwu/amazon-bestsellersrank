package com.saina.amazon.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Task;
import us.codecraft.webmagic.downloader.AbstractDownloader;
import us.codecraft.webmagic.downloader.Downloader;
import us.codecraft.webmagic.selector.PlainText;
import us.codecraft.webmagic.utils.WMCollections;

public class JsoupDownloaderUtil extends AbstractDownloader implements Downloader {
	String appkey = "";
	String secret = "";
	String proxyIP = "";
	int proxyPort = 8123;
	public static final String[] agents = new String[] { "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/14.0.835.163 Safari/535.1", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:6.0) Gecko/20100101 Firefox/6.0",
			"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50",
			"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.3; .NET4.0C; Tablet PC 2.0; .NET4.0E)",
			"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; InfoPath.3)",
			"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; GTB7.0)", "Mozilla/5.0 (Windows; U; Windows NT 6.1; ) AppleWebKit/534.12 (KHTML, like Gecko) Maxthon/3.0 Safari/534.12",
			"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.3; .NET4.0C; .NET4.0E)",
			"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.3; .NET4.0C; .NET4.0E; SE 2.X MetaSr 1.0)",
			"Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.3 (KHTML, like Gecko) Chrome/6.0.472.33 Safari/534.3 SE 2.X MetaSr 1.0",
			"Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.3; .NET4.0C; .NET4.0E)",
			"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.41 Safari/535.1 QQBrowser/6.9.11079.201",
			"Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.3; .NET4.0C; .NET4.0E) QQBrowser/6.9.11079.201",
			"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.116 Safari/537.36" };
	private Logger logger = LoggerFactory.getLogger(getClass());
	private static JsoupDownloaderUtil instance = null;

	private JsoupDownloaderUtil() {
	}

	public static JsoupDownloaderUtil getInstance() {
		if (instance == null) {
			return new JsoupDownloaderUtil();
		}
		return instance;
	}

	@Override
	public Page download(Request request, Task task) {

		Site site = null;
		if (task != null) {
			site = task.getSite();
		}
		Set<Integer> acceptStatCode;
		if (site != null) {
			acceptStatCode = site.getAcceptStatCode();
		} else {
			acceptStatCode = WMCollections.newHashSet(200);
		}
		logger.info("downloading page {}", request.getUrl());

		int statusCode = 0;

		int failure = 0;
		Document doc = null;
		Connection conn = Jsoup.connect(request.getUrl());

		while (true) {
			try {
				Response response = conn.header("User-Agent", agents[new Random().nextInt(agents.length)]).header("Cache-Control", "no-store").header("Connection", "close").header("Accept", "application/json, text/javascript, */*; q=0.01")
						.header("Accept-Encoding", "gzip, deflate").header("Accept-Language", "zh-CN,zh;q=0.8")
						 .header("Proxy-Authorization", getAuthHeader())
						 .proxy(proxyIP, proxyPort, null)
						.validateTLSCertificates(false).timeout(1000 * 20).execute();
				doc = response.parse();

				statusCode = response.statusCode();
				if (statusAccept(acceptStatCode, statusCode)) {
					Page page = new Page();
					page.setRawText(doc.html());
					page.setUrl(new PlainText(request.getUrl()));
					page.setRequest(request);
					page.setStatusCode(statusCode);
					onSuccess(request);
					return page;
				} else {
					logger.info("get page {} error, status code {} ", request.getUrl(), statusCode);
					return null;
				}
			} catch (Exception e) {
				// onError(request);
				failure++;
				if (failure == 3) {

					logger.error("get page {} error, 3 times, exception {} ", request.getUrl(), e.getMessage());
					if (e instanceof HttpStatusException) {
						if (((HttpStatusException) e).getStatusCode() == 503 || ((HttpStatusException) e).getStatusCode() == 500) {
							logger.error("Status 503, Amazon has throttled the response due to too many requests being send, need to slow down.");
						} else {
							logger.error("Unable to establish connection, sku might no longer exists, exception code" + e.toString() + " " + "will move on to next sku in list" + " " + "Admin please log this sku");
						}
					}
					return null;
				}
			} finally {
			}
		}
	}

	public Document singleRequest(String url) {

		Set<Integer> acceptStatCode;
		acceptStatCode = WMCollections.newHashSet(200);
		logger.info("downloading page {}", url);

		int statusCode = 0;

		int failure = 0;
		Document doc = null;
		Connection conn = Jsoup.connect(url);

		while (true) {
			try {
				Response response = conn.header("User-Agent", agents[new Random().nextInt(agents.length)]).header("Cache-Control", "no-store").header("Connection", "close").header("Accept", "application/json, text/javascript, */*; q=0.01")
						.header("Accept-Encoding", "gzip, deflate").header("Accept-Language", "zh-CN,zh;q=0.8")
						 .header("Proxy-Authorization", getAuthHeader())
						 .proxy(proxyIP, proxyPort, null)
						.validateTLSCertificates(false).timeout(1000 * 20).execute();
				doc = response.parse();

				statusCode = response.statusCode();
				if (statusAccept(acceptStatCode, statusCode)) {
					return doc;
				} else {
					logger.info("get page {} error, status code {} ", url, statusCode);
					return null;
				}
			} catch (Exception e) {
				// onError(request);
				failure++;
				if (failure == 3) {

					logger.error("get page {} error, 3 times, exception {} ", url, e.getMessage());
					if (e instanceof HttpStatusException) {
						if (((HttpStatusException) e).getStatusCode() == 503 || ((HttpStatusException) e).getStatusCode() == 500) {
							logger.error("Status 503, Amazon has throttled the response due to too many requests being send, need to slow down.");
						} else {
							logger.error("Unable to establish connection, sku might no longer exists, exception code" + e.toString() + " " + "will move on to next sku in list" + " " + "Admin please log this sku");
						}
					}
					return null;
				}
			} finally {
			}
		}
	}

	protected boolean statusAccept(Set<Integer> acceptStatCode, int statusCode) {
		return acceptStatCode.contains(statusCode);
	}

	@Override
	public void setThread(int arg0) {
		// TODO Auto-generated method stub

	}

	public String getAuthHeader() {

		// 创建参数表
		Map<String, String> paramMap = new HashMap<String, String>();
		paramMap.put("app_key", appkey);
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("GMT+8"));// 使用中国时间，以免时区不同导致认证错误
		paramMap.put("timestamp", format.format(new Date()));

		// 对参数名进行排序
		String[] keyArray = paramMap.keySet().toArray(new String[0]);
		Arrays.sort(keyArray);

		// 拼接有序的参数名-值串
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(secret);
		for (String key : keyArray) {
			stringBuilder.append(key).append(paramMap.get(key));
		}

		stringBuilder.append(secret);
		String codes = stringBuilder.toString();

		// MD5编码并转为大写， 这里使用的是Apache codec
		String sign = org.apache.commons.codec.digest.DigestUtils.md5Hex(codes).toUpperCase();

		paramMap.put("sign", sign);

		// 拼装请求头Proxy-Authorization的值，这里使用 guava 进行map的拼接
		String authHeader = "MYH-AUTH-MD5 " + Joiner.on('&').withKeyValueSeparator("=").join(paramMap);

		return authHeader;
	}
}
