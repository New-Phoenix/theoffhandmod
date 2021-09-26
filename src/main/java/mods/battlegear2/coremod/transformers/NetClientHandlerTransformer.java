package mods.battlegear2.coremod.transformers;

import mods.battlegear2.api.core.BattlegearTranslator;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.ListIterator;

public final class NetClientHandlerTransformer extends TransformerBase {

    public NetClientHandlerTransformer() {
        super("net.minecraft.client.network.NetHandlerPlayClient");
    }

    private String entityOtherPlayerMPClassName;
    private String playerInventoryFieldName;

    private String netClientHandlerHandleNamedEntitySpawnMethodName;
    private String netClientHandlerHandleNamedEntitySpawnMethodDesc;

    private String netClientHandlerHandleBlockItemSwitchMethodName;
    private String netClientHandlerHandleBlockItemSwitchMethodDesc;

    @Override
    boolean processMethods(List<MethodNode> methods) {
        int found = 0;
        for (MethodNode method : methods) {
            if ((method.name.equals(netClientHandlerHandleNamedEntitySpawnMethodName.split("!")[0]) || method.name.equals(netClientHandlerHandleNamedEntitySpawnMethodName.split("!")[1])) &&
                    method.desc.equals(netClientHandlerHandleNamedEntitySpawnMethodDesc)) {
                sendPatchLog("handleSpawnPlayer");

                replaceInventoryArrayAccess(method, entityOtherPlayerMPClassName, playerInventoryFieldName, 9, 14);
                found++;
            } else if ((method.name.equals(netClientHandlerHandleBlockItemSwitchMethodName.split("!")[0]) || method.name.equals(netClientHandlerHandleBlockItemSwitchMethodName.split("!")[1])) &&
                    method.desc.equals(netClientHandlerHandleBlockItemSwitchMethodDesc)) {
                sendPatchLog("handleHeldItemChange");

                ListIterator<AbstractInsnNode> insn = method.instructions.iterator();
                InsnList newList = new InsnList();

                while (insn.hasNext()) {

                    AbstractInsnNode nextNode = insn.next();

                    if (nextNode instanceof JumpInsnNode && nextNode.getOpcode() == IFLT) {
                        LabelNode label = ((JumpInsnNode) nextNode).label;
                        newList.add(new MethodInsnNode(INVOKESTATIC, "mods/battlegear2/api/core/InventoryPlayerBattle", "isValidSwitch", "(I)Z"));
                        newList.add(new JumpInsnNode(IFEQ, label));//"if equal" branch

                        found++;
                        nextNode = insn.next();
                        while (insn.hasNext() && !(nextNode instanceof JumpInsnNode) && nextNode.getOpcode() != IF_ICMPGE) {
                            nextNode = insn.next();//continue till "if int greater than or equal to" branch
                        }

                    } else {
                        newList.add(nextNode);
                    }

                }

                method.instructions = newList;
            }
        }
        return found == 2;
    }

    @Override
    boolean processFields(List<FieldNode> fields) {
        return true;
    }

    @Override
    void setupMappings() {

        entityOtherPlayerMPClassName = BattlegearTranslator.getMapedClassName("client.entity.EntityOtherPlayerMP");
        playerInventoryFieldName = "field_71071_by!inventory";

        netClientHandlerHandleNamedEntitySpawnMethodName = "func_147237_a!handleSpawnPlayer";
        netClientHandlerHandleNamedEntitySpawnMethodDesc = "(Lnet/minecraft/network/play/server/S0CPacketSpawnPlayer;)V";

        netClientHandlerHandleBlockItemSwitchMethodName = "func_147257_a!handleHeldItemChange";
        netClientHandlerHandleBlockItemSwitchMethodDesc = "(Lnet/minecraft/network/play/server/S09PacketHeldItemChange;)V";
    }

}
