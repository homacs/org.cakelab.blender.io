package org.cakelab.blender.io;

import java.io.IOException;
import java.nio.charset.Charset;

import org.cakelab.blender.io.FileHeader.Version;
import org.cakelab.blender.io.util.CDataReadWriteAccess;
import org.cakelab.blender.io.util.CStringUtils;


/**
 * This class corresponds to struct FileGlobal in DNA.
 * It is stored in a block with code "GLOB".
 * 
 * We need this information here to determine the minimum
 * and maximum blender version, the generated import/export 
 * code can understand.
 * 
 * It is not meant to be used by API programmers. There is a
 * facet called FileGlobal which has to be used instead for
 * compatibility reasons.
 * 
 * @author homac
 *
 */
public class FileVersionInfo {
	/**
	 * char subvstr[4];
	 */
	String subvstr;
	/**
	 * short subversion;
	 */
	short subversion;
	/**
	 * short pads;
	 */
	short pads;
	/**
	 * short minversion;
	 */
	short minversion;
	/**
	 * short minsubversion;
	 */
	short minsubversion;
	/**
	 * short displaymode;
	 */
	short displaymode;
	/**
	 * short winpos;
	 */
	short winpos;
	/**
	 * struct bScreen *curscreen;
	 */
	long bScreen;
	/**
	 * struct Scene *curscene;
	 */
	long curscreen;
	/**
	 * int fileflags;
	 */
	int minflags;
	/**
	 * int globalf;
	 */
	int globalf;
	/**
	 * svn revision from buildinfo 
	 * 
	 * int revision;
	 */
	int revision;
	/**
	 * int pad;
	 */
	int pad;
	/**
	 * file path where this was saved, for recover
	 * 
	 * char filename[1024]; 1024 = FILE_MAX
	 */
	String filename;
	

	
	/**
	 * This is NOT a member of FileGlobal. Its value is received from FileHeader.
	 */
	Version version;
	
	
	/**
	 * 
	 * @param cin
	 * @throws IOException
	 */
	public void read(CDataReadWriteAccess cin) throws IOException {
		byte[] buf = new byte[4];
		cin.readFully(buf);
		subvstr = CStringUtils.toString(buf, true);
		
		subversion = cin.readShort();
		
		pads = cin.readShort();
		
		minversion = cin.readShort();
		
		minsubversion = cin.readShort();
		
		displaymode = cin.readShort();
		
		winpos = cin.readShort();
		
		bScreen = cin.readLong();
		
		curscreen = cin.readLong();
		
		minflags = cin.readInt();
		
		globalf = cin.readInt();
		
		revision = cin.readInt();
		
		pad = cin.readInt();
		
		buf = new byte[1024];
		cin.readFully(buf);
		filename = CStringUtils.toNullTerminatedString(buf, Charset.forName("UTF-8"));
	}

	public String getSubvstr() {
		return subvstr;
	}

	public short getSubversion() {
		return subversion;
	}

	public short getMinversion() {
		return minversion;
	}

	public short getMinsubversion() {
		return minsubversion;
	}

	public int getMinflags() {
		return minflags;
	}

	public int getGlobalf() {
		return globalf;
	}

	public int getRevision() {
		return revision;
	}

	public String getFilename() {
		return filename;
	}

	public Version getVersion() {
		return version;
	}
	
	
	
}
