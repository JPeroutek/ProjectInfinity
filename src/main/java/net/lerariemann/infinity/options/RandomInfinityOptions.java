package net.lerariemann.infinity.options;

import net.lerariemann.infinity.dimensions.RandomDimension;
import net.lerariemann.infinity.dimensions.RandomProvider;
import net.lerariemann.infinity.util.CommonIO;
import net.minecraft.nbt.NbtCompound;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomInfinityOptions {
    NbtCompound data;
    String path;
    public RandomInfinityOptions(RandomDimension parent, boolean bl) {
        data = new NbtCompound();
        path = parent.getStoragePath();
        RandomProvider prov = parent.PROVIDER;
        Random r = parent.random;
        if (bl && prov.easterizer.optionmap.containsKey(parent.fullname)) {
            data = prov.easterizer.optionmap.get(parent.fullname);
        }
        if (bl) return;
        NbtCompound shader = new NbtCompound();
        if (prov.roll(r, "use_shaders")) {
            Object[] lst = genMatrix(r);
            shader = CommonIO.readCarefully(prov.configPath + "util/shader.json", lst);
        }
        data.put("shader", shader);
        data.putFloat("solar_size", (float)(30*r.nextExponential()));
        data.putFloat("lunar_size", (float)(20*r.nextExponential()));
        data.putDouble("time_scale", timeScale(r));
        data.putDouble("mavity", mavity(r));
    }

    public static double timeScale(Random r) {
        double d = r.nextDouble();
        if (d < 0.1) return r.nextDouble();
        if (d < 0.5) return 1.0;
        if (d < 0.95) return r.nextExponential()*3;
        return r.nextExponential()*30;
    }

    public static double mavity(Random r) {
        double d = r.nextDouble();
        if (d < 0.5) return 1.0;
        if (d < 0.75) return r.nextDouble();
        return 1 / (0.95*r.nextDouble() + 0.05);
    }

    public void save() {
        CommonIO.write(data, path, "options.json");
    }

    static Object[] genMatrix(Random r) {
        List<Float> points = new ArrayList<>();
        float scale = 2 + r.nextFloat();
        points.add(0.0f);
        points.add(scale);
        for (int i = 0; i < 8; i++) points.add(scale * r.nextFloat());
        Collections.sort(points);
        Object[] res = new Object[9];
        for (int i = 0; i < 9; i++) {
            res[i] = points.get(i+1) - points.get(i);
        }
        return res;
    }
}
