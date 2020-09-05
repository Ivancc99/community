package com.donghua.community;


import com.donghua.community.dao.AlphaDao;
import com.donghua.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testStrings() {
        String key = "test:count";
        redisTemplate.opsForValue().set(key, 1);
        System.out.println(redisTemplate.opsForValue().get(key));
        System.out.println(redisTemplate.opsForValue().increment(key));
        System.out.println(redisTemplate.opsForValue().decrement(key));
    }

    @Test
    public void testHashs() {
        String key = "test:user";
        redisTemplate.opsForHash().put(key, "id", 1);
        redisTemplate.opsForHash().put(key, "username", "zhangsan");

        System.out.println(redisTemplate.opsForHash().get(key, "id"));
        System.out.println(redisTemplate.opsForHash().get(key, "username"));

    }

    @Test
    public void testLists() {
        String key = "test:ids";

        redisTemplate.opsForList().leftPush(key, 101);
        redisTemplate.opsForList().leftPush(key, 102);
        redisTemplate.opsForList().leftPush(key, 103);

        System.out.println(redisTemplate.opsForList().size(key));
        System.out.println(redisTemplate.opsForList().index(key, 0));
        System.out.println(redisTemplate.opsForList().range(key, 0, 2));

        System.out.println(redisTemplate.opsForList().leftPop(key));
        System.out.println(redisTemplate.opsForList().leftPop(key));
        System.out.println(redisTemplate.opsForList().leftPop(key));
    }

    @Test
    public void testSets() {
        String key = "test:teachers";

        redisTemplate.opsForSet().add(key, "刘备", "关羽", "张飞", "赵云", "诸葛亮");

        System.out.println(redisTemplate.opsForSet().size(key));
        System.out.println(redisTemplate.opsForSet().pop(key));
        System.out.println(redisTemplate.opsForSet().members(key));

    }

    @Test
    public void testSortedSets() {
        String key = "test:student";

        redisTemplate.opsForZSet().add(key, "嬴政", 90);
        redisTemplate.opsForZSet().add(key, "李世民", 88);
        redisTemplate.opsForZSet().add(key, "刘彻", 89);
        redisTemplate.opsForZSet().add(key, "朱元璋", 82);
        redisTemplate.opsForZSet().add(key, "赵匡胤", 80);

        System.out.println(redisTemplate.opsForZSet().zCard(key));
        System.out.println(redisTemplate.opsForZSet().score(key, "赵匡胤"));
        System.out.println(redisTemplate.opsForZSet().reverseRank(key, "李世民"));
        System.out.println(redisTemplate.opsForZSet().reverseRange(key, 0, 2));

    }

    @Test
    public void testKeys() {
        String key = "test:user";

        redisTemplate.delete(key);
        System.out.println(redisTemplate.hasKey(key));

        redisTemplate.expire("test:teachers", 10, TimeUnit.SECONDS);
    }

    // 多次访问同一个ｋｅｙ
    @Test
    public void testBound() {
        String key = "test:count";

        BoundValueOperations operations = redisTemplate.boundValueOps(key);
        System.out.println(operations.get());
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        operations.increment();
        System.out.println(operations.get());
    }

    @Test
    public void testTransaction() {
        Object object = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String key = "test:tx";

                redisOperations.multi();
                redisTemplate.opsForSet().add(key, "zhangsan");
                redisTemplate.opsForSet().add(key, "lisi");
                redisTemplate.opsForSet().add(key, "wangwu");

                System.out.println(redisTemplate.opsForSet().members(key));

                return redisOperations.exec();
            }
        });
        System.out.println(object);
    }

    // 统计２０万个重复数据的独立总数
    @Test
    public void testHyperLogLog() {
        String redeskey = "test:hll:01";

        for (int i = 1; i <= 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redeskey, i);
        }

        for (int i = 0; i < 100000; i++) {
            int k = (int) (Math.random() * 100000 + 1);
            redisTemplate.opsForHyperLogLog().add(redeskey, k);
        }

        System.out.println(redisTemplate.opsForHyperLogLog().size(redeskey));
    }

    // 将三组数据进行合并，在统计合并后的独立整数
    @Test
    public void testHyperLogLogUnion() {
        String redisKey = "test:hll:02";
        for (int i = 0; i < 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey, i + 1);
        }

        String redisKey1 = "test:hll:03";
        for (int i = 5000; i < 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey1, i + 1);
        }

        String redisKey2 = "test:hll:04";
        for (int i = 10000; i < 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2, i + 1);
        }

        String unionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey, redisKey, redisKey1, redisKey2);
        System.out.println(redisTemplate.opsForHyperLogLog().size(unionKey));
    }

    // 统计一组数据的布尔值
    @Test
    public void testBitmap() {
        String redisKey = "test:bm:01";

        // 记录
        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        redisTemplate.opsForValue().setBit(redisKey, 4, true);
        redisTemplate.opsForValue().setBit(redisKey, 7, true);
        redisTemplate.opsForValue().setBit(redisKey, 9, true);

        // 查询
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));

        // 统计
        Object object = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(redisKey.getBytes());
            }
        });
        System.out.println(object);
    }

    // 统计三组数据的布尔值，并对这三组数据进行ＯＲ运算
    @Test
    public void testBitmapOperation() {
        String redisKey1 = "test:bm:02";

        // 记录
        redisTemplate.opsForValue().setBit(redisKey1, 0, true);
        redisTemplate.opsForValue().setBit(redisKey1, 1, true);
        redisTemplate.opsForValue().setBit(redisKey1, 2, true);

        String redisKey2 = "test:bm:03";

        // 记录
        redisTemplate.opsForValue().setBit(redisKey2, 2, true);
        redisTemplate.opsForValue().setBit(redisKey2, 3, true);
        redisTemplate.opsForValue().setBit(redisKey2, 4, true);

        String redisKey3 = "test:bm:04";

        // 记录
        redisTemplate.opsForValue().setBit(redisKey3, 4, true);
        redisTemplate.opsForValue().setBit(redisKey3, 5, true);
        redisTemplate.opsForValue().setBit(redisKey3, 6, true);

        String orKey = "test:bm:or";

        Object object = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR, orKey.getBytes(), redisKey1.getBytes(), redisKey2.getBytes(), redisKey3.getBytes());
                return connection.bitCount(orKey.getBytes());
            }
        });
        System.out.println(object);

        for (int i = 0; i < 7; i++) {
            System.out.println(redisTemplate.opsForValue().getBit(orKey, i));
        }




    }
}
