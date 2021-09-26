package mods.battlegear2;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import mods.battlegear2.api.core.IBattlePlayer;
import mods.battlegear2.api.core.InventoryPlayerBattle;
import mods.battlegear2.api.shield.IShield;
import mods.battlegear2.packet.BattlegearSyncItemPacket;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.tclproject.theoffhandmod.TheOffhandMod;

public final class BattlegearTickHandeler {
    public static final BattlegearTickHandeler INSTANCE = new BattlegearTickHandeler();

    private BattlegearTickHandeler(){}

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event){
        if(event.phase == TickEvent.Phase.START){
            tickStart(event.player);
        }else{
            tickEnd(event.player);
        }
    }

    public void tickStart(EntityPlayer entityPlayer) {

        if (entityPlayer.worldObj instanceof WorldServer) {

            if(((InventoryPlayerBattle)entityPlayer.inventory).hasChanged){

                ((WorldServer)entityPlayer.worldObj).getEntityTracker().func_151248_b(
                        entityPlayer, new BattlegearSyncItemPacket(entityPlayer).generatePacket()
                );

                ((IBattlePlayer)entityPlayer).setSpecialActionTimer(0);

                ((InventoryPlayerBattle)entityPlayer.inventory).hasChanged = entityPlayer.ticksExisted < 10;

            }
            //Force update every 3 seconds
            else if(((IBattlePlayer)entityPlayer).isBattlemode() && entityPlayer.ticksExisted % 20 == 0 && !entityPlayer.isUsingItem()){
                ((WorldServer)entityPlayer.worldObj).getEntityTracker().func_151248_b(
                        entityPlayer, new BattlegearSyncItemPacket(entityPlayer).generatePacket()
                );
            }

        }
    }

    public void tickEnd(EntityPlayer entityPlayer) {
        int timer = ((IBattlePlayer)entityPlayer).getSpecialActionTimer();
        if(timer > 0){
            ((IBattlePlayer)entityPlayer).setSpecialActionTimer(timer-1);
            int targetTime = -1;
            ItemStack offhand = ((InventoryPlayerBattle)entityPlayer.inventory).getCurrentOffhandWeapon();
            if(offhand != null && offhand.getItem() instanceof IShield){
                targetTime = ((IShield) offhand.getItem()).getBashTimer(offhand) / 2;
            }
            if(timer-1 == targetTime){
            	TheOffhandMod.proxy.doSpecialAction(entityPlayer, offhand);
            }
        }
    }
}
