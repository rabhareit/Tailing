package com.rabhareit.tailing.service;

import com.rabhareit.tailing.repository.TweetCountRepository;
import com.rabhareit.tailing.repository.UserConnectionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import twitter4j.*;

import java.util.List;
import java.util.Map;

public class TailingStausListenerImpl {

  @Autowired
  private JdbcTemplate jdbc;

  @Autowired
  UserConnectionRepository userConnection;

  @Autowired
  TweetCountRepository counter;

  public StatusListener getStattusLisener(Twitter twitter) {

    StatusListener listener = new StatusListener() {

      @Override
      public void onStatus(Status status) {
        String target = status.getUser().getScreenName();

        System.out.println("id = " + status.getId() + ", screenName = " + target + ", text = " + status.getText());

        if (status.getText().startsWith("RT")) {
          // MEMO リツイートは表示しない
          return;
        } else if(status.getText().contains("アーカイブ") && status.getText().contains("tailing")) {
          try {
            StringBuffer buffer = new StringBuffer();
            List<Map<String, Object>> allArc = jdbc.queryForList("select * from completed_task_model where ownerid = ?", target);
            allArc.stream().forEach( (task) -> buffer.append(task.get("title") + System.lineSeparator()) );
            twitter.updateStatus("@" + target + System.lineSeparator() + "アーカイブでスか？" + System.lineSeparator() + "ご確認クださイ" + System.lineSeparator() + buffer.toString());
          } catch(NullPointerException nul) {
            try{
              twitter.updateStatus("完了済ミのタスクはござイませんが…");
            } catch (TwitterException te) {
              te.printStackTrace();
            }
          } catch (TwitterException te) {
            te.printStackTrace();
          }
        }
        else {
          TimelineMonitor monitor = new TimelineMonitor();
          int count = counter.sayCount(target);
          if (count == 0) {
            //10分後に初期化
            monitor.start(target);
            counter.count(target);
            return;
          } else if (count == 4) {
            try {
              StringBuffer buffer = new StringBuffer();
              List<Map<String,Object>> allTask = jdbc.queryForList("select * from task_model where ownerid = ?",target);

              //タスクが登録されていない場合
              if(allTask.size() == 0) {
                return;
              }

              allTask.stream().forEach( (task) -> buffer.append(task.get("title") + " : " + task.get("deadline") + "まで" + System.lineSeparator()) );
              twitter.updateStatus("@" + target + System.lineSeparator() + buffer.toString());
            } catch(NullPointerException npe) {
              npe.printStackTrace();
            } catch(TwitterException te) {
              te.printStackTrace();
            }
            counter.reset(target);
            monitor.restart(target);
          } else if (count > 0 && count < 4) {
            counter.count(target);
            System.out.println(target + " : count++");
            return;
          }
        }
      }

      @Override
      public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
        // MEMO ステータスの削除通知 (delete)
        // MEMO 通知に従って保存領域から該当ツイートを削除しなければならない
        // MEMO ツイート主が削除したツイートは自分のデータベースからも削除しなければならない。
        // TODO ユーザーが消し去ったツイートがDBに残ってないことを明示できたら
        System.out.println("!-----[onDeletionNotice]-----");
        System.out.println("@"+statusDeletionNotice.getUserId() + ":" + statusDeletionNotice.getStatusId());
      }

      @Override
      public void onScrubGeo(long lat, long lng) {
        // MEMO 位置情報の削除通知 (scrub_geo)
        // MEMO 通知に従って保存領域から該当ツイートの位置情報を削除しなければならない
        // MEMO ツイート主が削除した位置情報は自分のデータベースからも削除しなければならない。
        // TODO ユーザーが消し去った位置情報がDB残ってないことを明示できたら
        System.out.println("!-----[onScrubGeo]-----");
      }

      @Override
      public void onTrackLimitationNotice(int i) {
        // MEMO 制限通知 (limit)
        // MEMO 速度制限の上限を超えたために取得できなかったツイートが存在する
        // TODO リアルタイム性が重視なのでツイートしなくてもいいかもしれないがエラーが継続してたら知らせる仕組みを作る
        System.out.println("!-----[onTrackLimitationNotice]-----");
      }

      @Override
      public void onStallWarning(StallWarning warning) {
        // NOTE 速度低下警告 (warning)
        // TODO メールするか放置か
        System.out.println("!-----[onStallWarning]-----");
      }

      @Override
      public void onException(Exception excptn) {
        // MEMO その他エラー
        // TODO ストリームを複数開いた,API上限,短時間に同じ内容のツイートなど,分岐できたら
        System.out.println("!-----[onException]-----");
        excptn.printStackTrace();
      }
    };

    return listener;
  }
}
