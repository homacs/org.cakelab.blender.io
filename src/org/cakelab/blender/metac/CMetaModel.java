package org.cakelab.blender.metac;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import org.cakelab.blender.io.Encoding;
import org.cakelab.blender.io.dna.DNAField;
import org.cakelab.blender.io.dna.DNAModel;
import org.cakelab.blender.io.dna.DNAStruct;
import org.cakelab.blender.metac.CType.CKind;


/**
 * CMetaModel is a meta model to describe types defined in C language.
 * <p>
 * For example a pointer in C is specified by its base type and its 
 * specification (e.g. base type 'int' and specification '**' makes a
 * pointer on a pointer on integer). Another example are arrays, such as
 * array of chars 'char[]'. Thus, a fully described type has
 * a type signature (e.g. 'int**' or 'char[32]') and related types. The 
 * 'int**' pointer references an 'int*' pointer which references an 'int' value 
 * and the char[32] array references 'char' values, 32 of them.
 * </p>
 * <p>
 * This meta model represents a given type specification in a chain 
 * of types, such as: 'pointer on' &#8594; 'pointer on' &#8594; 'int' as 
 * representation for 'int**' or 'array of' &#8594; 'pointer on' -> 'char' 
 * for char*[]. 
 * </p>
 * 
 * @see CType
 * @see CStruct
 * @see CField
 * 
 * @author homac
 *
 */
public class CMetaModel {

	HashMap<String, CType> types = new HashMap<String, CType>();
	ArrayList<CStruct> structs = new ArrayList<CStruct>();
	
	
	public CMetaModel(DNAModel model) {
		//
		// This method might look crazy but it has to be like 
		// this, since structs may have embedded structs.
		// This leads to (1) type of embedded struct is unknown 
		// when it gets processed or (2) type is known but size is
		// still unknown, since the embedded structs fields have not
		// been processed yet.
		//
		// Thus, the procedure here is to iterate over the structs 3 times:
		//
		// 1. register all existing structs as known types.
		// 2. add fields and their types to each struct.
		// 3. calculate sizes of structs until all structs know its size.
		
		//
		// register all struct types
		//
		for (DNAStruct bstruct : model.getStructs()) {
			CStruct struct = new CStruct(bstruct);
			types.put(bstruct.getType().getName(), struct);
			structs.add(struct);
		}
		
		//
		// add fields to struct types (including their types)
		//
		for (DNAStruct bstruct : model.getStructs()) {
			CStruct struct = (CStruct) types.get(bstruct.getType().getName());
			
			for (DNAField bfield : bstruct.getFields()) {
				
				String name = getFieldName(bfield);
				String typespec = getFieldTypeSpec(name, bfield.getSignatureName());
				String basetype = bfield.getType().getName();
				CType ctype = getType(basetype, typespec);
				CField cfield = new CField(name, ctype); 
				struct.addField(cfield);
				
			}
		}

		//
		// calculate size of struct types
		//
		LinkedList<CStruct> unresolved = new LinkedList<CStruct>(structs);
		do {
			int i = 0;
			do {
				CStruct struct = unresolved.get(i);
				
				boolean success = true;
				for (CField cfield : struct.getFields()) {
					CType ctype = cfield.getType();
					
					if (ctype.kind == CKind.TYPE_ARRAY) {
						// make sure the array type knows its size
						calcArraySize(ctype);
					}
					
					int size32 = ctype.sizeof(Encoding.ADDR_WIDTH_32BIT);
					if (size32 == 0) {
						if (ctype.kind == CKind.TYPE_VOID) {
							throw new IllegalArgumentException("error in struct size calculation. Struct contains field of type void");
						} 
						
						struct.size32 = 0;
						struct.size64 = 0;
						// ignore for now
						success = false;
						break;
					} else {
						struct.size32 += size32;
						struct.size64 += ctype.sizeof(Encoding.ADDR_WIDTH_64BIT);
					}
				}
				
				if (success) {
					// struct can be removed from unresolved
					unresolved.remove(i);
				} else {
					// skip current, we will retry in the next iteration of the outer while loop
					i++;
				}
			} while (i < unresolved.size());
		} while(!unresolved.isEmpty());
		
	}



	private CType getType(String basetype, String typespec) {
		CType type = types.get(basetype + typespec);
		if (type == null) {
			type = createType(basetype, typespec);
		}
		return type;
	}


	private CType createType(String basetype, String typespec) {
		CType ctype;
		String typesig = basetype + typespec;
		
		if (typespec.contains("(")) {
			ctype = new CType(typesig, CKind.TYPE_FUNCTION_POINTER, Encoding.ADDR_WIDTH_32BIT, Encoding.ADDR_WIDTH_64BIT);
		} else if (typespec.endsWith("]")) {
			return getArrayType(basetype, typespec);
		} else if (typespec.startsWith("*")) {
			return getPointerType(basetype, typespec);
		} else if (isScalar(typesig)) {
			ctype = new CType(typesig, CKind.TYPE_SCALAR, getScalarSize(basetype, Encoding.ADDR_WIDTH_32BIT), getScalarSize(basetype, Encoding.ADDR_WIDTH_32BIT));
		} else if (typesig.equals("void")) {
			ctype = new CType(typesig, CKind.TYPE_VOID, 0, 0);
		} else {
			ctype = null;
		}
		if (ctype != null) types.put(ctype.getSignature(), ctype);
		return ctype;
	}


	private CType getPointerType(String typename, String typespec) {
		CType ctype = new CType(typename + typespec, CKind.TYPE_POINTER, Encoding.ADDR_WIDTH_32BIT, Encoding.ADDR_WIDTH_64BIT);
		typespec = typespec.substring(1);
		ctype.referencedType = getType(typename, typespec);
		if (ctype.referencedType == null) {
			ctype.referencedType = getType("void", "");
		}
		
		return ctype;
	}


	public CType getArrayType(String typename, String typespec) {
		CType ctype = new CType(typename + typespec, CKind.TYPE_ARRAY);
		
		int start = typespec.indexOf('[');
		if (start < 0) throw new Error("inconsistent type declaration: " + typespec);
		
		int end = typespec.indexOf(']');
		String substr = typespec.substring(start + 1, end);
		ctype.arrayLength = Integer.valueOf(substr.trim());
		typespec = typespec.substring(0, start) + typespec.substring(end+1);
		
		ctype.referencedType = getType(typename, typespec);

		if (ctype.referencedType == null) {
			throw new IllegalArgumentException("unkown component type in array of fixed length");
		}
		
		calcArraySize(ctype);
		return ctype;
	}
	
	
	private void calcArraySize(CType ctype) {
		if (ctype.referencedType.kind == CKind.TYPE_ARRAY) {
			// array of arrays
			calcArraySize(ctype.referencedType);
		}
		
		if (ctype.size32 == 0  && ctype.referencedType.size32 != 0) {
			ctype.size32 = ctype.referencedType.size32 * ctype.arrayLength;
			ctype.size64 = ctype.referencedType.size64 * ctype.arrayLength;
		}
	}


	

	private String getFieldTypeSpec(String fieldName, String fieldSignature) {
		return fieldSignature.replaceFirst(fieldName, "");
	}

	private String getFieldName(DNAField field) {
		String name = field.getSignatureName();
		name = name.replaceAll("\\*", "");
		name = name.replaceAll("\\[.*\\]$", "");
		name = name.replaceAll("[()]", "");
		return name;
	}


	private boolean isScalar(String typeName) {
		return (typeName.equals("char")) 
				|| (typeName.equals("short"))
				|| (typeName.equals("ushort"))
				|| (typeName.equals("int"))
				|| (typeName.equals("uint"))
				|| (typeName.equals("long"))
				|| (typeName.equals("ulong"))
				|| (typeName.equals("int64_t"))
				|| (typeName.equals("uint64_t"))
				|| (typeName.equals("float"))
				|| (typeName.equals("double"))
				;
	}

	private int getScalarSize(String typeName, int addressWidth) {
		if (typeName.equals("char")) {
			return 1;
		} else if(typeName.equals("short") || typeName.equals("ushort")) {
			return 2;
		} else if(typeName.equals("int") || typeName.equals("uint") || typeName.equals("unsigned int")) {
			return 4;
		} else if(typeName.equals("long") || typeName.equals("ulong")) {
			if (addressWidth == Encoding.ADDR_WIDTH_32BIT) {
				return 4;
			} else {
				return 8;
			}
		} else if(typeName.equals("int64_t") || typeName.equals("uint64_t")) {
			return 8;
		} else if(typeName.equals("float")) {
			return 4;
		} else if(typeName.equals("double")) {
			return 8;
		} else {
			throw new IllegalArgumentException("unknown type name " + typeName);
		}
	}


	public ArrayList<CStruct> getStructs() {
		return structs;
	}


	public CStruct getStruct(int sdnaIndex) {
		return structs.get(sdnaIndex);
	}



	/**
	 * Retrieve a type description for a given type signature.
	 * @param typeSignature Signature of the type (e.g. a struct name such as "Scene" or "Mesh" or a type signature such as "int" or "int*" etc.)
	 * @return Associated type.
	 */
	public CType getType(String typeSignature) {
		return types.get(typeSignature);
	}

	
}
