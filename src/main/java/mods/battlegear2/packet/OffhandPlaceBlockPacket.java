package mods.battlegear2.packet;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import io.netty.buffer.ByteBuf;
import mods.battlegear2.BattlemodeHookContainerClass;
import mods.battlegear2.api.PlayerEventChild;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S02PacketChat;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.tclproject.theoffhandmod.TheOffhandMod;

public final class OffhandPlaceBlockPacket extends AbstractMBPacket{
    public static final String packetName = "MB2|Place";
    private int xPosition;
    private int yPosition;
    private int zPosition;
    /** The offset to use for block/item placement. */
    private int direction;
    private ItemStack itemStack;
    /** The offset from xPosition where the actual click took place */
    private float xOffset;
    /** The offset from yPosition where the actual click took place */
    private float yOffset;
    /** The offset from zPosition where the actual click took place */
    private float zOffset;

    public OffhandPlaceBlockPacket() {}

    public OffhandPlaceBlockPacket(int par1, int par2, int par3, int par4, ItemStack par5ItemStack, float par6, float par7, float par8){
        this.xPosition = par1;
        this.yPosition = par2;
        this.zPosition = par3;
        this.direction = par4;
        this.itemStack = par5ItemStack != null ? par5ItemStack.copy() : null;
        this.xOffset = par6;
        this.yOffset = par7;
        this.zOffset = par8;
    }

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void write(ByteBuf out) {
        out.writeInt(this.xPosition);
        out.writeByte(this.yPosition);
        out.writeInt(this.zPosition);
        out.writeByte(this.direction);
        ByteBufUtils.writeItemStack(out, this.itemStack);
        out.writeByte((int)(this.xOffset * 16.0F));
        out.writeByte((int)(this.yOffset * 16.0F));
        out.writeByte((int)(this.zOffset * 16.0F));
    }

    @Override
    public void process(ByteBuf in, EntityPlayer player) {
        try{
            this.xPosition = in.readInt();
            this.yPosition = in.readUnsignedByte();
            this.zPosition = in.readInt();
            this.direction = in.readUnsignedByte();
            this.itemStack = ByteBufUtils.readItemStack(in);
            this.xOffset = in.readUnsignedByte() / 16.0F;
            this.yOffset = in.readUnsignedByte() / 16.0F;
            this.zOffset = in.readUnsignedByte() / 16.0F;
        }catch(Exception io){
            return;
        }
        if(player == null || !(player instanceof EntityPlayerMP))
            return;
        ItemStack offhandWeapon = ((InventoryPlayerBattle) player.inventory).getCurrentOffhandWeapon();
        if(offhandWeapon!=null && !BattlegearUtils.usagePriorAttack(offhandWeapon))
            return;
        boolean flag = true;
        int i = xPosition;
        int j = yPosition;
        int k = zPosition;
        int l = direction;
        ((EntityPlayerMP)player).func_143004_u();
        if (direction == 255){
            if (offhandWeapon == null)
                return;
            PlayerInteractEvent event = new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, 0, 0, 0, -1, player.getEntityWorld());
            MinecraftForge.EVENT_BUS.post(new PlayerEventChild.UseOffhandItemEvent(event, offhandWeapon));
            if (event.useItem != Event.Result.DENY){
                BattlemodeHookContainerClass.tryUseItem(player, offhandWeapon, Side.SERVER);
            }
            flag = false;
        }
        else {
            MinecraftServer mcServer = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (yPosition >= mcServer.getBuildLimit() - 1 && (direction == 1 || yPosition >= mcServer.getBuildLimit())) {
                ChatComponentTranslation chat = new ChatComponentTranslation("build.tooHigh", mcServer.getBuildLimit());
                chat.getChatStyle().setColor(EnumChatFormatting.RED);
                ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S02PacketChat(chat));
            } else {
                double dist = ((EntityPlayerMP) player).theItemInWorldManager.getBlockReachDistance() + 1;
                dist *= dist;
                if (((EntityPlayerMP) player).playerNetServerHandler.hasMoved && player.getDistanceSq(i + 0.5D, j + 0.5D, k + 0.5D) < dist && !mcServer.isBlockProtected(player.getEntityWorld(), i, j, k, player)) {
                    activateBlockOrUseItem((EntityPlayerMP) player, offhandWeapon, i, j, k, l, xOffset, yOffset, zOffset);
                }
            }
        }
        if (flag){
            ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S23PacketBlockChange(i, j, k, player.getEntityWorld()));
            if (l == 0){
                --j;
            }
            if (l == 1){
                ++j;
            }
            if (l == 2){
                --k;
            }
            if (l == 3){
                ++k;
            }
            if (l == 4){
                --i;
            }
            if (l == 5){
                ++i;
            }
            ((EntityPlayerMP) player).playerNetServerHandler.sendPacket(new S23PacketBlockChange(i, j, k, player.getEntityWorld()));
        }
        offhandWeapon = ((InventoryPlayerBattle) player.inventory).getCurrentOffhandWeapon();
        if (offhandWeapon != null && BattlemodeHookContainerClass.isItemBlock(offhandWeapon.getItem())) {

            if (offhandWeapon != null && offhandWeapon.stackSize <= 0) {
                BattlegearUtils.setPlayerOffhandItem(player, null);
                offhandWeapon = null;
            }
            if (offhandWeapon == null || offhandWeapon.getMaxItemUseDuration() == 0) {
                ((EntityPlayerMP) player).isChangingQuantityOnly = true;
                BattlegearUtils.setPlayerOffhandItem(player, ItemStack.copyItemStack(((InventoryPlayerBattle) player.inventory).getCurrentOffhandWeapon()));
                player.openContainer.detectAndSendChanges();
                ((EntityPlayerMP) player).isChangingQuantityOnly = false;

                if (!ItemStack.areItemStacksEqual(((InventoryPlayerBattle) player.inventory).getCurrentOffhandWeapon(), this.itemStack)) {
                    TheOffhandMod.packetHandler.sendPacketToPlayer(new BattlegearSyncItemPacket(player).generatePacket(), (EntityPlayerMP) player);
                }
            }
        }
    }

    /**
     * From ItemInWorldManager:
     * Activate the clicked on block, or use the given itemStack.
     */
    public boolean activateBlockOrUseItem(EntityPlayerMP playerMP, ItemStack itemStack, int x, int y, int z, int side, float xOffset, float yOffset, float zOffset)
    {
        World theWorld = playerMP.getEntityWorld();
        PlayerInteractEvent event = new PlayerInteractEvent(playerMP, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, x, y, z, side, theWorld);
        MinecraftForge.EVENT_BUS.post(new PlayerEventChild.UseOffhandItemEvent(event, itemStack));
        if (event.isCanceled())
        {
            playerMP.playerNetServerHandler.sendPacket(new S23PacketBlockChange(x, y, z, theWorld));
            return false;
        }

        if (itemStack != null && itemStack.getItem().onItemUseFirst(itemStack, playerMP, theWorld, x, y, z, side, xOffset, yOffset, zOffset))
        {
            if (itemStack.stackSize <= 0) ForgeEventFactory.onPlayerDestroyItem(playerMP, itemStack);
            return true;
        }

        boolean useBlock = !playerMP.isSneaking() || ((InventoryPlayerBattle)playerMP.inventory).getCurrentOffhandWeapon() == null;
        if (!useBlock) useBlock = ((InventoryPlayerBattle)playerMP.inventory).getCurrentOffhandWeapon().getItem().doesSneakBypassUse(theWorld, x, y, z, playerMP);
        boolean result = false;

        if (useBlock)
        {
            if (event.useBlock != Event.Result.DENY)
            {
                result = theWorld.getBlock(x, y, z).onBlockActivated(theWorld, x, y, z, playerMP, side, xOffset, yOffset, zOffset);
            }
            else
            {
                playerMP.playerNetServerHandler.sendPacket(new S23PacketBlockChange(x, y, z, theWorld));
                result = event.useItem != Event.Result.ALLOW;
            }
        }

        if (itemStack != null && !result && event.useItem != Event.Result.DENY)
        {
            final int meta = itemStack.getItemDamage();
            final int size = itemStack.stackSize;
            result = itemStack.tryPlaceItemIntoWorld(playerMP, theWorld, x, y, z, side, xOffset, yOffset, zOffset);
            if (playerMP.theItemInWorldManager.isCreative())
            {
                itemStack.setItemDamage(meta);
                itemStack.stackSize = size;
            } else {
//                itemStack.stackSize--;
            }
            if (itemStack.stackSize <= 0) ForgeEventFactory.onPlayerDestroyItem(playerMP, itemStack);
        }
        return result;
    }

}
