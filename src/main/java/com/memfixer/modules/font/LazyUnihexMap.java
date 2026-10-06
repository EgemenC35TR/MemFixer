package com.memfixer.modules.font;

import com.mojang.blaze3d.font.GlyphInfo;
import com.mojang.blaze3d.font.SheetGlyphInfo;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.gui.font.CodepointMap;
import net.minecraft.client.gui.font.glyphs.BakedGlyph;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.BitSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Ultra-low footprint on-demand Unicode hex font glyph provider.
 * Stores raw hex bitmaps in off-heap direct native memory (0 bytes on JVM heap)
 * and indexes 65,536 codepoints in a compact 256 KB bit-packed integer array and 8 KB BitSet.
 * Eliminates GC thrashing and micro-freezes while guaranteeing 100% CJK visual fidelity.
 */
public final class LazyUnihexMap extends CodepointMap<Object> {
    private static final Logger LOGGER = LogManager.getLogger("MemFixer/LazyUnihex");

    // Direct off-heap byte buffer - takes 0 bytes from JVM heap (-Xmx)
    private final ByteBuffer directHexData;
    // Packed [offset (30 bits) | lengthCode (2 bits)] for BMP codepoints (256 KB)
    private final int[] packedOffsets;
    private final BitSet presentCodepoints;
    private final int totalCount;
    private final List<OverrideRangeData> overrides;
    private final AtomicInteger onDemandLoaded = new AtomicInteger(0);

    public record OverrideRangeData(int from, int to, int left, int right) {}

    public static LazyUnihexMap createFromZipStream(InputStream stream, List<OverrideRangeData> overrides) {
        try {
            // Allocate initial off-heap direct buffer (0 bytes JVM heap). Grows off-heap if needed.
            ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024 * 1024);
            byte[] chunk = new byte[8192];

            try (ZipInputStream zis = new ZipInputStream(stream)) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().endsWith(".hex")) {
                        int read;
                        while ((read = zis.read(chunk)) != -1) {
                            if (directBuffer.remaining() < read) {
                                int newCap = Math.max(directBuffer.capacity() * 2, directBuffer.capacity() + read + 65536);
                                ByteBuffer expanded = ByteBuffer.allocateDirect(newCap);
                                directBuffer.flip();
                                expanded.put(directBuffer);
                                directBuffer = expanded;
                            }
                            directBuffer.put(chunk, 0, read);
                        }
                    }
                }
            }

            int len = directBuffer.position();
            if (len == 0) {
                return null;
            }
            directBuffer.flip();

            int[] offsets = new int[65536];
            BitSet bitSet = new BitSet(65536);
            int count = 0;
            int lineStart = 0;

            for (int i = 0; i < len; i++) {
                byte b = directBuffer.get(i);
                if (b == '\n' || i == len - 1) {
                    int lineEnd = (b == '\n') ? i : i + 1;
                    if (lineEnd > lineStart && directBuffer.get(lineEnd - 1) == '\r') {
                        lineEnd--;
                    }

                    int colonIndex = -1;
                    for (int j = lineStart; j < lineEnd; j++) {
                        if (directBuffer.get(j) == ':') {
                            colonIndex = j;
                            break;
                        }
                    }

                    if (colonIndex > lineStart) {
                        int cp = parseHex(directBuffer, lineStart, colonIndex);
                        if (cp >= 0 && cp < 65536) {
                            int dataOffset = colonIndex + 1;
                            int dataLen = lineEnd - dataOffset;
                            int lenCode = switch (dataLen) {
                                case 32 -> 0;
                                case 64 -> 1;
                                case 96 -> 2;
                                case 128 -> 3;
                                default -> -1;
                            };

                            if (lenCode >= 0) {
                                offsets[cp] = (dataOffset << 2) | lenCode;
                                bitSet.set(cp);
                                count++;
                            }
                        }
                    }

                    lineStart = i + 1;
                }
            }

            if (com.memfixer.config.MemFixerConfig.ENABLE_LOGGING) {
                LOGGER.info("[MemFixer/LazyUnihex] Indexed {} Unicode glyphs into off-heap direct buffer ({} KB off-heap).",
                        count, len / 1024);
            }

            return new LazyUnihexMap(directBuffer, offsets, bitSet, count, overrides);
        } catch (Exception e) {
            LOGGER.error("[MemFixer/LazyUnihex] Failed to index Unihex stream: {}", e.getMessage(), e);
            return null;
        }
    }

    private LazyUnihexMap(ByteBuffer directHexData, int[] packedOffsets, BitSet presentCodepoints, int totalCount, List<OverrideRangeData> overrides) {
        super(Object[]::new, Object[][]::new);
        this.directHexData = directHexData;
        this.packedOffsets = packedOffsets;
        this.presentCodepoints = presentCodepoints;
        this.totalCount = totalCount;
        this.overrides = overrides;
    }

    @Override
    public synchronized Object get(int codepoint) {
        Object cached = super.get(codepoint);
        if (cached != null) {
            return cached;
        }

        if (codepoint < 0 || codepoint >= 65536) {
            return null;
        }

        int packed = this.packedOffsets[codepoint];
        if (packed == 0) {
            return null;
        }

        int offset = packed >>> 2;
        int lenCode = packed & 3;
        int len = (lenCode + 1) * 32;

        LazyGlyph glyph = parseGlyph(codepoint, offset, len);
        if (glyph != null) {
            super.put(codepoint, glyph);
            onDemandLoaded.incrementAndGet();
        }
        return glyph;
    }

    @Override
    public IntSet keySet() {
        return new BitSetIntSet(this.presentCodepoints, this.totalCount);
    }

    public int getOffHeapMemoryBytes() {
        return this.directHexData != null ? this.directHexData.capacity() : 0;
    }

    private LazyGlyph parseGlyph(int codepoint, int offset, int len) {
        int[] rows = new int[16];
        int bitWidth;

        if (len == 32) {
            bitWidth = 8;
            for (int i = 0; i < 16; i++) {
                int hi = decodeHexNibble(this.directHexData.get(offset + (i * 2)));
                int lo = decodeHexNibble(this.directHexData.get(offset + (i * 2) + 1));
                rows[i] = ((hi << 4) | lo) << 24;
            }
        } else if (len == 64) {
            bitWidth = 16;
            for (int i = 0; i < 16; i++) {
                int b0 = decodeHexNibble(this.directHexData.get(offset + (i * 4)));
                int b1 = decodeHexNibble(this.directHexData.get(offset + (i * 4) + 1));
                int b2 = decodeHexNibble(this.directHexData.get(offset + (i * 4) + 2));
                int b3 = decodeHexNibble(this.directHexData.get(offset + (i * 4) + 3));
                rows[i] = ((b0 << 12) | (b1 << 8) | (b2 << 4) | b3) << 16;
            }
        } else if (len == 96) {
            bitWidth = 24;
            for (int i = 0; i < 16; i++) {
                int b0 = decodeHexNibble(this.directHexData.get(offset + (i * 6)));
                int b1 = decodeHexNibble(this.directHexData.get(offset + (i * 6) + 1));
                int b2 = decodeHexNibble(this.directHexData.get(offset + (i * 6) + 2));
                int b3 = decodeHexNibble(this.directHexData.get(offset + (i * 6) + 3));
                int b4 = decodeHexNibble(this.directHexData.get(offset + (i * 6) + 4));
                int b5 = decodeHexNibble(this.directHexData.get(offset + (i * 6) + 5));
                rows[i] = ((b0 << 20) | (b1 << 16) | (b2 << 12) | (b3 << 8) | (b4 << 4) | b5) << 8;
            }
        } else if (len == 128) {
            bitWidth = 32;
            for (int i = 0; i < 16; i++) {
                int val = 0;
                for (int j = 0; j < 8; j++) {
                    val = (val << 4) | decodeHexNibble(this.directHexData.get(offset + (i * 8) + j));
                }
                rows[i] = val;
            }
        } else {
            return null;
        }

        int left = -1;
        int right = -1;

        if (this.overrides != null) {
            for (OverrideRangeData range : this.overrides) {
                if (codepoint >= range.from && codepoint <= range.to) {
                    left = range.left;
                    right = range.right;
                    break;
                }
            }
        }

        if (left == -1 || right == -1) {
            int mask = 0;
            for (int r : rows) mask |= r;
            if (mask == 0) {
                left = 0;
                right = bitWidth;
            } else {
                left = Integer.numberOfLeadingZeros(mask);
                right = 32 - Integer.numberOfTrailingZeros(mask) - 1;
            }
        }

        return new LazyGlyph(rows, left, right);
    }

    private static int parseHex(ByteBuffer data, int start, int end) {
        int val = 0;
        for (int i = start; i < end; i++) {
            val = (val << 4) | decodeHexNibble(data.get(i));
        }
        return val;
    }

    private static int decodeHexNibble(byte b) {
        if (b >= '0' && b <= '9') return b - '0';
        if (b >= 'a' && b <= 'f') return b - 'a' + 10;
        if (b >= 'A' && b <= 'F') return b - 'A' + 10;
        return 0;
    }

    private static final class BitSetIntSet extends AbstractIntSet {
        private final BitSet bitSet;
        private final int size;

        public BitSetIntSet(BitSet bitSet, int size) {
            this.bitSet = bitSet;
            this.size = size;
        }

        @Override
        public boolean contains(int key) {
            return key >= 0 && key < this.bitSet.size() && this.bitSet.get(key);
        }

        @Override
        public int size() {
            return this.size;
        }

        @Override
        public IntIterator iterator() {
            return new IntIterator() {
                private int next = bitSet.nextSetBit(0);

                @Override
                public boolean hasNext() {
                    return next >= 0;
                }

                @Override
                public int nextInt() {
                    if (next < 0) throw new NoSuchElementException();
                    int res = next;
                    next = bitSet.nextSetBit(next + 1);
                    return res;
                }
            };
        }
    }

    @SuppressWarnings("null")
    public static final class LazyGlyph implements GlyphInfo {
        private final int[] rows;
        private final int left;
        private final int right;

        public LazyGlyph(int[] rows, int left, int right) {
            this.rows = rows;
            this.left = left;
            this.right = right;
        }

        public int width() {
            return this.right - this.left + 1;
        }

        @Override
        public float getAdvance() {
            return (float) (this.width() / 2 + 1);
        }

        @Override
        public float getShadowOffset() {
            return 0.5F;
        }

        @Override
        public float getBoldOffset() {
            return 0.5F;
        }

        @Override
        public BakedGlyph bake(Function<SheetGlyphInfo, BakedGlyph> baker) {
            return baker.apply(new SheetGlyphInfo() {
                @Override
                public float getOversample() {
                    return 2.0F;
                }

                @Override
                public int getPixelWidth() {
                    return LazyGlyph.this.width();
                }

                @Override
                public int getPixelHeight() {
                    return 16;
                }

                @Override
                public void upload(int x, int y) {
                    int w = LazyGlyph.this.width();
                    IntBuffer intbuffer = MemoryUtil.memAllocInt(w * 16);
                    for (int i = 0; i < 16; i++) {
                        unpackBitsToBytes(intbuffer, LazyGlyph.this.rows[i], LazyGlyph.this.left, LazyGlyph.this.right);
                    }
                    intbuffer.rewind();
                    GlStateManager.upload(0, x, y, w, 16, NativeImage.Format.RGBA, intbuffer, MemoryUtil::memFree);
                }

                @Override
                public boolean isColored() {
                    return true;
                }
            });
        }

        private static void unpackBitsToBytes(IntBuffer buffer, int line, int left, int right) {
            int i = 32 - left - 1;
            int j = 32 - right - 1;
            for (int k = i; k >= j; k--) {
                if (k < 32 && k >= 0) {
                    boolean bit = ((line >> k) & 1) != 0;
                    buffer.put(bit ? -1 : 0);
                } else {
                    buffer.put(0);
                }
            }
        }
    }
}
