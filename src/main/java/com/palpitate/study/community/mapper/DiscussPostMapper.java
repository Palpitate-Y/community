package com.palpitate.study.community.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.palpitate.study.community.entity.DiscussPost;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DiscussPostMapper {

    //查找帖子分页显示(userId是动态需要条件，0表示不拼接，其余拼接)
    List<DiscussPost> selectDiscussPosts(@Param("userId") int userId,@Param("offset") int offset,@Param("limit") int limit);

    //查看（某用户）有多少帖子，动态拼接
    int selectDiscussPostRows(int userId);

    //插入帖子
    int insertDiscussPost(DiscussPost discussPost);

    //查看帖子详情
    DiscussPost selectDiscussById(int id);

    //修改帖子评论数
    int updateCommentCount(int discussPortId,int commentCount);

    //置顶，修改帖子的类型type
    int updateDiscussType(int discussPortId,int type);

    //加精，删除，修改帖子的状态
    int updateDiscussStatus(int discussPortId,int status);

    //修改帖子的分数
    int updateDiscussScore(int postId,double score);


}
