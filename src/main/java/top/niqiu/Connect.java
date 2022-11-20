package top.niqiu;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

public class Connect implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Connect");

    @Override
    public void onInitialize() {
    }

    public static HashMap<BlockPos, List<Integer>> data = new HashMap<>();
    public static HashMap<BlockPos, Long> coolDownData = new HashMap<>();
}