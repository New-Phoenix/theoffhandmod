package mods.battlegear2.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import mods.battlegear2.api.shield.IShield;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.tclproject.theoffhandmod.TheOffhandMod;

public final class SpecialActionPacket extends AbstractMBPacket{

    public static final String packetName = "MB2|Special";
	private EntityPlayer player;
	private Entity entityHit;

    @Override
    public void process(ByteBuf inputStream, EntityPlayer player) {
        try {
            this.player = player.worldObj.getPlayerEntityByName(ByteBufUtils.readUTF8String(inputStream));
            if (inputStream.readBoolean()) {
                entityHit = player.worldObj.getPlayerEntityByName(ByteBufUtils.readUTF8String(inputStream));
            } else {
                entityHit = player.worldObj.getEntityByID(inputStream.readInt());
            }
        }catch (Exception e){
            e.printStackTrace();
            return;
        }

        if(this.player!=null){
            if (entityHit instanceof EntityLivingBase) {
                ItemStack offhand = ((InventoryPlayerBattle) this.player.inventory).getCurrentOffhandWeapon();
                if (offhand != null && offhand.getItem() instanceof IShield) {
                    if (entityHit.canBePushed()) {
                        double d0 = entityHit.posX - this.player.posX;
                        double d1;

                        for (d1 = entityHit.posZ - this.player.posZ; d0 * d0 + d1 * d1 < 1.0E-4D; d1 = (Math.random() - Math.random()) * 0.01D) {
                            d0 = (Math.random() - Math.random()) * 0.01D;
                        }
                        double pow = 0;

                        ((EntityLivingBase) entityHit).knockBack(this.player, 0, -d0 * (1 + pow), -d1 * (1 + pow));
                    }
                    if (entityHit.getDistanceToEntity(this.player) < 2) {
                        float dam = 0;
                        if (dam > 0) {
                            entityHit.attackEntityFrom(DamageSource.causeThornsDamage(this.player), dam);
                            entityHit.playSound("damage.thorns", 0.5F, 1.0F);
                        }
                    }
                    if (!this.player.worldObj.isRemote && entityHit instanceof EntityPlayerMP) {
                    	TheOffhandMod.packetHandler.sendPacketToPlayer(this.generatePacket(), (EntityPlayerMP) entityHit);
                    }
                }
            }
        }
    }

    public SpecialActionPacket(EntityPlayer player, Entity entityHit) {
    	this.player = player;
    	this.entityHit = entityHit;
    }

	public SpecialActionPacket() {
	}

	@Override
	public String getChannel() {
		return packetName;
	}

	@Override
	public void write(ByteBuf out) {
		boolean isPlayer = entityHit instanceof EntityPlayer;

        ByteBufUtils.writeUTF8String(out, player.getCommandSenderName());

        out.writeBoolean(isPlayer);
        if(isPlayer){
            ByteBufUtils.writeUTF8String(out, entityHit.getCommandSenderName());
        }else{
            out.writeInt(entityHit != null?entityHit.getEntityId():-1);
        }
	}
}
