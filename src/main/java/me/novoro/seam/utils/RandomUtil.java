package me.novoro.seam.utils;

import java.util.*;

public class RandomUtil {
    private static final Random RANDOM = new Random();

    public static <T> T getRandomValue(List<T> list) {
        if (list.isEmpty()) return null;
        return list.get(RandomUtil.RANDOM.nextInt(list.size()));
    }
}
