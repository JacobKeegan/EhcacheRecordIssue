import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.core.internal.statistics.DefaultStatisticsService;
import org.ehcache.core.spi.service.StatisticsService;

public class Main {
    private static final StatisticsService statisticsService = new DefaultStatisticsService();
    private static final CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
            .using(statisticsService).build(true);
    private static final SampleRecord testKey = new SampleRecord(1);
    private static final Object testValue = new Object();

    /**
     * A basic record class.
     */
    private record SampleRecord(int id) {}

    public static void main(String[] args) {
        testCache(true);
        testCache(false);
    }

    /**
     * Simply attempts to put the testKey and testValue into a cache sized by entries or by MB,
     * and prints the results.
     */
    private static void testCache(boolean isSizedByEntries) {
        var resourcePoolsBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder()
                .heap(isSizedByEntries ? 10 : 1,
                        isSizedByEntries ? EntryUnit.ENTRIES : MemoryUnit.MB);
        var config = CacheConfigurationBuilder
                .newCacheConfigurationBuilder(SampleRecord.class, Object.class, resourcePoolsBuilder);
        String cacheName = isSizedByEntries ? "entryCache" : "memoryCache";
        Cache<SampleRecord, Object> currCache = cacheManager.createCache(cacheName, config);
        currCache.put(testKey, testValue);
        boolean putSucceeded = statisticsService.getCacheStatistics(cacheName).getCachePuts() != 0;
        System.out.printf("Attempt to put a value in the %s has %s.\n", cacheName,
                putSucceeded ? "succeeded" : "failed");
    }
}
