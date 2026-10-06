package com.memfixer.modules.font;

import com.memfixer.config.MemFixerConfig;
import com.memfixer.mixin.font.UnihexProviderAccessor;
import net.minecraft.client.gui.font.providers.UnihexProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Modular handler responsible for true on-demand Unicode font glyph deferral.
 * Eliminates eager allocation of 65,000+ glyph bitmaps while guaranteeing 100%
 * visual fidelity and zero missing glyphs when CJK or Unicode characters are rendered.
 */
public final class LazyUnihexHandler {
    private static final Logger LOGGER = LogManager.getLogger("MemFixer/Font");

    private LazyUnihexHandler() {}

    /**
     * Determines whether unifont loading should be bypassed to respect low-memory ceilings.
     *
     * @return true if lightweight on-demand provider should be substituted; false to proceed with vanilla parsing.
     */
    public static boolean shouldBypassEagerLoad() {
        if (!MemFixerConfig.OPTIMIZE_UNIFONT) {
            return false;
        }

        // Safety: Respect in-game Minecraft options if client has initialized
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc != null && mc.options != null) {
                // If the player explicitly turned on "Force Unicode Font", preserve eager unifont
                if (Boolean.TRUE.equals(mc.options.forceUnicodeFont().get())) {
                    if (MemFixerConfig.ENABLE_LOGGING) {
                        LOGGER.info("[MemFixer/Font] 'Force Unicode Font' option is enabled. Eager unifont loading preserved.");
                    }
                    return false;
                }

                // Check active in-game language code (e.g., zh_cn, ja_jp, ko_kr)
                String langCode = mc.options.languageCode;
                if (langCode != null) {
                    String lower = langCode.toLowerCase();
                    if (lower.startsWith("zh") || lower.startsWith("ja") || lower.startsWith("ko")) {
                        if (MemFixerConfig.ENABLE_LOGGING) {
                            LOGGER.info("[MemFixer/Font] CJK in-game language detected ('{}'). Eager unifont loading preserved.", langCode);
                        }
                        return false;
                    }
                }
            }
        } catch (Throwable ignored) {}

        // Fallback: Check JVM OS default locale
        try {
            String osLang = java.util.Locale.getDefault().getLanguage().toLowerCase();
            if (osLang.startsWith("zh") || osLang.startsWith("ja") || osLang.startsWith("ko")) {
                if (MemFixerConfig.ENABLE_LOGGING) {
                    LOGGER.info("[MemFixer/Font] CJK system locale detected ('{}'). Eager unifont loading preserved.", osLang);
                }
                return false;
            }
        } catch (Throwable ignored) {}

        if (MemFixerConfig.ENABLE_LOGGING) {
            LOGGER.info("[MemFixer/Font] Non-CJK environment detected. Engaging on-demand LazyUnihexMap engine.");
        }
        return true;
    }

    /**
     * Builds a lazy, on-demand UnihexProvider using LazyUnihexMap.
     */
    public static UnihexProvider buildLazyProvider(InputStream stream, List<?> sizeOverrides) {
        List<LazyUnihexMap.OverrideRangeData> converted = new ArrayList<>();
        if (sizeOverrides != null) {
            for (Object obj : sizeOverrides) {
                if (obj == null) continue;
                try {
                    Method fromMethod = obj.getClass().getMethod("from");
                    Method toMethod = obj.getClass().getMethod("to");
                    Method dimMethod = obj.getClass().getMethod("dimensions");
                    fromMethod.setAccessible(true);
                    toMethod.setAccessible(true);
                    dimMethod.setAccessible(true);

                    int from = (int) fromMethod.invoke(obj);
                    int to = (int) toMethod.invoke(obj);
                    Object dims = dimMethod.invoke(obj);

                    Method leftMethod = dims.getClass().getMethod("left");
                    Method rightMethod = dims.getClass().getMethod("right");
                    leftMethod.setAccessible(true);
                    rightMethod.setAccessible(true);

                    int left = (int) leftMethod.invoke(dims);
                    int right = (int) rightMethod.invoke(dims);

                    converted.add(new LazyUnihexMap.OverrideRangeData(from, to, left, right));
                } catch (Throwable t) {
                    LOGGER.debug("[MemFixer/Font] Could not parse size override range: {}", t.getMessage());
                }
            }
        }

        LazyUnihexMap lazyMap = LazyUnihexMap.createFromZipStream(stream, converted);
        if (lazyMap != null) {
            activeMap = lazyMap;
            return UnihexProviderAccessor.create(lazyMap);
        }

        return null;
    }

    private static volatile LazyUnihexMap activeMap;

    public static int getOffHeapMemoryKb() {
        return activeMap != null ? activeMap.getOffHeapMemoryBytes() / 1024 : 0;
    }
}
