package mcjty.rftoolsbuilder.modules.shield.blocks;

import mcjty.rftoolsbuilder.modules.shield.ShieldSetup;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.ModelDataManager;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class ShieldingTileEntity extends TileEntity {

    private BlockPos shieldProjector;
    private BlockState mimic;

    public static final ModelProperty<Integer> ICON_TOPDOWN = new ModelProperty<>();
    public static final ModelProperty<Integer> ICON_SIDE = new ModelProperty<>();
    public static final ModelProperty<BlockState> MIMIC = new ModelProperty<>();

    public ShieldingTileEntity() {
        super(ShieldSetup.TYPE_SHIELDING.get());
    }

    @Nullable
    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        CompoundNBT nbtTag = new CompoundNBT();
        this.write(nbtTag);
        return new SUpdateTileEntityPacket(pos, 1, nbtTag);
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        BlockState oldMimic = mimic;
        BlockPos oldShieldProjector = shieldProjector;
        read(pkt.getNbtCompound());
        if (!(Objects.equals(oldMimic, mimic) && Objects.equals(oldShieldProjector, shieldProjector))) {
            ModelDataManager.requestModelDataRefresh(this);
            BlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, Constants.BlockFlags.BLOCK_UPDATE + Constants.BlockFlags.NOTIFY_NEIGHBORS);
        }
    }

    public BlockPos getShieldProjector() {
        return shieldProjector;
    }

    public void setShieldProjector(BlockPos shieldProjector) {
        this.shieldProjector = shieldProjector;
        markDirty();
    }

    public BlockState getMimic() {
        return mimic;
    }

    public void setMimic(BlockState mimic) {
        this.mimic = mimic;
        markDirty();
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        int topdown = (z & 0x1) * 2 + (x & 0x1);
        int side = (y & 0x1) * 2 + ((x+z) & 0x1);
        return new ModelDataMap.Builder()
                .withInitial(ICON_SIDE, side)
                .withInitial(ICON_TOPDOWN, topdown)
                .withInitial(MIMIC, mimic)
                .build();
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        shieldProjector = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        if (tag.contains("mimic")) {
            mimic = NBTUtil.readBlockState(tag.getCompound("mimic"));
        } else {
            mimic = null;
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        tag.putInt("x", shieldProjector.getX());
        tag.putInt("y", shieldProjector.getY());
        tag.putInt("z", shieldProjector.getZ());
        if (mimic != null) {
            CompoundNBT camoNbt = NBTUtil.writeBlockState(mimic);
            tag.put("mimic", camoNbt);
        }
        return super.write(tag);
    }
}
