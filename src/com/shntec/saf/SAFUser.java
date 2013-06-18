package com.shntec.saf;

import java.util.Map;

/**
 * 用于实现用户系统的接口
 * 每一个用户在使用账号密码进行一次成功登陆之后会得到从服务器返回的授权令牌
 * 以便在下一次登陆app时可以不需要再次输入账号密码，授权令牌是有时效的，授权令牌过期
 * 那么需要重新使用账号密码进行登陆
 * 使用授权令牌登陆后不会获得新的授权令牌，
 * 实现类应该将授权令牌以及账号信息进行持久化存储
 * 
 * 无论使用第三方登录还是正常登录，都会返回授权令牌，最后退出时都是使用授权令牌进行退出
 * @author panshihao
 *
 */
public interface SAFUser {

	
	
	/**
	 * 登陆,根据账号密码进行登陆
	 * @param userName
	 * @param passWord
	 * @return
	 */
	public boolean login(String userName, String passWord);
	/**
	 * 根据用户授权令牌进行登陆
	 * @return
	 */
	public boolean LocalLogin();
	/**
	 * 使用QQ进行登录，传入openID
	 * @param openid
	 * @return
	 */
	public boolean loginFromQQ(String access_token,String openid);
	/**
	 * 使用新浪微博进行登录，传入uid
	 * @param uid
	 * @return
	 */
	public boolean loginFromSinaWeibo(String access_token, String uid);
	/**
	 * 登出,登出时使用用户授权令牌进行登出
	 * @return
	 */
	public boolean logout();
	/**
	 * 获取用户令牌
	 * @return
	 */
	public String getAUT();
	
	
}
