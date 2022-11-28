package top.niqiu;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.List;

public class Connect implements ModInitializer {
    @Override
    public void onInitialize() {
    }

    public static HashMap<BlockPos, List<Integer>> data = new HashMap<>();
    public static HashMap<BlockPos, Long> coolDownData = new HashMap<>();
}