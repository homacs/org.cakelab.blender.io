package org.cakelab.blender.generator.typemap;

import org.cakelab.blender.metac.CType;
import org.cakelab.blender.nio.CArrayFacade;
import org.cakelab.blender.nio.CPointer;



/**
 * Provides mapping of a given CType instance to a Java type 
 * according to Java .Blend's type mapping rules.
 * 
 * @author homac
 *
 */
public class JavaType {

	public enum JKind {
		TYPE_FUNCTION_POINTER,
		TYPE_ARRAY, 
		TYPE_SCALAR, 
		TYPE_OBJECT;
	}
	
	
	JKind kind;
	String name;
	JavaType referencedType;

	public JavaType(CType ctype) {
		switch(ctype.getKind()) {
		case TYPE_ARRAY:
			mapArray(ctype);
			break;
		case TYPE_FUNCTION_POINTER:
			mapFunctionPointer(ctype);
			break;
		case TYPE_POINTER:
			mapPointer(ctype);
			break;
		case TYPE_SCALAR:
			mapScalar(ctype);
			break;
		case TYPE_STRUCT:
			mapStruct(ctype);
			break;
		case TYPE_VOID:
			mapVoid(ctype);
			break;
		default:
			break;
		}
	}



	private void mapVoid(CType ctype) {
		this.kind = JKind.TYPE_OBJECT;
		name = Object.class.getSimpleName();
	}



	private void mapStruct(CType ctype) {
		this.kind = JKind.TYPE_OBJECT;
		if (ctype.size32 == 0) {
			name = Object.class.getSimpleName();
		} else {
			name = Renaming.mapStruct2Class(ctype.getSignature());
		}
	}



	private void mapScalar(CType ctype) {
		this.kind = JKind.TYPE_SCALAR;
		
		String signature = ctype.getSignature();
		
		if (signature.equals("char")) {
			name = "byte";
		} else if (signature.equals("short") || signature.equals("ushort")) {
			name = "short";
		} else if (signature.equals("int") || signature.equals("unsigned int")) {
			name = "int";
		} else if (signature.equals("long") 
				|| signature.equals("ulong")
				|| signature.equals("int64_t")
				|| signature.equals("uint64_t")
				) {
			name = "long";
		} else if (signature.equals("float")) {
			name = "float";
		} else if (signature.equals("double")) {
			name = "double";
		} else {
			throw new Error("unexpected type name " + signature);
		}
	}



	private void mapPointer(CType ctype) {
		this.kind = JKind.TYPE_OBJECT;
		this.name = CPointer.class.getSimpleName();
		this.referencedType = new JavaType(ctype.getReferencedType());
	}



	private void mapFunctionPointer(CType ctype) {
		this.kind = JKind.TYPE_FUNCTION_POINTER;
		this.name = long.class.getSimpleName();
	}



	private void mapArray(CType ctype) {
		this.kind = JKind.TYPE_OBJECT;
		this.name = CArrayFacade.class.getSimpleName();
		this.referencedType = new JavaType(ctype.getReferencedType());
	}



	public String getName() {
		if (kind == JKind.TYPE_ARRAY) {
			String basicType = getArrayBasicType().name;
			String suffix = getArrayTypeSuffix();
			return basicType + suffix;
		} else {
			return name;
		}
	}



	private String getArrayTypeSuffix() {
		String suffix = "[]";
		JavaType inner = referencedType;
		while (inner.kind == JKind.TYPE_ARRAY) {
			inner = inner.referencedType;
			suffix += "[]";
		}
		return suffix;
	}



	JavaType getArrayBasicType() {
		JavaType inner = referencedType;
		while (inner.kind == JKind.TYPE_ARRAY) {
			inner = inner.referencedType;
		}
		
		return inner;
	}



	public JKind getKind() {
		return kind;
	}



	public JavaType getReferencedType() {
		return referencedType;
	}




}
