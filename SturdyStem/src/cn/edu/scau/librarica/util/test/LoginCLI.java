package cn.edu.scau.librarica.util.test;

import java.util.Scanner;

import org.apache.http.client.fluent.*;
import com.alibaba.fastjson.*;
import com.github.cuter44.util.crypto.*;
import cn.edu.scau.librarica.util.conf.*;


/** login and retrieve session key
 */
public class LoginCLI
{
    /**
     * @param uid
     * @param pass in plaintext
     * @return session key
     */
    public static byte[] login(Long uid, String pass)
    {
        String resp = null;
        try
        {
            String baseurl = Configurator.get("librarica.server.web.baseurl");
            String encrypted = CryptoUtil.byteToHex(
                    EncryptPasswordCLI.encrypt(uid, pass)
                );

            resp = Request.Post(baseurl+"/user/login")
                .bodyForm(
                    Form.form()
                    .add("uid", uid.toString())
                    .add("pass", encrypted)
                    .build()
                )
                .execute()
                .returnContent()
                .asString();

            JSONObject json = (JSONObject)JSONObject.parse(resp);

            byte[] s = CryptoUtil.hexToBytes(json.getString("s"));

            return(s);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            System.out.println(resp);
            return(null);
        }
    }

    public static void main(String[] args)
    {
        try
        {
            Scanner scn = new Scanner(System.in);

            System.out.println("uid?");
            Long uid = scn.nextLong();
            scn.nextLine();

            System.out.println("pass?");
            String pass = scn.nextLine();

            System.out.println(
                CryptoUtil.byteToHex(
                    login(uid, pass)
                )
            );
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
