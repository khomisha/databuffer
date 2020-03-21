/*
 * Copyright 2007-2018 Mikhail Khodonov
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * $Id$
 */

package org.homedns.mkh.databuffer;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

/**
 * Implements various service methods
 */
public class Util {
	private static final String HEXES = "0123456789ABCDEF";
	private static Pattern numPattern = Pattern.compile( "-?\\d+(\\.\\d+)?" ); 

	/**
	 * Assembles an array elements to string using a delimiter string.
	 * 
	 * @param asSrc
	 *            the array to assemble.
	 * @param sDelim
	 *            the delimiter.
	 * 
	 * @return s string which assembles array elements or empty string if array
	 *         is empty or null
	 */
	public static String assemble( String[] asSrc, String sDelim ) {
		List< String > list = Arrays.asList( asSrc );
		return( assemble( list, sDelim ) );
	}

	/**
	 * Assembles an list elements to string using a delimiter string.
	 * 
	 * @param list
	 *            the list to assemble.
	 * @param sDelim
	 *            the delimiter.
	 * 
	 * @return s the string which assembles list elements or empty string if list
	 *         is empty or null
	 */
	public static String assemble( List< String > list, String sDelim ) {
		if( list == null || list.size( ) == 0 ) {
			return( "" );
		}
		StringBuffer s = new StringBuffer( );
		int iItem;
		for( iItem = 0; iItem < list.size( ) - 1; iItem++ ) {
			s.append( list.get( iItem ) );
			s.append( sDelim );
		}
		s.append( list.get( iItem ) );
		return( s.toString( ) );
	}

	/**
	 * Builds a string of the specified length by repeating the specified
	 * characters until the result string is long enough.
	 * 
	 * @param s
	 *            a string whose value will be repeated to fill the return
	 *            string
	 * @param iN
	 *            an integer whose value is the length of the string you want
	 *            returned
	 * 
	 * @return a string iN characters long filled with the characters in the
	 *         argument s. If the argument chars has more than iN characters,
	 *         the first iN characters of s are used to fill the return string.
	 *         If the argument s has fewer than iN characters, the characters in
	 *         s are repeated until the return string has iN characters. If any
	 *         argument's value is null, it returns null.
	 */
	public static String fill( String s, int iN ) {
		StringBuffer sb = new StringBuffer( );
		if( s.length( ) > iN ) {
			sb.append( s.substring( 0, iN ) );
		} else {
			int iCount = iN / s.length( );
			for( int i = 1; i <= iCount; i++ ) {
				sb.append( s );
			}
			if( iN - sb.length( ) > 0 ) {
				sb.append( s.substring( 0, iN - sb.length( ) ) );
			}
		}
		return( sb.toString( ) );
	}

	/**
	 * Returns true if input string is numeric and false otherwise
	 * 
	 * @param s
	 *            the string to test on numeric value
	 * 
	 * @return true if the string is a number; false otherwise.
	 */
	public static boolean isNumber( String s ) { 
	  return( numPattern.matcher( s ).matches( ) );  
	}

	/**
	 * Fixes ordinary and double quotes in input string
	 * 
	 * @param s
	 *            the input string
	 * 
	 * @return result string.
	 */
	public static String fixQuotes( String s ) {
		if( s == null || s.equals( "" ) ) {
			return( s );
		}
		s = s.replaceAll( "'", "~'" );
		return( s.replaceAll( "\"", "~\"" ) );
	}

	/**
	 * Converts integer to the byte array (little endian).
	 * 
	 * @param iValue
	 *            the integer to convert
	 * 
	 * @return byte array.
	 */
	public static final byte[] intToByteArray( int iValue ) {
	    return new byte[] {
	        ( byte )( iValue >>> 24 ),
	        ( byte )( iValue >>> 16 ),
	        ( byte )( iValue >>> 8 ),
	        ( byte )iValue
	    };
	}

	/**
	 * Converts byte array (little endian) to integer.
	 * 
	 * @param ab
	 *            the byte array to convert
	 * 
	 * @return integer.
	 */
	public static final int byteArrayToInt( byte[] ab ) {
	    return( ab[ 0 ] << 24 )
            + ( ( ab[ 1 ] & 0xFF ) << 16 )
            + ( ( ab[ 2 ] & 0xFF ) << 8 )
            + ( ab[ 3 ] & 0xFF );
	}

	/**
	 * Converts byte array (little endian) to integer.
	 * 
	 * @param ab
	 *            the byte array to convert
	 * 
	 * @return integer.
	 */
	public static final int byteArrayToInt( Byte[] ab ) {
	    return( ab[ 0 ] << 24 )
            + ( ( ab[ 1 ] & 0xFF ) << 16 )
            + ( ( ab[ 2 ] & 0xFF ) << 8 )
            + ( ab[ 3 ] & 0xFF );
	}

	/**
	 * Converts byte array to the hexadecimal string.
	 * 
	 * @param ab
	 *            the byte array to convert
	 * 
	 * @return hexadecimal string or null if input array is null.
	 */
	public static String getHex( byte[] ab ) {
		if( ab == null ) {
			return null;
		}
		final StringBuilder hex = new StringBuilder( 2 * ab.length );
		for( final byte b : ab ) {
			hex.append( HEXES.charAt( ( b & 0xF0 ) >> 4 ) ).append( HEXES.charAt( ( b & 0x0F ) ) );
		}
		return hex.toString( );
	}

	/**
	 * Converts byte array to the hexadecimal string.
	 * 
	 * @param ab
	 *            the byte array to convert
	 * 
	 * @return hexadecimal string or null if input array is null.
	 */
	public static String getHex( Byte[] ab ) {
		if( ab == null ) {
			return null;
		}
		final StringBuilder hex = new StringBuilder( 2 * ab.length );
		for( final byte b : ab ) {
			hex.append( HEXES.charAt( ( b & 0xF0 ) >> 4 ) ).append( HEXES.charAt( ( b & 0x0F ) ) );
		}
		return hex.toString( );
	}

	/**
	 * Converts integer in LSB to the integer in MSB. Usage:
	 * <pre>
	 * public static void main(String argv[]) {
	 *     before 0x01020304
	 *     after  0x04030201
	 *     int v = 0x01020304;
	 *     System.out.println("before : 0x" + Integer.toString(v,16));
	 *     System.out.println("after  : 0x" + Integer.toString(swabInt(v),16));
	 * }
	 * </pre>
	 * 
	 * @param i
	 *            integer to convert
	 * 
	 * @return integer in MSB first.
	 */
	public final static int swabInt( int i ) {
		return ( i >>> 24 ) | ( i << 24 ) | ( ( i << 8 ) & 0x00FF0000 ) | ( ( i >> 8 ) & 0x0000FF00 );
	}

	/**
	 * Converts hexadecimal string to the byte array.
	 * 
	 * @param s
	 *            the hexadecimal string to convert
	 * 
	 * @return byte array.
	 */
	public static byte[] hexStringToByteArray( String s ) {
	    int len = s.length( );
	    byte[] data = new byte[ len / 2 ];
	    for( int i = 0; i < len; i += 2 ) {
	    	data[ i / 2 ] = ( byte )( ( Character.digit( s.charAt( i ), 16 ) << 4 )
	                             + Character.digit( s.charAt( i + 1 ), 16 ) );
	    }
	    return data;
	}
	
	/**
	 * Returns resource bundle for given locale.
	 * 
	 * @param sBundle
	 *            the base name of the resource bundle, a fully qualified class
	 *            name
	 * @param locale
	 *            the locale object
	 * 
	 * @return resource bundle
	 */
	public static ResourceBundle getBundle( String sBundle, Locale locale ) {
		return( 
			ResourceBundle.getBundle( 
				sBundle, 
				locale,
                ResourceBundle.Control.getControl( ResourceBundle.Control.FORMAT_PROPERTIES )
            )
        );
	}

	/**
	 * Returns resource bundle for given locale.
	 * 
	 * @param locale
	 *            the locale object
	 * 
	 * @return resource bundle
	 */
	public static ResourceBundle getBundle( Locale locale ) {
		return( getBundle( "message", locale ) );
	}
}
