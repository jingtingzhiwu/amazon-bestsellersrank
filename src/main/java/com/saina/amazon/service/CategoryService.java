package com.saina.amazon.service;

import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.saina.amazon.config.AmazonConfig;
import com.saina.amazon.config.AmazonConfig.AmazonServiceUrl;
import com.saina.amazon.dao.DbUtilsTemplate;
import com.saina.amazon.utils.JsoupDownloaderUtil;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.monitor.SpiderMonitor;
import us.codecraft.webmagic.processor.PageProcessor;

@Component
@EnableScheduling
public class CategoryService implements PageProcessor {

	private static Logger log = Logger.getLogger(CategoryService.class);
	private static String sql = "insert into amazon.bz_amazon_category(category_id,category_name,alias_name,level,parent_category_id,parent_category_name,site,full_path) values(?,?,?,?,?,?,?,?);";

	@Autowired
	private DbUtilsTemplate dbu;

	@Scheduled(fixedRate = 60 * 1000 * 60 * 24)
	public void timer() {
		String[] urls = new String[AmazonConfig.AmazonServiceUrl.values().length];
		AmazonServiceUrl[] values = AmazonConfig.AmazonServiceUrl.values();
		for (int i = 0; i < values.length; i++) {
			AmazonServiceUrl url = values[i];
			urls[i] = url.getValue() + "/gp/bestsellers";
		}
		try {
			long start = System.currentTimeMillis();
			Spider sp = Spider.create(this).addUrl(urls).thread(5);
			sp.setUUID("CategoryFetcher_" + UUID.randomUUID().toString());
			sp.setDownloader(JsoupDownloaderUtil.getInstance());
			SpiderMonitor.instance().register(sp);
			sp.run();
			long end = System.currentTimeMillis();
			log.info(String.format("Category fetcher done，耗时%s分", (end - start) / 1000 / 60));
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		}
	}

	@Override
	public Site getSite() {
		return Site.me().setRetryTimes(3).setSleepTime(new Random().nextInt(15) + 5).setRetrySleepTime(10 * 1000).setTimeOut(60 * 1000).setDomain("https://www.facebook.com");
	}

	@Override
	public void process(Page page) {
		String categoryId;
		String categoryName;
		String aliasName;
		int level;
		String parentCategoryId;
		String parentCategoryName;
		String site;
		StringBuilder fullPath;

		Document doc = Jsoup.parse(page.getHtml().get());
		String url = page.getUrl().toString();
		Element rootElement = doc.select("#zg_browseRoot").first();
		if (null == rootElement)
			rootElement = doc.select("#crown-category-nav").first(); // 日本站是这个

		if (page.getRequest().getExtra("leaf") != Boolean.TRUE) {
			site = url.substring(0, url.indexOf("/gp"));
			Elements lis = rootElement.select("> ul > li");
			if (lis.isEmpty())
				lis = rootElement.select("a");
			for (int i = 0; i < lis.size(); i++) {
				Element li = lis.get(i);
				fullPath = new StringBuilder();
				String href = li.select("a").attr("href");

				categoryId = (i + 1) + "";
				// category name
				categoryName = li.select("a").text();

				// url name
				Pattern pattern = Pattern.compile("bestsellers/(.+)/ref?");
				Matcher matcher = pattern.matcher(href);
				if (matcher.find()) {
					aliasName = matcher.group(1);
				} else {
					pattern = Pattern.compile("top-sellers/(.+)/ref?");
					matcher = pattern.matcher(href);
					if (matcher.find()) {
						aliasName = matcher.group(1);
					} else {
						pattern = Pattern.compile("Best-Sellers-.+/(.+)/ref?");
						matcher = pattern.matcher(href);
						if (matcher.find()) {
							aliasName = matcher.group(1);
						} else {
							pattern = Pattern.compile(site + "/(.+)/ref?");
							matcher = pattern.matcher(href);
							if (matcher.find()) {
								aliasName = matcher.group(1);
							} else {
								pattern = Pattern.compile("/(.+)/ref?");
								matcher = pattern.matcher(href);
								if (matcher.find()) {
									aliasName = matcher.group(1);
								} else {
									aliasName = "";
								}
							}
						}
					}
				}
				level = page.getRequest().getExtra("level") == null ? 1 : (int) page.getRequest().getExtra("level");
				parentCategoryId = page.getRequest().getExtra("parentCategoryId") == null ? "" : page.getRequest().getExtra("parentCategoryId").toString();
				parentCategoryName = page.getRequest().getExtra("parentCategoryName") == null ? "" : page.getRequest().getExtra("parentCategoryName").toString();
				for (Element selected : rootElement.select(".zg_selected")) {
					fullPath.append(selected.text() + " > ");
				}
				fullPath.append(categoryName);
				dbu.update(sql, new Object[] { categoryId, categoryName, aliasName, level, parentCategoryId, parentCategoryName, site, fullPath.toString() });

				Request childRequest = new Request(href);
				childRequest.putExtra("parentCategoryId", categoryId);
				childRequest.putExtra("parentCategoryName", fullPath.toString());
				childRequest.putExtra("site", site);
				childRequest.putExtra("level", level + 1);
				childRequest.putExtra("leaf", Boolean.TRUE);
				childRequest.setPriority(level + 1);
				page.addTargetRequest(childRequest);
			}
		} else {
			Element uls = doc.select("span.zg_selected").last().parent().nextElementSibling();
			if (null != uls) {
				site = page.getRequest().getExtra("site").toString();
				Elements lis = uls.select(">li");
				for (Element li : lis) {
					fullPath = new StringBuilder();
					String href = li.select("a").attr("href");

					// url name
					Pattern pattern = Pattern.compile("bestsellers/(.+)/(\\d+)/ref?");
					Matcher matcher = pattern.matcher(href);
					if (matcher.find()) {
						aliasName = matcher.group(1);
						categoryId = matcher.group(2);
					} else {
						pattern = Pattern.compile("top-sellers/(.+)/(\\d+)/ref?");
						matcher = pattern.matcher(href);
						if (matcher.find()) {
							aliasName = matcher.group(1);
							categoryId = matcher.group(2);
						} else {
							pattern = Pattern.compile("Best-Sellers-.+/(.+)/(\\d+)/ref?");
							matcher = pattern.matcher(href);
							if (matcher.find()) {
								aliasName = matcher.group(1);
								categoryId = matcher.group(2);
							} else {
								pattern = Pattern.compile(site + "/(.+)/(\\d+)/ref?");
								matcher = pattern.matcher(href);
								if (matcher.find()) {
									aliasName = matcher.group(1);
									categoryId = matcher.group(2);
								} else {
									pattern = Pattern.compile("/(.+)/(\\d+)/ref?");
									matcher = pattern.matcher(href);
									if (matcher.find()) {
										aliasName = matcher.group(1);
										categoryId = matcher.group(2);
									} else {
										aliasName = "";
										categoryId = null;
									}
								}
							}
						}
					}
					// category name
					categoryName = li.select("a").text();
					level = page.getRequest().getExtra("level") == null ? 1 : (int) page.getRequest().getExtra("level");
					parentCategoryId = page.getRequest().getExtra("parentCategoryId") == null ? "" : page.getRequest().getExtra("parentCategoryId").toString();
					parentCategoryName = page.getRequest().getExtra("parentCategoryName") == null ? "" : page.getRequest().getExtra("parentCategoryName").toString();
					/*
					 * for (Element selected : rootElement.select(".zg_selected")) {
					 * fullPath.append(selected.text() + " > "); }
					 */
					fullPath.append(parentCategoryName);
					fullPath.append(" > ");
					fullPath.append(categoryName);
					dbu.update(sql, new Object[] { categoryId, categoryName, aliasName, level, parentCategoryId, parentCategoryName, site, fullPath.toString() });

					if (level < 3) {
						Request childRequest = new Request(href);
						childRequest.putExtra("parentCategoryId", categoryId);
						childRequest.putExtra("parentCategoryName", fullPath.toString());
						childRequest.putExtra("site", site);
						childRequest.putExtra("level", level + 1);
						childRequest.putExtra("leaf", Boolean.TRUE);
						childRequest.setPriority(level + 1);
						page.addTargetRequest(childRequest);
					}
				}

			}
		}
	}
}