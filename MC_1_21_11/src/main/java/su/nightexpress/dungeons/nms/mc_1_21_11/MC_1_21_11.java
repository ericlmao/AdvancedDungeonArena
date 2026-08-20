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
import org.jspecify.annotations.NonNull;
import su.nightexpress.dungeons.api.schema.SchemaBlock;
import su.nightexpress.dungeons.nightcore.util.nbt.NbtProvider;
import su.nightexpress.dungeons.nms.DungeonNMS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MC_1_21_11 implements DungeonNMS, NbtProvider {

    /**
     * ItemStack &lt;-&gt; SNBT, on mojang-mapped internals.
     * <p>
     * Replaces nightcore's {@code util.nbt.NbtUtil}/{@code NbtSerializer}/{@code DataFixerUtil}, which
     * reached the same code through obfuscation-mapped reflection - including a hardcoded
     * {@code References.ITEM_STACK} field name ("u"/"t") that had to be re-checked every Minecraft
     * release. The wire format is unchanged, so existing kit inventories and reward items still load.
     */
    // jspecify's annotations are TYPE_USE, so on a qualified name they bind to the simple name.
    @Override
    @NonNull
    public String toTagString(org.bukkit.inventory.@NonNull ItemStack itemStack) {
        net.minecraft.world.item.ItemStack nmsStack = org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(itemStack);
        net.minecraft.core.RegistryAccess registryAccess = net.minecraft.server.MinecraftServer.getServer().registryAccess();

        Tag tag = net.minecraft.world.item.ItemStack.CODEC
            .encodeStart(registryAccess.createSerializationContext(NbtOps.INSTANCE), nmsStack)
            .getOrThrow(error -> new IllegalStateException("Could not encode ItemStack into NBT: " + error));

        return tag.toString();
    }

    @Override
    public org.bukkit.inventory.ItemStack fromTagString(@NonNull String tagString, int sourceDataVersion) {
        CompoundTag tag;
        try {
            tag = TagParser.parseCompoundFully(tagString);
        }
        catch (Exception exception) {
            return null;
        }

        net.minecraft.core.RegistryAccess registryAccess = net.minecraft.server.MinecraftServer.getServer().registryAccess();
        int targetVersion = net.minecraft.SharedConstants.getCurrentVersion().dataVersion().version();

        Tag fixed = tag;
        if (sourceDataVersion > 0 && sourceDataVersion < targetVersion) {
            com.mojang.serialization.Dynamic<Tag> dynamic = new com.mojang.serialization.Dynamic<>(NbtOps.INSTANCE, tag);
            fixed = net.minecraft.util.datafix.DataFixers.getDataFixer()
                .update(net.minecraft.util.datafix.fixes.References.ITEM_STACK, dynamic, sourceDataVersion, targetVersion)
                .getValue();
        }

        return net.minecraft.world.item.ItemStack.CODEC
            .parse(registryAccess.createSerializationContext(NbtOps.INSTANCE), fixed)
            .result()
            .map(org.bukkit.craftbukkit.inventory.CraftItemStack::asBukkitCopy)
            .orElse(null);
    }


    @Override
    public void setSchemaBlock(@NonNull World world, @NonNull SchemaBlock schemaBlock) {
        ServerLevel level = ((CraftWorld)world).getHandle();

        CraftBlock craftBlock = (CraftBlock) schemaBlock.blockPos().toLocation(world).getBlock();
        craftBlock.setBlockData(schemaBlock.blockData());

        if (schemaBlock.nbt() instanceof CompoundTag tag) {
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

    @NonNull
    @Override
    public List<SchemaBlock> loadSchema(@NonNull File file, boolean compressed) {
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
            su.nightexpress.dungeons.nightcore.util.geodata.pos.BlockPos blockPos = new su.nightexpress.dungeons.nightcore.util.geodata.pos.BlockPos(pos.getX(), pos.getY(), pos.getZ());

            schemaBlocks.add(new SchemaBlock(blockPos, craftBlockData, nbt));
        });

        return schemaBlocks;
    }

    @Override
    public void saveSchema(@NonNull World world, @NonNull List<Block> blocks, @NonNull File file) {
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

    @NonNull
    private ListTag newIntegerList(int... arr) {
        ListTag tag = new ListTag();

        for (int value : arr) {
            tag.add(IntTag.valueOf(value));
        }

        return tag;
    }
}
