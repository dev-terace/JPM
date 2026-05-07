package io.jpm.core.jpm_repository.runtime.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;



public final class MurmurHash3 {

    private MurmurHash3() {}

    // ===== public API =====

    /** 128-bit hex (32 chars) */
    public static String hash128Hex(String input) {
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        long[] h = x64_128(data, 0, data.length, 0);
        return toHex(h[0]) + toHex(h[1]);
    }

    /** 64-bit hex (16 chars) - statementId 용으로 추천 */
    public static String hash64Hex(String input) {
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        long[] h = x64_128(data, 0, data.length, 0);
        return toHex(h[0]);
    }

    /** raw 128-bit (h1, h2) */
    public static long[] hash128(String input) {
        byte[] data = input.getBytes(StandardCharsets.UTF_8);
        return x64_128(data, 0, data.length, 0);
    }

    // ===== core =====

    public static long[] x64_128(byte[] key, int offset, int len, int seed) {
        final int nblocks = len >>> 4;

        long h1 = seed & 0xffffffffL;
        long h2 = seed & 0xffffffffL;

        final long c1 = 0x87c37b91114253d5L;
        final long c2 = 0x4cf5ad432745937fL;

        // body
        ByteBuffer buffer = ByteBuffer.wrap(key, offset, len).order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < nblocks; i++) {
            long k1 = buffer.getLong();
            long k2 = buffer.getLong();

            k1 *= c1; k1 = Long.rotateLeft(k1, 31); k1 *= c2; h1 ^= k1;
            h1 = Long.rotateLeft(h1, 27); h1 += h2; h1 = h1 * 5 + 0x52dce729;

            k2 *= c2; k2 = Long.rotateLeft(k2, 33); k2 *= c1; h2 ^= k2;
            h2 = Long.rotateLeft(h2, 31); h2 += h1; h2 = h2 * 5 + 0x38495ab5;
        }

        // tail
        long k1 = 0L;
        long k2 = 0L;

        int tailStart = nblocks << 4;
        switch (len & 15) {
            case 15: k2 ^= ((long) key[tailStart + 14] & 0xffL) << 48;
            case 14: k2 ^= ((long) key[tailStart + 13] & 0xffL) << 40;
            case 13: k2 ^= ((long) key[tailStart + 12] & 0xffL) << 32;
            case 12: k2 ^= ((long) key[tailStart + 11] & 0xffL) << 24;
            case 11: k2 ^= ((long) key[tailStart + 10] & 0xffL) << 16;
            case 10: k2 ^= ((long) key[tailStart + 9]  & 0xffL) << 8;
            case 9:  k2 ^= ((long) key[tailStart + 8]  & 0xffL);
                k2 *= c2; k2 = Long.rotateLeft(k2, 33); k2 *= c1; h2 ^= k2;

            case 8:  k1 ^= ((long) key[tailStart + 7]  & 0xffL) << 56;
            case 7:  k1 ^= ((long) key[tailStart + 6]  & 0xffL) << 48;
            case 6:  k1 ^= ((long) key[tailStart + 5]  & 0xffL) << 40;
            case 5:  k1 ^= ((long) key[tailStart + 4]  & 0xffL) << 32;
            case 4:  k1 ^= ((long) key[tailStart + 3]  & 0xffL) << 24;
            case 3:  k1 ^= ((long) key[tailStart + 2]  & 0xffL) << 16;
            case 2:  k1 ^= ((long) key[tailStart + 1]  & 0xffL) << 8;
            case 1:  k1 ^= ((long) key[tailStart]      & 0xffL);
                k1 *= c1; k1 = Long.rotateLeft(k1, 31); k1 *= c2; h1 ^= k1;
        }

        // finalization
        h1 ^= len;
        h2 ^= len;

        h1 += h2;
        h2 += h1;

        h1 = fmix64(h1);
        h2 = fmix64(h2);

        h1 += h2;
        h2 += h1;

        return new long[]{h1, h2};
    }

    private static long fmix64(long k) {
        k ^= (k >>> 33);
        k *= 0xff51afd7ed558ccdL;
        k ^= (k >>> 33);
        k *= 0xc4ceb9fe1a85ec53L;
        k ^= (k >>> 33);
        return k;
    }

    private static String toHex(long v) {
        return String.format("%016x", v);
    }
}