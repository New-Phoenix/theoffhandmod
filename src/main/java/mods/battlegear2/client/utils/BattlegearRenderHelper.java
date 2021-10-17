package mods.battlegear2.client.utils;

import static net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED;
import static net.minecraftforge.client.IItemRenderer.ItemRendererHelper.BLOCK_3D;

import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mods.battlegear2.api.IBackSheathedRender;
import mods.battlegear2.api.ISheathed;
import mods.battlegear2.api.RenderPlayerEventChild.PlayerElementType;
import mods.battlegear2.api.RenderPlayerEventChild.PostRenderPlayerElement;
import mods.battlegear2.api.RenderPlayerEventChild.PostRenderSheathed;
import mods.battlegear2.api.RenderPlayerEventChild.PreRenderPlayerElement;
import mods.battlegear2.api.RenderPlayerEventChild.PreRenderSheathed;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.IBattlePlayer;
import mods.battlegear2.api.core.IOffhandRender;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import mods.battlegear2.api.shield.IArrowDisplay;
import mods.battlegear2.api.shield.IShield;
import mods.battlegear2.client.BattlegearClientTickHandeler;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.IItemRenderer.ItemRenderType;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.tclproject.mysteriumlib.asm.fixes.MysteriumPatchesFixesO;

public final class BattlegearRenderHelper {

    private static final ItemStack dummyStack = new ItemStack(Blocks.flowing_lava);
    public static final float RENDER_UNIT = 1F/16F;//0.0625
    public static float PROGRESS_INCREMENT_LIMIT = 0.4F;
    public static EntityLivingBase dummyEntity;

    private static final ResourceLocation ITEM_GLINT = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    private static final ResourceLocation DEFAULT_ARROW = new ResourceLocation("textures/entity/arrow.png");

    public static final float[] arrowX = new float[64];
    public static final float[] arrowY = new float[arrowX.length];
    public static final float[] arrowDepth = new float[arrowX.length];
    public static final float[] arrowPitch = new float[arrowX.length];
    public static final float[] arrowYaw = new float[arrowX.length];

    static{
        for(int i = 0; i < arrowX.length; i++){
            double r = Math.random()*5;
            double theta = Math.random()*Math.PI*2;

            arrowX[i] = (float)(r * Math.cos(theta));
            arrowY[i] = (float)(r * Math.sin(theta));
            arrowDepth[i] = (float)(Math.random()* 0.5 + 0.5F);

            arrowPitch[i] = (float)(Math.random()*50 - 25);
            arrowYaw[i] = (float)(Math.random()*50 - 25);
        }
    }
    
    private static FloatBuffer colorBuffer = GLAllocation.createDirectFloatBuffer(16);
	public static boolean renderingItem;
    private static final Vec3 field_82884_b = Vec3.createVectorHelper(0.20000000298023224D, 1.0D, -0.699999988079071D).normalize();
    private static final Vec3 field_82885_c = Vec3.createVectorHelper(-0.20000000298023224D, 1.0D, 0.699999988079071D).normalize();
    
    private static FloatBuffer setColorBuffer(double p_74517_0_, double p_74517_2_, double p_74517_4_, double p_74517_6_)
    {
        /**
         * Update and return colorBuffer with the RGBA values passed as arguments
         */
        return setColorBuffer((float)p_74517_0_, (float)p_74517_2_, (float)p_74517_4_, (float)p_74517_6_);
    }

    /**
     * Update and return colorBuffer with the RGBA values passed as arguments
     */
    private static FloatBuffer setColorBuffer(float p_74521_0_, float p_74521_1_, float p_74521_2_, float p_74521_3_)
    {
        colorBuffer.clear();
        colorBuffer.put(p_74521_0_).put(p_74521_1_).put(p_74521_2_).put(p_74521_3_);
        colorBuffer.flip();
        /** Float buffer used to set OpenGL material colors */
        return colorBuffer;
    }
    private static void enableCustomShading() {
//	    GL11.glEnable(GL11.GL_LIGHTING);
	    GL11.glEnable(GL11.GL_LIGHT0);
	    GL11.glEnable(GL11.GL_LIGHT1);
//	    GL11.glEnable(GL11.GL_COLOR_MATERIAL);
	    GL11.glColorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
	    float f = 0.4F;
	    float f1 = 0.6F;
	    float f2 = 0.0F;
	    GL11.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, setColorBuffer(field_82884_b.xCoord, field_82884_b.yCoord, field_82884_b.zCoord, 0.0D));
	    GL11.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, setColorBuffer(f1, f1, f1, 1.0F));
	    GL11.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
	    GL11.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, setColorBuffer(f2, f2, f2, 1.0F));
	    GL11.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION, setColorBuffer(field_82885_c.xCoord, field_82885_c.yCoord, field_82885_c.zCoord, 0.0D));
	    GL11.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, setColorBuffer(f1, f1, f1, 1.0F));
	    GL11.glLight(GL11.GL_LIGHT1, GL11.GL_AMBIENT, setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
	    GL11.glLight(GL11.GL_LIGHT1, GL11.GL_SPECULAR, setColorBuffer(f2, f2, f2, 1.0F));
	    GL11.glShadeModel(GL11.GL_FLAT);
	    GL11.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, setColorBuffer(f, f, f, 1.0F));
    }

    @SideOnly(Side.CLIENT)
    public static void renderItemInFirstPerson(float frame, Minecraft mc, ItemRenderer itemRenderer) {
    	if(dummyEntity == null){
            dummyEntity = new EntityChicken(mc.theWorld);
        }
        if(dummyEntity.worldObj != mc.theWorld){
            dummyEntity = new EntityChicken(mc.theWorld);
        }

        IOffhandRender offhandRender = (IOffhandRender)itemRenderer;
        ItemStack itemToRender = offhandRender.getItemToRender();
        
        if (itemToRender != dummyStack) {
            float progress = offhandRender.getPrevEquippedProgress() + (offhandRender.getEquippedProgress() - offhandRender.getPrevEquippedProgress()) * frame;

            EntityClientPlayerMP player = mc.thePlayer;

            float rotation = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * frame;
            GL11.glPushMatrix();
            GL11.glRotatef(rotation, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * frame, 0.0F, 1.0F, 0.0F);
            RenderHelper.enableStandardItemLighting();
            GL11.glPopMatrix();
            float var6;
            float var7;

            var6 = player.prevRenderArmPitch + (player.renderArmPitch - player.prevRenderArmPitch) * frame;
            var7 = player.prevRenderArmYaw + (player.renderArmYaw - player.prevRenderArmYaw) * frame;
            GL11.glRotatef((player.rotationPitch - var6) * 0.1F, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef((player.rotationYaw - var7) * 0.1F, 0.0F, 1.0F, 0.0F);

            int var18 = mc.theWorld.getLightBrightnessForSkyBlocks(MathHelper.floor_double(player.posX), MathHelper.floor_double(player.posY), MathHelper.floor_double(player.posZ), 0);
            int var8 = var18 % 65536;
            int var9 = var18 / 65536;
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float) var8 / 1.0F, (float) var9 / 1.0F);
            float var10;
            float var21;
            float var20;

            if (itemToRender != null) {
                applyColorFromItemStack(itemToRender, 0);
            } else {
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            }

            float var11;
            float var12;
            float var13;
            RenderPlayer var26 = (RenderPlayer) RenderManager.instance.getEntityRenderObject(mc.thePlayer);
            RenderPlayerEvent preRender = new RenderPlayerEvent.Pre(player, var26, frame);
            RenderPlayerEvent postRender = new RenderPlayerEvent.Post(player, var26, frame);
            var7 = 0.8F;
            if (itemToRender != null) {

	        	if(itemToRender.getItem() instanceof IShield){
                    GL11.glPushMatrix();

                    float swingProgress =
                            (float)((IBattlePlayer)player).getSpecialActionTimer() / (
                                    float)((IShield)itemToRender.getItem()).getBashTimer(
                                    itemToRender);

	        		GL11.glTranslatef(-0.7F * var7 + 0.25F*MathHelper.sin(swingProgress*(float)Math.PI),
	        				-0.65F * var7 - (1.0F - progress) * 0.6F - 0.4F,
                            -0.9F * var7+0.1F - 0.25F*MathHelper.sin(swingProgress*(float)Math.PI));

	        		if(((IBattlePlayer)player).isBlockingWithShield()){
	        			GL11.glTranslatef(0.25F, 0.15F, 0);
	        		}

	        		GL11.glRotatef(25, 0, 0, 1);
	        		GL11.glRotatef(325-35*MathHelper.sin(swingProgress*(float)Math.PI), 0, 1, 0);

	        		if(!BattlegearUtils.RENDER_BUS.post(new PreRenderPlayerElement(preRender, true, PlayerElementType.ItemOffhand, itemToRender)))
	        			itemRenderer.renderItem(player, itemToRender, 0);
                    BattlegearUtils.RENDER_BUS.post(new PostRenderPlayerElement(postRender, true, PlayerElementType.ItemOffhand, itemToRender));
	        		GL11.glPopMatrix();

	        	}else{
                    GL11.glPushMatrix();

                    if (player.getItemInUseCount() > 0) {
                        EnumAction action = itemToRender.getItemUseAction();
                        
                        if (MysteriumPatchesFixesO.leftclicked) {
                        	action = EnumAction.none;
                        }

                        if (action == EnumAction.eat || action == EnumAction.drink) {
                            var21 = (float) player.getItemInUseCount() - frame + 1.0F;
                            var10 = 1.0F - var21 / (float) itemToRender.getMaxItemUseDuration();
                            var11 = 1.0F - var10;
                            var11 = var11 * var11 * var11;
                            var11 = var11 * var11 * var11;
                            var11 = var11 * var11 * var11;
                            var12 = 1.0F - var11;
                            GL11.glTranslatef(0.0F, MathHelper.abs(MathHelper.cos(var21 / 4.0F * (float) Math.PI) * 0.1F) * (float) ((double) var10 > 0.2D ? 1 : 0), 0.0F);
                            GL11.glTranslatef(var12 * 0.1F, -var12 * 0.1F, 0.0F);
                            GL11.glRotatef(var12 * 2.0F, 0.0F, 1.0F, 0.0F);
                            GL11.glRotatef(var12 * 5.0F, 1.0F, 0.0F, 0.0F);
                            GL11.glRotatef(var12 * 3.0F, 0.0F, 0.0F, 1.0F);
                        }
                    } else {
                        var20 = ((IBattlePlayer)player).getOffSwingProgress(frame);
                        var21 = MathHelper.sin(var20 * (float) Math.PI);
                        var10 = MathHelper.sin(MathHelper.sqrt_float(var20) * (float) Math.PI);
                        //Flip the (x direction)
                        GL11.glTranslatef(var10 * 0.4F, MathHelper.sin(MathHelper.sqrt_float(var20) * (float) Math.PI * 2.0F) * 0.2F, -var21 * 0.2F);
                    }
                    //Translate x in the opposite direction
                    GL11.glTranslatef(-0.7F * var7, -0.65F * var7 - (1.0F - progress) * 0.6F, -0.9F * var7);

                    //Rotate y in the opposite direction
                    GL11.glRotatef(-45.0F, 0.0F, 1.0F, 0.0F);

                    GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                    var20 = ((IBattlePlayer)player).getOffSwingProgress(frame);


                    var21 = MathHelper.sin(var20 * var20 * (float) Math.PI);
                    var10 = MathHelper.sin(MathHelper.sqrt_float(var20) * (float) Math.PI);

                    GL11.glRotatef(-var21 * 20.0F, 0.0F, 1.0F, 0.0F);
                    //Rotate z in the opposite direction
                    GL11.glRotatef(var10 * 20.0F, 0.0F, 0.0F, 1.0F);
                    GL11.glRotatef(-var10 * 80.0F, 1.0F, 0.0F, 0.0F);

                    //Rotate y back to original position + 45
                    GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);

                    var11 = 0.4F;
                    GL11.glScalef(var11, var11, var11);
                    float var14;
                    float var15;

                    if (player.getItemInUseCount() > 0) {
                        EnumAction action = itemToRender.getItemUseAction();
                        
                        if (MysteriumPatchesFixesO.leftclicked) {
                        	action = EnumAction.none;
                        }

                        if (action == EnumAction.block) {
                            GL11.glTranslatef(0.0F, 0.2F, 0.0F);
                            GL11.glRotatef(30.0F, 0.0F, 1.0F, 0.0F);
                            GL11.glRotatef(30.0F, 1.0F, 0.0F, 0.0F);
                            GL11.glRotatef(60.0F, 0.0F, 1.0F, 0.0F);
                        } else if (action == EnumAction.bow) {
                            GL11.glRotatef(-18.0F, 0.0F, 0.0F, 1.0F);
                            GL11.glRotatef(-12.0F, 0.0F, 1.0F, 0.0F);
                            GL11.glRotatef(-8.0F, 1.0F, 0.0F, 0.0F);
                            GL11.glTranslatef(-0.9F, 0.2F, 0.0F);
                            var13 = (float) itemToRender.getMaxItemUseDuration() - ((float) player.getItemInUseCount() - frame + 1.0F);
                            var14 = var13 / 20.0F;
                            var14 = (var14 * var14 + var14 * 2.0F) / 3.0F;

                            if (var14 > 1.0F) {
                                var14 = 1.0F;
                            }

                            if (var14 > 0.1F) {
                                GL11.glTranslatef(0.0F, MathHelper.sin((var13 - 0.1F) * 1.3F) * 0.01F * (var14 - 0.1F), 0.0F);
                            }

                            GL11.glTranslatef(0.0F, 0.0F, var14 * 0.1F);
                            GL11.glRotatef(-335.0F, 0.0F, 0.0F, 1.0F);
                            GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
                            GL11.glTranslatef(0.0F, 0.5F, 0.0F);
                            var15 = 1.0F + var14 * 0.2F;
                            GL11.glScalef(1.0F, 1.0F, var15);
                            GL11.glTranslatef(0.0F, -0.5F, 0.0F);
                            GL11.glRotatef(50.0F, 0.0F, 1.0F, 0.0F);
                            GL11.glRotatef(335.0F, 0.0F, 0.0F, 1.0F);
                        }
                    }

                    if (itemToRender.getItem().shouldRotateAroundWhenRendering()) {
                        GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                    }
                    if(!BattlegearUtils.RENDER_BUS.post(new PreRenderPlayerElement(preRender, true, PlayerElementType.ItemOffhand, itemToRender))){
                    	
                    	if (!Minecraft.getMinecraft().gameSettings.keyBindAttack.getIsKeyPressed() && Minecraft.getMinecraft().gameSettings.keyBindUseItem.getIsKeyPressed() && ItemStack.areItemStacksEqual(((InventoryPlayerBattle)player.inventory).getCurrentOffhandWeapon(), Minecraft.getMinecraft().playerController.currentItemHittingBlock)) {
	                    	GL11.glTranslatef(0F, 0.5F, 0F);
	                    	if (ItemStack.areItemStacksEqual(((InventoryPlayerBattle)player.inventory).getCurrentOffhandWeapon(), ((InventoryPlayerBattle)player.inventory).getStackInSlot(((InventoryPlayerBattle)player.inventory).currentItem))) {
	                    		GL11.glTranslatef(0.6F, -0.4F, 0.3F);
	                    		GL11.glScalef(0.9F, 0.85F, 0.9F);
	                    	}
                    	}
                        itemRenderer.renderItem(player, itemToRender, 0);
                    	if (itemToRender.getItem().requiresMultipleRenderPasses()) {
	                        for (int x = 1; x < itemToRender.getItem().getRenderPasses(itemToRender.getItemDamage()); x++) {
	                            applyColorFromItemStack(itemToRender, x);
	                            itemRenderer.renderItem(player, itemToRender, x);
	                        }
	                    }
                    }
                    BattlegearUtils.RENDER_BUS.post(new PostRenderPlayerElement(postRender, true, PlayerElementType.ItemOffhand, itemToRender));
	        		
                    GL11.glPopMatrix();
                }
            } else if (!player.isInvisible()) {
                GL11.glPushMatrix();

                GL11.glScalef(-1.0F, 1.0F, 1.0F);

                var20 = ((IBattlePlayer)player).getOffSwingProgress(frame);
                var21 = MathHelper.sin(var20 * (float) Math.PI);
                var10 = MathHelper.sin(MathHelper.sqrt_float(var20) * (float) Math.PI);
                GL11.glTranslatef(-var10 * 0.3F, MathHelper.sin(MathHelper.sqrt_float(var20) * (float) Math.PI * 2.0F) * 0.4F, -var21 * 0.4F);
                GL11.glTranslatef(var7 * var7, -0.75F * var7 - (1.0F - progress) * 0.6F, -0.9F * var7);

                GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);

                GL11.glEnable(GL12.GL_RESCALE_NORMAL);
                var21 = MathHelper.sin(var20 * var20 * (float) Math.PI);
                GL11.glRotatef(var10 * 70.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(var21 * 20.0F, 0.0F, 0.0F, 1.0F);

                mc.getTextureManager().bindTexture(player.getLocationSkin());
                GL11.glTranslatef(-1.0F, 3.6F, 3.5F);
                GL11.glRotatef(120.0F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(200.0F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(-135.0F, 0.0F, 1.0F, 0.0F);

                GL11.glScalef(1.0F, 1.0F, -1.0F);
                GL11.glTranslatef(5.6F, 0.0F, 0.0F);
                GL11.glScalef(1.0F, 1.0F, 1.0F);
                if(!BattlegearUtils.RENDER_BUS.post(new PreRenderPlayerElement(preRender, true, PlayerElementType.Offhand, null))) {
                    var26.renderFirstPersonArm(mc.thePlayer);
                }
                BattlegearUtils.RENDER_BUS.post(new PostRenderPlayerElement(postRender, true, PlayerElementType.Offhand, null));
	        		
                GL11.glPopMatrix();
            }

            GL11.glDisable(GL12.GL_RESCALE_NORMAL);
            RenderHelper.disableStandardItemLighting();
        }
    }

    @SideOnly(Side.CLIENT)
    public static void updateEquippedItem(ItemRenderer itemRenderer, Minecraft mc) {
        IOffhandRender offhandRender = (IOffhandRender)itemRenderer;
        offhandRender.setPrevEquippedProgress(offhandRender.getEquippedProgress());
        int slot = mc.thePlayer.inventory.currentItem + ((InventoryPlayerBattle)mc.thePlayer.inventory).getOffsetToInactiveHand();
        EntityPlayer var1 = mc.thePlayer;
        boolean moreThan = offhandRender.getEquippedItemSlot() > 157;
        ItemStack var2 = ((IBattlePlayer)var1).isBattlemode() && offhandRender.getEquippedItemSlot() > 0 ?
                var1.inventory.getStackInSlot(moreThan ? offhandRender.getEquippedItemSlot() - ((InventoryPlayerBattle)mc.thePlayer.inventory).getOffsetToInactiveHand() : ((InventoryPlayerBattle)var1.inventory).currentItemInactive) : dummyStack;

        boolean var3 = offhandRender.getEquippedItemSlot() == slot && var2 == offhandRender.getItemToRender();

        if (offhandRender.getItemToRender() == null && var2 == null) {
            var3 = true;
        }

        if (var2 != null && offhandRender.getItemToRender() != null &&
                var2 != offhandRender.getItemToRender() && var2.getItem() == offhandRender.getItemToRender().getItem() &&
                var2.getItemDamage() == offhandRender.getItemToRender().getItemDamage()) {
            offhandRender.setItemToRender(var2);
            var3 = true;
        }

        if(var3) {
            ItemStack offhand = ((IBattlePlayer) var1).isBattlemode() ? var1.inventory.getStackInSlot(slot) : dummyStack;
            var3 = (offhandRender.getEquippedItemSlot() == slot && offhand == offhandRender.getItemToRender());
        }

        float increment = (var3 ? 1.0F : 0.0F) - offhandRender.getEquippedProgress();
        
        if (increment < -PROGRESS_INCREMENT_LIMIT) {
            increment = -PROGRESS_INCREMENT_LIMIT;
        }

        if (increment > PROGRESS_INCREMENT_LIMIT) {
            increment = PROGRESS_INCREMENT_LIMIT;
        }

        offhandRender.setEquippedProgress(offhandRender.getEquippedProgress()+increment);

        if (offhandRender.getEquippedProgress() < 0.1F) {
            offhandRender.setItemToRender(var2);
            offhandRender.setEquippedItemSlot(slot);
        }
    }

    public static void moveOffHandArm(Entity entity, ModelBiped biped, float frame) {
        if (entity instanceof IBattlePlayer) {
            IBattlePlayer player = (IBattlePlayer) entity;
            float offhandSwing = 0.0F;

            if(player.isBattlemode()){
                ItemStack offhand = ((InventoryPlayerBattle)((EntityPlayer) entity).inventory).getCurrentOffhandWeapon();
                if(offhand != null && offhand.getItem() instanceof IShield){
                    offhandSwing = (float)player.getSpecialActionTimer() / (float)((IShield)offhand.getItem()).getBashTimer(offhand);
                }else{
                    offhandSwing = player.getOffSwingProgress(frame);
                }
            }

            if (offhandSwing > 0.0F) {
                if(biped.bipedBody.rotateAngleY!=0.0F){
                    biped.bipedLeftArm.rotateAngleY -= biped.bipedBody.rotateAngleY;
                    biped.bipedLeftArm.rotateAngleX -= biped.bipedBody.rotateAngleY;
                }
                biped.bipedBody.rotateAngleY = -MathHelper.sin(MathHelper.sqrt_float(offhandSwing) * (float)Math.PI * 2.0F) * 0.2F;

                //biped.bipedRightArm.rotationPointZ = MathHelper.sin(biped.bipedBody.rotateAngleY) * 5.0F;
                //biped.bipedRightArm.rotationPointX = -MathHelper.cos(biped.bipedBody.rotateAngleY) * 5.0F;

                biped.bipedLeftArm.rotationPointZ = -MathHelper.sin(biped.bipedBody.rotateAngleY) * 5.0F;
                biped.bipedLeftArm.rotationPointX = MathHelper.cos(biped.bipedBody.rotateAngleY) * 5.0F;

                //biped.bipedRightArm.rotateAngleY += biped.bipedBody.rotateAngleY;
                //biped.bipedRightArm.rotateAngleX += biped.bipedBody.rotateAngleY;
                float f6 = 1.0F - offhandSwing;
                f6 = 1.0F - f6*f6*f6;
                double f8 = MathHelper.sin(f6 * (float)Math.PI) * 1.2D;
                double f10 = MathHelper.sin(offhandSwing * (float)Math.PI) * -(biped.bipedHead.rotateAngleX - 0.7F) * 0.75F;
                biped.bipedLeftArm.rotateAngleX -= f8 + f10;
                biped.bipedLeftArm.rotateAngleY += biped.bipedBody.rotateAngleY * 3.0F;
                biped.bipedLeftArm.rotateAngleZ = MathHelper.sin(offhandSwing * (float)Math.PI) * -0.4F;
            }
        }
    }

    public static void renderItemIn3rdPerson(EntityPlayer par1EntityPlayer, ModelBiped modelBipedMain, float frame) {

    	boolean moreThan = ((InventoryPlayerBattle) par1EntityPlayer.inventory).currentItem + ((InventoryPlayerBattle) par1EntityPlayer.inventory).getOffsetToInactiveHand() > 157;
        ItemStack var21 = moreThan ? ((InventoryPlayerBattle) par1EntityPlayer.inventory).getStackInSlot(((InventoryPlayerBattle) par1EntityPlayer.inventory).currentItem - 4 + ((InventoryPlayerBattle) par1EntityPlayer.inventory).getOffsetToInactiveHand()) : ((InventoryPlayerBattle) par1EntityPlayer.inventory).getStackInSlot(((InventoryPlayerBattle) par1EntityPlayer.inventory).currentItem + ((InventoryPlayerBattle) par1EntityPlayer.inventory).getOffsetToInactiveHand());

        if (var21 != null) {

            float var7;
            RenderPlayer render = (RenderPlayer) RenderManager.instance.getEntityRenderObject(par1EntityPlayer);
            RenderPlayerEvent preRender = new RenderPlayerEvent.Pre(par1EntityPlayer, render, frame);
            RenderPlayerEvent postRender = new RenderPlayerEvent.Post(par1EntityPlayer, render, frame);
            
            GL11.glPushMatrix();
            if(!BattlegearUtils.RENDER_BUS.post(new PreRenderPlayerElement(preRender, false, PlayerElementType.Offhand, null)))
            	modelBipedMain.bipedLeftArm.postRender(RENDER_UNIT);
            BattlegearUtils.RENDER_BUS.post(new PostRenderPlayerElement(postRender, false, PlayerElementType.Offhand, null));
        	
            GL11.glTranslatef(RENDER_UNIT, 0.4375F, RENDER_UNIT);

            if (par1EntityPlayer.fishEntity != null) {
                var21 = new ItemStack(Items.stick);
            }

            EnumAction var23 = null;

            if (par1EntityPlayer.getItemInUseCount() > 0) {
                var23 = var21.getItemUseAction();
            }
            
            if (MysteriumPatchesFixesO.leftclicked) {
            	var23 = EnumAction.none;
            }

            IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(var21, EQUIPPED);
            boolean is3D = (customRenderer != null && customRenderer.shouldUseRenderHelper(EQUIPPED, var21, BLOCK_3D));
            
            if(var21.getItem() instanceof IShield){
                var7 = 0.625F;
                GL11.glScalef(var7, -var7, var7);

                GL11.glTranslated(8F/16F, -11F/16F, -RENDER_UNIT);

                GL11.glRotatef(-100.0F+90, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(45.0F-90, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(25, 0.0F, 0.0F, 1.0F);
                if(!BattlegearUtils.RENDER_BUS.post(new PreRenderPlayerElement(preRender, false, PlayerElementType.ItemOffhand, var21))){
	                if (var21.getItem().requiresMultipleRenderPasses()) {
	                    for (int var27 = 0; var27 < var21.getItem().getRenderPasses(var21.getItemDamage()); ++var27) {
                            applyColorFromItemStack(var21, var27);
	                        RenderManager.instance.itemRenderer.renderItem(par1EntityPlayer, var21, var27);
	                    }
	                } else {
                        applyColorFromItemStack(var21, 0);
	                    RenderManager.instance.itemRenderer.renderItem(par1EntityPlayer, var21, 0);
	                }
                }
            }else{

                if (var21.getItem() instanceof ItemBlock && (is3D || RenderBlocks.renderItemIn3d(Block.getBlockFromItem(var21.getItem()).getRenderType()))) {
                    var7 = 0.5F;
                    GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
                    var7 *= 0.75F;
                    GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glScalef(-var7, -var7, var7);
                } else if (BattlegearUtils.isBow(var21.getItem())) {
                    var7 = 0.625F;
                    GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
                    GL11.glRotatef(-20.0F, 0.0F, 1.0F, 0.0F);
                    GL11.glScalef(var7, -var7, var7);
                    GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
                } else if (var21.getItem().isFull3D()) {
                    var7 = 0.625F;

                    if (var21.getItem().shouldRotateAroundWhenRendering()) {
                        GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                        GL11.glTranslatef(0.0F, -0.125F, 0.0F);
                    }

                    if (par1EntityPlayer.getItemInUseCount() > 0 && var23 == EnumAction.block) {
                        GL11.glTranslatef(0.05F, 0.0F, -0.1F);
                        GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
                        GL11.glRotatef(-10.0F, 1.0F, 0.0F, 0.0F);
                        GL11.glRotatef(-60.0F, 0.0F, 0.0F, 1.0F);
                    }

                    GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
                    GL11.glScalef(var7, -var7, var7);
                    GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
                } else {
                    var7 = 0.375F;
                    GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
                    GL11.glScalef(var7, var7, var7);
                    GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
                    GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
                    GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
                }

                if(!BattlegearUtils.RENDER_BUS.post(new PreRenderPlayerElement(preRender, false, PlayerElementType.ItemOffhand, var21))){
    	            
	                if (var21.getItem().requiresMultipleRenderPasses()) {
	                    for (int var27 = 0; var27 < var21.getItem().getRenderPasses(var21.getItemDamage()); ++var27) {
                            applyColorFromItemStack(var21, var27);
	                        RenderManager.instance.itemRenderer.renderItem(par1EntityPlayer, var21, var27);
	                    }
	                } else {
                        applyColorFromItemStack(var21, 0);
	                    RenderManager.instance.itemRenderer.renderItem(par1EntityPlayer, var21, 0);
	                }
                }
            }
            BattlegearUtils.RENDER_BUS.post(new PostRenderPlayerElement(postRender, false, PlayerElementType.ItemOffhand, var21));
            GL11.glPopMatrix();
        } else {
            if(!((IBattlePlayer) par1EntityPlayer).isBattlemode())
                renderSheathedItems(par1EntityPlayer, modelBipedMain, frame);
        }
    }

    private static void renderSheathedItems(EntityPlayer par1EntityPlayer, ModelBiped modelBipedMain, float frame) {
        ItemStack mainhandSheathed = BattlegearClientTickHandeler.getPreviousMainhand(par1EntityPlayer);
        ItemStack offhandSheathed = BattlegearClientTickHandeler.getPreviousOffhand(par1EntityPlayer);

        RenderPlayer render = (RenderPlayer) RenderManager.instance.getEntityRenderObject(par1EntityPlayer);
        ModelBiped chestModel = render.modelArmorChestplate;
        ModelBiped legsModel = render.modelArmor;

        boolean hasChestArmour = false;
        boolean hasLegArmour = false;
        ItemStack chest = par1EntityPlayer.getEquipmentInSlot(3);
        if(chest != null){
            chestModel = ForgeHooksClient.getArmorModel(par1EntityPlayer, chest, 1, chestModel);
            hasChestArmour = true;
        }
        ItemStack legs =  par1EntityPlayer.getEquipmentInSlot(2);
        if(legs != null){
            legsModel = ForgeHooksClient.getArmorModel(par1EntityPlayer, legs, 2, legsModel);
            hasLegArmour = true;
        }

        int backCount = hasChestArmour?1:0;
        RenderPlayerEvent preRender = new RenderPlayerEvent.Pre(par1EntityPlayer, render, frame);
        RenderPlayerEvent postRender = new RenderPlayerEvent.Post(par1EntityPlayer, render, frame);
        
        if(mainhandSheathed != null && !(mainhandSheathed.getItem() instanceof ItemBlock)){

            boolean onBack = isBackSheathed(mainhandSheathed);

            ModelBiped target = modelBipedMain;
            if(chestModel != null){
                target = chestModel;
            }else if(legsModel != null && !onBack){
                target = legsModel;
            }

            GL11.glPushMatrix();
            target.bipedBody.postRender(RENDER_UNIT);
            if(onBack){
                if(mainhandSheathed.getItem() instanceof IBackSheathedRender){
                    ((IBackSheathedRender)mainhandSheathed.getItem()).preRenderBackSheathed(mainhandSheathed, backCount, preRender, true);
                }else {
                    GL11.glScalef(0.6F, 0.6F, 0.6F);
                }
                GL11.glTranslatef(-8F / 16F, 0, 6F / 16F);
                GL11.glRotatef(-5F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(130.0F, 0.0F, 1.0F, 0.0F);
                GL11.glTranslatef(0, 0, 4F/16F - backCount*2F/16F);
                backCount++;
            }else{
                GL11.glScalef(0.6F, 0.6F, 0.6F);
                GL11.glTranslatef(8F/16F, 1, -4F/16F);
                if(hasChestArmour || hasLegArmour){
                    GL11.glTranslatef(2F/16F, 0, 0);
                }
                GL11.glRotatef(35F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(40.0F, 0.0F, 1.0F, 0.0F);
            }

            if(!BattlegearUtils.RENDER_BUS.post(new PreRenderSheathed(preRender, onBack, backCount, true, mainhandSheathed))){
    	        
	            if (mainhandSheathed.getItem().requiresMultipleRenderPasses()) {
	                for (int var27 = 0; var27 < mainhandSheathed.getItem().getRenderPasses(mainhandSheathed.getItemDamage()); ++var27) {
                        applyColorFromItemStack(mainhandSheathed, var27);
	                    RenderManager.instance.itemRenderer.renderItem(dummyEntity, mainhandSheathed, var27);
	                }
	            } else {
                    applyColorFromItemStack(mainhandSheathed, 0);
	                RenderManager.instance.itemRenderer.renderItem(dummyEntity, mainhandSheathed, 0);
	            }
            }

            BattlegearUtils.RENDER_BUS.post(new PostRenderSheathed(postRender, onBack, backCount, true, mainhandSheathed));
            
            GL11.glPopMatrix();
        }

        if(offhandSheathed != null && !(offhandSheathed.getItem() instanceof ItemBlock)){
            boolean onBack = isBackSheathed(offhandSheathed);

            ModelBiped target = modelBipedMain;
            if(chestModel != null){
                target = chestModel;
            }else if(legsModel != null && !onBack){
                target = legsModel;
            }

            GL11.glPushMatrix();
            target.bipedBody.postRender(RENDER_UNIT);

            if(onBack){
                if(offhandSheathed.getItem() instanceof IBackSheathedRender){
                    ((IBackSheathedRender)offhandSheathed.getItem()).preRenderBackSheathed(offhandSheathed, backCount, preRender, false);
                }else if(offhandSheathed.getItem() instanceof IShield){
                    GL11.glScalef(-0.6F, -0.6F, 0.6F);
                    GL11.glTranslatef(0, -1, 0);
                }else{
                    GL11.glScalef(-0.6F, 0.6F, 0.6F);
                }
                GL11.glTranslatef(-8F / 16F, 0, 6F / 16F);
                GL11.glRotatef(-5F, 0.0F, 0.0F, 1.0F);
                GL11.glRotatef(40.0F+90, 0.0F, 1.0F, 0.0F);
                GL11.glTranslatef(0, 0, 4F/16F - backCount*2F/16F);
                backCount++;
            }else{
                GL11.glScalef(0.6F, 0.6F, 0.6F);
                GL11.glTranslatef(-7F/16F, 1, -4F/16F);
                if(hasChestArmour || hasLegArmour){
                    GL11.glTranslatef(-2F/16F, 0, 0);
                }
                GL11.glRotatef(35F, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(40.0F, 0.0F, 1.0F, 0.0F);
            }
            if(!BattlegearUtils.RENDER_BUS.post(new PreRenderSheathed(preRender, onBack, backCount, false, offhandSheathed))){
        	    
	            if (offhandSheathed.getItem().requiresMultipleRenderPasses()) {
	                for (int var27 = 0; var27 < offhandSheathed.getItem().getRenderPasses(offhandSheathed.getItemDamage()); ++var27) {
                        applyColorFromItemStack(offhandSheathed, var27);
	                    RenderManager.instance.itemRenderer.renderItem(dummyEntity, offhandSheathed, var27);
	                }
	            } else {
                    applyColorFromItemStack(offhandSheathed, 0);
	                RenderManager.instance.itemRenderer.renderItem(dummyEntity, offhandSheathed, 0);
	            }
            }

            BattlegearUtils.RENDER_BUS.post(new PostRenderSheathed(postRender, onBack, backCount, false, offhandSheathed));
            GL11.glPopMatrix();
        }
    }

    public static void applyColorFromItemStack(ItemStack itemStack, int pass){
        int col = itemStack.getItem().getColorFromItemStack(itemStack, pass);
        float r = (float) (col >> 16 & 255) / 255.0F;
        float g = (float) (col >> 8 & 255) / 255.0F;
        float b = (float) (col & 255) / 255.0F;
        GL11.glColor4f(r, g, b, 1.0F);
    }

    private static boolean isBackSheathed(ItemStack sheathed) {
        if(sheathed.getItem() instanceof ISheathed){
            return ((ISheathed) sheathed.getItem()).sheatheOnBack(sheathed);
        }else if (BattlegearUtils.isBow(sheathed.getItem())){
            return true;
        }
        return false;
    }

    @SideOnly(Side.CLIENT)
    public static void renderEnchantmentEffects(Tessellator tessellator) {
        GL11.glDepthFunc(GL11.GL_EQUAL);
        GL11.glDisable(GL11.GL_LIGHTING);
        Minecraft.getMinecraft().renderEngine.bindTexture(ITEM_GLINT);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE);
        float f7 = 0.76F;
        GL11.glColor4f(0.5F * f7, 0.25F * f7, 0.8F * f7, 1.0F);
        GL11.glMatrixMode(GL11.GL_TEXTURE);
        GL11.glPushMatrix();
        float f8 = 0.125F;
        GL11.glScalef(f8, f8, f8);
        float f9 = (float) (Minecraft.getSystemTime() % 3000L) / 3000.0F * 8.0F;
        GL11.glTranslatef(f9, 0.0F, 0.0F);
        GL11.glRotatef(-50.0F, 0.0F, 0.0F, 1.0F);
        ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, RENDER_UNIT);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glScalef(f8, f8, f8);
        f9 = (float) (Minecraft.getSystemTime() % 4873L) / 4873.0F * 8.0F;
        GL11.glTranslatef(-f9, 0.0F, 0.0F);
        GL11.glRotatef(10.0F, 0.0F, 0.0F, 1.0F);
        ItemRenderer.renderItemIn2D(tessellator, 0.0F, 0.0F, 1.0F, 1.0F, 256, 256, RENDER_UNIT);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
    }

    public static void renderArrows(ItemStack stack, boolean isEntity){
        if(stack.getItem() instanceof IArrowDisplay){
            int arrowCount = ((IArrowDisplay)stack.getItem()).getArrowCount(stack);
            //Bounds checking (rendering this many is quite silly, any more would look VERY silly)
            if(arrowCount > 64)
                arrowCount = 64;
            for(int i = 0; i < arrowCount; i++){
                BattlegearRenderHelper.renderArrow(isEntity, i);
            }
        }
    }

    public static void renderArrow(boolean isEntity, int id){
        if(id<arrowX.length){
            float pitch = arrowPitch[id]+90F;
            float yaw = arrowYaw[id]+45F;
            renderArrow(isEntity, arrowX[id], arrowY[id], arrowDepth[id], pitch, yaw);
        }
    }

    @SideOnly(Side.CLIENT)
    public static void renderArrow(boolean isEntity, float x, float y, float depth, float pitch, float yaw){
        GL11.glPushMatrix();
        //depth = 1;
        Minecraft.getMinecraft().renderEngine.bindTexture(DEFAULT_ARROW);

        float f10 = 0.05F;
        GL11.glScalef(f10, f10, f10);
        if(isEntity){
            GL11.glScalef(1, 1, -1);
        }

        GL11.glTranslatef(x + 10.5F, y + 9.5F, 0);

        GL11.glRotatef(pitch, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(yaw, 1.0F, 0.0F, 0.0F);
        GL11.glNormal3f(f10, 0.0F, 0.0F);

        double f2 = 12F/32F * depth;
        double f3 = 0D;
        double f5 = 5 / 32.0F;
        Tessellator tessellator = Tessellator.instance;
        for (int i = 0; i < 2; ++i)
        {
            GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glNormal3f(0.0F, 0.0F, f10);
            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV(0.0D * depth, -2.0D, 0.0D, f2, f3);
            tessellator.addVertexWithUV(16.0D * depth, -2.0D, 0.0D, f3, f3);
            tessellator.addVertexWithUV(16.0D * depth, 2.0D, 0.0D, f3, f5);
            tessellator.addVertexWithUV(0.0D * depth, 2.0D, 0.0D, f2, f5);
            tessellator.draw();

            tessellator.startDrawingQuads();
            tessellator.addVertexWithUV(0.0D * depth, 2.0D, 0.0D, f2, f5);
            tessellator.addVertexWithUV(16.0D * depth, 2.0D, 0.0D, f3, f5);
            tessellator.addVertexWithUV(16.0D * depth, -2.0D, 0.0D, f3, f3);
            tessellator.addVertexWithUV(0.0D * depth, -2.0D, 0.0D, f2, f3);
            tessellator.draw();
        }
        GL11.glPopMatrix();
    }

    public static void renderTexturedQuad(int x, int y, float z, int width, int height)
    {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)(x + 0), (double)(y + height), (double)z, 0D, 1D);
        tessellator.addVertexWithUV((double)(x + width), (double)(y + height), (double)z, 1D, 1D);
        tessellator.addVertexWithUV((double)(x + width), (double)(y + 0), (double)z, 1D, 0D);
        tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), (double)z, 0D, 0D);
        tessellator.draw();
    }
}
