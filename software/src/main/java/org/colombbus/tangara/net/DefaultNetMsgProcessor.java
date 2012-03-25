/**
 * Tangara is an educational platform to get started with programming.
 * Copyright (C) 2008 Colombbus (http://www.colombbus.org)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.colombbus.tangara.net;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.colombbus.tangara.Configuration;

/**
 * @author gwen
 *
 */
public class DefaultNetMsgProcessor implements NetMsgProcessor {

	private static final String MSG_HANDLER_LIST_PROP = "network.msg-handler.list";//$NON-NLS-1$

	private static final String UNSUPPORTED_HANDLER_PROP = "network.msg-handler.unsupported";//$NON-NLS-1$

	private static final String SEPARATOR_PROP = "network.msg-handler.separator";//$NON-NLS-1$


	/** default separator for configuration parameter */
	private static final String DFLT_SEP = ",";

	/**
	 *
	 */
	public DefaultNetMsgProcessor() {
		super();
	}

	/**
	 * @return handlerMap.isEmpty()
	 */
	public boolean hasHandlers() {
		return handlerMap.isEmpty() == false;
	}

	/**
	 * Load the NetworkMessageHandlers declared in configuration
	 * <p>
	 * The following parameters are used:
	 * <ul>
	 * <li><code>network.msg-handler.list</code></li>
	 * <li><code>network.msg-handler.separator</code></li>
	 * </ul>
	 * </p>
	 * TODO enhance this
	 */
	public void loadConfiguration() {
		Configuration cfg = Configuration.instance();
		String hdlListStr = cfg.getProperty(MSG_HANDLER_LIST_PROP);
		if (hdlListStr == null) {
			LOG.warn("Property network.msg-handler.list is missing");//$NON-NLS-1$
			return;
		} else {
			String separator = cfg.getString(SEPARATOR_PROP, DFLT_SEP);

			for( StringTokenizer tokenizer = new StringTokenizer(hdlListStr, separator); tokenizer.hasMoreTokens();) {
				String classname = tokenizer.nextToken().trim();
				NetMessageHandler handler = instanciateHandler(classname);
				if( handler != null) {
					addMessageHandler(handler);
					LOG.info("Handle "+handler.getHandledType()+" messages with " +handler.getClass().getCanonicalName() );//$NON-NLS-1$
				}
			}

			//load unsupported handler
			String classname = cfg.getProperty(UNSUPPORTED_HANDLER_PROP);
			if( classname == null) {
				String msg = String
						.format(
								"Cannot found property %s. A %s will be used to handle unsupported messages",//$NON-NLS-1$
								UNSUPPORTED_HANDLER_PROP,
								NullNetMessageHandler.class.getName());
				LOG.warn(msg);
				setUnsupportedMessageHandler(new NullNetMessageHandler());
			} else {
				classname = classname.trim();
				NetMessageHandler unsupportedHandler = instanciateHandler(classname);
				if( unsupportedHandler != null) {
					setUnsupportedMessageHandler(unsupportedHandler);
					LOG.info("Handle unsupported messages with " +unsupportedHandler.getClass().getCanonicalName() );//$NON-NLS-1$
				}
			}
		}
	}

	/**
	 * Create an handler from its classname
	 * @param classLoader
	 * @param classname
	 * @return NetMessageHandler
	 */
	private NetMessageHandler instanciateHandler(String classname) {
		ClassLoader classLoader = DefaultNetMsgProcessor.class.getClassLoader();
		NetMessageHandler handler = null;
		try {
			Class<?> clazz = classLoader.loadClass(classname);
			if (NetMessageHandler.class.isAssignableFrom(clazz) == false) {
				String msg = String.format( "Class %s is not a %s", //$NON-NLS-1$
											classname,
											AbstractNetMessageHandler.class.getCanonicalName());
				LOG.warn(msg);
			} else {
				handler = (NetMessageHandler) clazz.newInstance();
			}
		} catch (ClassNotFoundException e) {
			String msg = String.format("Class %s cannot be found", classname);//$NON-NLS-1$
			LOG.warn(msg, e);
		} catch (InstantiationException e) {
			String msg = String.format("Fail to instanciate class %s", classname);//$NON-NLS-1$
			LOG.warn(msg, e);
		} catch (IllegalAccessException e) {
			String msg = String.format("Fail to access to class %s", classname);//$NON-NLS-1$
			LOG.warn(msg, e);
		} catch (ClassCastException e) {
			String msg = String.format(	"Fail to cast an instance of class %s to class %s",
										classname,
										AbstractNetMessageHandler.class.getCanonicalName());//$NON-NLS-1$
			LOG.warn(msg, e);
		}
		return handler;
	}


	/**
	 * Add a handler of messages.
	 * <p>
	 * If a previous handler handles the same type of messages, it is replaced.
	 * </p>
	 *
	 * @param handler
	 *            the message handler
	 */
	public void addMessageHandler(NetMessageHandler handler) {
		handlerMap.put(handler.getHandledType(), handler);
	}

	/**
	 * Set the handler of unsupported messages
	 *
	 * @param unsupportedMsgHandler
	 *            the new handler
	 */
	public void setUnsupportedMessageHandler(
			NetMessageHandler unsupportedMsgHandler) {
		this.unsupportedMsgHandler = unsupportedMsgHandler;
	}

	/* (non-Javadoc)
	 * @see org.colombbus.tangara.net.NetMsgProcessor#processMessage(java.lang.String, org.colombbus.tangara.net.NetMessage, java.util.List)
	 */
	@Override
	public void processMessage(String connectID, NetMessage msg, List<NetMessage> response) {
		NetMessageHandler handler = handlerMap.get(msg.getType());
		if (handler != null) {
			LOG.info("handle message of type " + msg.getType());
			handler.processMessage(msg, connectID, response);
		} else {
			LOG.info("handle message of unknown type "
					+ msg.getType());
			unsupportedMsgHandler.processMessage(msg, connectID,
					response);
		}
	}

	/** The handlers of messages */
	private Map<String, NetMessageHandler> handlerMap = Collections.synchronizedMap(new Hashtable<String, NetMessageHandler>());

	/** Handler of unsupported messages */
	private NetMessageHandler unsupportedMsgHandler = new NullNetMessageHandler();

	/** Class logger */
	private static Logger LOG = Logger.getLogger(DefaultNetMsgProcessor.class);
}
