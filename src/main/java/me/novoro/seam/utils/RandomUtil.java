package me.novoro.seam.utils;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {
    public static <T> T getRandomValue(List<T> list) {
        if (list.isEmpty()) return null;
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    public static int randomIntBetween(int low, int high) {
        return ThreadLocalRandom.current().nextInt(low, high + 1);
    }

    public static boolean randomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }
}
