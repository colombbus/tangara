package org.colombbus.helpgenerator;

import java.io.*;
import java.util.Properties;

import org.apache.commons.lang3.Validate;
import org.apache.velocity.*;
import org.apache.velocity.app.Velocity;
import org.colombbus.helpgenerator.model.*;
import org.slf4j.*;

class HtmlPageGeneratorImpl implements HtmlPageGenerator {

	private static final String TOC_CTXT_ATT = "toc"; //$NON-NLS-1$
	private static final String PAGE_CTXT_ATT = "page"; //$NON-NLS-1$
	private static final String TEMPLATE_PATH = "/org/colombbus/helpgenerator/page-template.html"; //$NON-NLS-1$
	private static final Logger LOG = LoggerFactory.getLogger(HtmlPageGeneratorImpl.class);
	private Template template;
	private Toc toc;

	public HtmlPageGeneratorImpl() {
		initTemplateEngine();
		loadTemplate();
	}

	private static void initTemplateEngine() {
		Properties initProps = new Properties();
		initProps.put("resource.loader", "class"); //$NON-NLS-1$//$NON-NLS-2$
		initProps.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader"); //$NON-NLS-1$ //$NON-NLS-2$

		Velocity.init(initProps);
	}

	private void loadTemplate() {
		try {
			template = Velocity.getTemplate(TEMPLATE_PATH);
		} catch (Exception ex) {
			LOG.error("Fail to load template " + TEMPLATE_PATH, ex); //$NON-NLS-1$
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void setToc(Toc toc) {
		Validate.notNull(toc);
		this.toc = toc;
	}

	@Override
	public String generatePage(Page page) {
		Validate.notNull(page);
		verifyInitialized();

		VelocityContext context = createContext(page);
		StringWriter writer = new StringWriter();
		template.merge(context, writer);
		return writer.toString();
	}

	private void verifyInitialized() {
		Validate.validState(toc != null, "toc not set"); //$NON-NLS-1$
	}

	private VelocityContext createContext(Page page) {
		VelocityContext context = new VelocityContext();
		context.put(PAGE_CTXT_ATT, page);
		context.put(TOC_CTXT_ATT, toc);
		return context;
	}

}
