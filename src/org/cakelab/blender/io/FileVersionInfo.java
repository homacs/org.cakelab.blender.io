package org.cakelab.blender.io;

import java.io.IOException;

import org.cakelab.blender.io.FileHeader.Version;
import org.cakelab.blender.io.util.CDataReadWriteAccess;
import org.cakelab.blender.metac.CField;
import org.cakelab.blender.metac.CStruct;
import org.cakelab.blender.metac.CType;
import org.cakelab.blender.metac.CType.CKind;


/**
 * This class reads the version specifiers from struct FileGlobal
 * which is stored in block "GLOB".
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
	 * subversion;
	 */
	int subversion;
	/**
	 * minversion;
	 */
	int minversion;
	/**
	 * minsubversion;
	 */
	int minsubversion;
	
	/**
	 * This is NOT a member of FileGlobal. Its value is received from FileHeader.
	 */
	Version version;
	
	
	/**
	 * 
	 * @param struct type info for FileGlobal
	 * @param cin
	 * @throws IOException
	 */
	public void read(CStruct struct, CDataReadWriteAccess cin) throws IOException {
		// need to find 3 fields in the struct
		int remaining = 3;
		for (CField field : struct.getFields()) {
			if (field.getName().equals("subversion")) {
				subversion = getIntegerValue(field, cin);
				remaining--;
			} else if (field.getName().equals("minversion")) {
				minversion = getIntegerValue(field, cin);
				remaining--;
			} else if (field.getName().equals("minsubversion")) {
				minsubversion = getIntegerValue(field, cin);
				remaining--;
			} else {
				skipField(field, cin);
			}
			if (remaining == 0) {
				// all necessary fields read -> leave
				break;
			}
		}
		
		if (remaining != 0) {
			throw new IOException("didn't found all required version specifiers in FileGlobal");
		}
	}

	private void skipField(CField field, CDataReadWriteAccess cin) throws IOException {
		cin.skip(field.getType().sizeof(cin.getPointerSize()));
	}

	private int getIntegerValue(CField field, CDataReadWriteAccess cin) throws IOException {
		CType type = field.getType();
		if (type.getKind().equals(CKind.TYPE_SCALAR)) {
			String typeName = type.getSignature();
			if (typeName.contains("short")) {
				return cin.readShort();
			} else if (typeName.contains("int")) {
				return cin.readInt();
			} else if (typeName.contains("int64")) {
				return (int) cin.readInt64();
			} else if (typeName.contains("long")) {
				return (int) cin.readLong();
			}
		}
		throw new IOException("version specifier field not an integer value");
	}

	public int getSubversion() {
		return subversion;
	}

	public int getMinversion() {
		return minversion;
	}

	public int getMinsubversion() {
		return minsubversion;
	}

	public Version getVersion() {
		return version;
	}
	
	
	
}
