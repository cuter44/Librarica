package cn.edu.scau.librarica.douban.core;

import org.apache.http.client.fluent.*;
import com.alibaba.fastjson.*;
import com.github.cuter44.util.dao.*;
import cn.edu.scau.librarica.util.conf.*;

import cn.edu.scau.librarica.douban.dao.*;

/** 以 HttpClient 取得用户授权 token 并存入数据库中
 */
public class DoubanTokenRetriever
{
    private static String client_id = Configurator.get("librarica.douban.client_id");
    private static String client_secret = Configurator.get("librarica.douban.client_secret");
    private static String redirect_uri = Configurator.get("librarica.douban.redirect_uri");
    private static final String doubanTokenURL = Configurator.get("librarica.douban.tokenurl");

    /** 以 code 换取 token 并存入数据库
     * @return 豆瓣返回的json
     */
    public static DoubanProfile viaCode(Long uid, String code)
        throws Exception
    {
        String resp = null;

        resp = Request.Post(doubanTokenURL)
            .bodyForm(
                Form.form()
                .add("client_id", client_id)
                .add("client_secret", client_secret)
                .add("redirect_uri", redirect_uri)
                .add("grant_type","authorization_code")
                .add("code", code)
                .build()
            )
            .execute()
            .returnContent()
            .asString();

        JSONObject json = JSON.parseObject(resp);

        DoubanProfile dp = DoubanProfileMgr.get(uid);
        if (dp == null)
            dp = DoubanProfileMgr.create(uid);

        // 总觉得这样 hardcode 风险非常大...
        dp.setAccess_token(json.getString("access_token"));
        dp.setDouban_user_id(json.getString("douban_user_id"));
        dp.setRefresh_token(json.getString("refresh_token"));

        HiberDao.update(dp);

        System.err.println(JSON.toJSONString(dp));

        return(dp);
    }
}
