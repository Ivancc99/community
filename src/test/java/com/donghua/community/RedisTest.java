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
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
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
}
