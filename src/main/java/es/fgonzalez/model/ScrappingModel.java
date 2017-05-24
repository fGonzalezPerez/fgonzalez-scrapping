package es.fgonzalez.model;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.jcr.Node;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.fgonzalez.service.JsoupManager;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.cache.Cache;
import info.magnolia.module.cache.inject.CacheFactoryProvider;
import info.magnolia.rendering.model.RenderingModel;
import info.magnolia.rendering.model.RenderingModelImpl;
import info.magnolia.rendering.template.RenderableDefinition;
import info.magnolia.rendering.template.TemplateDefinition;

public class ScrappingModel extends RenderingModelImpl<TemplateDefinition> {

	private static final Logger LOGGER = LoggerFactory.getLogger(ScrappingModel.class);
	private final Provider<CacheFactoryProvider> cacheFactoryProvider;
	private static final String URL = "url";
	private static final String CSS_QUERY = "cssQuery";
	private static final String ATTR = "attr";
	private static final String HTML_CHECK = "htmlCheck";

	@Inject
	public ScrappingModel(Node content, RenderableDefinition definition, RenderingModel parent,
			Provider<CacheFactoryProvider> cacheFactoryProvider) {
		super(content, (TemplateDefinition) definition, parent);
		this.cacheFactoryProvider = cacheFactoryProvider;
	}

	public String getScrapping() {
		String s = getScrappingList().get(0);
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("s:{}",s);
		}
		return s != null ? s : "";
	}

	public List<String> getScrappingList() {
		JsoupManager jsm = new JsoupManager();
		String url = PropertyUtil.getString(content, URL, "");
		String cssQuery = PropertyUtil.getString(content, CSS_QUERY, "");
		String attr = PropertyUtil.getString(content, ATTR, "");
		String htmlCheck = PropertyUtil.getString(content, HTML_CHECK, "false");
		
		Cache scrappingCache = getScrappingCache();
		String idCache = url+"|"+cssQuery+"|"+ attr +"|"+htmlCheck;
		List <String> scrappingList = null;
		if (scrappingCache !=null) {
			scrappingList = (List<String>) scrappingCache.getQuiet(idCache);
		}
		if(scrappingList==null){
			scrappingList = jsm.getScrapping(url, cssQuery, attr, htmlCheck);
		}
		return scrappingList;
	}

	public Cache getScrappingCache() {
		return cacheFactoryProvider.get().get().getCache("scrappingCache");
	}

}
