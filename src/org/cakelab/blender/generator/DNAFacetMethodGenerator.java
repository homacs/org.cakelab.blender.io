package org.cakelab.blender.generator;

import org.cakelab.blender.generator.code.ClassGenerator;
import org.cakelab.blender.generator.code.MethodGenerator;

public abstract class DNAFacetMethodGenerator extends MethodGenerator {
	protected static final String MEMBER__dna__blockTable = "__dna__blockTable";

	public DNAFacetMethodGenerator(ClassGenerator classGenerator) {
		super(classGenerator);
	}

}
