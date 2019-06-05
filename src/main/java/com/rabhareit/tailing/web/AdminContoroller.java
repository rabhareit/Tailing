package com.rabhareit.tailing.web;

import com.rabhareit.tailing.properties.TailingTwitterContext;
import com.rabhareit.tailing.repository.TasksModelRepository;
import com.rabhareit.tailing.service.TaskExtractor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

@Controller
@RequestMapping("/admin")
public class AdminContoroller {

    @Autowired
    public TasksModelRepository taskRepo;

    @Autowired
    TailingTwitterContext context;

    @RequestMapping("/config")
    public String administratorConfiguration() throws TwitterException {
        Configuration conf = new ConfigurationBuilder()
                .setOAuthConsumerKey(context.getOauthconsumerkey())
                .setOAuthConsumerSecret(context.getOauthconsumersecret())
                .setOAuthAccessToken(context.getOauthaccesstoken())
                .setOAuthAccessTokenSecret(context.getOauthaccesstokensecret())
                .build();

        Twitter twitter = new TwitterFactory(conf).getInstance();
        TwitterStream twStream = new TwitterStreamFactory(conf).getInstance();
        TaskExtractor taskext = new TaskExtractor();

        StatusListener listener = new StatusListener() {

            @Override
            public void onStatus(Status status) {
                if (status.getText().startsWith("RT")) {
                    // MEMO リツイートは表示しない
                    return;
                } else {
                    // 確認用
                    System.out.println("id = " + status.getId() + ", screenName = " + status.getUser().getScreenName() + ", text = " + status.getText());
                    System.out.println(" -> tweet to @"+ status.getUser().getScreenName());
                    try {
                        twitter.updateStatus("@" + status.getUser().getScreenName() +" "+ taskext.tweetDraft(taskRepo));
                    } catch(NullPointerException npe) {
                        npe.printStackTrace();
                    } catch(TwitterException te) {
                        te.printStackTrace();
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
                // TODO リアルタイム性が重視なのでツイートしなくてもいいかもしれないが、エラーが継続してたら知らせる仕組みを作る
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

        // MEMO ストリームの開始
        twStream.addListener(listener);

        FilterQuery filter = new FilterQuery();
        filter.follow(new long[]{826175150391320576l});  //MyID(@rispadhar)
        twStream.filter(filter);

        return "adConfig";
    }

}