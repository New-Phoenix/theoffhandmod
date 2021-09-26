package mods.battlegear2.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.tclproject.theoffhandmod.TheOffhandMod;

public final class BattlegearShieldFlashPacket extends AbstractMBPacket{

    public static final String packetName = "MB2|ShieldFlash";
	private String username;
	private float damage;

    public BattlegearShieldFlashPacket(EntityPlayer player, float damage) {
    	this.username = player.getCommandSenderName();
    	this.damage = damage;
    }

	public BattlegearShieldFlashPacket() {
	}

    @Override
    public void process(ByteBuf in,EntityPlayer player) {
        try {
            username = ByteBufUtils.readUTF8String(in);
            damage = in.readFloat();
        }catch (Exception e){
            e.printStackTrace();
            return;
        }
        EntityPlayer targetPlayer = player.worldObj.getPlayerEntityByName(username);
        if(targetPlayer!=null)
        	TheOffhandMod.proxy.startFlash(targetPlayer, damage);
    }

	@Override
	public String getChannel() {
		return packetName;
	}

	@Override
	public void write(ByteBuf out) {
        ByteBufUtils.writeUTF8String(out, username);
        out.writeFloat(damage);
	}
}
