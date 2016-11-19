package com.xx.eventbus.model;

import java.io.Serializable;

/**
 * Created by wxx on 2016/11/6.
 * 新闻实体类
 */

public class NewsModel implements Serializable {

    private String id;
    private int commentCount;//评论数
    private int likeCount;//点赞数
    private String isLiked;//是否点过赞0表示未点赞，1表示已点赞

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public String getIsLiked() {
        return isLiked;
    }

    public void setIsLiked(String isLiked) {
        this.isLiked = isLiked;
    }
}
