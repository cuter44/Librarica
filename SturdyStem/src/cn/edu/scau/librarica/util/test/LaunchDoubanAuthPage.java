package cn.edu.scau.librarica.util.test;

import cn.edu.scau.librarica.util.conf.*;

/** 打开豆瓣授权引导页
 * works ONLY on Windows
 */
public class LaunchDoubanAuthPage
{
    public static void main(String[] args)
    {
        String doubanAuthURL = Configurator.get("librarica.conn.douban.authurl");
        String client_id = Configurator.get("librarica.conn.douban.client_id");
        String redirect_uri = Configurator.get("librarica.conn.douban.redirect_uri");

        StringBuilder sb = new StringBuilder();
        sb.append("rundll32 url.dll,FileProtocolHandler ")
            .append(doubanAuthURL).append('?')
            .append("client_id=").append(client_id).append('&')
            .append("redirect_uri=").append(redirect_uri).append('&')
            .append("response_type=code");//.append('&')

        try
        {
            Runtime.getRuntime().exec(sb.toString());
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        return;
    }
}
