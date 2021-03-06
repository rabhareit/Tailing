package com.rabhareit.tailing.repository;

import com.rabhareit.tailing.entity.UserConnection;
import com.rabhareit.tailing.mapper.UserConnectionRowMapper;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.stream.Collectors;

@Repository
public class UserConnectionRepository {

  UserConnectionRowMapper mapper = new UserConnectionRowMapper();

  @Autowired
  JdbcTemplate jdbc;

  public void insertConnection() { }

  public void updateBannerUrl(String bannerurl) { jdbc.update("update userconnection set bannerurl = ?",bannerurl); }

  public UserConnection getConnectionById(String userid) {
    return jdbc.queryForObject("select * from userconnection where userid = ?", mapper, userid);
  }

  public long[] getUserIdArray() {
    return ArrayUtils.toPrimitive(
        jdbc.query("select * from userconnection",mapper)
          .stream()
          .map(user -> user.getProvideruserid())
          .collect(Collectors.toList())
          .toArray(new Long[0])
    );
  }

  public void deleteConnection(String userid) {
    jdbc.update("delete from userconnection where userid = ?", userid);
  }
}
