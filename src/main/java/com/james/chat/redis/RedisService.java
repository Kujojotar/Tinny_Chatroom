package com.james.chat.redis;

import com.james.chat.util.JacksonUtil;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

@Service
public class RedisService {

    final JedisPool jedisPool;

    private class ActiveUserBitmap{
        private String bitmapName = "active_user_bitmap";
        private volatile long count = 4000000000L;

        private boolean checkOffset(long offset) {
            if (offset < 0 || offset >= count) {
                return false;
            }
            return true;
        }

        public boolean setBitTrueIfExists(long offset) {
            Jedis jedis = null;
            if (!checkOffset(offset)) {
                return false;
            }
            try {
                jedis = RedisService.this.jedisPool.getResource();
                jedis.setbit(bitmapName, offset, true);
                return true;
            } catch (Exception e) {
                return false;
            } finally {
                jedis.close();
            }
        }


        public boolean setBitFalseIfExists(long offset) {
            Jedis jedis = null;
            if (!checkOffset(offset)) {
                return false;
            }
            try {
                jedis = RedisService.this.jedisPool.getResource();
                jedis.setbit(bitmapName, offset, false);
                return true;
            } catch (Exception e) {
                return false;
            } finally {
                jedis.close();
            }
        }

        public int getBitIfExists(long offset) {
            if (!checkOffset(offset)) {
                return -1;
            }
            Jedis jedis = null;
            boolean res;
            try {
                jedis = jedisPool.getResource();
                res = jedis.getbit(bitmapName, offset);
                if (res) {
                    return 1;
                } else {
                    return 0;
                }
            } catch (Exception e) {
                return -1;
            } finally {
                jedis.close();
            }
        }
    }

    private class ZSetInstance {
        String name = "ActiveUserSet";
        private long level = 0;

        public ZSetInstance() {
            long currentTime = System.currentTimeMillis();
            level =  currentTime / Integer.MAX_VALUE;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    long currentTime = System.currentTimeMillis();
                    long curLevel = currentTime / Integer.MAX_VALUE;
                    if (curLevel == level) {
                        // Do nothing
                    } else {
                        double tmpScore = timeToScore(currentTime);
                        tmpScore += Integer.MAX_VALUE;
                        Jedis jedis = null;
                        try {
                            jedis = jedisPool.getResource();
                            jedis.zremrangeByScore(name, 0, tmpScore - (5 * 60 * 1000));
                            Set<String> strings = jedis.zrangeByScore(name, tmpScore - (5 * 60 * 1000), tmpScore + (5 * 60 * 1000));
                            tmpScore -= Integer.MAX_VALUE;
                            for (String key : strings) {
                                jedis.zadd(name, tmpScore, key);
                            }
                            level = curLevel;
                        } catch (Exception e) {
                            if (jedis != null) {
                                jedis.del(name);
                            }
                            level = curLevel;
                        } finally {
                            jedis.close();
                        }
                    }
                }
            }, 24*60*60*100L);
        }

        private double timeToScore(Long time) {
            time = time % Integer.MAX_VALUE;
            return Double.longBitsToDouble(time);
        }

        public void addToSet(String userId, long timeStamp) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                jedis.zadd(name, Double.longBitsToDouble(timeStamp), userId);
            } finally {
                jedis.close();
            }
        }

        public void delFromSet(String userId) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                jedis.zrem(name, userId);
            } finally {
                jedis.close();
            }
        }

        public boolean existsUserId(String userId) {
            Jedis jedis = null;
            try {
                jedis = jedisPool.getResource();
                return jedis.zscore(name, userId) != null;
            } catch (Exception e) {
                return false;
            } finally {
                jedis.close();
            }
        }
    }

    private ActiveUserBitmap bitmap = new ActiveUserBitmap();

    private ZSetInstance setInstance = new ZSetInstance();

    public RedisService(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    /**
     * 向Redis中存值，永久有效
     */
    public String set(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String res = jedis.set(key, value);
            return res;
        } catch (Exception e) {
            return "0";
        } finally {
            jedis.close();
        }
    }

    /**
     * 根据传入Key获取指定Value
     */
    public String get(String key) {
        Jedis jedis = null;
        String value;
        try {
            jedis = jedisPool.getResource();
            value = jedis.get(key);
        } catch (Exception e) {
            return "0";
        } finally {
            jedis.close();
        }
        return value;
    }

    /**
     * 校验Key值是否存在
     */
    public Boolean exists(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.exists(key);
        } catch (Exception e) {
            return false;
        } finally {
            jedis.close();
        }
    }

    /**
     * 删除指定Key-Value
     */
    public Long del(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.del(key);
        } catch (Exception e) {
            return 0L;
        } finally {
            jedis.close();
        }
    }

    /**
     * 分布式锁
     *
     * @param key
     * @param value
     * @param time  锁的超时时间，单位：秒
     * @return 获取锁成功返回"OK"，失败返回null
     */
    public String getDistributedLock(String key, String value, int time) {
        Jedis jedis = null;
        String ret = "";
        try {
            jedis = jedisPool.getResource();

            ret = jedis.set(key, value, new SetParams().nx().ex(time));
            return ret;
        } catch (Exception e) {
            return null;
        } finally {
            jedis.close();
        }
    }

    /**
     * 从redis连接池获取redis实例
     */
    public <T> T get(BasePrefix prefix, String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //对key增加前缀，即可用于分类，也避免key重复
            String realKey = prefix.getPrefix() + key;
            String str = jedis.get(realKey);
            T t = stringToBean(str, clazz);
            return t;
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 从redis连接池获取redis实例
     */
    public <T> T get(String key, Class<T> clazz) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //对key增加前缀，即可用于分类，也避免key重复
            String realKey =  key;
            String str = jedis.get(realKey);
            T t = stringToBean(str, clazz);
            return t;
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 存储对象
     */
    public <T> Boolean set(BasePrefix prefix, String key, T value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            String str = beanToString(value);
            if (str == null || str.length() <= 0) {
                return false;
            }
            String realKey = prefix.getPrefix() + key;
            int seconds = prefix.expireSeconds();//获取过期时间
            if (seconds <= 0) {
                jedis.set(realKey, str);
            } else {
                jedis.setex(realKey, seconds, str);
            }
            return true;
        } finally {
            returnToPool(jedis);
        }

    }

    /**
     * 删除
     */
    public boolean delete(BasePrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix() + key;
            long ret = jedis.del(realKey);
            return ret > 0;
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 判断key是否存在
     */
    public <T> boolean exists(BasePrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix() + key;
            return jedis.exists(realKey);
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 增加值
     * Redis Incr 命令将 key 中储存的数字值增一。    如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作
     */
    public <T> Long incr(BasePrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix() + key;
            return jedis.incr(realKey);
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     * 减少值
     */
    public <T> Long decr(BasePrefix prefix, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix() + key;
            return jedis.decr(realKey);
        } finally {
            returnToPool(jedis);
        }
    }


    public static <T> String beanToString(T value) {
        if (value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();
        if (clazz == int.class || clazz == Integer.class) {
            return String.valueOf(value);
        } else if (clazz == long.class || clazz == Long.class) {
            return String.valueOf(value);
        } else if (clazz == String.class) {
            return (String) value;
        } else {
            return JacksonUtil.writeObject(value);
        }

    }

    public static <T> T stringToBean(String str, Class<T> clazz) {
        if (str == null || str.length() <= 0 || clazz == null) {
            return null;
        }
        if (clazz == int.class || clazz == Integer.class) {
            return (T) Integer.valueOf(str);
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) Long.valueOf(str);
        } else if (clazz == String.class) {
            return (T) str;
        } else {
            return JacksonUtil.<T>getObject(str, clazz);
        }
    }

    private void returnToPool(Jedis jedis) {
        if (jedis != null) {
            jedis.close();//不是关闭，只是返回连接池
        }
    }

    private void sendMessage() {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            //生成真正的key
        } finally {
            returnToPool(jedis);
        }
    }

    public boolean setBit(long offset, int val) {
        if (val == 0) {
            return bitmap.setBitFalseIfExists(offset);
        } else {
            return bitmap.setBitTrueIfExists(offset);
        }
    }

    public int getBitmapVal(long offset) {
        return bitmap.getBitIfExists(offset);
    }

    public void addZSetMember(String userId) {
        setInstance.addToSet(userId, System.currentTimeMillis());
    }

    public void removeFromSet(String userId) {
        setInstance.delFromSet(userId);
    }

    public boolean isUserExistsInSet(String userId) {
        return setInstance.existsUserId(userId);
    }

}
