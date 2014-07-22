/**
 * librarica 的主要包
 * 以下的结构以功能模块划分, 除 sys, util, x 以外, 每个包均具有类似的仿 JavaEE 结构:
 * * dao, 包括 bean 和持久化描述符, 作模型
 * * core, 封装主要业务逻辑, 作控制器
 * * servlet, 提供 http 接口, 接受及解析参数, 组装输出, 作视图
 * * filter, 提供用于 servlet 的拦截器, 导出为其他模块利用(比如验证是否已登录等)
 * * exception, 描述可能会抛出的异常类型.
 *
 */
package cn.edu.scau.librarica;

