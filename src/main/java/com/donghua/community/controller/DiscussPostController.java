package com.donghua.community.controller;

import com.donghua.community.entity.Comment;
import com.donghua.community.entity.DiscussPost;
import com.donghua.community.entity.Page;
import com.donghua.community.entity.User;
import com.donghua.community.service.CommentService;
import com.donghua.community.service.DiscussPostService;
import com.donghua.community.service.LikeServicce;
import com.donghua.community.service.UserService;
import com.donghua.community.util.CommunityConstant;
import com.donghua.community.util.CommunityUtil;
import com.donghua.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
@RequestMapping(path = "/discuss")
public class DiscussPostController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private CommentService commnetService;

    @Autowired
    private LikeServicce likeServicce;

    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content){
        User user = hostHolder.getUser();
        if (user == null){
            return CommunityUtil.getJSONString(403, "你还没有登录哦！");
        }
        DiscussPost discussPost = new DiscussPost();
        discussPost.setTitle(title);
        discussPost.setContent(content);
        discussPost.setUserId(user.getId());
        discussPost.setCreateTime(new Date());
        discussPostService.insertDiscussPost(discussPost);

        // 报错的情况将来统一处理
        return CommunityUtil.getJSONString(0, "发布成功！");
    }

    @RequestMapping(path = "/detail/{dicussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable int dicussPostId, Model model, Page page){
        // 帖子
        DiscussPost post = discussPostService.findDiscussPostById(dicussPostId);
        model.addAttribute("post", post);

        // 作者
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        //　点赞数量
        long likeCount = likeServicce.findEntityLikeCount(ENTITY_TYPE_POST, dicussPostId);
        model.addAttribute("likeCount", likeCount);

        // 点赞状态
        int likeStatus = hostHolder.getUser() == null ? 0 :
                likeServicce.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_POST, dicussPostId);
        model.addAttribute("likeStatus", likeStatus);

        // 评论分页信息
        page.setPath("/discuss/detail/" + dicussPostId);
        page.setRows(post.getCommentCount());
        page.setLimit(5);

        // comment :　给帖子的评论
        // reply : 给评论的评论
        // 评论列表
        List<Comment> commentList = commnetService.findCommentsByEntity(ENTITY_TYPE_POST, dicussPostId, page.getOffset(), page.getLimit());
        // 建一个哈希ｍａｐ列表来存放数据,　一个ｍａｐ记录一个评论的所有信息，包括发布者，内容，相关回复等
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null){
            for(Comment comment : commentList){
                Map<String, Object> commentVo = new HashMap<>();
                commentVo.put("comment", comment);
                commentVo.put("user", userService.findUserById(comment.getUserId()));

                //　点赞数量
                likeCount = likeServicce.findEntityLikeCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeCount", likeCount);

                // 点赞状态
                likeStatus = hostHolder.getUser() == null ? 0 :
                        likeServicce.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("likeStatus", likeStatus);

                //　回复列表
                List<Comment> replys = commnetService.findCommentsByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // 记录回复的相关信息，内容，用户等
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replys != null){
                    for(Comment reply : replys){
                        Map<String, Object> replyVo = new HashMap<>();
                        replyVo.put("reply", reply);
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        User targetUser = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", targetUser);
                        //　点赞数量
                        likeCount = likeServicce.findEntityLikeCount(ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeCount", likeCount);

                        // 点赞状态
                        likeStatus = hostHolder.getUser() == null ? 0 :
                                likeServicce.findEntityLikeStatus(hostHolder.getUser().getId(), ENTITY_TYPE_COMMENT, reply.getId());
                        replyVo.put("likeStatus", likeStatus);
                        replyVoList.add(replyVo);
                    }
                }
                commentVo.put("replys", replyVoList);
                // 回复的数量
                int replyCount = commnetService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);
            }
        }
        model.addAttribute("comments", commentVoList);

        return "/site/discuss-detail";
    }


}
