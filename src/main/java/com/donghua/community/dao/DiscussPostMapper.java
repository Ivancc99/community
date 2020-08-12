package com.donghua.community.dao;

import com.donghua.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    List<DiscussPost> selectDisscussPosts(int userId, int offset, int limit);  // 参数userid是为了后来增加查询我发布的帖子做铺垫，可以说是个动态sql

    // @Param 用语给参数起别名
    // 如果只有一个参数，并且在<if>中使用，则必须加别名
    int selectDisscussPossRows(@Param("userId") int userId);
}
