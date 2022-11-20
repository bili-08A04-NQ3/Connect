package top.niqiu.Mixin;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.TargetBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.checkerframework.checker.units.qual.C;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.niqiu.Connect;

import static net.minecraft.state.property.Properties.POWER;

@Mixin(TargetBlock.class)
public class TargetBlockMixin {
    @Inject(method = "onProjectileHit",
            at = @At("HEAD")
    )
    public void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile, CallbackInfo info) {
        int i = trigger(world, state, hit, projectile);
        Entity entity = projectile.getOwner();
        if (entity instanceof ServerPlayerEntity serverPlayerEntity) {
            serverPlayerEntity.incrementStat(Stats.TARGET_HIT);
            Criteria.TARGET_HIT.trigger(serverPlayerEntity, projectile, hit.getPos(), i);
        }
    }


//    @Inject(method = "trigger",
//            at = @At("HEAD")
//    )
    private static int trigger(WorldAccess world, BlockState state, BlockHitResult hitResult, Entity entity) {
        // 1145141919810373553
        BlockPos pos = hitResult.getBlockPos();
        Block up1 = world.getBlockState(pos.up()).getBlock();
        BlockEntity up = world.getBlockEntity(pos.up(3));
        Connect.coolDownData.putIfAbsent(pos, 0L);
        if (up1 instanceof NoteBlock noteBlock && up instanceof SignBlockEntity signBlockEntity && world.getTickOrder() - Connect.coolDownData.get(pos) > 1) {
            if(Connect.data.containsKey(pos.up())) {
                // reset clock
                Connect.coolDownData.put(pos, world.getTickOrder());
                if (Connect.data.get(pos.up()).size() != 0) {
                    int i = Connect.data.get(pos.up()).get(0);
                    Connect.data.get(pos.up()).remove(0);
                    int j = entity instanceof PersistentProjectileEntity ? 20 : 8;
                    if (!world.getBlockTickScheduler().isQueued(hitResult.getBlockPos(), state.getBlock())) {
                        setPower(world, state, i, hitResult.getBlockPos(), j);
                    }
                    return i;
                }
            }
        }
        return 0;
    }

    private static int calculatePower(BlockHitResult hitResult, Vec3d pos) {
        Direction direction = hitResult.getSide();
        double d = Math.abs(MathHelper.fractionalPart(pos.x) - 0.5);
        double e = Math.abs(MathHelper.fractionalPart(pos.y) - 0.5);
        double f = Math.abs(MathHelper.fractionalPart(pos.z) - 0.5);
        Direction.Axis axis = direction.getAxis();
        double g;
        if (axis == Direction.Axis.Y) {
            g = Math.max(d, f);
        } else if (axis == Direction.Axis.Z) {
            g = Math.max(d, e);
        } else {
            g = Math.max(e, f);
        }

        return Math.max(1, MathHelper.ceil(15.0 * MathHelper.clamp((0.5 - g) / 0.5, 0.0, 1.0)));
    }

    private static void setPower(WorldAccess world, BlockState state, int power, BlockPos pos, int delay) {
        world.setBlockState(pos, (BlockState)state.with(POWER, power), 3);
        world.createAndScheduleBlockTick(pos, state.getBlock(), delay);
    }
}
