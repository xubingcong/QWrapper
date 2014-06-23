
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightDetail;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.OneWayFlightInfo;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;

/**
 * 单程请求
 * 塞浦路斯航空
 * http://cyprusair.com/843,0,0,0,2-default.aspx
 * @author xubc
 *
 */                  
public class Wrapper_gjdaircy001 implements QunarCrawler{
     private  String execution="";
     private  String refererUrl="";
     private  String cookie="";
     private  QFHttpClient httpClient = null;
 	
 	public static void main(String[] args) {
 		/*
 		 *   gjdaircy001
 		 *   测试条件 svo-tlv  lca-lon
 	     */
 				
 		Wrapper_gjdaircy001 instance = new Wrapper_gjdaircy001();
 		
 		
 		FlightSearchParam p =new FlightSearchParam();
 		p.setWrapperid("gjdaircy001");
 		p.setDep("SVO");
 		p.setArr("TLV");
 		p.setDepDate("2014-07-10");
 		p.setTimeOut("120000");
 		String html="";
 		try {
 			 html=instance.getHtml(p);
 			Files.write(html, new File("E:\\006.html"),Charsets.UTF_8);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
// 		String page="";
// 		try {
// 			page = Files.toString(new File("D:\\Noname1.html"),Charsets.UTF_8);
// 		} catch (IOException e) {
// 			e.printStackTrace();
// 		}
 		ProcessResultInfo result =instance. process(html, p);
 		if(result.isRet() && result.getStatus().equals(Constants.SUCCESS))
 		{
 			List<OneWayFlightInfo> flightList = (List<OneWayFlightInfo>) result.getData();
 			for (OneWayFlightInfo in : flightList){
 				System.out.println("************" + in.getInfo().toString());
 				System.out.println("++++++++++++" + in.getDetail().toString());
 			}
 		}
 		else
 		{
 			System.out.println(result.getStatus());
 		}
 	}
     @Override
	public BookingResult getBookingInfo(FlightSearchParam arg0) {
		
	
		String bookingUrlPre = "https://wl-prod.sabresonicweb.com/SSW2010/CYCY/webqtrip.html";
		BookingResult bookingResult = new BookingResult();
		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo.setAction(bookingUrlPre);
		bookingInfo.setMethod("get");
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("searchType", "NORMAL");
		map.put("journeySpan", "OW");
		map.put("origin", arg0.getDep());
		map.put("destination", arg0.getArr());
		map.put("departureDate", arg0.getDepDate());
		map.put("numAdults", "1");
		map.put("alternativeLandingPage", "True");
		map.put("lang", "en");
		bookingInfo.setInputs(map);		
		bookingResult.setData(bookingInfo);
		bookingResult.setRet(true);
		return bookingResult;
	}
	@Override
	public String getHtml(FlightSearchParam param) {
		QFGetMethod get = null;	QFGetMethod get1 = null;	
		try {	
		 httpClient = new QFHttpClient(param, false);
		 httpClient.getParams().setCookiePolicy(
				CookiePolicy.BROWSER_COMPATIBILITY);
		 String getUrl = String.format("https://wl-prod.sabresonicweb.com/SSW2010/CYCY/webqtrip.html?searchType=NORMAL&journeySpan=OW&origin=%s&destination=%s&departureDate=%s&numAdults=1&alternativeLandingPage=True&lang=en", param.getDep(), param.getArr(),param.getDepDate());
			get = new QFGetMethod(getUrl);
			try {
				get.setFollowRedirects(false);
				get.getParams().setContentCharset("utf-8");
				httpClient.executeMethod(get);
				if(get.getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY || get.getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY){
					Header location = get.getResponseHeader("Location");
					String url = "";
					if(location !=null){
						url = location.getValue();
						if(!url.startsWith("http")){
							url = get.getURI().getScheme() + "://" + get.getURI().getHost() + (get.getURI().getPort()==-1?"":(":"+get.getURI().getPort())) + url;
						}
					}else{
						return "";
					}
					execution=StringUtils.substringAfter(url, "execution=");
					refererUrl=url;
				    cookie = StringUtils.join(httpClient.getState().getCookies(),"; ");
					get1 = new QFGetMethod(url);
					get1.addRequestHeader("Cookie",cookie);
					httpClient.executeMethod(get1);
				   return get1.getResponseBodyAsString();
				}
			} catch (Exception e) {
			e.printStackTrace();
			} finally {
				if(get1!=null){
					get1.releaseConnection();
				}
			}
		} catch (Exception e) {			
			e.printStackTrace();
		} finally{
			if (null != get){
				get.releaseConnection();
			}
		}
		return "Exception";
	}
	

   
	@Override
	public  ProcessResultInfo process(String html, FlightSearchParam param) {
		ProcessResultInfo result = new ProcessResultInfo();
		if ("Exception".equals(html)) {	
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;			
		}	
		if ("ParamError".equals(html)) {	
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;			
		}
		if(html.contains("No Flights Available")){
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;		
		}
		if(html.contains("Error: Please see description below")){
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;		
		}
		try {
			List<OneWayFlightInfo> flightList = new ArrayList<OneWayFlightInfo>();
			String flightTable=StringUtils.substringBetween(html,"class=\"flight-list-section flight-list\">","<div wl:section=\"discountIndicator\"");
			String[] results = flightTable.split("<tr class=");
			for(int j=1;j<results.length;j++){
				OneWayFlightInfo baseFlight = new OneWayFlightInfo();
				List<FlightSegement> segs = new ArrayList<FlightSegement>();
				FlightDetail flightDetail = new FlightDetail();
				List<String> flightNoList = new ArrayList<String>();
				    //获取td中的票价信息,根据radio的可选状态获取第一个td票价，即为最低价
			     String[] priceResults = results[j].split(" <td class=\"price price");
		    	for(int i=1;i<priceResults.length;i++){
				 if(priceResults[i].contains("radio")){
					String radio_id_value=StringUtils.substringBetween(priceResults[i], "<input id=","type=\"radio").trim();
					String radio_id=StringUtils.substringBetween(radio_id_value, "both","\"").trim();
				      //获取返回的price_json串 
					String priceResponse=this.resquestPostPrice(radio_id);
					  //获取返回的flight_json串  
					String flightResponse=this.resquestPostFlight(radio_id);
					JSONArray json = JSONObject.parseObject(priceResponse).getJSONArray("content");
					JSONArray json_ = JSONObject.parseObject(flightResponse).getJSONArray("content");
					JSONArray outbounds=json_.getJSONObject(0).getJSONObject("model").getJSONArray("outbounds");
					JSONObject flightObject=outbounds.getJSONObject(j-1);
					JSONArray segments=	flightObject.getJSONArray("segments");
					for(int n=0;n<segments.size();n++){
						FlightSegement seg=new FlightSegement();
						JSONObject	flightSegments= segments.getJSONObject(n);
					    //获取航空公司编号
						String company=flightSegments.getJSONArray("airlineCodes").getString(0);
					    seg.setCompany(company);
					    //获取航班号
					    String flightNo=company+flightSegments.getJSONArray("flightNumber").getString(0);
					    seg.setFlightno(flightNo);
					    flightNoList.add(flightNo);
					    //获取起飞时间
					    String depDate=flightSegments.getString("departureDate");
					    seg.setDepDate(depDate.substring(0, 10).replace("/", "-"));
					    seg.setDeptime(depDate.substring(11,depDate.length()-3));
					    //获取到达时间
					    String arrDate=flightSegments.getString("arrivalDate");
					    seg.setArrDate(arrDate.substring(0, 10).replace("/", "-"));
					    seg.setArrtime(arrDate.substring(11,depDate.length()-3));
					    //获取起飞城市
					    String depairport=flightSegments.getString("departureCode");
					    seg.setDepairport(depairport);
					    //获取到达城市
					    String arrairport=flightSegments.getString("arrivalCode");	
					    seg.setArrairport(arrairport);
					    segs.add(seg);
					}
					JSONObject content=	json.getJSONObject(0).getJSONObject("model");
					JSONObject ero=content.getJSONObject("total").getJSONArray("priceAlternatives").getJSONObject(0).getJSONObject("pricesPerCurrency").getJSONObject("EUR");
					//获取总票价
					String totalPrice=ero.getString("amount");
					//获取币种
					String currencyCode=ero.getJSONObject("currency").getString("code");
					flightDetail.setPrice(Double.parseDouble(totalPrice));
					flightDetail.setMonetaryunit(currencyCode);
					flightDetail.setFlightno(flightNoList);
					flightDetail.setDepcity(param.getDep());
    				flightDetail.setArrcity(param.getArr());
    				flightDetail.setWrapperid(param.getWrapperid());
    				flightDetail.setDepdate(String2Date(param.getDepDate()));
    				baseFlight.setDetail(flightDetail);
    				baseFlight.setInfo(segs);
    				flightList.add(baseFlight);
					break;
				}
			 }
			}
			result.setRet(true);
			result.setStatus(Constants.SUCCESS);
			result.setData(flightList);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			result.setRet(false);
			result.setStatus(Constants.PARSING_FAIL);
			return result;
		}
	}
	/**
	 * 二次请求获取票价json详细
	 * 
	 * @param radio_id
	 * @return
	 */
	public String resquestPostPrice(String radio_id) {
		QFPostMethod post = null;
		try {
			// httpClient = new QFHttpClient(param, false);
			String url = "https://wl-prod.sabresonicweb.com/SSW2010/CYCY/webqtrip.html";
			NameValuePair[] pairs = new NameValuePair[] {
					new NameValuePair("_eventId_ajax", ""),
					new NameValuePair("execution", execution),
					new NameValuePair("ajaxSource", "true"),
					new NameValuePair("contextObject",
							getObjectString(radio_id)), };
			post = new QFPostMethod(url);
			post.setRequestBody(pairs);
			post.addRequestHeader("Cookie", cookie);
			post.addRequestHeader("X-Requested-With", "XMLHttpRequest");
			post.addRequestHeader("Referer", refererUrl);
			post.addRequestHeader("Content-Type",
					"application/x-www-form-urlencoded; charset=UTF-8");
			int status = httpClient.executeMethod(post);
			return post.getResponseBodyAsString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != post) {
				post.releaseConnection();
			}
		}
		return "Exception";
	}

	/**
	 * 二次请求获取航班json详细
	 * 
	 * @param radio_id
	 * @return
	 */
	public String resquestPostFlight(String radio_id) {
		QFPostMethod post = null;
		try {
			// httpClient = new QFHttpClient(param, false);
			String url = "https://wl-prod.sabresonicweb.com/SSW2010/CYCY/webqtrip.html";
			NameValuePair[] pairs = new NameValuePair[] {
					new NameValuePair("_eventId_ajax", ""),
					new NameValuePair("execution", execution),
					new NameValuePair("ajaxSource", "true"),
					new NameValuePair("contextObject",
							getObjectString1(radio_id)), };
			post = new QFPostMethod(url);
			post.setRequestBody(pairs);
			post.addRequestHeader("Cookie", cookie);
			post.addRequestHeader("X-Requested-With", "XMLHttpRequest");
			post.addRequestHeader("Referer", refererUrl);
			post.addRequestHeader("Content-Type",
					"application/x-www-form-urlencoded; charset=UTF-8");
			int status = httpClient.executeMethod(post);
			return post.getResponseBodyAsString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != post) {
				post.releaseConnection();
			}
		}
		return "Exception";
	}

	/**
	 * 构建json参数串
	 * 
	 * @param radio_id
	 * @return
	 */
	public static String getObjectString(String radio_id) {
		JSONObject o = new JSONObject();
		JSONArray data = new JSONArray();
		JSONObject o_data = new JSONObject();
		o_data.put("componentType", "cart");
		o_data.put("actionCode", "checkPrice");
		data.add(o_data);
		JSONObject querydata = new JSONObject();
		querydata.put("actionCode", "checkPrice");
		querydata.put("componentType", "cart");
		querydata.put("componentId", "cart_1");
		querydata.put("queryData", "");
		JSONArray requestPartials = new JSONArray();
		requestPartials.add("initialized");
		JSONArray selectedBasketRefs = new JSONArray();
		selectedBasketRefs.add(Integer.parseInt(radio_id));
		querydata.put("requestPartials", requestPartials);
		querydata.put("selectedBasketRefs", selectedBasketRefs);
		o_data.put("queryData", querydata);
		o.put("transferObjects", data);
		return o.toJSONString();
	}

	/**
	 * 构建json参数串
	 * 
	 * @param radio_id
	 * @return
	 */
	public static String getObjectString1(String radio_id) {
		JSONObject o = new JSONObject();
		JSONArray data = new JSONArray();
		JSONObject o_data = new JSONObject();
		o_data.put("actionCode", "getAdvisors");
		o_data.put("componentType", "flc");
		data.add(o_data);
		JSONObject querydata = new JSONObject();
		querydata.put("componentId", "flc_1");
		querydata.put("componentType", "flc");
		querydata.put("actionCode", "getAdvisors");
		querydata.put("queryData", "null");
		querydata.put("direction", "both");
		JSONArray requestPartials = new JSONArray();
		requestPartials.add("__flightAdvisorOutboundRow");
		JSONArray basketHashRefs = new JSONArray();
		basketHashRefs.add(Integer.parseInt(radio_id));
		querydata.put("basketHashRefs", basketHashRefs);
		querydata.put("requestPartials", requestPartials);
		o_data.put("queryData", querydata);
		o.put("transferObjects", data);
		return o.toJSONString();
	}
	
	/**
	 * String 转 Date
	 * 
	 * @param strdate
	 * @return
	 */
	public static Date String2Date(String strdate){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");   
		Date retuenDate =null;
		try {
			retuenDate = format.parse(strdate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retuenDate;
		
	}
	
	

}
