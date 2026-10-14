package com.hzr.seckillwithsql.constant;

/**
 * 秒杀业务 Redis 数据统一常量。
 *
 * <p>本项目在 Redis 中一共使用 5 类数据结构，全部以 {@link #SECKILL_PREFIX} 开头，便于运维按前缀排查：</p>
 *
 * <pre>
 * ┌────┬──────────┬───────────────────────────────────────┬──────────────────────┬─────────┐
 * │ 序号│ Redis类型 │ key                                    │ value / member       │ TTL     │
 * ├────┼──────────┼───────────────────────────────────────┼──────────────────────┼─────────┤
 * │ 1  │ String   │ seckill:product:stock:{productId}      │ 剩余库存数            │ 不过期   │
 * │ 2  │ Set      │ seckill:product:active                 │ 已开启秒杀的商品ID     │ 不过期   │
 * │ 3  │ Set      │ seckill:product:bought:{productId}     │ 已下单的用户ID         │ 不过期   │
 * │ 4  │ String   │ seckill:product:rate:{userId}          │ "1"（防抖占位）        │ 5 秒    │
 * │ 5  │ List     │ seckill:order:queue                    │ 订单 JSON 字符串       │ 不过期   │
 * └────┴──────────┴───────────────────────────────────────┴──────────────────────┴─────────┘
 * </pre>
 *
 * <p>生产 / 消费约定：库存与活动集合由 {@code ProductDataServiceImpl#initSeckill} 写入；
 * 订单队列由 {@code OrderDataServiceImpl#participateSeckill} 使用 leftPush 生产，
 * 由 {@code OrderConsumer} 使用 rightPop 消费（先进先出）。</p>
 *
 * @author Administrator
 */
public class UserConstant {

    /** 工具类，禁止实例化 */
    private UserConstant() {
    }

    // ==================================================================================
    // 0. 统一前缀
    // ==================================================================================

    /** 秒杀业务统一前缀，所有 key 均以此开头 */
    public static final String SECKILL_PREFIX = "seckill:";

    // ==================================================================================
    // 1. 商品库存（String）
    // ==================================================================================

    /** 商品库存 key 前缀：{@code seckill:product:stock:{productId}} */
    public static final String PRODUCT_STOCK_PREFIX = SECKILL_PREFIX + "product:stock:";

    // ==================================================================================
    // 2. 秒杀活动商品集合（Set）
    // ==================================================================================

    /** 已开启秒杀活动的商品 ID 集合：{@code seckill:product:active}，member 为 productId */
    public static final String ACTIVE_PRODUCT_KEY = SECKILL_PREFIX + "product:active";

    // ==================================================================================
    // 3. 一人一单 · 已购用户集合（Set）
    // ==================================================================================

    /** 已购用户集合 key 前缀：{@code seckill:product:bought:{productId}}，member 为 userId */
    public static final String BOUGHT_USER_PREFIX = SECKILL_PREFIX + "product:bought:";

    // ==================================================================================
    // 4. 下单防抖 / 限流（String + TTL）
    // ==================================================================================

    /** 下单防抖 key 前缀：{@code seckill:product:rate:{userId}}，每个用户 N 秒只允许下单一次 */
    public static final String RATE_LIMIT_PREFIX = SECKILL_PREFIX + "product:rate:";

    /** 防抖占位值（仅用于占位，SetIfAbsent 成功即代表通过） */
    public static final String RATE_LIMIT_VALUE = "1";

    /** 防抖时间窗，单位：秒 */
    public static final long RATE_LIMIT_SECONDS = 5L;

    // ==================================================================================
    // 5. 订单异步队列（List）
    // ==================================================================================

    /** 订单队列：{@code seckill:order:queue}，leftPush 入队、rightPop 出队 */
    public static final String ORDER_QUEUE_KEY = SECKILL_PREFIX + "order:queue";

    /** 消费者阻塞弹出订单的超时时间，单位：秒 */
    public static final long ORDER_QUEUE_POP_TIMEOUT_SECONDS = 3L;

    /** 消费异常时的退避时间，单位：毫秒 */
    public static final long CONSUMER_ERROR_BACKOFF_MILLIS = 1_000L;

    // ==================================================================================
    // 6. key 构建方法（统一拼接方式，避免各处手写字符串）
    // ==================================================================================

    /**
     * 构建商品库存 key。
     *
     * @param productId 商品 ID（String / Integer 等均可）
     * @return {@code seckill:product:stock:{productId}}
     */
    public static String productStockKey(Object productId) {
        return PRODUCT_STOCK_PREFIX + productId;
    }

    /**
     * 构建某商品的已购用户集合 key。
     *
     * @param productId 商品 ID
     * @return {@code seckill:product:bought:{productId}}
     */
    public static String boughtUserKey(Object productId) {
        return BOUGHT_USER_PREFIX + productId;
    }

    /**
     * 构建用户下单防抖 key。
     *
     * @param userId 用户 ID
     * @return {@code seckill:product:rate:{userId}}
     */
    public static String rateLimitKey(Object userId) {
        return RATE_LIMIT_PREFIX + userId;
    }
}
