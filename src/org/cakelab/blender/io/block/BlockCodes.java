package org.cakelab.blender.io.block;

import org.cakelab.blender.io.dna.internal.StructDNA;
import org.cakelab.blender.io.util.Identifier;

/**
 * Block codes associate a block with a certain group of data
 * such as Library, Object, Scene etc. but most of the blocks
 * nowadays have a generic code called 'DATA'. The most 
 * important codes are 'ENDB' and 'DNA1' which are both unique
 * in a Blender file.
 * </p>
 * <b>List of known block codes:</b>
 * <table border="1">
 * <tr><td>code</td><td>description</td></tr>
 * <tr><td>ENDB</td><td>Marks the end of the Blender file.</td></tr>
 * <tr><td>DNA1</td><td>Contains the {@link StructDNA}</td></tr>
 * <tr><td>DATA</td><td>Arbitrary data.</td></tr>
 * </table>
 * <p>

 * @author homac
 *
 */
public interface BlockCodes {


	/* all known block codes as of Blender v2.80 
	 * see 'source/blender/makesdna/DNA_ID.h' */
	
	/** Scene */
	Identifier ID_SCE = new Identifier(new byte[]{'S', 'C', 0, 0});
	/** Library */
	Identifier ID_LI = new Identifier(new byte[]{'L', 'I', 0, 0});
	/** Object */
	Identifier ID_OB = new Identifier(new byte[]{'O', 'B', 0, 0});
	/** Mesh */
	Identifier ID_ME = new Identifier(new byte[]{'M', 'E', 0, 0});
	/** Curve */
	Identifier ID_CU = new Identifier(new byte[]{'C', 'U', 0, 0});
	/** MetaBall */
	Identifier ID_MB = new Identifier(new byte[]{'M', 'B', 0, 0});
	/** Material */
	Identifier ID_MA = new Identifier(new byte[]{'M', 'A', 0, 0});
	/** Texture */
	Identifier ID_TE = new Identifier(new byte[]{'T', 'E', 0, 0});
	/** Image */
	Identifier ID_IM = new Identifier(new byte[]{'I', 'M', 0, 0});
	/** Lattice */
	Identifier ID_LT = new Identifier(new byte[]{'L', 'T', 0, 0});
	/** Lamp */
	Identifier ID_LA = new Identifier(new byte[]{'L', 'A', 0, 0});
	/** Camera */
	Identifier ID_CA = new Identifier(new byte[]{'C', 'A', 0, 0});
	/** Ipo (depreciated, replaced by FCurves) */
	Identifier ID_IP = new Identifier(new byte[]{'I', 'P', 0, 0});
	/** Key (shape key) */
	Identifier ID_KE = new Identifier(new byte[]{'K', 'E', 0, 0});
	/** World */
	Identifier ID_WO = new Identifier(new byte[]{'W', 'O', 0, 0});
	/** Screen */
	Identifier ID_SCR = new Identifier(new byte[]{'S', 'R', 0, 0});
	/** VectorFont */
	Identifier ID_VF = new Identifier(new byte[]{'V', 'F', 0, 0});
	 /** Text */
	Identifier ID_TXT = new Identifier(new byte[]{'T', 'X', 0, 0});
	/** Speaker */
	Identifier ID_SPK = new Identifier(new byte[]{'S', 'K', 0, 0});
	/** Sound */
	Identifier ID_SO = new Identifier(new byte[]{'S', 'O', 0, 0});
	/** Group */
	Identifier ID_GR = new Identifier(new byte[]{'G', 'R', 0, 0});
	/** Armature */
	Identifier ID_AR = new Identifier(new byte[]{'A', 'R', 0, 0});
	/** Action */
	Identifier ID_AC = new Identifier(new byte[]{'A', 'C', 0, 0});
	/** Script (depreciated) */
	// Identifier ID_SCRIPT = new Identifier(new byte[]{'P', 'Y', 0, 0}); // no longer exists since 2.80
	/** NodeTree */
	Identifier ID_NT = new Identifier(new byte[]{'N', 'T', 0, 0});
	/** Brush */
	Identifier ID_BR = new Identifier(new byte[]{'B', 'R', 0, 0});
	/** ParticleSettings */
	Identifier ID_PA = new Identifier(new byte[]{'P', 'A', 0, 0});
	/** GreasePencil */
	Identifier ID_GD = new Identifier(new byte[]{'G', 'D', 0, 0});
	/** WindowManager */
	Identifier ID_WM = new Identifier(new byte[]{'W', 'M', 0, 0});
	/** MovieClip */
	Identifier ID_MC = new Identifier(new byte[]{'M', 'C', 0, 0});
	/** Mask */
	Identifier ID_MSK = new Identifier(new byte[]{'M', 'S', 0, 0});
	/** FreestyleLineStyle */
	Identifier ID_LS = new Identifier(new byte[]{'L', 'S', 0, 0}); 
	/** Palette */
	Identifier ID_PAL = new Identifier(new byte[]{'P', 'L', 0, 0}); 
	/** Paint Curve */
	Identifier ID_PC = new Identifier(new byte[]{'P', 'C', 0, 0}); 
	/** Cache File */
	Identifier ID_CF = new Identifier(new byte[]{'C', 'F', 0, 0}); 
	/** Work Space */
	Identifier ID_WS = new Identifier(new byte[]{'W', 'S', 0, 0}); 
	/** LightProbe */
	Identifier ID_LP = new Identifier(new byte[]{'L', 'P', 0, 0}); 
	
	
	/** Only used as 'placeholder' in .blend files for directly linked data-blocks. */
	Identifier ID_ID = new Identifier(new byte[]{'I', 'D', 0, 0});
	/** depreciated, but still heavily in use */
	Identifier ID_SCRN = new Identifier(new byte[]{'S', 'N', 0, 0});

	
	/** NOTE! Fake IDs, needed for g.sipo->blocktype or outliner */
	Identifier ID_SEQ = new Identifier(new byte[]{'S', 'Q', 0, 0});
	/** constraint.
	 * <br/>NOTE! Fake IDs, needed for g.sipo->blocktype or outliner. */
	Identifier ID_CO = new Identifier(new byte[]{'C', 'O', 0, 0});
	/** pose (action channel, used to be ID_AC in code, so we keep code for backwards compat)
	 * <br/>NOTE! Fake IDs, needed for g.sipo->blocktype or outliner. */
	Identifier ID_PO = new Identifier(new byte[]{'A', 'C', 0, 0});
	/** used in outliner... 
	 * <br/>NOTE! Fake IDs, needed for g.sipo->blocktype or outliner.*/
	Identifier ID_NLA = new Identifier(new byte[]{'N', 'L', 0, 0});
	/** fluidsim Ipo 
	 * <br/>NOTE! Fake IDs, needed for g.sipo->blocktype or outliner.*/
	Identifier ID_FLUIDSIM = new Identifier(new byte[]{'F', 'S', 0, 0});
	
	
	
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

}
