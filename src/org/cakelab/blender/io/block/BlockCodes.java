package org.cakelab.blender.io.block;

import org.cakelab.blender.io.dna.internal.StructDNA;
import org.cakelab.blender.io.util.Identifier;

/**
 * Block codes associate a block with a certain group of data
 * such as Library, Object, Scene etc. but most of the blocks
 * nowadays have a generic code called 'DATA'. The most 
 * important codes are 'ENDB' and 'DNA1' which are both unique
 * in a Blender file.
 * @author homac
 *
 */
public interface BlockCodes {


	/* all known block codes as of Blender v2.83 
	 * see 'source/blender/makesdna/DNA_ID.h' */
	
	/** Scene */
	Identifier ID_SCE = MAKE_ID2('S', 'C');
	/** Library */
	Identifier ID_LI = MAKE_ID2('L', 'I');
	/** Object */
	Identifier ID_OB = MAKE_ID2('O', 'B');
	/** Mesh */
	Identifier ID_ME = MAKE_ID2('M', 'E');
	/** Curve */
	Identifier ID_CU = MAKE_ID2('C', 'U');
	/** MetaBall */
	Identifier ID_MB = MAKE_ID2('M', 'B');
	/** Material */
	Identifier ID_MA = MAKE_ID2('M', 'A');
	/** Texture */
	Identifier ID_TE = MAKE_ID2('T', 'E');
	/** Image */
	Identifier ID_IM = MAKE_ID2('I', 'M');
	/** Lattice */
	Identifier ID_LT = MAKE_ID2('L', 'T');
	/** Lamp */
	Identifier ID_LA = MAKE_ID2('L', 'A');
	/** Camera */
	Identifier ID_CA = MAKE_ID2('C', 'A');
	/** Ipo (depreciated, replaced by FCurves) */
	Identifier ID_IP = MAKE_ID2('I', 'P');
	/** Key (shape key) */
	Identifier ID_KE = MAKE_ID2('K', 'E');
	/** World */
	Identifier ID_WO = MAKE_ID2('W', 'O');
	/** Screen */
	Identifier ID_SCR = MAKE_ID2('S', 'R');
	/** VectorFont */
	Identifier ID_VF = MAKE_ID2('V', 'F');
	 /** Text */
	Identifier ID_TXT = MAKE_ID2('T', 'X');
	/** Speaker */
	Identifier ID_SPK = MAKE_ID2('S', 'K');
	/** Sound */
	Identifier ID_SO = MAKE_ID2('S', 'O');
	/** Group */
	Identifier ID_GR = MAKE_ID2('G', 'R');
	/** Armature */
	Identifier ID_AR = MAKE_ID2('A', 'R');
	/** Action */
	Identifier ID_AC = MAKE_ID2('A', 'C');
	/** Script (depreciated) */
	// Identifier ID_SCRIPT = MAKE_ID2('P', 'Y'); // no longer exists since 2.80
	/** NodeTree */
	Identifier ID_NT = MAKE_ID2('N', 'T');
	/** Brush */
	Identifier ID_BR = MAKE_ID2('B', 'R');
	/** ParticleSettings */
	Identifier ID_PA = MAKE_ID2('P', 'A');
	/** GreasePencil */
	Identifier ID_GD = MAKE_ID2('G', 'D');
	/** WindowManager */
	Identifier ID_WM = MAKE_ID2('W', 'M');
	/** MovieClip */
	Identifier ID_MC = MAKE_ID2('M', 'C');
	/** Mask */
	Identifier ID_MSK = MAKE_ID2('M', 'S');
	/** FreestyleLineStyle */
	Identifier ID_LS = MAKE_ID2('L', 'S'); 
	/** Palette */
	Identifier ID_PAL = MAKE_ID2('P', 'L'); 
	/** Paint Curve */
	Identifier ID_PC = MAKE_ID2('P', 'C'); 
	/** Cache File */
	Identifier ID_CF = MAKE_ID2('C', 'F'); 
	/** Work Space */
	Identifier ID_WS = MAKE_ID2('W', 'S'); 
	/** LightProbe */
	Identifier ID_LP = MAKE_ID2('L', 'P'); 
	
	/* SINCE v2.83 */
	/** Hair */
	Identifier ID_HA = MAKE_ID2('H', 'A');
	/** PointCloud */
	Identifier ID_PT = MAKE_ID2('P', 'T');
	/** Volume */
	Identifier ID_VO = MAKE_ID2('V', 'O');  
	
	/* SINCE v2.90 */
	/** Simulation */
	Identifier ID_SIM = MAKE_ID2('S', 'I');
	
	
	/** Only used as 'placeholder' in .blend files for directly linked data-blocks. */
	Identifier ID_ID = MAKE_ID2('I', 'D');
	/** depreciated, but still heavily in use */
	Identifier ID_SCRN = MAKE_ID2('S', 'N');

	
	/** NOTE! Fake IDs, needed for g.sipo->blocktype or outliner */
	Identifier ID_SEQ = MAKE_ID2('S', 'Q');
	/** constraint.
	 * <br/>NOTE! Fake IDs, needed for g.sipo->blocktype or outliner. */
	Identifier ID_CO = MAKE_ID2('C', 'O');
	/** pose (action channel, used to be ID_AC in code, so we keep code for backwards compat)
	 * <br/>NOTE! Fake IDs, needed for g.sipo->blocktype or outliner. */
	Identifier ID_PO = MAKE_ID2('A', 'C');
	/** used in outliner... 
	 * <br/>NOTE! Fake IDs, needed for g.sipo->blocktype or outliner.*/
	Identifier ID_NLA = MAKE_ID2('N', 'L');
	/** fluidsim Ipo 
	 * <br/>NOTE! Fake IDs, needed for g.sipo->blocktype or outliner.*/
	Identifier ID_FLUIDSIM = MAKE_ID2('F', 'S');
	
	
	
	/** block code of the last block. */
	Identifier ID_ENDB = new Identifier("ENDB");
	/** block code of the block containing the {@link StructDNA} struct. */
	Identifier ID_DNA1 = new Identifier("DNA1");
	/** Block code of a block containing struct {@link Link}. */
	Identifier ID_REND = new Identifier("REND");
	/** Block code of a block containing struct {@link Link}. */
	Identifier ID_TEST = new Identifier("TEST");
	/** Block code of a block containing struct {@link FileGlobal}. */
	Identifier ID_GLOB = new Identifier("GLOB");
	/** Block code of a block containing data related to other blocks. */
	Identifier ID_DATA = new Identifier("DATA");
	
	
	
	
	static Identifier MAKE_ID2(char c, char d) {
		return new Identifier(new byte[]{(byte) c, (byte) d, 0, 0});
	}

}
