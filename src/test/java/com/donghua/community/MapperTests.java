package com.donghua.community;

import com.donghua.community.dao.*;
import com.donghua.community.entity.*;
import com.donghua.community.util.CommunityConstant;
import com.donghua.community.util.CommunityUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private LoginTicketMapper loginTicketMapper;
    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private MessageMapper messageMapper;

    @Test
    public void testSelectUser() {
        User user = userMapper.selectById(101);
        System.out.println(user);

        User liubei = userMapper.selectByName("liubei");
        System.out.println(liubei);

        user = userMapper.selectByEmail("nowcoder101@sina.com");
        System.out.println(user);
    }

    @Test
    public void testInsertUser() {
        User user = new User();
        user.setUsername("test");
        user.setPassword("123456");
        user.setSalt("abc");
        user.setEmail("test@qq.com");
        user.setHeaderUrl("http://www.nowcoder.com/101.png");
        user.setCreateTime(new Date());

        int rows = userMapper.insertUser(user);
        System.out.println(rows);
        System.out.println(user.getId());
    }

    @Test
    public void updateUser() {
        int rows = userMapper.updateStatus(150, 1);
        System.out.println(rows);

        rows = userMapper.updateHeaderUrl(150, "http://www.nowcoder.com/102.png");
        System.out.println(rows);

        rows = userMapper.updatePassword(150, "hello");
        System.out.println(rows);
    }

    @Test
    public void testSelectDisscussPost() {
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, 0, 10, 0);
        for (DiscussPost post :
                list) {
            System.out.println(post);
        }

        int i = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(i);
    }

    @Test
    public void testInserLoginTicket() {
        LoginTicket ticket = new LoginTicket();
        ticket.setStatus(1);
        ticket.setTicket(CommunityUtil.generateUUID());
        ticket.setUserId(100);
        ticket.setExpired(new Date(System.currentTimeMillis() + 1000 * 60 * 10));

        loginTicketMapper.insertLoginTicket(ticket);
    }

    @Test
    public void testSelectLoginTicket() {
        LoginTicket ticket = loginTicketMapper.selectByTicket("9ef3d19c1612432ababfc6de2df238d5");
        System.out.println(ticket);
    }

    @Test
    public void testUpdateTicketStatus() {
        int i = loginTicketMapper.updateStatus("9ef3d19c1612432ababfc6de2df238d5", 0);
        System.out.println(i);
    }

    @Test
    public void testInsertDiscusspost() {
        List<DiscussPost> list = discussPostMapper.selectDiscussPosts(0, 0, 10, 0);
        DiscussPost discussPost = list.get(2);
        System.out.println(discussPost);
        discussPost.setUserId(9999);
        int i = discussPostMapper.insertDiscussPost(discussPost);
        System.out.println(i);
    }

    @Test
    public void testInsertComment() {
        Comment comment = new Comment();
        comment.setContent("大家好，我是陈冠希");
        comment.setCreateTime(new Date());
        comment.setEntityId(228);
        comment.setEntityType(1);
        comment.setStatus(0);
        comment.setTargetId(0);
        comment.setUserId(4);
        commentMapper.insertComment(comment);
    }

    @Test
    public void testSelectLetters() {
        List<Message> messages = messageMapper.selectConversations(111, 0, 20);
        for (Message message : messages
        ) {
            System.out.println(message);
        }

        int i = messageMapper.selectConversationCount(111);
        System.out.println(i);

        messages = messageMapper.selectLetters("111_112", 0, 20);
        for (Message message : messages
        ) {
            System.out.println(message);
        }

        i = messageMapper.selectLetterCount("111_112");
        System.out.println(i);

        i = messageMapper.selectLetterUnreadCount(131, "111_131");
        System.out.println(i);

    }

    @Test
    public void insertMessages(){
        Message message =new Message();
        message.setContent("java才是最好的语言！");
        message.setConversationId("111_112");
        message.setCreateTime(new Date());
        message.setFromId(112);
        message.setToId(111);
        message.setStatus(0);

        messageMapper.insertMessage(message);
        List<Message> messages = messageMapper.selectLetters("111_112", 0, 10);
        System.out.println(messages);
    }

    @Test
    public void updateMessageStatus(){
        List<Integer> list = new ArrayList<>();
        list.add(355);
        list.add(356);
        messageMapper.updateStatus(list, 1);
        List<Message> messages = messageMapper.selectLetters("111_112", 0, 10);
        for(Message message:messages){
            System.out.println(message);
        }
    }

    @Test
    public void testUnreadNoticeCount(){
        System.out.println(messageMapper.selectNoticeUnreadCount(111,TOPIC_COMMENT));
        System.out.println(messageMapper.selectNoticeUnreadCount(111,TOPIC_FOLLOW));
        System.out.println(messageMapper.selectNoticeUnreadCount(111,TOPIC_LIKE));
        System.out.println(messageMapper.selectNoticeUnreadCount(111,null));
    }
}
