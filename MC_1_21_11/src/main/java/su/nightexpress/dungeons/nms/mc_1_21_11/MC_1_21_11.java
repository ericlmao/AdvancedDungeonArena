package su.nightexpress.dungeons.nms.mc_1_21_11;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.block.data.CraftBlockData;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.dungeons.api.schema.SchemaBlock;
import su.nightexpress.dungeons.nms.DungeonNMS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MC_1_21_11 implements DungeonNMS {

    @Override
    public void setSchemaBlock(@NotNull World world, @NotNull SchemaBlock schemaBlock) {
        ServerLevel level = ((CraftWorld)world).getHandle();

        CraftBlock craftBlock = (CraftBlock) schemaBlock.getBlockPos().toLocation(world).getBlock();
        craftBlock.setBlockData(schemaBlock.getBlockData());

        if (schemaBlock.getNbt() instanceof CompoundTag tag) {
            BlockPos blockPos = craftBlock.getPosition();
            BlockEntity blockEntity = level.getBlockEntity(blockPos);
            if (blockEntity == null) return;

            //entityState.loadData(tag);
            //entityState.update(true, false);

            // Load NBT data directly to NMS block.
            // CraftBlockEntityState#load wipes out some data, for example CraftSign overrides #load() and wipes out sign text.
            blockEntity.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
            blockEntity.setChanged();
        }
    }

    @NotNull
    @Override
    public List<SchemaBlock> loadSchema(@NotNull File file, boolean compressed) {
        List<SchemaBlock> schemaBlocks = new ArrayList<>();
        CompoundTag schemTag;

        try {
            schemTag = compressed ? NbtIo.readCompressed(file.toPath(), NbtAccounter.unlimitedHeap()) : NbtIo.read(file.toPath());
        }
        catch (IOException exception) {
            exception.printStackTrace();
            return schemaBlocks;
        }
        if (schemTag == null) {
            return schemaBlocks;
        }

        ListTag blocksTag = schemTag.getListOrEmpty("blocks");

        blocksTag.forEach(tag -> {
            CompoundTag blockTag = (CompoundTag) tag;

            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK, blockTag.getCompound("state").orElseThrow());

            ListTag posTag = blockTag.getListOrEmpty("pos");
            BlockPos pos = new BlockPos(posTag.getIntOr(0, 0), posTag.getIntOr(1, 0), posTag.getIntOr(2, 0));
            CompoundTag nbt = blockTag.getCompound("nbt").orElse(null);

            CraftBlockData craftBlockData = CraftBlockData.fromData(state);
            su.nightexpress.nightcore.util.geodata.pos.BlockPos blockPos = new su.nightexpress.nightcore.util.geodata.pos.BlockPos(pos.getX(), pos.getY(), pos.getZ());

            schemaBlocks.add(new SchemaBlock(blockPos, craftBlockData, nbt));
        });

        return schemaBlocks;
    }

    @Override
    public void saveSchema(@NotNull World world, @NotNull List<Block> blocks, @NotNull File file) {
        ServerLevel level = ((CraftWorld) world).getHandle();

        CompoundTag root = new CompoundTag();
        ListTag blocksTag = new ListTag();

        for (Block block : blocks) {
            CraftBlockState craftState = (CraftBlockState) block.getState();
            BlockPos blockPos = craftState.getPosition();
            BlockEntity blockEntity = level.getBlockEntity(blockPos);

            CompoundTag blockTag = new CompoundTag();
            blockTag.put("state", NbtUtils.writeBlockState(craftState.getHandle()));
            blockTag.put("pos", this.newIntegerList(blockPos.getX(), blockPos.getY(), blockPos.getZ()));
            if (blockEntity != null) {
                TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());//TagValueOutput.createWithContext(reporter, level.registryAccess());
                blockEntity.saveWithId(output);
                blockTag.put("nbt", output.buildResult());
            }
            blocksTag.add(blockTag);
        }

        root.put("blocks", blocksTag);

        try {
            NbtIo.writeCompressed(root, file.toPath());
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @NotNull
    private ListTag newIntegerList(int... arr) {
        ListTag tag = new ListTag();

        for (int value : arr) {
            tag.add(IntTag.valueOf(value));
        }

        return tag;
    }
}
