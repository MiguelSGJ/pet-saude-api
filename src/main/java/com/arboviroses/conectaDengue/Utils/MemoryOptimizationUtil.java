package com.arboviroses.conectaDengue.Utils;

import java.util.List;

/**
 * Utilitário para otimização de memória durante processamento de arquivos grandes
 */
public class MemoryOptimizationUtil {

    private static final Runtime runtime = Runtime.getRuntime();
    private static final long MB = 1024 * 1024;
    
    /**
     * Força limpeza de memória e verifica se há memória suficiente
     */
    public static void forceGarbageCollection() {
        System.gc();
        System.runFinalization();
        System.gc();
    }
    
    /**
     * Verifica se a memória disponível está baixa (menos de 50MB)
     */
    public static boolean isMemoryLow() {
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long maxMemory = runtime.maxMemory();
        
        long availableMemory = maxMemory - (totalMemory - freeMemory);
        return availableMemory < (50 * MB);
    }
    
    /**
     * Limpa e redimensiona listas para liberar memória
     */
    public static void clearAndTrimLists(List<?>... lists) {
        for (List<?> list : lists) {
            if (list != null) {
                list.clear();
                // Se for ArrayList, força redimensionamento
                if (list instanceof java.util.ArrayList) {
                    ((java.util.ArrayList<?>) list).trimToSize();
                }
            }
        }
    }
    
    /**
     * Calcula o tamanho de batch dinâmico baseado na memória disponível
     */
    public static int getDynamicBatchSize() {
        long availableMemory = getAvailableMemory();
        
        if (availableMemory > 100 * MB) {
            return 100;
        } else if (availableMemory > 50 * MB) {
            return 50;
        } else if (availableMemory > 20 * MB) {
            return 25;
        } else {
            return 10;
        }
    }
    
    public static long getAvailableMemory() {
        long freeMemory = runtime.freeMemory();
        long totalMemory = runtime.totalMemory();
        long maxMemory = runtime.maxMemory();
        
        return maxMemory - (totalMemory - freeMemory);
    }
    
    public static void logMemoryStats(String operation) {
        long totalMemory = runtime.totalMemory() / MB;
        long freeMemory = runtime.freeMemory() / MB;
        long maxMemory = runtime.maxMemory() / MB;
        long usedMemory = totalMemory - freeMemory;
        
        System.out.println(String.format(
            "[%s] Memória - Usada: %dMB, Livre: %dMB, Total: %dMB, Max: %dMB",
            operation, usedMemory, freeMemory, totalMemory, maxMemory
        ));
    }
}
