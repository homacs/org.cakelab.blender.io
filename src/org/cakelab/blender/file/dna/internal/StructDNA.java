package org.cakelab.blender.file.dna.internal;

import java.io.IOException;
import java.util.Arrays;

import org.cakelab.blender.file.util.CStringUtils;
import org.cakelab.blender.file.util.CDataReadWriteAccess;
import org.cakelab.blender.file.util.Identifier;


/**
 * <p>
 * Struct DNA is stored in the file-block with code 'DNA1'. Its block's 
 * position in the file is arbitrary.
 * </p>
 * <p>
 * Struct DNA contains the meta-information of <b>all</b> data types 
 * (c-structs and scalar types) supported by the Blender version the 
 * file was created in, thus it can be very long. All data types 
 * considered are declared in one of the header files in directory
 * {@linkplain source/blender/makesdna} which also contains the code to
 * generate this information in the file. 
 * {@linkplain source/blender/makesdna/DNA_documentation.h} describes
 * the rules for the declaration of structs.
 * </p>
 * <p>
 * A C struct is described by its type name, a length and its fields 
 * (member variables). Each field has a name and a basic type which 
 * can be scalar (char, int, double, etc.) or a struct. Furthermore
 * the field can be a pointer, an array or an array of pointers or
 * arrays etc.. This information is encoded in the fields name the
 * way a variable declaration in C would contain it.
 * </p>
 * <p>
 * <b>Example meta-information on a field:</b>
 * <table border="1">
 * <tr align="center"><td>type</td><td>name</td></tr>
 * <tr align="center"><td>int</td><td>*verts[128]</td></td>
 * </table>
 * </p>
 * <p>
 * Thus, the actual name of a field has to be extracted and the type is
 * a combination of both: type and name information.
 * <p>
 */
public class StructDNA {
	public class Struct {
		public class Field {
			/** type of field as index in {@link StructDNA#types} */
			public short type;
			/** name of field as index in {@link StructDNA#names} */
			public short name;
			
			public void read(CDataReadWriteAccess in) throws IOException {
				type = in.readShort();
				name = in.readShort();
			}

			@Override
			public String toString() {
				return "Field [type=" + type + ", name=" + name + "]";
			}
			
		}
		/** Index in {@link StructDNA#types} containing the name of the structure. */
		public short type;
		/** Number of fields in this structure */
		public short fields_len;
		/** Fields (member) of the structure (class). */
		public Field[] fields;
		
		public void read(CDataReadWriteAccess in) throws IOException {
			type = in.readShort();
			fields_len = in.readShort();
			fields = new Field[fields_len];
			for (int i = 0; i < fields_len; i++) {
				fields[i] = new Field();
				fields[i].read(in);
			}
		}

		@Override
		public String toString() {
			return "Struct [type=" + type + ", fields_len=" + fields_len
					+ ", fields=" + Arrays.toString(fields) + "]";
		}
		
	}
	
	/** 'SDNA' marks the begin of the SDNA struct.*/
	static final Identifier SDNA = new Identifier("SDNA");
	
	/* 
	 * List of variable names
	 */
 	/** 'NAME' marks beginning of the name list */
	static final Identifier NAME = new Identifier("NAME");
 	/** number of names to follow */
	public int names_len;
 	/** list of names. Each name is a Zero terminated string. 
 	 * These names can contain pointer and simple array definitions (e.g. '*vertex[3]\0') */
 	public String[] names;
 	
 	/*
 	 * List of variable types
 	 */
 	/** 'TYPE' marks the beginning of the types list.*/
 	static final Identifier TYPE = new Identifier("TYPE");
 	/** length of the type list */
 	public int types_len;
 	/** list of type names as zero terminated strings.*/
 	public String[] types;
 	
 	/*
 	 * List of type lengths
 	 */
 	/** 'TLEN' marks the beginning of the list of type lengths.*/
 	static final Identifier TLEN = new Identifier("TLEN");
	/** list with the length of each type in bytes. */
 	public short[] type_lengths;
 	
 	/*
 	 * List of structures
 	 */
 	/** 'STRC' marks the beginning of the list of struct types.*/
 	static final Identifier STRC = new Identifier("STRC");
 	/** length of the struct type list */
 	public int structs_len;
	/** structure informations */
 	public Struct[] structs;
 	
 	
 	
 	public void read(CDataReadWriteAccess in) throws IOException {
 		Identifier ident = new Identifier();
 		ident.consume(in, SDNA);
 		ident.consume(in, NAME);
 		names_len = in.readInt();
 		names = new String[names_len];
 		for (int i = 0; i < names_len; i++) {
 			names[i] = CStringUtils.readNullTerminatedString(in, true);
 		}
 		
 		in.padding(4);
 		ident.consume(in, TYPE);
 		types_len = in.readInt();
 		types = new String[types_len];
 		for (int i = 0; i < types_len; i++) {
 			types[i] = CStringUtils.readNullTerminatedString(in, true);
 		}
 		
 		in.padding(4);
 		ident.consume(in, TLEN);
 		type_lengths = new short[types_len];
 		for (int i = 0; i < types_len; i++) {
 			type_lengths[i] = in.readShort();
 		}
 	 	
 		in.padding(4);
 		ident.consume(in, STRC);
 		structs_len = in.readInt();
 		structs = new Struct[structs_len];
 		for (int i = 0; i < structs_len; i++) {
 			structs[i] = new Struct();
 			structs[i].read(in);
 			
 		}
 	 	
	}



	@Override
	public String toString() {
		return "StructDNA {\n"
						+ "\tnames_len=" + names_len + "\n"
						+ "\tnames=["
						+ Arrays.toString(names) + "\n"
						+ "\ttypes_len=" + types_len + "\n"
						+ "\ttypes=[" + "\n"
						+ Arrays.toString(types) + "\n"
						+ "\ttype_lengths=[" + "\n"
						+ Arrays.toString(type_lengths) + "\n"
						+ "structs_len=" + structs_len  + "\n"
						+ "structs=[" + "\n"
						+ Arrays.toString(structs) + "\n"
						+ "]\n";
	}
 	
 	
}
