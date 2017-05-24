package es.fgonzalez.commands;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.fgonzalez.service.JsoupManager;
import info.magnolia.commands.MgnlCommand;
import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.PropertyUtil;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.mail.MailModule;
import info.magnolia.module.mail.MgnlMailFactory;
import info.magnolia.module.mail.templates.MgnlEmail;
import info.magnolia.objectfactory.Components;

public class ScrappingCommand extends MgnlCommand {
	
	private static final String REGEX_REPLACE = "--variable--";
	private static final String EMAIL_LIST = "emailList";
	private static final String CSS_QUERIES = "commandListJSoup";
	private static final String URL = "url";
	private static final String EMAIL_BODY = "emailBody";
	private static final String REPOSITORY = "scrapping";
	private static final String QUERY = "SELECT * FROM mgnl:scrapping";
	private static final String CONTENT_FORMAT = "text/html";
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String SUBJECT = "Scrapping Module Results ";
	private static final String EMAIL_FROM = "fgonzalez.magnolia@gmail.com";
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ScrappingCommand.class);

	private boolean resultCommand;
	private JsoupManager jsm;
	
	@Override
	public boolean execute(Context context) throws Exception {
		LOGGER.debug("Starting execute( context )");
		this.resultCommand = true;
		List<Node> listNodes = this.getScrappingItems();
		this.jsm = new JsoupManager();
		if(!listNodes.isEmpty()){
			for(Node node: listNodes) {
				this.workWithScrappingItem(node, context);
			}
		}
		
		return this.resultCommand;
	}
	
	private void workWithScrappingItem(Node node, Context context) {
		try {
			LOGGER.debug("Starting workWithScrappingItem( Node:{} )", node.getName());
		} catch (RepositoryException e) {
			this.resultCommand = false;
			LOGGER.error(e.getMessage(), e);
		}
		
		String body = PropertyUtil.getString(node, EMAIL_BODY, "");
		String url = PropertyUtil.getString(node, URL, "");
		//String cssQueries = PropertyUtil.getPropertyValueObject(node, CSS_QUERIES);
		//String[] cssQueriesList = cssQueries.replaceAll("[", "").replaceAll("]", "").split(",");
		List<String> cssQueriesList =  (List<String>) PropertyUtil.getPropertyValueObject(node, CSS_QUERIES);
//		String email = PropertyUtil.getString(node, EMAIL_LIST, "");
//		String[] emailList = email.replaceAll("[", "").replaceAll("]", "").split(",");
		List<String> emailList =  (List<String>) PropertyUtil.getPropertyValueObject(node, EMAIL_LIST);//
//		LOGGER.debug("body:\n{}\n url:-{}- cssQueries:-{}- email:-{}-", body, url, cssQueries, email);
		
		for(String cssQuery: cssQueriesList) {
			//ENGANCHE
			String resultScrapping = this.jsm.getScrapping(url, cssQuery, "text", "false").get(0);
			body = body.replaceFirst(REGEX_REPLACE, resultScrapping);
		}
		LOGGER.debug("FINAL BODY after replace personalized values:\n {}",body);
		
		for(String destination: emailList) {
			try {
				this.sendEmail(destination, context, body);
			} catch (IOException e) {
				this.resultCommand = false;
				LOGGER.error(e.getMessage(), e);
			}
		}
	}



	private List<Node> getScrappingItems() {
		LOGGER.debug("Starting getScrappingItems()");
		List<Node> list = new ArrayList<>();

		try {

			final QueryManager jcrQueryManager = MgnlContext.getJCRSession(REPOSITORY).getWorkspace().getQueryManager();
			LOGGER.debug("Query:-{}-", QUERY);
			Query query = jcrQueryManager.createQuery(QUERY, Query.SQL);
			query.setLimit(Integer.MAX_VALUE);
			QueryResult qr = query.execute();
			RowIterator it = qr.getRows();
			while (it.hasNext()) {
				Row row = it.nextRow();
				list.add(row.getNode());
			}
		} catch (RepositoryException e) {
			this.resultCommand = false;
			if (LOGGER.isDebugEnabled()) {
				LOGGER.error(e.getMessage(), e);
			} else {
				LOGGER.error(e.getMessage());
			}
		}

		return list;
	}
	
	
	private void sendEmail( String email, Context context, String body ) throws IOException {
		LOGGER.debug("Starting sendEmail( {}, context, bodyEmail )", email);
        LOGGER.info( "Im trying send Email" );
        ModuleRegistry moduleRegistry = Components.getComponent( ModuleRegistry.class );
        LOGGER.info( "Creating mail factory...." );
        MgnlMailFactory factory = moduleRegistry.getModuleInstance( MailModule.class ).getFactory();
        LOGGER.info( "Creating email...." );
        MgnlEmail mail = factory.getEmail( context );
        Session jcrSession;
        try {

            mail.setFrom( EMAIL_FROM );
            mail.setToList( email );
            Date date = new Date();
            SimpleDateFormat sd = new SimpleDateFormat(DATE_FORMAT);
            mail.setSubject( SUBJECT +  sd.format(date));
            mail.setContent( body, CONTENT_FORMAT );
            LOGGER.info( "Sending email...." );
            factory.getEmailHandler().sendMail( mail );
            LOGGER.info( "Email send successfully!" );
        } catch ( Exception e ) {
        	this.resultCommand = false;
            LOGGER.error( "Error while trying send email", e );
        }
    }
}
