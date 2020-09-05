package com.donghua.community.controller;

import com.donghua.community.annotation.LoginRequired;
import com.donghua.community.entity.Event;
import com.donghua.community.entity.User;
import com.donghua.community.event.EventProducer;
import com.donghua.community.service.LikeServicce;
import com.donghua.community.util.CommunityConstant;
import com.donghua.community.util.CommunityUtil;
import com.donghua.community.util.HostHolder;
import com.donghua.community.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant{

    @Autowired
    private LikeServicce likeServicce;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private EventProducer eventProducer;

    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        User user = hostHolder.getUser();

        // 点赞
        likeServicce.like(user.getId(), entityType, entityId, entityUserId);

        // 获取点赞数量
        long likeCount = likeServicce.findEntityLikeCount(entityType, entityId);

        // 获取点赞状态
        int likeStatus = likeServicce.findEntityLikeStatus(user.getId(), entityType, entityId);

        Map<String, Object> map = new HashMap<>();

        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        // 触发点赞通知
        if (likeStatus == 1) {
            // 如果是取消赞就没必要通知
            Event event = new Event()
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setTopic(TOPIC_LIKE)
                    .setUserId(hostHolder.getUser().getId())
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);

        }

        if(ENTITY_TYPE_POST == entityType){
            // 计算帖子分数
            String redisKey = RedisUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }

        return CommunityUtil.getJSONString(0, map);
    }
}
