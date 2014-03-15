package cn.edu.scau.librarica.util.test;

import java.util.Scanner;

import org.apache.http.client.fluent.*;

import com.github.cuter44.util.crypto.*;
import com.github.cuter44.util.dao.*;

import cn.edu.scau.librarica.util.conf.*;
import cn.edu.scau.librarica.authorize.core.*;
import cn.edu.scau.librarica.authorize.dao.*;

/** activate user
 * @warn server-only
 */
public class ActivateUserCLI
{
    /**
     * @param uid
     * @param pass new password in plaintext
     * activation-code is retrieved from db
     * @return response body
     */
    public static String activate(Long uid, String pass)
    {
        String resp = "";
        try
        {
            HiberDao.begin();

            User u = UserMgr.get(uid);

            assert(u!=null);

            HiberDao.close();

            String encrypted = CryptoUtil.byteToHex(
                EncryptPasswordCLI.encrypt(uid, pass)
            );

            assert(encrypted!=null);

            String baseurl = Configurator.get("librarica.server.api.baseurl");

            assert(baseurl!=null);
            resp = Request.Post(baseurl+"/user/activate")
                .bodyForm(
                    Form.form()
                    .add("uid", uid.toString())
                    .add("code", CryptoUtil.byteToHex(u.getPass()))
                    .add("pass", encrypted)
                    .build()
                )
                .execute()
                .returnContent()
                .asString();

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            HiberDao.close();
        }

        return(resp);
    }

    public static void main(String[] args)
    {
        Scanner scn = new Scanner(System.in);

        System.out.println("uid?");
        Long uid = scn.nextLong();
        scn.nextLine();

        System.out.println("pass?");
        String pass = scn.nextLine();

        System.out.println(
            activate(uid, pass)
        );
    }
}
