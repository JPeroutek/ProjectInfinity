package net.lerariemann.infinity.dimensions.features;

import net.fabricmc.loader.api.FabricLoader;
import net.lerariemann.infinity.InfinityMod;
import net.lerariemann.infinity.dimensions.RandomFeaturesList;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.lerariemann.infinity.var.ModMaterialConditions;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

import static java.nio.file.Files.walk;

public class RandomText extends RandomisedFeature {
    public RandomText(RandomFeaturesList parent) {
        super(parent, "text");
        id = "random_text";
        save_with_placement();
    }

    void placement() {
        int a = (int)random.nextGaussian(daddy.sea_level, 16);
        int b = random.nextInt(daddy.sea_level, daddy.height + daddy.min_y);
        placement_floating(1 + random.nextInt(16), Math.max(a, daddy.min_y), b);
    }

    NbtCompound feature() {
        NbtCompound config = new NbtCompound();
        addRandomBlockProvider(config, "block_provider", "full_blocks");
        NbtList replaceable = new NbtList();
        replaceable.add(RandomProvider.Block(parent.parent.parent.default_fluid.getString("Name")));
        config.put("replaceable", replaceable);
        config.putInt("orientation", random.nextInt(24));
        config.putString("text", genText());
        return feature(config);
    }

    String select(String str, boolean trim) {
        if (trim) str = str.replaceAll("\\s+","");
        int i2 = random.nextInt(8, 128);
        if (str.length() <= i2) return str;
        int i1 = random.nextInt(str.length() - i2);
        String substring = str.substring(i1, i1 + i2);
        return substring;
    }

    String genText() {
        Path p = Path.of(parent.PROVIDER.configPath).toAbsolutePath().getParent().getParent();
        Path p1 = Objects.requireNonNull(FabricLoader.getInstance().getModContainer(InfinityMod.MOD_ID).orElse(null)).getRootPaths().get(0);
        File f = p.resolve("config/infinity/text.txt").toFile();
        try {
            switch (random.nextInt(f.exists() ? 5 : 4)) {
                case 0 -> {
                    return genTextFromPath(p1, true);
                }
                case 1 -> {
                    return genTextFromPath(p, true);
                }
                case 2 -> {
                    return genTextFromFile(p.resolve("logs/latest.log").toFile(), false);
                }
                default -> {
                    return genTextRandomly();
                }
                case 4 -> {
                    return genTextFromFile(f, false);
                }
            }
        }
        catch (IOException e) {
            return genTextRandomly();
        }
    }

    String genTextFromPath(Path p, boolean trim) throws IOException {
        List<File> lst = new ArrayList<>();
        walk(p).forEach(a -> {
                if (a.toFile().isFile()) {
                    String s = a.toFile().toString();
                    if (!s.endsWith(".mca") && !s.endsWith(".png") && !s.endsWith(".gz")) lst.add(a.toFile());
                }
            });
        return genTextFromList(lst, trim);
    }

    String genTextFromList(List<File> lst, boolean trim) throws IOException {
        return genTextFromFile(lst.get(random.nextInt(lst.size())), trim);
    }

    String genTextFromFile(File f, boolean trim) throws IOException {
        return select(FileUtils.readFileToString(f, StandardCharsets.UTF_8), trim);
    }

    String genTextRandomly() {
        Set<Character> s = ModMaterialConditions.TextCondition.storage.keySet();
        StringBuilder res = new StringBuilder();
        for (int j = 0; j<64; j++) res.append(s.stream().toList().get(random.nextInt(s.size())));
        return res.toString();
    }
}
