/** 身份认证/密钥服务
 *
 * HTTP级别支持以下功能
 * * 注册/激活
 * * 登录/登出
 * * 变更密码
 * * 给定uid获得一个RSA密钥
 *
 * Servlet级别支持以下功能
 * * 检查skey/pass的合法性(过滤器)
 *
 * API级别支持以下功能
 * * User对象的CRUD
 * * 账户状态变更的回调服务(比如成功验证, 被封号等)
 *
 * 计划支持的功能
 * * 封号
 *
 */
package cn.edu.scau.librarica.authorize;