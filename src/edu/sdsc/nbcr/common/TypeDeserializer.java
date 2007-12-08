/*
 * TypeDeserializer.java
 *
 * Created on December 1, 2004, 2:41 PM
 */

package edu.sdsc.nbcr.common;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.apache.axis.message.EnvelopeHandler;
import org.apache.axis.message.SOAPHandler;

import org.xml.sax.SAXException;

import org.apache.log4j.Logger;

/**
 *
 * @author  Brent
 */
public class TypeDeserializer {

    // get an instance of the log4j logger
    private static Logger logger =
	Logger.getLogger(TypeDeserializer.class.getName());

    /** Creates a new instance of TypeDeserializer */
    public TypeDeserializer() {
    }
    
    /*
     * method to deserialize an Axis produced schema object
     *
     */
    public static Object getValue( String path, Object ret ) 
	throws NoSuchMethodException, 
	       IllegalAccessException, 
	       InvocationTargetException,
	       SAXException,
	       IOException {

	logger.info("called");

        // path: path to xml file to deserialize
        // ret: instance of class that will hold deserialized object
	Class retClass = ret.getClass();
        
	// simulating
	// org.apache.axis.encoding.Deserializer des = ret.getDeserializer("",
	// ret.getClass(), ret.getTypeDesc().getXmlType());
        
	// get TypeDesc object
	Method getTypeDesc = retClass.getDeclaredMethod( "getTypeDesc", (Class[]) null );
	Object typeDescRet = getTypeDesc.invoke( ret, (Object[]) null );
        
	//System.err.println( getTypeDesc.toString() );
        
	// get xmlType (QName) object
	Method getXmlType = typeDescRet.getClass().getMethod( "getXmlType", (Class[]) null );
	Object xmlTypeRet = getXmlType.invoke( typeDescRet, (Object[]) null );
        
	//System.err.println( getXmlType.toString() );
        
	// call getDeserializer method
	// args: String mechType, Class javaType, QName xmlType
	// empty string is the value for "mechType" parameter
	String mechType = "";
	Class[] classes = new Class[ 3 ];
	classes[0] = mechType.getClass();
	classes[1] = mechType.getClass().getClass();
	classes[2] = xmlTypeRet.getClass();
	Method getDeserializer = retClass.getMethod( "getDeserializer",
						     classes );
	
	org.apache.axis.encoding.Deserializer des =
            (org.apache.axis.encoding.Deserializer) getDeserializer.
	    invoke(retClass, new Object[]{ mechType, retClass, xmlTypeRet } );
	
	//System.err.println( des.toString() );
	
	org.apache.axis.MessageContext mc2 = 
	    new org.apache.axis.MessageContext( new org.apache.axis.client.AxisClient() );
	
	File dfile = new File( path );
	org.xml.sax.InputSource is = new org.xml.sax.InputSource( new FileReader( dfile ) );
        
	org.apache.axis.encoding.DeserializationContext dc = 
	    new org.apache.axis.encoding.DeserializationContext( is, mc2, "" );
	
	dc.pushElementHandler(  new EnvelopeHandler( (SOAPHandler) des ) );
	dc.parse();            
        
	return des.getValue();
    }
}
