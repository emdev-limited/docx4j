package org.docx4j.jaxb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

public class NamespacePrefixMapperUtils {
	
	private static Logger log = LoggerFactory.getLogger(NamespacePrefixMapperUtils.class);		
	
	/*
	 * As from 2010 08 26,  
	 * both com.sun.xml.bind.marshaller.NamespacePrefixMapper
	 * and  com.sun.xml.internal.bind.marshaller.NamespacePrefixMapper
	 * are provided in the jar JAXB-NamespacePrefixMapperInterfaces.jar
	 * so that people can build docx4j without needing both JAXB
	 * implementations.
	 * 
	 * But if that jar is on their classpath, testing for either
	 * of these classes will always succeed.
	 * 
	 * So, we have to test for something else.  The following will do:
	 * 
	 * com.sun.xml.bind.marshaller.MinimumEscapeHandler
	 * com.sun.xml.internal.bind.marshaller.MinimumEscapeHandler
	 */
	
	private static JAXBContext testContext;
	
	private static Object prefixMapper;
	private static Object prefixMapperRels;
	
	private static boolean haveTried = false;
	
	public static Object getPrefixMapper() throws JAXBException {
		
		if (prefixMapper!=null) return prefixMapper;
		
		if (haveTried) return null;
		// will be true soon..
		haveTried = true;
		
		
		if (testContext==null) {
			java.lang.ClassLoader classLoader = NamespacePrefixMapperUtils.class.getClassLoader();
			testContext = JAXBContext.newInstance("org.docx4j.relationships",classLoader );
		}
		
		if (testContext==null) {
			throw new JAXBException("Couldn't create context for org.docx4j.relationships.  Everything is broken!");
		}
		
		Marshaller m=testContext.createMarshaller();
		
		if (System.getProperty("java.vendor").contains("Android")) {
			log.info("Android .. assuming RI.");  // Avoid unwanted Android logging; art logs the full ClassNotFoundException 
			return tryUsingRI(m);						
		}
		
		try {
			// Assume use of Java 6 implementation (ie not RI)
			Class c = Class.forName("org.docx4j.jaxb.NamespacePrefixMapperSunInternal");
			
//			m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", c.newInstance() );
			log.info("Using NamespacePrefixMapperSunInternal, which is suitable for Java 6");
			prefixMapper = c.newInstance();
			return prefixMapper;
		} catch (java.lang.NoClassDefFoundError notJava6) {
			log.warn(notJava6.getMessage() + " .. trying RI.");
			return tryUsingRI(m);			
//		} catch (javax.xml.bind.PropertyException notJava6) {
//			// OpenJDK (1.6.0_23) does this
//			log.warn(notJava6.getMessage() + " .. trying RI.");
//			return tryUsingRI(m);			
		}  catch (ClassNotFoundException notJava6) {
			// We shouldn't get here on Android, but we may using RI elsewhere
			log.warn(notJava6.getMessage() + " .. trying RI.");
			return tryUsingRI(m);			
		} catch (InstantiationException notJava6) {
			// We shouldn't get here since Class.forName will have already thrown an exception
			log.warn(notJava6.getMessage() + " .. trying RI.");
			return tryUsingRI(m);			
		} catch (IllegalAccessException notJava6) {
			// We shouldn't get here since Class.forName will have already thrown an exception
			log.warn(notJava6.getMessage() + " .. trying RI.");
			return tryUsingRI(m);			
		}
	}


	private static Object tryUsingRI(Marshaller m)
			throws JAXBException {
		try {
			// Try RI suitable one
//			m.setProperty("com.sun.xml.bind.namespacePrefixMapper", 
//					new NamespacePrefixMapper() );
			log.info("Using NamespacePrefixMapper, which is suitable for the JAXB RI");
			prefixMapper = new NamespacePrefixMapper();
			return prefixMapper;
		} catch (java.lang.NoClassDefFoundError notRIEither) {
			notRIEither.printStackTrace();
			log.error("JAXB: neither Reference Implementation nor Java 6 implementation present?", notRIEither);
			throw new JAXBException("JAXB: neither Reference Implementation nor Java 6 implementation present?");
		} 
//		catch (javax.xml.bind.PropertyException notRIEither) {
//			notRIEither.printStackTrace();
//			log.error("JAXB: neither Reference Implementation nor Java 6 implementation present?", notRIEither);
//			throw new JAXBException("JAXB: neither Reference Implementation nor Java 6 implementation present?");
//		}
	}

	
	public static Object getPrefixMapperRelationshipsPart() throws JAXBException {

		if (prefixMapperRels!=null) return prefixMapperRels;
		if (testContext==null) {
			java.lang.ClassLoader classLoader = NamespacePrefixMapperUtils.class.getClassLoader();
			testContext = JAXBContext.newInstance("org.docx4j.relationships",classLoader );
		}
		
		Marshaller m=testContext.createMarshaller();
		try {
			// Assume use of Java 6 implementation (ie not RI)
			Class c = Class.forName("org.docx4j.jaxb.NamespacePrefixMapperRelationshipsPartSunInternal");
			
//			m.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", c.newInstance() );
			log.info("Using NamespacePrefixMapperRelationshipsPartSunInternal, which is suitable for Java 6");
			prefixMapperRels = c.newInstance();
			return prefixMapperRels;
		} catch (java.lang.NoClassDefFoundError notJava6) {
			// javax.xml.bind.PropertyException
			log.warn(notJava6.getMessage() + " .. trying RI.");
			return tryRIforRelationshipsPart(m);
//		} catch (javax.xml.bind.PropertyException notJava6) {
//			log.warn(notJava6.getMessage() + " .. trying RI.");
//			return tryRIforRelationshipsPart(m);
		}  catch (ClassNotFoundException notJava6) {
			// We shouldn't get here on Android, but we may using RI elsewhere
			log.warn(notJava6.getMessage() + " .. trying RI.");
			return tryRIforRelationshipsPart(m);			
		} catch (InstantiationException notJava6) {
			// We shouldn't get here since Class.forName will have already thrown an exception
			log.warn(notJava6.getMessage() + " .. trying RI.");
			return tryRIforRelationshipsPart(m);			
		} catch (IllegalAccessException notJava6) {
			// We shouldn't get here since Class.forName will have already thrown an exception
			log.warn(notJava6.getMessage() + " .. trying RI.");
			return tryRIforRelationshipsPart(m);			
		}
	}


	private static Object tryRIforRelationshipsPart(Marshaller m)
			throws JAXBException {
		try {
			// Try RI suitable one
//			m.setProperty("com.sun.xml.bind.namespacePrefixMapper", 
//					new NamespacePrefixMapperRelationshipsPart() );
			log.info("Using NamespacePrefixMapperRelationshipsPart, which is suitable for the JAXB RI");
			prefixMapperRels = new NamespacePrefixMapperRelationshipsPart();
			return prefixMapperRels;
		} catch (java.lang.NoClassDefFoundError notRIEither) {
			notRIEither.printStackTrace();
			log.error("JAXB: neither Reference Implementation nor Java 6 implementation present?", notRIEither);
			throw new JAXBException("JAXB: neither Reference Implementation nor Java 6 implementation present?");
		}
//		catch (javax.xml.bind.PropertyException notRIEither) {
//			notRIEither.printStackTrace();
//			log.error("JAXB: neither Reference Implementation nor Java 6 implementation present?", notRIEither);
//			throw new JAXBException("JAXB: neither Reference Implementation nor Java 6 implementation present?");
//		}
	}
	
	/**
	 * setProperty on 'com.sun.xml.bind.namespacePrefixMapper' or
	 * 'com.sun.xml.internal.bind.namespacePrefixMapper', as appropriate,
	 * depending on whether JAXB reference implementation, or Java 6 
	 * implementation is being used.
	 * 
	 * @param marshaller
	 * @param namespacePrefixMapper
	 * @throws JAXBException
	 */
	public static void setProperty(Marshaller marshaller, Object namespacePrefixMapper) throws JAXBException {
		
		namespacePrefixMapper = null;
		log.debug("attempting to setProperty on marshaller " + marshaller.getClass().getName() );
		if (namespacePrefixMapper == null) {
			return;
		}
		try {
			if ( namespacePrefixMapper.getClass().getName().equals(
						"org.docx4j.jaxb.NamespacePrefixMapper")
					|| namespacePrefixMapper.getClass().getName().equals(
							"org.docx4j.jaxb.NamespacePrefixMapperRelationshipsPart") ) {
			
				marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", 
						namespacePrefixMapper ); 
			
				// Reference implementation appears to be present (in endorsed dir?)
				log.debug("setProperty: com.sun.xml.bind.namespacePrefixMapper");
//				System.out.println("setProperty: com.sun.xml.bind.namespacePrefixMapper");
				
			} else {
				
				// Use JAXB distributed in Java 6 - note 'internal' 
				// Switch to other mapper
				log.debug("attempting to setProperty: com.sun.xml.INTERNAL.bind.namespacePrefixMapper");
				marshaller.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper", namespacePrefixMapper);
//				System.out.println("setProperty: com.sun.xml.INTERNAL.bind.namespacePrefixMapper");
			}
			
		} catch (javax.xml.bind.PropertyException e) {
			
			log.error("Couldn't setProperty on marshaller " + marshaller.getClass().getName() );
			log.error(e.getMessage(), e);
			throw e;
			
		}
		
	}
	
	public static String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) throws JAXBException {

		NamespacePrefixMapperInterface namespacePrefixMapper = (NamespacePrefixMapperInterface)getPrefixMapper();
		return namespacePrefixMapper.getPreferredPrefix(namespaceUri, suggestion, requirePrefix); 
		
	}
	
    private static final String[] EMPTY_STRING = new String[0];
	
    public static String[] getPreDeclaredNamespaceUris(String mcIgnorable) {
    	
    	if (mcIgnorable==null) {    	
    		return EMPTY_STRING;
    	}

    	List<String> entries = new ArrayList<String>();
    	
		StringTokenizer st = new StringTokenizer(mcIgnorable, " ");
		while (st.hasMoreTokens()) {
			String prefix = (String) st.nextToken();
			
			String uri = NamespacePrefixMappings.getNamespaceURIStatic(prefix);
			
			if (uri==null) {
				log.warn("No mapping for prefix '" + prefix + "'");
			} else {
		    	//  { "prefix1", "namespace1", "prefix2", "namespace2", ... }
				//entries.add(prefix);
				entries.add(uri);
			}
		}
		return  entries.toArray(new String[entries.size()]);
    	
    }

    public static Map<String, String> getPreDeclaredNamespaceMap(String mcIgnorable) {
    
    	Map<String, String> entries = new HashMap<String, String>();

    	if (mcIgnorable==null) {    	
    		return entries;
    	}

		StringTokenizer st = new StringTokenizer(mcIgnorable, " ");
		while (st.hasMoreTokens()) {
			String prefix = (String) st.nextToken();
			
			String uri = NamespacePrefixMappings.getNamespaceURIStatic(prefix);
			
			if (uri==null) {
				log.warn("No mapping for prefix '" + prefix + "'");
			} else {
		    	//  { "prefix1", "namespace1", "prefix2", "namespace2", ... }
				//entries.add(prefix);
				entries.put(prefix, uri);
			}
		}
		return  entries;
    	
    }
    
	/**
	 * Word requires all mcIgnorable prefixes to be declared at the document level.
	 * 
	 * @param mcIgnorable
	 * @param doc
	 */
	public static void declareNamespaces(String mcIgnorable, Document doc) {
		
		if (mcIgnorable==null) return;
		
		StringTokenizer st = new StringTokenizer(mcIgnorable, " ");
		while (st.hasMoreTokens()) {
			String prefix = (String) st.nextToken();
			
			String uri = NamespacePrefixMappings.getNamespaceURIStatic(prefix);
			
			if (uri==null) {
				log.warn("No mapping for prefix '" + prefix + "'");
			} else {
	    		doc.getDocumentElement().setAttributeNS("http://www.w3.org/2000/xmlns/" ,
	    				"xmlns:" + prefix, uri);
				
			}
		}
		
	}
    
    
}
