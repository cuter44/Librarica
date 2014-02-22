package cn.edu.scau.librarica.douban.core;

public class DoubanAPIErrorCode
{
    public static String toString(int statusCode)
    {
        switch (statusCode)
        {
            case 100:
                return("invalid_request_scheme ���������Э��");
            case 101:
                return("invalid_request_method ��������󷽷�");
            case 102:
                return("access_token_is_missing δ�ҵ�access_token");
            case 103:
                return("invalid_access_token access_token�����ڻ��ѱ��û�ɾ���������û��޸�������");
            case 104:
                return("invalid_apikey apikey�����ڻ���ɾ��");
            case 105:
                return("apikey_is_blocked apikey�ѱ�����");
            case 106:
                return("access_token_has_expired access_token�ѹ���");
            case 107:
                return("invalid_request_uri �����ַδע��");
            case 108:
                return("invalid_credencial1 �û�δ��Ȩ���ʴ�����");
            case 109:
                return("invalid_credencial2 apikeyδ�����Ȩ��");
            case 110:
                return("not_trial_user δע��Ĳ����û�");
            case 111:
                return("rate_limit_exceeded1 �û������ٶ�����");
            case 112:
                return("rate_limit_exceeded2 IP�����ٶ�����");
            case 113:
                return("required_parameter_is_missing ȱ�ٲ���");
            case 114:
                return("unsupported_grant_type �����grant_type");
            case 115:
                return("unsupported_response_type �����response_type");
            case 116:
                return("client_secret_mismatch client_secret��ƥ��");
            case 117:
                return("redirect_uri_mismatch redirect_uri��ƥ��");
            case 118:
                return("invalid_authorization_code authorization_code�����ڻ��ѹ���");
            case 119:
                return("invalid_refresh_token refresh_token�����ڻ��ѹ���");
            case 120:
                return("username_password_mismatch �û������벻ƥ��");
            case 121:
                return("invalid_user �û������ڻ���ɾ��");
            case 122:
                return("user_has_blocked �û��ѱ�����");
            case 123:
                return("access_token_has_expired_since_password_changed ���û��޸����������access_token����");
            case 124:
                return("access_token_has_not_expired access_tokenδ����");
            case 125:
                return("invalid_request_scope ���ʵ�scope���Ϸ��������߲���̫��ע��һ�㲻����ָô���");
            case 999:
                return("unknown δ֪����");
            case 1000:
                return("need_permission ��ҪȨ��");
            case 1001:
                return("uri_not_found ��Դ������");
            case 1002:
                return("missing_args ������ȫ");
            case 1003:
                return("image_too_large �ϴ���ͼƬ̫��");
            case 1004:
                return("has_ban_word ������Υ����");
            case 1005:
                return("input_too_short ����Ϊ�գ�����������������");
            case 1006:
                return("target_not_fount ��صĶ��󲻴��ڣ�����ظ�����ʱ������С�鱻ɾ����");
            case 1007:
                return("need_captcha ��Ҫ��֤�룬��֤������");
            case 1008:
                return("image_unknow ��֧�ֵ�ͼƬ��ʽ");
            case 1009:
                return("image_wrong_format ��Ƭ��ʽ����(��֧��JPG,JPEG,GIF,PNG��BMP)");
            case 1010:
                return("image_wrong_ck ����˽��ͼƬck��֤����");
            case 1011:
                return("image_ck_expired ����˽��ͼƬck����");
            case 1012:
                return("title_missing ��ĿΪ��");
            case 1013:
                return("desc_missing ����Ϊ��");
        }
        return("unknown");
    }
}
