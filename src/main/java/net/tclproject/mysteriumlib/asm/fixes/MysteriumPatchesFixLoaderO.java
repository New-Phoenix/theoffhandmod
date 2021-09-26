package net.tclproject.mysteriumlib.asm.fixes;

import mods.battlegear2.coremod.transformers.EntityAIControlledByPlayerTransformer;
import mods.battlegear2.coremod.transformers.EntityOtherPlayerMPTransformer;
import mods.battlegear2.coremod.transformers.EntityPlayerTransformer;
import mods.battlegear2.coremod.transformers.ItemInWorldTransformer;
import mods.battlegear2.coremod.transformers.ItemRendererTransformer;
import mods.battlegear2.coremod.transformers.ItemStackTransformer;
import mods.battlegear2.coremod.transformers.MinecraftTransformer;
import mods.battlegear2.coremod.transformers.NetClientHandlerTransformer;
import mods.battlegear2.coremod.transformers.NetServerHandlerTransformer;
import mods.battlegear2.coremod.transformers.PlayerControllerMPTransformer;
import mods.battlegear2.coremod.transformers.TransformerBase;
import net.tclproject.mysteriumlib.asm.common.CustomClassTransformer;
import net.tclproject.mysteriumlib.asm.common.CustomLoadingPlugin;
import net.tclproject.mysteriumlib.asm.common.FirstClassTransformer;

public class MysteriumPatchesFixLoaderO extends CustomLoadingPlugin {

	TransformerBase[] bt_transformers = {
		new EntityPlayerTransformer(),
		new NetClientHandlerTransformer(),
		new NetServerHandlerTransformer(),
		new PlayerControllerMPTransformer(),
		new ItemRendererTransformer(),
		new MinecraftTransformer(),
		new ItemStackTransformer(),
		new ItemInWorldTransformer(),
		new EntityAIControlledByPlayerTransformer(),
		new EntityOtherPlayerMPTransformer()
    };
	
    // Turns on MysteriumASM Lib. You can do this in only one of your Fix Loaders.
    @Override
    public String[] getASMTransformerClass() 
    {
        return new String[]{
    			FirstClassTransformer.class.getName()
    			};
    }

    @Override
    public void registerFixes() 
    {
    	for (TransformerBase transformer : bt_transformers) {
    		CustomClassTransformer.registerPostTransformer(transformer);
    	}
        //Registers the class where the methods with the @Fix annotation are
    	registerClassWithFixes("net.tclproject.mysteriumlib.asm.fixes.MysteriumPatchesFixesO");
    }
}
