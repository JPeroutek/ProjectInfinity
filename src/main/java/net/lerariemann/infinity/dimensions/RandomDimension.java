package net.lerariemann.infinity.dimensions;


import net.lerariemann.infinity.InfinityMod;
import net.minecraft.nbt.*;
import java.util.Random;


public class RandomDimension {
    private final NbtCompound data;
    private final int id;
    private final String PATH;
    private final RandomProvider PROVIDER;
    public String name;
    private final Random random;
    public int height;

    public RandomDimension(int i, RandomProvider provider, String path) {
        random = new Random(i);
        PROVIDER = provider;
        PATH = path + "/" + InfinityMod.MOD_ID + "/data/" + InfinityMod.MOD_ID;
        id = i;
        name = "generated_"+i;
        data = new NbtCompound();
        RandomDimensionType type = new RandomDimensionType(id, PROVIDER, PATH);
        data.putString("type", type.fullname);
        height = type.height;
        data.put("generator", randomDimensionGenerator());
        CommonIO.write(data, PATH + "/dimension", name + ".json");
    }

    NbtCompound randomDimensionGenerator() {
        NbtCompound res = new NbtCompound();
        String type = PROVIDER.GENERATOR_TYPES.getRandomElement(random);
        res.putString("type", type);
        switch (type) {
            case "minecraft:flat" -> {
                res.put("settings", randomSuperflatSettings());
                return res;
            }
            case "minecraft:noise" -> {
                res.putString("settings", randomNoiseSettings());
                res.put("biome_source", randomBiomeSource());
                return res;
            }
            default -> {
                return res;
            }
        }
    }

    NbtCompound superflatLayer(int height, WeighedStructure<String> str) {
        NbtCompound res = new NbtCompound();
        res.putInt("height", height);
        res.putString("block", str.getRandomElement(random));
        return res;
    }

    NbtCompound randomSuperflatSettings() {
        NbtCompound res = new NbtCompound();
        NbtList layers = new NbtList();
        if (RandomProvider.weighedRandom(random, 1, 3)) {
            int layer_count = Math.min(64, 1 + (int) Math.floor(random.nextExponential() * 2));
            int heightLeft = height;
            for (int i = 0; i < layer_count; i++) {
                int layerHeight = Math.min(heightLeft, 1 + (int) Math.floor(random.nextExponential() * 2));
                heightLeft -= layerHeight;
                layers.add(superflatLayer(layerHeight, PROVIDER.FULL_BLOCKS));
                if (heightLeft <= 1) {
                    break;
                }
            }
            if (random.nextBoolean()) {
                layers.add(superflatLayer(1, PROVIDER.ALL_BLOCKS));
            }
        }
        res.put("layers", layers);
        res.putString("biome", randomBiome());
        res.putBoolean("lakes", random.nextBoolean());
        res.putBoolean("features", random.nextBoolean());
        return res;
    }

    NbtCompound randomBiomeSource() {
        NbtCompound res = new NbtCompound();
        String type = PROVIDER.BIOME_SOURCES.getRandomElement(random);
        res.putString("type",type);
        switch (type) {
            case "minecraft:the_end" -> {
                return res;
            }
            case "minecraft:checkerboard" -> {
                res.put("biomes", randomBiomesCheckerboard());
                res.putInt("scale", Math.min(62, (int) Math.floor(random.nextExponential() * 2)));
                return res;
            }
            case "minecraft:multi_noise" -> {
                WeighedStructure<Integer> list2 = new WeighedStructure<>();
                list2.add(2, 0.1);
                list2.add(1, 0.1);
                list2.add(0, 0.8);
                switch (list2.getRandomElement(random)) {
                    case 2 -> res.putString("preset", "minecraft:overworld");
                    case 1 -> res.putString("preset", "minecraft:nether");
                    default -> res.put("biomes", randomBiomes());
                }
                return res;
            }
            case "minecraft:fixed" -> res.putString("biome", randomBiome());
        }
        return res;
    }

    NbtList randomBiomesCheckerboard() {
        NbtList res = new NbtList();
        int biome_count = Math.min(64, 2 + (int) Math.floor(random.nextExponential()));
        for (int i = 0; i < biome_count; i++) {
            res.add(NbtString.of(randomBiome()));
        }
        return res;
    }

    NbtList randomBiomes() {
        NbtList res = new NbtList();
        int biome_count = Math.min(16, 2 + (int) Math.floor(random.nextExponential()));
        for (int i = 0; i < biome_count; i++) {
            NbtCompound element = new NbtCompound();
            element.putString("biome", randomBiome());
            element.put("parameters", randomMultiNoiseParameters());
            res.add(element);
        }
        return res;
    }

    NbtCompound randomMultiNoiseParameters() {
        NbtCompound res = new NbtCompound();
        res.put("temperature", randomMultiNoiseParameter());
        res.put("humidity", randomMultiNoiseParameter());
        res.put("continentalness", randomMultiNoiseParameter());
        res.put("erosion", randomMultiNoiseParameter());
        res.put("weirdness", randomMultiNoiseParameter());
        res.put("depth", randomMultiNoiseParameter());
        res.put("offset", NbtDouble.of(random.nextDouble()));
        return res;
    }

    NbtElement randomMultiNoiseParameter() {
        if (random.nextBoolean()) {
            NbtCompound res = new NbtCompound();
            double a = (random.nextFloat()-0.5)*4;
            double b = (random.nextFloat()-0.5)*4;
            res.putFloat("min", (float)Math.min(a, b));
            res.putFloat("max", (float)Math.max(a, b));
            return res;
        }
        return NbtDouble.of((random.nextDouble()-0.5)*4);
    }

    String randomBiome() {
        if (random.nextBoolean()) {
            return PROVIDER.BIOMES.getRandomElement(random);
        }
        else {
            RandomBiome biome = new RandomBiome(random.nextInt(), PROVIDER, PATH);
            return biome.fullname;
        }
    }

    String randomNoiseSettings() {
        if (true) {
            return PROVIDER.NOISE_PRESETS.getRandomElement(random);
        }
        else {
            RandomNoisePreset preset = new RandomNoisePreset(id, PROVIDER, PATH);
            return preset.fullname;
        }
    }
}
