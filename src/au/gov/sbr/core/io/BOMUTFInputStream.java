package au.gov.sbr.core.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

public class BOMUTFInputStream extends PushbackInputStream {

	/** UTF-8 BOM */
	private final byte[] BOM_UTF8 = { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF };
	/** UTF-16 Little Endian */
	private final byte[] BOM_UTF16_LITTLE_ENDIAN = { (byte) 0xFF, (byte) 0xFE };
	/** UTF-16 Big Endian */
	private final byte[] BOM_UTF16_BIG_ENDIAN = { (byte) 0xFE, (byte) 0xFF };
	/** UTF-8 Encoding Description */
	private final String UTF8 = "UTF-8";
	/** UTF-16 Little Endian Encoding Description */
	private final String UTF16_LITTLE_ENDIAN = "UTF-16 Little Endian";
	/** UTF-16 Big Endian Encoding Description */
	private final String UTF16_BIG_ENDIAN = "UTF-16 Big Endian";

	/** Determine if BOM should be skipped */
	private boolean skipped = false;
	/** BOM Description */
	private String description;
	/** Determine if UTF8 BOM was found */
	private boolean utfBomFound = false;

	/**
	 * Read a BOM UTF-8 encoded input stream.
	 * @param is InputStream
	 * @throws IOException
	 */
	public BOMUTFInputStream(InputStream is) throws IOException {
		super (is, 3);

		if (is == null)
			throw new IllegalArgumentException("Input stream must not be null");

		final byte bom[] = new byte[3];
		final int read  = read(bom);
		if (bom[0] == BOM_UTF8[0] &&
			bom[1] == BOM_UTF8[1] &&
			bom[2] == BOM_UTF8[2]) {
			description = UTF8;
			utfBomFound = true;
		} else if (bom[0] == BOM_UTF16_LITTLE_ENDIAN[0] &&
			       bom[1] == BOM_UTF16_LITTLE_ENDIAN[1]) {
			description = UTF16_LITTLE_ENDIAN;
			utfBomFound = true;
		} else if (bom[0] == BOM_UTF16_BIG_ENDIAN[0] &&
			       bom[1] == BOM_UTF16_BIG_ENDIAN[1]) {
			description = UTF16_BIG_ENDIAN;
			utfBomFound = true;			
		} 
		else {
			description = "none or non UTF-8 or UTF-16";
			utfBomFound = false;
		}

		if (read > 0) {
			unread(bom,0,read);
		}
	}

	/**
	 * Determine if a UTF8 BOM was found
	 * @return boolean - UTF8 BOM found
	 */
	public boolean hasUTFBom() {
		return utfBomFound;
	}

    /**
     * Returns a <code>String</code> representation of this <code>BOM</code>
     * value.
     */
    public final String toString() {
    	return description;
    }

	/**
	   * Skips the <code>BOM</code> that was found in the wrapped
	   * <code>InputStream</code> object.
	   * 
	   * @return this <code>BOMUTF8InputStream</code>.
	   * 
	   * @throws IOException when trying to skip the BOM from the wrapped
	   * <code>InputStream</code> object.
	   */
	  public final synchronized BOMUTFInputStream skipBOM() throws IOException
	  {
		  if (!skipped)
		  {
			  if (description.equals(UTF8)) {
				  skip(BOM_UTF8.length);
				  skipped = true;
			  } else if (description.equals(UTF16_LITTLE_ENDIAN)) {
				  skip(BOM_UTF16_LITTLE_ENDIAN.length);
				  skipped = true;				  
			  } else if (description.equals(UTF16_BIG_ENDIAN)) {
				  skip(BOM_UTF16_BIG_ENDIAN.length);
				  skipped = true;				  
			  }
		  }

		  return this;
	  }	
}
