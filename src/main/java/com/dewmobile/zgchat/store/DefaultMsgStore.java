package com.dewmobile.zgchat.store;

import com.dewmobile.zgchat.bean.Message;
import com.dewmobile.zgchat.common.ThreadFactoryImpl;
import com.dewmobile.zgchat.constant.MsgType;
import com.dewmobile.zgchat.constant.RedisConfig;
import com.dewmobile.zgchat.util.MsgUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import javax.annotation.PreDestroy;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 默认消息存储
 *
 * @author zc
 * @date 2019-10-09
 */
@Slf4j
@Component
public class DefaultMsgStore implements MsgStore {

    private final MongoTemplate mongoTemplate;
    private final StringRedisTemplate redisTemplate;
    private final ThreadPoolExecutor threadPoolExecutor =
            new ThreadPoolExecutor(1, 4, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(500), new ThreadFactoryImpl("MsgStoreThread"));

    public DefaultMsgStore(StringRedisTemplate redisTemplate, MongoTemplate mongoTemplate) {
        this.redisTemplate = redisTemplate;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void start() {
        log.info("redis queue listening...");

        while (true) {
            String content = redisTemplate.opsForList().rightPop(RedisConfig.KEY_CHAT_MSG);
            if (!StringUtils.isEmpty(content)) {
                log.info("receive data: " + content);
                threadPoolExecutor.execute(new StoreMessage(content));
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    @PreDestroy
    public void shutdown() {
        System.out.println("thread pool shutdown");
        threadPoolExecutor.shutdown();
    }

    class StoreMessage implements Runnable {
        String content;

        StoreMessage(String content) {
            this.content = content;
        }

        @Override
        public void run() {
            try {
                Message message = MsgUtil.resolve2Message(content);
                String type = message.getType();
                if (!StringUtils.isEmpty(type) && type.equals(MsgType.TYPE_TXT)) {
                    try {
                        mongoTemplate.insert(message, "messages");
                        log.info("Success!!!");
                    } catch (Exception e) {
                        log.error("Error: " + e.getMessage());
                    }
                } else {
                    log.warn("Skip: type is " + type);
                }
            } catch (Exception e) {
                log.warn("Warn: message is not valid format : " + content);
            }
        }
    }
}
