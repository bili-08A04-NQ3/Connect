package top.niqiu.Mixin;

import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.niqiu.Connect;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static net.minecraft.block.NoteBlock.NOTE;

@Mixin(NoteBlock.class)
public class NoteBlockMixin {
    @Inject(method = "onUse",
            at = @At("HEAD")
    )
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<CallbackInfo> info) {
        Block down1 = world.getBlockState(pos.down()).getBlock();
        BlockEntity up = world.getBlockEntity(pos.up(2));
        if (down1 instanceof TargetBlock targetBlock && up instanceof SignBlockEntity signBlockEntity) {
            world.setBlockState(pos.up(), Blocks.YELLOW_TERRACOTTA.getDefaultState(), 4);
            new Thread(() -> {
                try {
                    StringBuilder builder = new StringBuilder();
                    for (int i = 0; i < 4; i++) {
                        builder.append(signBlockEntity.getTextOnRow(i).getString());
                    }
                    URL url = new URL(builder.toString());
                    URLConnection connection = url.openConnection();
                    InputStream stream = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                    String s = reader.readLine();
                    List<Integer> data = new ArrayList<>();
                    for (String i : s.split(",")) {
                        data.add(Integer.parseInt(i));
                    }
                    Connect.data.put(pos, data);
                    reader.close();
                    world.setBlockState(pos.up(), Blocks.GREEN_TERRACOTTA.getDefaultState(), 2);
                } catch (IOException e) {
                    e.printStackTrace();
                    world.setBlockState(pos.up(), Blocks.RED_TERRACOTTA.getDefaultState());
                }
            }).start();
        }
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else {
            state = state.cycle(NOTE);
            world.setBlockState(pos, state, 3);
            this.playNote(world, pos);
            player.incrementStat(Stats.TUNE_NOTEBLOCK);
            return ActionResult.CONSUME;
        }
    }

    private void playNote(World world, BlockPos pos) {
        if (world.getBlockState(pos.up()).isAir()) {
            world.addSyncedBlockEvent(pos, null, 0, 0);
        }
    }
}
