package cn.edu.scau.librarica.util.test;

import java.util.Scanner;

import org.apache.http.client.fluent.*;
import com.alibaba.fastjson.*;

import cn.edu.scau.librarica.conn.douban.core.DoubanTokenRetriever;

/** ≤‚ ‘ cn.edu.scau.librarica.conn.douban.core.DoubanTokenRetriever.viaCode()
 */
public class RetrieveDoubanTokenViaCode
{

    public static void main(String[] args)
    {
        try
        {
            Scanner scn = new Scanner(System.in);

            System.out.println("bind to uid?");
            Long uid = Long.valueOf(scn.nextLine());

            System.out.println("code?");
            String code = scn.nextLine();

            DoubanTokenRetriever.viaCode(uid, code);
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
