package com.ruoyi.lijun.utils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tool<T> {
	/**
	 * 获取访问的域名
	 * @return
	 */
	public static String getDomain(){
		try {
			return (((HttpServletRequest)getRequest_Response_Session()[0]).getServerName()+":"+((HttpServletRequest)getRequest_Response_Session()[0]).getServerPort());
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	/**
	 * 获取带http或https等开头的完整访问的域名
	 * @return
	 */
	public static String getHttpDomain(){
		StringBuffer url=((HttpServletRequest)getRequest_Response_Session()[0]).getRequestURL();
		return url.delete(url.length() - ((HttpServletRequest)getRequest_Response_Session()[0]).getRequestURI().length(), url.length()).append("").toString();
	}
	/**
	 * 获取Ip地址
	 * @return
	 */
	public static String getIpAdrress() {
		HttpServletRequest request= (HttpServletRequest) getRequest_Response_Session()[0];
		String Xip = request.getHeader("X-Real-IP");
		String XFor = request.getHeader("X-Forwarded-For");
		if(StringUtils.isNotEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)){
			//多次反向代理后会有多个ip值，第一个ip才是真实ip
			int index = XFor.indexOf(",");
			if(index != -1){
				return XFor.substring(0,index);
			}else{
				return XFor;
			}
		}
		XFor = Xip;
		if(StringUtils.isNotEmpty(XFor) && !"unKnown".equalsIgnoreCase(XFor)){
			return XFor;
		}
		if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
			XFor = request.getHeader("Proxy-Client-IP");
		}
		if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
			XFor = request.getHeader("WL-Proxy-Client-IP");
		}
		if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
			XFor = request.getHeader("HTTP_CLIENT_IP");
		}
		if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
			XFor = request.getHeader("HTTP_X_FORWARDED_FOR");
		}
		if (StringUtils.isBlank(XFor) || "unknown".equalsIgnoreCase(XFor)) {
			XFor = request.getRemoteAddr();
		}
		return XFor;
	}
	/**
	 * 辅助方法:判断字符串是否为空字符串或空
	 * @param string
	 * @return
	 */
	public static <T> boolean isNull(T string){
		return string==null||"".equals(string.toString().trim())||"null".equals(string.toString().trim());
	}
	/**
	 * 判断map里面是否有这个key,并且这key是否不为null,两者有一个否,就返回false
	 * @param m
	 * @param k
	 * @return
	 */
	public static boolean mapGetKeyNotEmpty(Map<String, Object> m,String k){
		return m.containsKey(k)&&m.get(k)!=null&&(m.get(k) instanceof String?m.get(k).toString().trim()!=""||m.get(k).toString().trim()!="null":true);
	}
	/**
	 * 辅助方法:判断集合是否为空
	 * @param list
	 * @return
	 */
	public static boolean listIsNull(List<?>list){return(list==null||list.isEmpty()||list.size()==0||(list.size()==1&&(list.get(0)==null||list.get(0).toString().trim()=="")));}
	public static <T>T IFNULL(T toObject,T reObject){
		if(isNull(toObject))return reObject;
		else return toObject;
	}
	public List<T> IFNULL(List<T> toObject, List<T> reObject){
		if(listIsNull(toObject))return reObject;
		else return toObject;
	}
	/**
	 * HttpServletRequest从上下文中获取,HttpServletResponse和HttpSession从HttpServletRequest获取
	 * @return Object{HttpServletRequest,HttpServletResponse,HttpSession}
	 */
	public static final Object[] getRequest_Response_Session(){
		RequestAttributes ra = RequestContextHolder.getRequestAttributes();
		ServletRequestAttributes sra = (ServletRequestAttributes) ra;
		List<Object>req_res_session=new ArrayList<>();
		if(sra!=null){
			req_res_session.add(sra.getRequest());
			ServletWebRequest servletWebRequest = new ServletWebRequest((HttpServletRequest)req_res_session.get(0));
			req_res_session.add(servletWebRequest.getResponse());
			req_res_session.add(((HttpServletRequest)req_res_session.get(0)).getSession());
			return req_res_session.toArray();
		}else{
			return null;
		}
	}

	/**
	 * 根据需要排序的字段数字,按顺序降序排列
	 * @param list
	 * @param keys
	 * @return
	 */
	public static final List<Map<String, Object>> ListMapOrderByMapKeyDesc(List<Map<String, Object>> list,final String ... keys){
		Collections.sort(list,new Comparator<Map>() {
			public int compare(Map o1, Map o2) {
				return recursion(o1, o2, 0);
			}
			private int recursion(Map o1, Map o2, int i) {
				if (o1.containsKey(keys[i]) && o2.containsKey(keys[i])) {
					Object value1 = o1.get(keys[i]);
					Object value2 = o2.get(keys[i]);
					if (value1 == null && value2 == null) {
						if ((i+1) < keys.length) {
							int recursion = recursion(o1, o2, i+1);
							return recursion;
						}else{
							return 0;
						}
					}else if(value1 == null && value2 != null){
						return 1;
					}else if(value1 != null && value2 == null){
						return -1;
					}else{
						if (value1.equals(value2)) {
							if ((i+1) < keys.length) {
								return recursion(o1, o2, i+1);
							}else{
								return 0;
							}
						}else{
							if (value1 instanceof String && value2 instanceof String) {
								return value2.toString().compareTo(value1.toString());
							}else if(value1 instanceof Timestamp && value2 instanceof Timestamp){
								return ((Timestamp)(value2)).compareTo(new Date(((Timestamp)(value1)).getTime()));
							}else{
								return new BigDecimal(value2.toString()).compareTo(new BigDecimal(value1.toString()));
							}
						}
					}
				}else{
					System.out.println(" ** The current map do not containskey : " + keys[i] + ",or The value of key is null **");
					return 0;
				}
			}
		});
		return list;
	}
	/**
	 * 根据需要排序的字段数字,按顺序升序排列
	 * @param list
	 * @param keys
	 * @return
	 */
	public static final List<Map<String, Object>> ListMapOrderByMapKeyAsc(List<Map<String, Object>> list,final String ... keys){
		Collections.sort(list,new Comparator<Map>() {
			public int compare(Map o1, Map o2) {
				return recursion(o1, o2, 0);
			}
			private int recursion(Map o1, Map o2, int i) {
				if (o1.containsKey(keys[i]) && o2.containsKey(keys[i])) {
					Object value1 = o1.get(keys[i]);
					Object value2 = o2.get(keys[i]);
					if (value1 == null && value2 == null) {
						if ((i+1) < keys.length) {
							int recursion = recursion(o1, o2, i+1);
							return recursion;
						}else{
							return 0;
						}
					}else if(value1 == null && value2 != null){
						return 1;
					}else if(value1 != null && value2 == null){
						return -1;
					}else{
						if (value1.equals(value2)) {
							if ((i+1) < keys.length) {
								return recursion(o1, o2, i+1);
							}else{
								return 0;
							}
						}else{
							if (value1 instanceof String && value2 instanceof String) {
								return value1.toString().compareTo(value2.toString());
							}else if(value1 instanceof Timestamp && value2 instanceof Timestamp){
								return ((Timestamp)(value1)).compareTo(new Date(((Timestamp)(value2)).getTime()));
							}else{
								return new BigDecimal(value1.toString()).compareTo(new BigDecimal(value2.toString()));
							}
						}
					}
				}else{
					System.out.println(" ** The current map do not containskey : " + keys[i] + ",or The value of key is null **");
					return 0;
				}
			}
		});
		return list;
	}

	/**
	 * 根据keys数组来移除map里对应的key和value
	 * @param map
	 * @param keys
	 */
	public static  void removeMapParmeByKey(Map map,String[]keys){
		for (String key : keys) {
			if(map.containsKey(key))map.remove(key);
		}
	}
	/**
	 * 随机生成指定位数验证码
	 * @return
	 */
	public static String getRandomNum(int count){
		StringBuffer sb = new StringBuffer();
		String str = "0123456789";
		Random r = new Random();
		for(int i=0;i<count;i++){
			int num = r.nextInt(str.length());
			sb.append(str.charAt(num));
			str = str.replace((str.charAt(num)+""), "");
		}
		return sb.toString();
	}
	/**
	 * 把时间根据时、分、秒转换为时间段
	 * @param StrDate
	 */
	public static String getTimes(String StrDate){
		String resultTimes = "";
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now;
		now = new Date();
		Date date=new Date();
        try {
            date=df.parse(StrDate);
        } catch (ParseException e) {
            return "未知";
        }
        long times = now.getTime()-date.getTime();
		long day  =  times/(24*60*60*1000);
		long hour = (times/(60*60*1000)-day*24);
		long min  = ((times/(60*1000))-day*24*60-hour*60);
		long sec  = (times/1000-day*24*60*60-hour*60*60-min*60);

		StringBuffer sb = new StringBuffer();
		//sb.append("发表于：");
		if(day>0 ){
			sb.append(day+"天前");
		}else if(hour>0 ){
			sb.append(hour+"小时前");
		} else if(min>0){
			sb.append(min+"分钟前");
		} else{
			sb.append(sec+"秒前");
		}
		resultTimes = sb.toString();
		return resultTimes;
	}

	/**
	 * XML格式字符串转换为Map
	 *
	 * @param strXML XML字符串
	 * @return XML数据转换后的Map
	 * @throws Exception
	 */
	public static Map<String, Object> xmlToMap(String strXML) throws Exception {
		try {
			Map<String, Object> data = new HashMap<String, Object>();
			DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			InputStream stream = new ByteArrayInputStream(strXML.getBytes("UTF-8"));
			org.w3c.dom.Document doc = documentBuilder.parse(stream);
			doc.getDocumentElement().normalize();
			NodeList nodeList = doc.getDocumentElement().getChildNodes();
			for (int idx = 0; idx < nodeList.getLength(); ++idx) {
				Node node = nodeList.item(idx);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					org.w3c.dom.Element element = (org.w3c.dom.Element) node;
					data.put(element.getNodeName(),  element.getTextContent());
				}
			}
			try {
				stream.close();
			} catch (Exception ex) {
				// do nothing
			}
			return data;
		} catch (Exception ex) {
			getLogger().warn("Invalid XML, can not convert to map. Error message: {}. XML content: {}", ex.getMessage(), strXML);
			throw ex;
		}

	}
	/**
	 * 日志
	 * @return
	 */
	public static Logger getLogger() {
		Logger logger = LoggerFactory.getLogger("wxpay java sdk");
		return logger;
	}

	/**
	 * 输入参数,返回一个String,用于判断数据库字段类型为字符串的字段值,为数字的不要用这个方法:</br>
	 * xx is not null and xx<>'' 多个中间用 and 连接
	 * @param arg 需要排除空值和null的字段名
	 * @return
	 */
	public static String notEmptySQL(String ...arg){
		for (int i = 0; i < arg.length; i++) {
			arg[i]=(" "+arg[i]+" is not null and "+arg[i]+"<>'' ");
		}
		return org.apache.commons.lang3.StringUtils.join(arg," and ");
	}


	/**
	 * 本项目controller之间调用/接收返回
	 * @param paramsMap
	 * @return
	 */
	public T controllerToControllerByPOST(String portAllName, Map<String,String>paramsMap,Class<T> returnClass) throws IOException {
		CloseableHttpClient client = HttpClients.createDefault();
		String responseText = "";
		CloseableHttpResponse response = null;
		HttpPost method = new HttpPost("http://"+getDomain()+portAllName);
		if (paramsMap != null) {
			List <NameValuePair> paramList=new ArrayList<>();
			for (Map.Entry<String,String> param: paramsMap.entrySet()){
				NameValuePair pair = new BasicNameValuePair(param.getKey(),param.getValue());
				paramList.add(pair);
			}
			method.setEntity(new UrlEncodedFormEntity(paramList,"UTF-8"));
		}
		response = client.execute(method);
		HttpEntity entity = response.getEntity();
		T result=null;
		if (entity != null) {
			responseText = EntityUtils.toString(entity, "UTF-8");
			Map<String,Object> responseMap= JSONObject.fromObject(responseText);
			if(responseMap.containsKey("code")&&!responseMap.get("code").equals(200)){
				return result;
			}
			result= (T) JSONObject.toBean(JSONObject.fromObject(responseText),returnClass.getClass());
		}
		response.close();
		client.close();
		return result;
	}

	public static Map<String,Object>getUrlRequestParame(){
		Map<String,Object>result=new HashMap<>();
		Map<String,Object>parameMap=new HashMap<>();
		HttpServletRequest request=((HttpServletRequest)getRequest_Response_Session()[0]);
		StringBuffer buffer=new StringBuffer("?");
		List<String>parame=new ArrayList<>();
		Enumeration enu=request.getParameterNames();
		while(enu.hasMoreElements()){
			String paraName=(String)enu.nextElement();
			parameMap.put(paraName,request.getParameter(paraName));
			parame.add((paraName+"="+request.getParameter(paraName)));
		}
		result.put("parameMap",parameMap);
		result.put("parameString",("?"+ StringUtils.join(parame,"&")));
		return result;
	}

	public static Map<String,Object> getWXAuthByCode(String code,String secret,String appid) throws IOException {
		String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appid +
				"&secret=" + secret +
				"&code=" + code +
				"&grant_type=authorization_code";
		return doGetJson(url);
	}
	public static JSONObject getWXAuthByOpenId(JSONObject getWXAuthByCode) throws IOException{
		String url = "https://api.weixin.qq.com/sns/userinfo?access_token=" + getWXAuthByCode.getString("access_token") +
				"&openid=" + getWXAuthByCode.getString("openid") +
				"&lang=zh_CN";
		return doGetJson(url);
	}

	public static JSONObject doGetJson(String url) throws IOException {
		JSONObject jsonObject = new JSONObject();
		DefaultHttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		HttpResponse response = client.execute(httpGet);
		HttpEntity entity = response.getEntity();
		if (entity != null){
			String result = EntityUtils.toString(entity, "UTF-8");
			jsonObject=JSONObject.fromObject(result);
		}
		httpGet.releaseConnection();
		return jsonObject;
	}
	/**
	 * 根据网络文件获取该文件16进制的前20位字符串并判断返回该文件的真实类型(后缀名)
	 * @param multipartFile
	 * @return
	 */
	public static String getFileSuffixBy16(MultipartFile multipartFile) throws IOException {
		StringWriter sw = new StringWriter();
		FileInputStream fin= (FileInputStream) multipartFile.getInputStream();
		int len = 1;
		byte[] temp = new byte[len];
		/*16进制转化模块*/
		for (; (fin.read(temp, 0, len)) != -1;) {
			if (temp[0] > 0xf && temp[0] <= 0xff) {
				sw.write(Integer.toHexString(temp[0]));
			} else if (temp[0] >= 0x0 && temp[0] <= 0xf) {//对于只有1位的16进制数前边补“0”
				sw.write("0" + Integer.toHexString(temp[0]));
			} else { //对于int<0的位转化为16进制的特殊处理，因为Java没有Unsigned int，所以这个int可能为负数
				sw.write(Integer.toHexString(temp[0]).substring(6));
			}
			if(sw.toString().length()>20)break;
		}
		if(isNull(sw))return null;
		if(sw.toString().toUpperCase().startsWith("FFD8FF"))return"jpg"; //JPEG (jpg)
		else if(sw.toString().toUpperCase().startsWith("89504E47"))return"png"; //PNG (png)
		else if(sw.toString().toUpperCase().startsWith("47494638"))return"gif"; //GIF (gif)
		else if(sw.toString().toUpperCase().startsWith("49492A00"))return"tif"; //TIFF (tif)
		else if(sw.toString().toUpperCase().startsWith("424D"))return"bmp"; //16色位图(bmp)
		else if(sw.toString().toUpperCase().startsWith("41433130"))return"dwg"; //CAD (dwg)
		else if(sw.toString().toUpperCase().startsWith("3c21444f435459504520"))return"html"; //HTML (html)
		else if(sw.toString().toUpperCase().startsWith("3c21646f637479706520"))return"htm"; //HTM (htm)
		else if(sw.toString().toUpperCase().startsWith("48544d4c207b0d0a0942"))return"css"; //css
		else if(sw.toString().toUpperCase().startsWith("696b2e71623d696b2e71"))return"js"; //js
		else if(sw.toString().toUpperCase().startsWith("7B5C727466"))return"rtf"; //Rich Text Format (rtf)
		else if(sw.toString().toUpperCase().startsWith("38425053"))return"psd"; //Photoshop (psd)
		else if(sw.toString().toUpperCase().startsWith("46726f6d3a203d3f6762"))return"eml"; //Email [Outlook Express 6] (eml)
		else if(sw.toString().toUpperCase().startsWith("d0cf11e0a1b11ae10000"))return"doc"; //MS Excel 注意：word、msi 和 excel的文件头一样
		else if(sw.toString().toUpperCase().startsWith("d0cf11e0a1b11ae10000"))return"vsd"; //Visio 绘图
		else if(sw.toString().toUpperCase().startsWith("5374616E64617264204A"))return"mdb"; //MS Access (mdb)
		else if(sw.toString().toUpperCase().startsWith("252150532D41646F6265"))return"ps";
		else if(sw.toString().toUpperCase().startsWith("255044462d312e350d0a"))return"pdf"; //Adobe Acrobat (pdf)
		else if(sw.toString().toUpperCase().startsWith("2e524d46000000120001"))return"rmvb"; //rmvb/rm相同
		else if(sw.toString().toUpperCase().startsWith("464c5601050000000900"))return"flv"; //flv与f4v相同
		else if(sw.toString().toUpperCase().startsWith("00000020667479706d70"))return"mp4";
		else if(sw.toString().toUpperCase().startsWith("49443303000000002176"))return"mp3";
		else if(sw.toString().toUpperCase().startsWith("000001ba210001000180"))return"mpg"; //
		else if(sw.toString().toUpperCase().startsWith("3026b2758e66cf11a6d9"))return"wmv"; //wmv与asf相同
		else if(sw.toString().toUpperCase().startsWith("52494646e27807005741"))return"wav"; //Wave (wav)
		else if(sw.toString().toUpperCase().startsWith("52494646d07d60074156"))return"avi";
		else if(sw.toString().toUpperCase().startsWith("4d546864000000060001"))return"mid"; //MIDI (mid)
		else if(sw.toString().toUpperCase().startsWith("504b0304140000000800"))return"zip";
		else if(sw.toString().toUpperCase().startsWith("526172211a0700cf9073"))return"rar";
		else if(sw.toString().toUpperCase().startsWith("235468697320636f6e66"))return"ini";
		else if(sw.toString().toUpperCase().startsWith("504b03040a0000000000"))return"jar";
		else if(sw.toString().toUpperCase().startsWith("4d5a9000030000000400"))return"exe";//可执行文件
		else if(sw.toString().toUpperCase().startsWith("3c25402070616765206c"))return"jsp";//jsp文件
		else if(sw.toString().toUpperCase().startsWith("4d616e69666573742d56"))return"mf";//MF文件
		else if(sw.toString().toUpperCase().startsWith("3C3F786D6C"))return"xml";//xml文件
		else if(sw.toString().toUpperCase().startsWith("494e5345525420494e54"))return"sql";//xml文件
		else if(sw.toString().toUpperCase().startsWith("7061636b616765207765"))return"java";//java文件
		else if(sw.toString().toUpperCase().startsWith("406563686f206f66660d"))return"bat";//bat文件
		else if(sw.toString().toUpperCase().startsWith("1f8b0800000000000000"))return"gz";//gz文件
		else if(sw.toString().toUpperCase().startsWith("6c6f67346a2e726f6f74"))return"properties";//bat文件
		else if(sw.toString().toUpperCase().startsWith("cafebabe0000002e0041"))return"class";//bat文件
		else if(sw.toString().toUpperCase().startsWith("49545346030000006000"))return"chm";//bat文件
		else if(sw.toString().toUpperCase().startsWith("04000000010000001300"))return"mxp";//bat文件
		else if(sw.toString().toUpperCase().startsWith("504b0304140006000800"))return"docx";//docx文件
		else if(sw.toString().toUpperCase().startsWith("d0cf11e0a1b11ae10000"))return"wps";//WPS文字wps、表格et、演示dps都是一样的
		else if(sw.toString().toUpperCase().startsWith("6431303a637265617465"))return"torrent";
		else if(sw.toString().toUpperCase().startsWith("6D6F6F76"))return"mov"; //Quicktime (mov)
		else if(sw.toString().toUpperCase().startsWith("FF575043"))return"wpd"; //WordPerfect (wpd)
		else if(sw.toString().toUpperCase().startsWith("CFAD12FEC5FD746F"))return"dbx"; //Outlook Express (dbx)
		else if(sw.toString().toUpperCase().startsWith("2142444E"))return"pst"; //Outlook (pst)
		else if(sw.toString().toUpperCase().startsWith("AC9EBD8F"))return"qdf"; //Quicken (qdf)
		else if(sw.toString().toUpperCase().startsWith("E3828596"))return"pwl"; //Windows Password (pwl)
		else if(sw.toString().toUpperCase().startsWith("2E7261FD"))return"ram"; //Real Audio (ram)
		else return null;
	}


	public static Map<String,Object> getJSONObject(Object JSONstr){
		try {
			if(Tool.isNull(JSONstr)){
				return new HashMap<>();
			}else{
				return JSONObject.fromObject(JSONstr.toString().replaceAll("\n",""));
			}
		}catch (Exception e){
			return new HashMap<>();
		}
	}
	public static List<Map<String,Object>> getJSONArray(Object JSONstr){
		try {
			if(Tool.isNull(JSONstr)){
				return new ArrayList<>();
			}else{
				return JSONArray.fromObject(JSONstr.toString().replaceAll("\n",""));
			}
		}catch (Exception e){
			return new ArrayList<>();
		}
	}
	public static JSONObject getJSONObjectAsJSON(Object JSONstr){
		try {
			if(Tool.isNull(JSONstr)){
				return new JSONObject();
			}else{
				return JSONObject.fromObject(JSONstr.toString().replaceAll("\n",""));
			}
		}catch (Exception e){
			return new JSONObject();
		}
	}
	public static JSONArray getJSONArrayAsJSON(Object JSONstr){
		try {
			if(Tool.isNull(JSONstr)){
				return new JSONArray();
			}else{
				return JSONArray.fromObject(JSONstr.toString().replaceAll("\n",""));
			}
		}catch (Exception e){
			return new JSONArray();
		}
	}
	public static boolean strIJSONArray(String JSONstr){
		try {
			if(Tool.isNull(JSONstr)){
				return false;
			}else{
				JSONArray.fromObject(JSONstr.replaceAll("\n",""));
				return true;
			}
		}catch (Exception e){
			return false;
		}
	}

    public static String nowStr(String pattern) {
		return new SimpleDateFormat(pattern).format(System.currentTimeMillis());
    }

    public static String formatTimeByPattern(Object time,String pattern){
		return new SimpleDateFormat(pattern).format(time);
	}

	/**
	 * 百分比
	 * @param y 被除数
	 * @param z 除数
	 * @return
	 */
	public static String percent(int y, int z) {
		String baifenbi = "";// 接受百分比的值
		double baiy = y * 1.0;
		double baiz = z * 1.0;
		double fen = baiz!=0?(baiy / baiz):0.00;
		DecimalFormat df1 = new DecimalFormat("##.00%");
		baifenbi = fen==0?"0%":df1.format(fen);
		return baifenbi;
	}

	/**
	 * 从富文本中提取图片url
	 * @param htmlStr
	 * @return
	 */
	public static List<String> getImgStr(String htmlStr) {
		List<String> list = new ArrayList<>();
		String img = "";
		Pattern p_image;
		Matcher m_image;
		// String regEx_img = "<img.*src=(.*?)[^>]*?>"; //图片链接地址
		String regEx_img = "<img.*src\\s*=\\s*(.*?)[^>]*?>";
		p_image = Pattern.compile(regEx_img, Pattern.CASE_INSENSITIVE);
		m_image = p_image.matcher(htmlStr);
		while (m_image.find()) {
			// 得到<img />数据
			img = m_image.group();
			// 匹配<img>中的src数据
			Matcher m = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(img);
			while (m.find()) {
				list.add(m.group(1));
			}
		}
		return list;
	}
	public static String get32UUID() {
		String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
		return uuid;
	}
	public static String FormatNumzz(Object num_, Boolean kBool) {
		String num=null;
		if(num_==null||"".equals(num_.toString().trim())||"null".equals(num_.toString().trim()))return "0";
		StringBuffer sb = new StringBuffer();
		num=num_.toString().indexOf(".")>0?num_.toString().substring(0,num_.toString().indexOf(".")):num_.toString();
		if (!StringUtils.isNumeric(num))
			return "0";
		if (kBool == null)
			kBool = false;
		BigDecimal b0 = new BigDecimal("1000");
		BigDecimal b1 = new BigDecimal("10000");
		BigDecimal b2 = new BigDecimal("100000000");
		BigDecimal myself = new BigDecimal("100000");
		if("".equals(num.trim()))return "0";
		BigDecimal b3 = new BigDecimal(num);

		String formatNumStr = "";
		String nuit = "";

		// 以千为单位处理
		if (kBool) {
			if (b3.compareTo(b0) == 0 || b3.compareTo(b0) == 1) {
				return "999+";
			}
			return num;
		}

		// 以万为单位处理
		if (b3.compareTo(b1) == -1) {
			sb.append(b3.toString());
		} else if ((b3.compareTo(b1) == 0 && b3.compareTo(b1) == 1)
				|| b3.compareTo(b2) == -1) {
			formatNumStr = b3.divide(b1).toString();
			nuit = "w+";
		} else if (b3.compareTo(b2) == 0 || b3.compareTo(b2) == 1) {
			formatNumStr = b3.divide(b2).toString();
			nuit = "e+";
		}

		if (!"".equals(formatNumStr)) {
			if (Integer.parseInt(num) >= 10000 && Integer.parseInt(num) <= 100000) {
				int i = formatNumStr.indexOf(".");
				if (i == -1) {
					sb.append(formatNumStr).append(nuit);
				} else {
					i = i + 1;
					String v = formatNumStr.substring(i, i + 1);
					if (!v.equals("0")) {
						sb.append(formatNumStr.substring(0, i + 1)).append(nuit);
					} else {
						sb.append(formatNumStr.substring(0, i - 1)).append(nuit);
					}
				}
			} else {
				int i = formatNumStr.indexOf(".");
				if(i==-1){
					sb.append(formatNumStr).append(nuit);
				}else{
					sb.append(formatNumStr.substring(0, i)).append(nuit);
				}

			}
		}
		if (sb.length() == 0)
			return "0";
		return sb.toString();
	}
}
