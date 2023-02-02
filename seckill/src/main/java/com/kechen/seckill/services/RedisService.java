package com.kechen.seckill.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.Resource;
import java.util.Collections;

@Slf4j
@Service
public class RedisService {
    @Resource
    private JedisPool jedisPool;

    /**
     * 设置值
     *
     * @param key
     * @param value
     */
    public void setValue(String key, Long value) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.set(key, value.toString());
        jedisClient.close();
    }

    /**
     * 获取值
     *
     * @param key
     * @return
     */
    public String getValue(String key) {
        Jedis jedisClient = jedisPool.getResource();
        String value = jedisClient.get(key);
        jedisClient.close();
        return value;
    }

    /**
     * 缓存中库存判断和扣减
     * @param key
     * @return
     * @throws Exception
     */
    public boolean stockDeductValidator(String key)  {
        try(Jedis jedisClient = jedisPool.getResource()) {

            String script = "if redis.call('exists',KEYS[1]) == 1 then\n" +
                    "                 local stock = tonumber(redis.call('get', KEYS[1]))\n" +
                    "                 if( stock <=0 ) then\n" +
                    "                    return -1\n" +
                    "                 end;\n" +
                    "                 redis.call('decr',KEYS[1]);\n" +
                    "                 return stock - 1;\n" +
                    "             end;\n" +
                    "             return -1;";

            Long stock = (Long) jedisClient.eval(script, Collections.singletonList(key), Collections.emptyList());
            if (stock < 0) {
                System.out.println("Inventory shortage");
                return false;
            } else {
                System.out.println("Congratulations, the snap up is successful");
            }
            return true;
        } catch (Throwable throwable) {
            System.out.println("Inventory deduction failed：" + throwable.toString());
            return false;
        }
    }

    /**
     * 超时未支付 Redis 库存回滚
     *
     * @param key
     */

    public void revertStock(String key) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.incr(key);
        jedisClient.close();
    }

    public boolean isInLimitMember(long seckillActivityId, long userId) {
        /**
         * Determine whether it is in the purchase restriction list 判断是否在限购名单中
         *
         * @param activityId
         * @param userId
         * @return
         */
        Jedis jedisClient = jedisPool.getResource();
        boolean sismember = jedisClient.sismember("seckillActivity_users:" + seckillActivityId, String.valueOf(userId));
        jedisClient.close();
        log.info("userId:{} activityId:{} in the purchased list:{}", userId, seckillActivityId, sismember);
            return sismember;
        }

    /**
     * 添加限购名单
     *
     * @param activityId
     * @param userId
     */
    public void addLimitMember(long activityId, long userId) {
        Jedis jedisClient = jedisPool.getResource();
        jedisClient.sadd("seckillActivity_users:" + activityId,
                String.valueOf(userId));
        jedisClient.close();
    }

    public void removeLimitMember(Long seckillActivityId, Long userId) {
        /**
         * Remove Restricted List 移除限购名单
         *
         * @param activityId
         * @param userId
         */
            Jedis jedisClient = jedisPool.getResource();
            jedisClient.srem("seckillActivity_users:" + seckillActivityId,
                    String.valueOf(userId));
            jedisClient.close();
        }
    }
