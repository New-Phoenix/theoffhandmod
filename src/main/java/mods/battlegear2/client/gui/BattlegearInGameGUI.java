package mods.battlegear2.client.gui;

import cpw.mods.fml.client.FMLClientHandler;
import mods.battlegear2.api.RenderItemBarEvent;
import mods.battlegear2.api.core.IBattlePlayer;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import mods.battlegear2.api.shield.IShield;
import mods.battlegear2.client.BattlegearClientTickHandeler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.tclproject.theoffhandmod.TheOffhandMod;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class BattlegearInGameGUI extends Gui {

    public static final float[] COLOUR_DEFAULT = new float[]{0, 0.75F, 1};
    public static final float[] COLOUR_RED = new float[]{1, 0.1F, 0.1F};
    public static final float[] COLOUR_YELLOW = new float[]{1, 1F, 0.1F};
    public static final int SLOT_H = 22;
    public static final RenderItem itemRenderer = new RenderItem();
    public static final ResourceLocation resourceLocation = new ResourceLocation("textures/gui/widgets.png");
    public static final ResourceLocation resourceLocationShield = new ResourceLocation("battlegear2", "textures/gui/Shield Bar.png");
    private final Minecraft mc;

    public BattlegearInGameGUI() {
        super();
        mc = FMLClientHandler.instance().getClient();
    }

    public void renderGameOverlay(float frame, int mouseX, int mouseY) {

        if(TheOffhandMod.battlegearEnabled && !this.mc.playerController.enableEverythingIsScrewedUpMode()){

                ScaledResolution scaledresolution = new ScaledResolution(this.mc, this.mc.displayWidth, this.mc.displayHeight);
                int width = scaledresolution.getScaledWidth();
                int height = scaledresolution.getScaledHeight();
                RenderGameOverlayEvent renderEvent = new RenderGameOverlayEvent(frame, scaledresolution, mouseX, mouseY);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                zLevel = -90.0F;

                RenderItemBarEvent event = new RenderItemBarEvent.BattleSlots(renderEvent, true);
                if(!MinecraftForge.EVENT_BUS.post(event)){
                    renderBattleSlots(width / 2 + 121 + event.xOffset, height - 22 + event.yOffset, frame, true);
                }
                event = new RenderItemBarEvent.BattleSlots(renderEvent, false);
                if(!MinecraftForge.EVENT_BUS.post(event)){
                    renderBattleSlots(width / 2 - 184 + event.xOffset, height - 22 + event.yOffset, frame, false);
                }

                ItemStack offhand = ((InventoryPlayerBattle) mc.thePlayer.inventory).getCurrentOffhandWeapon();
                if(offhand!= null && offhand.getItem() instanceof IShield){
                    event = new RenderItemBarEvent.ShieldBar(renderEvent, offhand);
                    if(!MinecraftForge.EVENT_BUS.post(event))
                        renderBlockBar(width / 2 - 91 + event.xOffset, height - 35 + event.yOffset);
                }
        }
    }

    public void renderBattleSlots(int x, int y, float frame, boolean isMainHand) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(resourceLocation);

        drawTexturedModalRect(x, y, 0, 0, 31, SLOT_H);
        drawTexturedModalRect(x + 31, y, 151, 0, 31, SLOT_H);

        if (mc.thePlayer!=null){
            if(((IBattlePlayer) mc.thePlayer).isBattlemode())
                this.drawTexturedModalRect(x + (mc.thePlayer.inventory.currentItem - InventoryPlayerBattle.OFFSET) * 20-1,
                        y - 1, 0, 22, 24, SLOT_H);
            GL11.glEnable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.enableGUIStandardItemLighting();
            for (int i = 0; i < InventoryPlayerBattle.WEAPON_SETS; ++i) {
                int varx = x + i * 20 + 3;
                this.renderInventorySlot(i + InventoryPlayerBattle.OFFSET+(isMainHand?0:InventoryPlayerBattle.WEAPON_SETS),
                        varx, y+3, frame);
            }
            RenderHelper.disableStandardItemLighting();
            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        }
        GL11.glDisable(GL11.GL_BLEND);
    }

    public void renderBlockBar(int x, int y) {
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture(resourceLocationShield);

        if(mc.thePlayer!=null){
            if(mc.thePlayer.capabilities.isCreativeMode){
                if(mc.thePlayer.isRidingHorse()){
                    y-=5;
                }
            }else{
                y-= 16;
                if(mc.thePlayer.isRidingHorse() || mc.thePlayer.getAir() < 300 || ForgeHooks.getTotalArmorValue(mc.thePlayer) > 0){
                    y-=10;
                }
            }
        }

        this.drawTexturedModalRect(x, y, 0, 0, 182, 9);

        float[] colour = COLOUR_DEFAULT;
        if(BattlegearClientTickHandeler.getBlockTime() < 0.33F){
            colour = COLOUR_RED;
        }
        if(BattlegearClientTickHandeler.getFlashTimer() > 0 && (System.currentTimeMillis() / 250) % 2 == 0){
            colour = COLOUR_YELLOW;
        }
        GL11.glColor3f(colour[0], colour[1], colour[2]);
        this.drawTexturedModalRect(x, y, 0, 9, (int) (182 * BattlegearClientTickHandeler.getBlockTime()), 9);

        GL11.glDisable(GL11.GL_BLEND);
    }

    private void renderInventorySlot(int par1, int par2, int par3, float par4) {
        ItemStack itemstack = this.mc.thePlayer.inventory.getStackInSlot(par1);
        renderStackAt(par2, par3, itemstack, par4);
    }

    private void renderStackAt(int x, int y, ItemStack itemstack, float frame){
        if (itemstack != null) {
            float f1 = (float) itemstack.animationsToGo - frame;

            if (f1 > 0.0F) {
                GL11.glPushMatrix();
                float f2 = 1.0F + f1 / 5.0F;
                GL11.glTranslatef((float) (x + 8), (float) (y + 12), 0.0F);
                GL11.glScalef(1.0F / f2, (f2 + 1.0F) / 2.0F, 1.0F);
                GL11.glTranslatef((float) (-(x + 8)), (float) (-(y + 12)), 0.0F);
            }

            itemRenderer.renderItemAndEffectIntoGUI(this.mc.fontRenderer, this.mc.renderEngine, itemstack, x, y);
            if (f1 > 0.0F) {
                GL11.glPopMatrix();
            }
            itemRenderer.renderItemOverlayIntoGUI(this.mc.fontRenderer, this.mc.renderEngine, itemstack, x, y);
        }
    }

}
