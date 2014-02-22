package cn.edu.scau.librarica.douban.core;

public class DoubanAPIErrorCode
{
    public static String toString(int statusCode)
    {
        switch (statusCode)
        {
            case 100:
                return("invalid_request_scheme 错误的请求协议");
            case 101:
                return("invalid_request_method 错误的请求方法");
            case 102:
                return("access_token_is_missing 未找到access_token");
            case 103:
                return("invalid_access_token access_token不存在或已被用户删除，或者用户修改了密码");
            case 104:
                return("invalid_apikey apikey不存在或已删除");
            case 105:
                return("apikey_is_blocked apikey已被禁用");
            case 106:
                return("access_token_has_expired access_token已过期");
            case 107:
                return("invalid_request_uri 请求地址未注册");
            case 108:
                return("invalid_credencial1 用户未授权访问此数据");
            case 109:
                return("invalid_credencial2 apikey未申请此权限");
            case 110:
                return("not_trial_user 未注册的测试用户");
            case 111:
                return("rate_limit_exceeded1 用户访问速度限制");
            case 112:
                return("rate_limit_exceeded2 IP访问速度限制");
            case 113:
                return("required_parameter_is_missing 缺少参数");
            case 114:
                return("unsupported_grant_type 错误的grant_type");
            case 115:
                return("unsupported_response_type 错误的response_type");
            case 116:
                return("client_secret_mismatch client_secret不匹配");
            case 117:
                return("redirect_uri_mismatch redirect_uri不匹配");
            case 118:
                return("invalid_authorization_code authorization_code不存在或已过期");
            case 119:
                return("invalid_refresh_token refresh_token不存在或已过期");
            case 120:
                return("username_password_mismatch 用户名密码不匹配");
            case 121:
                return("invalid_user 用户不存在或已删除");
            case 122:
                return("user_has_blocked 用户已被屏蔽");
            case 123:
                return("access_token_has_expired_since_password_changed 因用户修改密码而导致access_token过期");
            case 124:
                return("access_token_has_not_expired access_token未过期");
            case 125:
                return("invalid_request_scope 访问的scope不合法，开发者不用太关注，一般不会出现该错误");
            case 999:
                return("unknown 未知错误");
            case 1000:
                return("need_permission 需要权限");
            case 1001:
                return("uri_not_found 资源不存在");
            case 1002:
                return("missing_args 参数不全");
            case 1003:
                return("image_too_large 上传的图片太大");
            case 1004:
                return("has_ban_word 输入有违禁词");
            case 1005:
                return("input_too_short 输入为空，或者输入字数不够");
            case 1006:
                return("target_not_fount 相关的对象不存在，比如回复帖子时，发现小组被删掉了");
            case 1007:
                return("need_captcha 需要验证码，验证码有误");
            case 1008:
                return("image_unknow 不支持的图片格式");
            case 1009:
                return("image_wrong_format 照片格式有误(仅支持JPG,JPEG,GIF,PNG或BMP)");
            case 1010:
                return("image_wrong_ck 访问私有图片ck验证错误");
            case 1011:
                return("image_ck_expired 访问私有图片ck过期");
            case 1012:
                return("title_missing 题目为空");
            case 1013:
                return("desc_missing 描述为空");
        }
        return("unknown");
    }
}
