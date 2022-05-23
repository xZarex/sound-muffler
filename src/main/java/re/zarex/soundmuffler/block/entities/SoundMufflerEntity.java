package re.zarex.soundmuffler.block.entities;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import re.zarex.soundmuffler.SoundMufflerMod;
import re.zarex.soundmuffler.block.SoundMuffler;


public class SoundMufflerEntity extends BlockEntity {

    public SoundMufflerEntity(BlockPos pos, BlockState state) {
        super(SoundMuffler.ENTITY_TYPE, pos, state);
    }

    public void onPlaced(World world, BlockPos pos, BlockState state)
    {
        SoundMufflerMod.addMuffler((ServerWorld) world, pos);
    }

    public void onBreak(World world, BlockPos pos, BlockState state)
    {
        SoundMufflerMod.removeMuffler((ServerWorld) world, pos);
    }

}
