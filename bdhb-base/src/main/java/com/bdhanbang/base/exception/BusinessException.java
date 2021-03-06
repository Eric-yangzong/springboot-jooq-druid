package com.bdhanbang.base.exception;

/**
 * @ClassName: BusinessException
 * @Description: 业务处理相关错误<BR/>
 *               默前台返回 400 Bad Request<BR/>
 *               状态码：说明由于明显的客户端错误（例如，格式错误的请求语法，太大的大小，无效的请求消息或欺骗性路由请求），服务器不能或不会处理该请求。
 * @author yangxz
 * @date 2018年7月21日 下午12:11:27
 * 
 */
public class BusinessException extends BaseException {

	private static final long serialVersionUID = 1L;

	public BusinessException(Exception e, String status, String error) {
		super(e, status, error);
	}

	public BusinessException(String status, String error) {
		super(new RuntimeException(), status, error);
	}
}
