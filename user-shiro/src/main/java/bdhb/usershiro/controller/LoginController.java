package bdhb.usershiro.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.bdhanbang.base.common.ApiResult;
import com.bdhanbang.base.common.Query;
import com.bdhanbang.base.exception.AuthenticationException;
import com.bdhanbang.base.exception.BusinessException;
import com.bdhanbang.base.message.CommonMessage;
import com.bdhanbang.base.util.AES;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.generator.tables.SysUser;
import com.generator.tables.WxConfig;
import com.generator.tables.WxUserInfo;
import com.generator.tables.pojos.SysUserEntity;
import com.generator.tables.pojos.WxConfigEntity;
import com.generator.tables.pojos.WxUserInfoEntity;

import bdhb.usershiro.common.AppCommon;
import bdhb.usershiro.common.CurrentUser;
import bdhb.usershiro.service.SysPermissionService;
import bdhb.usershiro.service.SysUserService;
import bdhb.usershiro.service.TableService;
import bdhb.usershiro.util.JwtUtils;
import bdhb.usershiro.vo.Menu;
import bdhb.usershiro.vo.SysUserVo;
import bdhb.usershiro.vo.WeXinDataVo;
import bdhb.usershiro.vo.WeXinLogin;
import bdhb.usershiro.vo.WeXinResoult;
import springfox.documentation.annotations.ApiIgnore;

@RestController
@RequestMapping("/login")
public class LoginController {

	@Autowired
	SysUserService sysUserService;

	@Autowired
	SysPermissionService sysPermissionService;

	@Autowired
	TableService tableService;

	final Base64.Encoder encoder = Base64.getEncoder();

	@RequestMapping(value = "/wx", method = RequestMethod.GET)
	public WeXinResoult login(@RequestHeader(AppCommon.TENANT_ID) String tenantId,
			@RequestHeader(AppCommon.X_WX_CODE) String wxCode,
			@RequestHeader(AppCommon.X_WX_ENCRYPTED_DATA) String wxEncryptedData,
			@RequestHeader(AppCommon.X_WX_IV) String wxIv, HttpServletResponse response)
			throws JsonProcessingException {

		String schema = String.format("%s%s", tenantId, AppCommon.scheam);
		WeXinResoult rt = new WeXinResoult();
		WeXinDataVo data = new WeXinDataVo();

		try {

			List<WxConfigEntity> queryList = tableService.queryList(schema, WxConfig.class, WxConfigEntity.class,
					new Query());

			if (Objects.isNull(queryList) || queryList.size() == 0) {
				throw new BusinessException("20000", "未设置微信的appId");
			}

			WxConfigEntity wxConfigEntity = queryList.get(0);

			WeXinLogin weXinLogin = this.getWeXinLogin(wxConfigEntity.getAppId(), wxConfigEntity.getAppSecret(),
					wxCode);

			String sessionKey = weXinLogin.getSession_key();
			String openId = weXinLogin.getOpenid();
			// 角析用户信息
			WxUserInfoEntity userinfo = getWeXinUserinfo(wxEncryptedData, sessionKey, wxIv);

			Map<String, String> claims = new HashMap<>();

			claims.put(AppCommon.PAYLOAD_NAME, openId);

			String token = JwtUtils.createToken(claims);

			// 保存用户信息
			SysUserEntity sysUserEntity = saveUserinfo(tenantId, userinfo);

			ObjectMapper mapper = new ObjectMapper();

			if (Objects.isNull(userinfo.getJsonb()) || userinfo.getJsonb().isNull()) {
				userinfo.setJsonb(mapper.readTree("{}"));
			}

			if (Objects.isNull(sysUserEntity.getRoles()) || sysUserEntity.getRoles().length == 0) {
				((ObjectNode) userinfo.getJsonb()).put("roles", "");
			} else {
				((ObjectNode) userinfo.getJsonb()).put("roles", sysUserEntity.getRoles()[0]);
			}

			data.setToken(token);
			data.setUserinfo(userinfo);
			data.setSkey(openId);

			response.setHeader(AppCommon.TOKEN, token);

		} catch (Exception e) {
			// 设置错误信息
			data.setError("10001");
			data.setMessage(e.getMessage());
			// 后台出现问题
			rt.setCode(-1);
		}

		rt.setData(data);

		return rt;

	}

	private SysUserEntity saveUserinfo(String tenantId, WxUserInfoEntity userinfo) {

		String realSchema = tenantId + AppCommon.scheam;

		Query query = new Query();

		query.add("openId", userinfo.getOpenId());

		List<WxUserInfoEntity> userList = tableService.queryList(realSchema, WxUserInfo.class, WxUserInfoEntity.class,
				query);

		List<SysUserEntity> sysUsers = sysUserService.queryList(realSchema, SysUser.class, SysUserEntity.class,
				query.getQuerys());

		SysUserEntity sysUser = new SysUserEntity();

		// 把微信用户和系统用户关联
		if (Objects.isNull(sysUsers) || sysUsers.size() == 0) {

			sysUser.setUserId(UUID.randomUUID());
			sysUser.setTenantId(tenantId);
			sysUser.setUserName(userinfo.getOpenId());
			sysUser.setFullName(userinfo.getNickName());
			sysUser.setPassword(AppCommon.DEFAULT_PASSWORD);
			sysUser.setSalt(String.valueOf(((Double) (Math.random() * 100)).intValue()));
			String inPassword = DigestUtils.md5Hex(sysUser.getPassword() + sysUser.getSalt());
			sysUser.setPassword(inPassword);
			sysUser.setOpenId(userinfo.getOpenId());

			tableService.insertEntity(realSchema, SysUser.class, sysUser);

		} else {
			sysUser = sysUsers.get(0);
		}

		// 保存用户信息
		if (Objects.isNull(userList) || userList.size() == 0) {

			userinfo.setId(UUID.randomUUID());
			tableService.insertEntity(realSchema, WxUserInfo.class, userinfo);

		} else {

			userinfo.setId(userList.get(0).getId());

			// 这么做是为了不更新昵称
			userinfo.setNickName(userList.get(0).getNickName());
			tableService.updateEntity(realSchema, WxUserInfo.class, userinfo);

		}

		return sysUser;

	}

	private WxUserInfoEntity getWeXinUserinfo(String wxEncryptedData, String sessionKey, String wxIv) throws Exception {

		Decoder decoder = Base64.getDecoder();

		byte[] result = AES.decrypt(decoder.decode(wxEncryptedData), decoder.decode(sessionKey),
				AES.generateIV(decoder.decode(wxIv)));
		String s = new String(result, "UTF-8");

		ObjectMapper mapper = new ObjectMapper();

		WxUserInfoEntity userinfo = mapper.readValue(s, WxUserInfoEntity.class);
		return userinfo;
	}

	private WeXinLogin getWeXinLogin(String appId, String appSecret, String wxCode)
			throws ClientProtocolException, IOException {

		StringBuffer urlOverHttps = new StringBuffer();

		urlOverHttps.append("https://api.weixin.qq.com/sns/jscode2session?appid=");
		urlOverHttps.append(appId);
		urlOverHttps.append("&secret=");
		urlOverHttps.append(appSecret);
		urlOverHttps.append("&js_code=");
		urlOverHttps.append(wxCode);
		urlOverHttps.append("&grant_type=authorization_code");

		CloseableHttpClient httpClient = HttpClients.createDefault();
		HttpGet getMethod = new HttpGet(urlOverHttps.toString());

		HttpResponse response = httpClient.execute(getMethod);

		HttpEntity h = response.getEntity();
		InputStream inputStream = h.getContent();

		ObjectMapper mapper = new ObjectMapper();
		// 没有的值不要
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		WeXinLogin w = mapper.readValue(inputStream, WeXinLogin.class);
		inputStream.close();
		httpClient.close();

		return w;
	}

	@RequestMapping(value = "/token", method = RequestMethod.PUT)
	public ApiResult<String> token(@ApiIgnore @CurrentUser SysUserEntity currentUser, HttpServletResponse response) {

		ApiResult<String> apiResult = new ApiResult<>();

		Map<String, String> claims = new HashMap<>();

		claims.put(AppCommon.PAYLOAD_NAME, currentUser.getUserName());

		// 生成token
		String token = JwtUtils.createToken(claims);
		response.setHeader(AppCommon.TOKEN, token);

		apiResult.setData("置换成功");

		apiResult.setStatus(CommonMessage.SUCCESS.getStatus());
		apiResult.setMessage(CommonMessage.SUCCESS.getMessage());

		return apiResult;

	}

	@RequestMapping(method = RequestMethod.POST)
	public ApiResult<Pair<SysUserEntity, List<Menu>>> login(@Valid @RequestBody SysUserVo sysUserEntity,
			HttpServletResponse response) {

		String schema = String.format("%s%s", sysUserEntity.getTenantId(), AppCommon.scheam);

		ApiResult<Pair<SysUserEntity, List<Menu>>> apiResult = new ApiResult<>();

		Query query = new Query();

		query.add(new Query("userName", sysUserEntity.getUserName()));

		List<SysUserEntity> sysUserEntitys = sysUserService.queryList(schema, SysUser.class, SysUserEntity.class,
				query.getQuerys());

		if (Objects.isNull(sysUserEntitys) || sysUserEntitys.size() == 0) {
			throw new AuthenticationException("用户名或密码错误。");
		}

		SysUserEntity sysUser = sysUserEntitys.get(0);

		String inPassword = DigestUtils.md5Hex(sysUserEntity.getPassword() + sysUser.getSalt());

		if (inPassword.equals(sysUser.getPassword())) {
			Map<String, String> claims = new HashMap<>();

			claims.put(AppCommon.PAYLOAD_NAME, sysUserEntity.getUserName());

			// 生成token
			String token = JwtUtils.createToken(claims);
			response.setHeader(AppCommon.TOKEN, token);

			// 去掉敏感信息
			sysUser.setPassword("");
			sysUser.setSalt("");

			Pair<SysUserEntity, List<Menu>> pair = new Pair<>(sysUser,
					sysPermissionService.getMenus(sysUserEntity.getTenantId(), sysUser.getRoles()));

			// 生成左侧导航
			apiResult.setData(pair);

		} else {
			throw new AuthenticationException("用户名或密码错误。");
		}

		apiResult.setStatus(CommonMessage.SUCCESS.getStatus());
		apiResult.setMessage(CommonMessage.SUCCESS.getMessage());

		return apiResult;

	}

}
