package com.webank.wecube.platform.auth.server.authentication;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.webank.wecube.platform.auth.server.model.SysUser;
import com.webank.wecube.platform.auth.server.model.UmAuthContext;

@Component("umAuthenticationChecker")
public class UmAuthenticationChecker implements AuthenticationChecker {
    private static final Logger log = LoggerFactory.getLogger(UmAuthenticationChecker.class);

    private ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            false);

    @Autowired
    private RestTemplate restTemplate;

    public void checkAuthentication(UserDetails user, Authentication token) {

        UsernamePasswordAuthenticationToken userToken = (UsernamePasswordAuthenticationToken) token;
        verifyAuthToken(userToken);

        SysUser sysUser = (SysUser) user;

        UmAuthContext authCtx = parseLdapUmAuthContext(sysUser.getAuthContext());

        UmSubSystemAuthResultDto subSystemAuthResult = null;
        try {
            subSystemAuthResult = performSubSystemAuth(authCtx);
        } catch (Exception e) {
            log.info("errors while perform sub system authentication", e);
            throw new BadCredentialsException("System errors caused by " + e.getMessage());
        }

        if (subSystemAuthResult == null || subSystemAuthResult.getRetCode() == null || subSystemAuthResult.getRetCode() != 0) {
            throw new BadCredentialsException("Bad credential:bad authentication context.");
        }

        UmUserAuthResultDto userAuthResult = null;
        try {
            userAuthResult = performUserAuthentication(authCtx, subSystemAuthResult, userToken);
        } catch (Exception e) {
            log.info("User authentication failed", e);
            throw new BadCredentialsException("System errors caused by " + e.getMessage());
        }

        if (userAuthResult == null) {
            throw new BadCredentialsException("Bad credential:bad authentication context.");
        }

        if (userAuthResult.getRetCode() == null || userAuthResult.getRetCode() != 0) {
            throw new BadCredentialsException("Bad credential:bad authentication," + userAuthResult.getDesc());
        }

        return;

    }

    private UmUserAuthResultDto performUserAuthentication(UmAuthContext authCtx,
            UmSubSystemAuthResultDto subSystemAuthResult, UsernamePasswordAuthenticationToken userToken)
            throws JsonParseException, JsonMappingException, IOException {
        String host = authCtx.getHost();
        int port = authCtx.getPort();
        String userId = userToken.getName();
        String pwd = (String) userToken.getCredentials();
        String appid = subSystemAuthResult.getId();
        String tmp = generatePwd(userId, pwd);
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String sign = md5(userId + tmp + timeStamp);
        String token = subSystemAuthResult.getTok();
        String auth = subSystemAuthResult.getAuth();

        String url = String.format(
                "http://%s:%s/um_service?style=6&appid=%s&id=%s&sign=%s&timeStamp=%s&token=%s&auth=%s", host, port,
                appid, userId, sign, timeStamp, token, auth);
        
        String reqSeqNo = String.valueOf(System.currentTimeMillis());
        if (log.isInfoEnabled()) {
            log.info("SEND {}:url={}", reqSeqNo, url);
        }

        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<String> resp = sendGetRequestWithUrlParamMap(restTemplate, url, headers, String.class);

        if (log.isInfoEnabled()) {
            log.info("RECV {}:resp={}", reqSeqNo, resp.getBody());
        }
        UmUserAuthResultDto authResult = objectMapper.readValue(resp.getBody(), UmUserAuthResultDto.class);

        return authResult;
    }

    private UmSubSystemAuthResultDto performSubSystemAuth(UmAuthContext authCtx)
            throws JsonParseException, JsonMappingException, IOException {
        String appid = authCtx.getAppid();
        Long current = System.currentTimeMillis();
        String nonce = Long.toString(current % 90000 + 10000);
        String timeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        String tmp = md5(authCtx.getAppid() + nonce + timeStamp);
        String appToken = authCtx.getAppname();
        String sign = md5(tmp + appToken);

        String url = String.format("http://%s:%s/um_service?style=2&appid=%s&nonce=%s&sign=%s&timeStamp=%s",
                authCtx.getHost(), authCtx.getPort(), appid, nonce, sign, timeStamp);

        String reqSeqNo = String.valueOf(System.currentTimeMillis());
        if (log.isInfoEnabled()) {
            log.info("SEND {}:url={}", reqSeqNo, url);
        }
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<String> resp = sendGetRequestWithUrlParamMap(restTemplate, url, headers, String.class);
        
        if (log.isInfoEnabled()) {
            log.info("RECV {}:resp={}", reqSeqNo, resp.getBody());
        }

        UmSubSystemAuthResultDto result = objectMapper.readValue(resp.getBody(), UmSubSystemAuthResultDto.class);

        return result;
    }

    private void verifyAuthToken(UsernamePasswordAuthenticationToken authToken) {
        String username = authToken.getName();
        String password = (String) authToken.getCredentials();

        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new BadCredentialsException("Bad credential:blank username or password.");
        }
    }

    public <T> ResponseEntity<T> sendGetRequestWithUrlParamMap(RestTemplate restTemplate, String requestUri,
            HttpHeaders headers, Class<T> clazz) {
        HttpMethod method = HttpMethod.GET;
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<Object> requestEntity = new HttpEntity<>(headers);

        return restTemplate.exchange(requestUri, method, requestEntity, clazz);
    }

    private UmAuthContext parseLdapUmAuthContext(String authContext) {
        String[] keyValuePairs = authContext.split(";");
        Map<String, String> kvMap = new HashMap<>();
        for (String keyValuePair : keyValuePairs) {
            String[] keyValue = keyValuePair.split("=");
            if (keyValue.length == 2) {
                kvMap.put(keyValue[0], keyValue[1]);
            }
        }

        String protocol = StringUtils.isBlank(kvMap.get("protocol")) ? "http" : kvMap.get("protocol");

        UmAuthContext ctx = new UmAuthContext();
        ctx.setHost(kvMap.get("host"));
        ctx.setPort(Integer.parseInt(kvMap.get("port")));
        ctx.setProtocol(protocol);
        ctx.setAppid(kvMap.get("appid"));
        ctx.setAppname(kvMap.get("appname"));

        if (StringUtils.isBlank(ctx.getProtocol()) || StringUtils.isBlank(ctx.getAppid())
                || StringUtils.isBlank(ctx.getAppname()) || StringUtils.isBlank(ctx.getHost())) {
            throw new BadCredentialsException("Bad credential:bad authentication context.");
        }

        return ctx;
    }

    private String generatePwd(String loginId, String loginPwd) {
        StringBuffer result = new StringBuffer();

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(loginPwd.getBytes("UTF-8"));
            String salt = "{" + loginId + "}";
            md.update(salt.getBytes("UTF-8"));
            byte[] var8;
            int var7 = (var8 = md.digest()).length;

            for (int var6 = 0; var6 < var7; ++var6) {
                byte b = var8[var6];
                result.append(String.format("%02x", b));
            }

            return result.toString();
        } catch (NoSuchAlgorithmException e1) {
            throw new RuntimeException(e1);
        } catch (UnsupportedEncodingException e2) {
            throw new RuntimeException(e2);
        }

    }

    private String byte2hex(byte[] b) {
        String hs = "";
        String stmp = "";

        for (int n = 0; n < b.length; ++n) {
            stmp = Integer.toHexString(b[n] & 255);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs;
    }

    private String md5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes());
            return byte2hex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static class UmUserAuthResultDto {
        private Integer retCode;
        private String desc;
        private String id;
        private String userName;
        private String org;
        private String dept;
        private int actype;
        private String email;

        public Integer getRetCode() {
            return retCode;
        }

        public void setRetCode(Integer retCode) {
            this.retCode = retCode;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getOrg() {
            return org;
        }

        public void setOrg(String org) {
            this.org = org;
        }

        public String getDept() {
            return dept;
        }

        public void setDept(String dept) {
            this.dept = dept;
        }

        public int getActype() {
            return actype;
        }

        public void setActype(int actype) {
            this.actype = actype;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("UmUserAuthResult [retCode=");
            builder.append(retCode);
            builder.append(", desc=");
            builder.append(desc);
            builder.append(", id=");
            builder.append(id);
            builder.append(", userName=");
            builder.append(userName);
            builder.append(", org=");
            builder.append(org);
            builder.append(", dept=");
            builder.append(dept);
            builder.append(", actype=");
            builder.append(actype);
            builder.append(", email=");
            builder.append(email);
            builder.append("]");
            return builder.toString();
        }

    }

    public static class UmSubSystemAuthResultDto {
        private Integer retCode;
        private String desc;
        private String id;
        private String tok;
        private String auth;
        private long expTime;

        public Integer getRetCode() {
            return retCode;
        }

        public void setRetCode(Integer retCode) {
            this.retCode = retCode;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTok() {
            return tok;
        }

        public void setTok(String tok) {
            this.tok = tok;
        }

        public String getAuth() {
            return auth;
        }

        public void setAuth(String auth) {
            this.auth = auth;
        }

        public long getExpTime() {
            return expTime;
        }

        public void setExpTime(long expTime) {
            this.expTime = expTime;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return "SubSystemAuthResult [retCode=" + retCode + ", desc=" + desc + ", id=" + id + ", tok=" + tok
                    + ", auth=" + auth + ", expTime=" + expTime + "]";
        }

    }
}
