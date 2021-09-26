package mods.battlegear2.client.gui;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import mods.battlegear2.Battlegear;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;

public final class BattlegearGuiKeyHandler {

	private final KeyBinding battleInv, openSigilEditor;
    public static final BattlegearGuiKeyHandler INSTANCE = new BattlegearGuiKeyHandler();

    private BattlegearGuiKeyHandler() {
        battleInv = new KeyBinding("Battle Inventory", Keyboard.KEY_I, "key.categories.inventory");
        openSigilEditor = new KeyBinding("Open Sigil Editor", Keyboard.KEY_P, "key.categories.misc");
		ClientRegistry.registerKeyBinding(battleInv);
        ClientRegistry.registerKeyBinding(openSigilEditor);
	}

	@SubscribeEvent
	public void keyDown(InputEvent.KeyInputEvent event) {
		if(Battlegear.battlegearEnabled){
            Minecraft mc = FMLClientHandler.instance().getClient();
            //null checks to prevent any crash outside the world (and to make sure we have no screen open)
            if (mc != null && mc.thePlayer != null && mc.theWorld != null && mc.currentScreen == null) {
                if (Keyboard.getEventKey() == battleInv.getKeyCode()) {
                	BattleEquipGUI.open(mc.thePlayer);
                }
                else if (Keyboard.getEventKey() == openSigilEditor.getKeyCode()) {
                    BattlegearSigilGUI.open(mc.thePlayer);
                }
            }
		}
	}

}
