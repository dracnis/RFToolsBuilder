package mcjty.rftoolsbuilder.modules.shield.network;

import mcjty.lib.network.TypedMapTools;
import mcjty.lib.tileentity.GenericTileEntity;
import mcjty.lib.typed.TypedMap;
import mcjty.lib.varia.Logging;
import mcjty.rftoolsbuilder.modules.shield.blocks.ShieldProjectorTileEntity;
import mcjty.rftoolsbuilder.modules.shield.filters.ShieldFilter;
import mcjty.rftoolsbuilder.setup.RFToolsBuilderMessages;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public class PacketGetFilters {

    protected BlockPos pos;
    protected TypedMap params;

    public PacketGetFilters() {
    }

    public PacketGetFilters(PacketBuffer buf) {
        pos = buf.readBlockPos();
        params = TypedMapTools.readArguments(buf);
    }

    public PacketGetFilters(BlockPos pos) {
        this.pos = pos;
        this.params = TypedMap.EMPTY;
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeBlockPos(pos);
        TypedMapTools.writeArguments(buf, params);
    }

    public void handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context ctx = supplier.get();
        ctx.enqueueWork(() -> {
            World world = ctx.getSender().getCommandSenderWorld();
            if (world.hasChunkAt(pos)) {
                TileEntity te = world.getBlockEntity(pos);
                if (te instanceof GenericTileEntity) {
                    List<ShieldFilter> list = ((GenericTileEntity) te).executeServerCommandList(ShieldProjectorTileEntity.CMD_GETFILTERS.getName(), ctx.getSender(), params, ShieldFilter.class);
                    RFToolsBuilderMessages.INSTANCE.sendTo(new PacketFiltersReady(pos, ShieldProjectorTileEntity.CMD_GETFILTERS.getName(), list),
                            ctx.getSender().connection.connection, NetworkDirection.PLAY_TO_CLIENT);
                } else {
                    Logging.log("Command not handled!");
                    return;
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
