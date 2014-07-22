package cn.edu.scau.librarica.util.test;

import cn.edu.scau.librarica.util.conf.*;

import java.util.Scanner;
import java.net.URLEncoder;

/** 连接豆瓣帐号
 * 调试目标由配置文件注册的 client_id 对应回调地址决定.
 * works ONLY on Windows
 */
public class BindDoubanAccount
{
    public static void main(String[] args)
    {
        String doubanAuthURL = Configurator.get("librarica.douban.authurl");
        String client_id = Configurator.get("librarica.douban.client_id");
        String redirect_uri = Configurator.get("librarica.douban.redirect_uri");

        try
        {
            Scanner scn = new Scanner(System.in);

            System.out.println("uid to bind?");
            String uid = scn.nextLine();
            System.out.println("skey?");
            String skey = scn.nextLine();
            String state = URLEncoder.encode(
                "{\"action\":\"bind\",\"id\":"+uid+",\"s\":\""+skey+"\"}",
                "utf-8"
            );

            StringBuilder sb = new StringBuilder();
            sb.append("rundll32 url.dll,FileProtocolHandler ")
                .append(doubanAuthURL).append('?')
                .append("client_id=").append(client_id).append('&')
                .append("redirect_uri=").append(redirect_uri).append('&')
                .append("response_type=code").append('&')
                .append("state=").append(state);

            Runtime.getRuntime().exec(sb.toString());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return;
    }
}
