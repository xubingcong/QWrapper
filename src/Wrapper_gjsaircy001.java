package org.com.qunar;
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
import com.qunar.qfwrapper.bean.search.RoundTripFlightInfo;
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
public class Wrapper_gjsaircy001 implements QunarCrawler{
     private  String execution="";
     private  String refererUrl="";
     private  String cookie="";
     private  QFHttpClient httpClient = null;
 	
 	public static void main(String[] args) {
 		/*
 		 *   gjsaircy001
 		 *   测试条件 SVO-TLV  LCA-LON
 	     */
 				
 		Wrapper_gjsaircy001 instance = new Wrapper_gjsaircy001();
 		
 		
 		FlightSearchParam p =new FlightSearchParam();
 		p.setWrapperid("gjsaircy001");
 		p.setDep("LCA");
 		p.setArr("LON");
 		p.setDepDate("2014-07-10");
 		p.setRetDate("2014-07-17");;
 		p.setTimeOut("1200000");
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
			List<RoundTripFlightInfo> flightList = (List<RoundTripFlightInfo>) result.getData();
			for (RoundTripFlightInfo in : flightList){
				System.out.println("------------" + in.getDetail());
				System.out.println("************" + in.getInfo().toString());
				System.out.println("++++++++++++" + in.getRetinfo().toString());
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
		map.put("journeySpan", "RT");
		map.put("origin", arg0.getDep());
		map.put("destination", arg0.getArr());
		map.put("departureDate", arg0.getDepDate());
		map.put("returnDate", arg0.getRetDate());
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
		   //https://wl-prod.sabresonicweb.com/SSW2010/CYCY/webqtrip.html?searchType=NORMAL&journeySpan=RT&origin=SVO&destination=TLV&departureDate=2014-07-10&returnDate=2014-07-17&numAdults=1&alternativeLandingPage=True&lang=en
		 String getUrl = String.format("https://wl-prod.sabresonicweb.com/SSW2010/CYCY/webqtrip.html?searchType=NORMAL&journeySpan=RT&origin=%s&destination=%s&departureDate=%s&returnDate=%s&numAdults=1&alternativeLandingPage=True&lang=en", param.getDep(), param.getArr(),param.getDepDate(),param.getRetDate());
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
	

   //59260400
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
			List<RoundTripFlightInfo> flightList = new ArrayList<RoundTripFlightInfo>();
			//往
			String ouFlightTable=StringUtils.substringBetween(html,"class=\"flight-list-section flight-list\">","<div wl:section=\"discountIndicator\"");
			//返
			String ouFlightDiv=StringUtils.substringAfter(html, "<div class=\"flight-list-container\" id=\"inbounds\">");
			String inFlightTable=StringUtils.substringBetween(ouFlightDiv,"class=\"flight-list-section flight-list\">","<div wl:section=\"discountIndicator\"");
			
			String[] ou_results = ouFlightTable.split("<tr class=");
			String[] in_results = inFlightTable.split("<tr class=");
			for(int j=1;j<ou_results.length;j++){
				RoundTripFlightInfo baseFlight = new RoundTripFlightInfo();
				List<FlightSegement> segs = new ArrayList<FlightSegement>();
				FlightDetail flightDetail = new FlightDetail();
				List<String> flightNoList = new ArrayList<String>();
				    //获取td中的票价信息,根据radio的可选状态获取第一个td票价，即为最低价
			     String[] priceResults = ou_results[j].split(" <td class=\"price price");
		    	for(int i=1;i<priceResults.length;i++){
				 if(priceResults[i].contains("radio")){
					String radio_id_value=StringUtils.substringBetween(priceResults[i], "<input id=","type=\"radio").trim();
					//"outbounds-1491063369"
					String radio_id=StringUtils.substringBetween(radio_id_value, "outbounds","\"").trim();
					  //获取返回的flight_json串  
					String flightResponse=this.resquestPostFlight(radio_id);
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
			
					flightDetail.setFlightno(flightNoList);
					flightDetail.setDepcity(param.getDep());
					flightDetail.setArrcity(param.getArr());
					flightDetail.setWrapperid(param.getWrapperid());
					flightDetail.setDepdate(String2Date(param.getDepDate()));

     			 	 List<FlightSegement> re_segs = new ArrayList<FlightSegement>();
    			 	 List<String> retflightno = new ArrayList<String>();
    				for(int k=1;k<in_results.length;k++){
    					   String[] inPriceResults = in_results[k].split(" <td class=\"price price");
    						for(int m=1;m<inPriceResults.length;m++){
    							//获取第一个radio id 即跳出循环
    					   if(inPriceResults[m].contains("radio")){
    							String in_radio_id_value=StringUtils.substringBetween(inPriceResults[m], "<input id=","type=\"radio").trim();
    							String in_radio_id=StringUtils.substringBetween(in_radio_id_value, "inbounds","\"").trim();
   					     		JSONArray inbounds=json_.getJSONObject(0).getJSONObject("model").getJSONArray("inbounds");
    							JSONObject in_flightObject=inbounds.getJSONObject(k-1);
    							JSONArray in_segments=	in_flightObject.getJSONArray("segments");
    							for(int n=0;n<in_segments.size();n++){
    								FlightSegement re_seg=new FlightSegement();
    								JSONObject	flightSegments= in_segments.getJSONObject(n);
    							    //获取返程航空公司编号
    								String company=flightSegments.getJSONArray("airlineCodes").getString(0);
    								re_seg.setCompany(company);
    							    //获取返程航班号
    							    String flightNo=company+flightSegments.getJSONArray("flightNumber").getString(0);
    							    re_seg.setFlightno(flightNo);
    							    retflightno.add(flightNo);
    							    //获取返程起飞时间
    							    String depDate=flightSegments.getString("departureDate");
    							    re_seg.setDepDate(depDate.substring(0, 10).replace("/", "-"));
    							    re_seg.setDeptime(depDate.substring(11,depDate.length()-3));
    							    //获取返程到达时间
    							    String arrDate=flightSegments.getString("arrivalDate");
    							    re_seg.setArrDate(arrDate.substring(0, 10).replace("/", "-"));
    							    re_seg.setArrtime(arrDate.substring(11,depDate.length()-3));
    							    //获取返程起飞城市
    							    String depairport=flightSegments.getString("departureCode");
    							    re_seg.setDepairport(depairport);
    							    //获取返程到达城市
    							    String arrairport=flightSegments.getString("arrivalCode");	
    							    re_seg.setArrairport(arrairport);
    							    re_segs.add(re_seg);
    							}  
  						       //获取返回的price_json串 
    							String in_priceResponse=this.resquestPostPrice(radio_id,in_radio_id);
    							JSONArray in_json = JSONObject.parseObject(in_priceResponse).getJSONArray("content");
    							JSONObject in_content=	in_json.getJSONObject(0).getJSONObject("model");
    							JSONObject in_ero=in_content.getJSONObject("total").getJSONArray("priceAlternatives").getJSONObject(0).getJSONObject("pricesPerCurrency").getJSONObject("EUR");
    							//获取币种
    							String currencyCode=in_ero.getJSONObject("currency").getString("code");
    							String inTotalPrice=in_ero.getString("amount");
    							flightDetail.setMonetaryunit(currencyCode);
    					        flightDetail.setPrice(Double.parseDouble(inTotalPrice));
    					       baseFlight.setDetail(flightDetail);
    					       baseFlight.setRetdepdate(String2Date(param.getRetDate()));
    					       baseFlight.setRetinfo(re_segs);
    					       baseFlight.setInfo(segs);
    					       baseFlight.setRetflightno(retflightno);
    					       flightList.add(baseFlight);
    					       break;
    					      }
    			    	  }
    						
    			    	}
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
	public String resquestPostPrice(String radio_id,String radio_id2) {
		QFPostMethod post = null;
		try {
			// httpClient = new QFHttpClient(param, false);
			String url = "https://wl-prod.sabresonicweb.com/SSW2010/CYCY/webqtrip.html";
			NameValuePair[] pairs = new NameValuePair[] {
					new NameValuePair("_eventId_ajax", ""),
					new NameValuePair("execution", execution),
					new NameValuePair("ajaxSource", "true"),
					new NameValuePair("contextObject",
							getObjectString(radio_id,radio_id2)), };
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
	public static String getObjectString(String radio_id,String radio_id2) {
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
		selectedBasketRefs.add(Integer.parseInt(radio_id2));
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
