package com.memfixer.client;

import com.memfixer.modules.font.LazyUnihexHandler;
import com.memfixer.modules.model.ConditionDeduplicator;
import com.memfixer.modules.model.QuadDeduplicator;
import com.memfixer.modules.texture.TextureReaperHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.function.LongFunction;

/**
 * Client-only diagnostic telemetry provider.
 * Strictly isolated from MemFixerCommand to guarantee zero ClassNotFoundException / NoClassDefFoundError
 * when executing diagnostic commands on Dedicated Server distributions.
 */
public final class ClientTelemetry {

    private ClientTelemetry() {}

    public static void appendModelQuads(MutableComponent report, LongFunction<String> formatNumber) {
        long dedupedQuads = QuadDeduplicator.getDeduplicatedCount();
        long totalQuads = QuadDeduplicator.getTotalRequests();
        report.append(Component.literal(" §7• §bModel Quads: §f" + formatNumber.apply(dedupedQuads) + " §7deduplicated"));
        if (totalQuads > 0) {
            int ratio = (int) ((dedupedQuads * 100) / totalQuads);
            report.append(Component.literal(" §8(" + ratio + "% hit rate)"));
        }
        report.append(Component.literal("\n"));
    }

    public static void appendMultipartConditions(MutableComponent report, LongFunction<String> formatNumber) {
        int predHits = ConditionDeduplicator.getPredicateHits();
        int pairHits = ConditionDeduplicator.getPairHits();
        report.append(Component.literal(" §7• §bMultipart Conditions: §f" + formatNumber.apply((long) predHits) + " §7predicates, §f"
                + formatNumber.apply((long) pairHits) + " §7model pairs saved\n"));
    }

    public static void appendFontAndTextures(MutableComponent report, LongFunction<String> formatNumber) {
        int unifontKb = LazyUnihexHandler.getOffHeapMemoryKb();
        int texturesReaped = TextureReaperHandler.getTotalReapedCount();
        report.append(Component.literal(" §7• §bFont & Textures: §7CJK unifont on-demand (§f~" + unifontKb + " KB§7), §f"
                + formatNumber.apply((long) texturesReaped) + " §7CPU texture buffers reaped\n"));
    }
}
