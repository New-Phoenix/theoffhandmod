package mods.battlegear2.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import mods.battlegear2.api.core.BattlegearUtils;
import mods.battlegear2.api.core.IBattlePlayer;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.tclproject.theoffhandmod.TheOffhandMod;

public final class PickBlockPacket extends AbstractMBPacket{
    public final static String packetName = "MB2|CreaPick";
    private ItemStack stack;
    private int slot;
    public PickBlockPacket(){}
    public PickBlockPacket(ItemStack stack, int slot){
        this.stack = stack;
        this.slot = slot;
    }

    @Override
    public String getChannel() {
        return packetName;
    }

    @Override
    public void write(ByteBuf out) {
        out.writeInt(slot);
        ByteBufUtils.writeItemStack(out, stack);
    }

    @Override
    public void process(ByteBuf inputStream, EntityPlayer player) {
        if(player!=null && !((IBattlePlayer)player).isBattlemode()){
            try {
                slot = inputStream.readInt();
                stack = ByteBufUtils.readItemStack(inputStream);
            }catch (Exception e){
                e.printStackTrace();
                return;
            }
            if(InventoryPlayerBattle.isValidSwitch(slot)){
                player.inventory.currentItem = slot;
                if(player.capabilities.isCreativeMode && !ItemStack.areItemStacksEqual(stack, player.getCurrentEquippedItem())){
                    BattlegearUtils.setPlayerCurrentItem(player, stack);
                }
                if(player instanceof EntityPlayerMP)
                	TheOffhandMod.packetHandler.sendPacketToPlayer(this.generatePacket(),(EntityPlayerMP) player);
            }
        }
    }
}
