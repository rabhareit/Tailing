package com.rabhareit.tailing.repository;

import com.rabhareit.tailing.entity.UserConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TweetCountRepository {

  @Autowired
  JdbcTemplate jdbc;

  public void setCountor(long id, String username, int count) { jdbc.update("insert into tweetcount values (?,?,?)", id, username, count); }

  public int sayCount(String target) { return jdbc.queryForObject("select count from tweetcount where username = ?", Integer.class, target); }

  public void count(String target){
    jdbc.update("update tweetcount set count = count+1 where username = ?", target);
  }

  public void reset(String target) {
    jdbc.update("update tweetcount set count = 0 where username = ?", target);
  }

}
