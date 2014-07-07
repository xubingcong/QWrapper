
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
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
 * 往返请求
 * @author xubc
 * 亚德里亚航空
 * 航空公司主页：https://www.adria.si/en
 *
 */                  
public class Wrapper_gjsairjp001 implements QunarCrawler{
	public static void main(String[] args) {
		/*
		 * 测试条件
		 * KBP-LJU 2014-07-10 2014-07-27 LWO-LJU 2014-08-16 2014-08-20 LJU-TXL 2014-08-16 2014-08-27
	     */
		Wrapper_gjsairjp001 instance = new Wrapper_gjsairjp001();
		FlightSearchParam p =new FlightSearchParam();
		p.setWrapperid("gjdairjp001");
		p.setDep("KBP");
		p.setArr("LJU");
		p.setDepDate("2014-07-10");
		p.setRetDate("2014-07-17");
		p.setTimeOut("1000000");
		String html=instance.getHtml(p);
		String page="";
//		try {
//			page = Files.toString(new File("E:\\006.html"),Charsets.UTF_8);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		ProcessResultInfo result =instance. process(html, p);
		String str=com.alibaba.fastjson.JSON.toJSONString(result, SerializerFeature.DisableCircularReferenceDetect);
		System.out.println(str);
		if(result.isRet() && result.getStatus().equals(Constants.SUCCESS))
		{
			List<RoundTripFlightInfo> flightList = (List<RoundTripFlightInfo>) result.getData();
			for (RoundTripFlightInfo in : flightList){
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
	public BookingResult getBookingInfo(FlightSearchParam param) {
		
		String bookingUrlPre = "https://www.adria.si/en/submit_reservations//";
		BookingResult bookingResult = new BookingResult();
		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo.setAction(bookingUrlPre);
		bookingInfo.setMethod("post");
		Map<String, String> map = new LinkedHashMap<String, String>();
		
		map.put("trip-type", "R");
		map.put("from-airport",param.getDep());
		map.put("to-airport", param.getArr());
		map.put("out-date-sys", param.getDepDate());
		map.put("out-date", param.getDepDate().replaceAll("(....)-(..)-(..)", "$2/$3/$1"));
		map.put("in-date-sys", param.getRetDate());
		map.put("in-date", param.getRetDate().replaceAll("(....)-(..)-(..)", "$2/$3/$1"));
		map.put("passengers", "1");
		map.put("children", "0");
		map.put("infants", "0");
		map.put("flight-submit", "Find+flight");
		bookingInfo.setInputs(map);		
		bookingResult.setData(bookingInfo);
		bookingResult.setRet(true);
		return bookingResult;
	}

	@Override
	public String getHtml(FlightSearchParam param) {
		QFGetMethod get = null;	
		QFPostMethod post = null;
		try {	
			QFHttpClient httpClient = new QFHttpClient(param, false);
			 httpClient.getParams().setCookiePolicy(
						CookiePolicy.BROWSER_COMPATIBILITY);
			post=new QFPostMethod("https://www.adria.si/en/submit_reservations//");
			post.setFollowRedirects(false);
	     	NameValuePair[] pairs = new NameValuePair[]{
	    			new NameValuePair("trip-type", "R"),
	    			new NameValuePair("from-airport", param.getDep()),
	    			new NameValuePair("to-airport", param.getArr()),
	    			new NameValuePair("out-date-sys", param.getDepDate()),
	    			new NameValuePair("out-date",  param.getDepDate().replaceAll("(....)-(..)-(..)", "$2/$3/$1")),
	    			new NameValuePair("in-date-sys",  param.getRetDate()),
	    			new NameValuePair("in-date", param.getRetDate().replaceAll("(....)-(..)-(..)", "$2/$3/$1")),
	    			new NameValuePair("passengers", "1"),
	    			new NameValuePair("children", "0"),
	    			new NameValuePair("infants", "0"),
	    			new NameValuePair("flight-submit", "Find+flight"),
		  };
		     post.setRequestBody(pairs);
		     String cookie = StringUtils.join(httpClient.getState()
		    		 .getCookies(), "; ");
		     post.addRequestHeader("Cookie", cookie);
		     post.addRequestHeader("Referer", "https://www.adria.si/en");
		    int statuscode = httpClient.executeMethod(post);
			if(statuscode>=400){
				return "StatusError"+statuscode;
			}
		    if (statuscode == HttpStatus.SC_MOVED_TEMPORARILY
					|| statuscode == HttpStatus.SC_MOVED_PERMANENTLY) {
		      try{	
				Header location = post.getResponseHeader("Location");
				String url = "";
				if (location != null) {
					url = location.getValue();
					if (!url.startsWith("http")) {
						url = post.getURI().getScheme()
								+ "://"
								+ post.getURI().getHost()
								+ (post.getURI().getPort() == -1 ? ""
										: (":" + post.getURI().getPort()))
								+ url;
					}
				} else {
					return "";
				}


				get = new QFGetMethod(url);
				get.setFollowRedirects(false);
				get.addRequestHeader("Cookie", cookie);
				get.addRequestHeader("Referer", "https://www.adria.si/en");
				httpClient.executeMethod(get);
				String html=get.getResponseBodyAsString();
			    return html;
		      }catch (Exception e) {
		    		e.printStackTrace();
				}finally{
					if (null != get){
						get.releaseConnection();
					}
				}
		    } else {
				return post.getResponseBodyAsString();

			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if (null != post){
				post.releaseConnection();
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
		if(html.contains("The selected search data is invalid.")){
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;		
		}
		try {
			//获取结果页面表格中= Lowest price的价格
	       String low_price="";
	      //定义价格对应的航班
	    Map<String,String> map =new HashMap<String,String>();
		String div_price=StringUtils.substringBetween(html,"class=\"fdff_tableFF\">","</table>");
		String [] array_price=div_price.split("name=\"FamilyButton\"");
		for(int n=1;n<array_price.length;n++){
			String str=StringUtils.substringBetween(array_price[n],"/>"," </td>").trim();
			if(!str.contains("from")){
				str=StringUtils.substringAfter(str, "&euro;");
				low_price+=str+"|";
			}
		}	
		// 截取页面中的json串
		String strJson=	org.apache.commons.lang.StringUtils.substringBetween(html, "new String('", "');");
		map=getFlightMap(strJson,low_price);
		
		List<RoundTripFlightInfo> flightList = new ArrayList<RoundTripFlightInfo>();
		for(Map.Entry<String, String> entry:map.entrySet()){
		String [] array_flight=	entry.getKey().split("\\|");
		    // 	String	detail_html = Files.toString(new File("E:\\001.html"),Charsets.UTF_8);
		    RoundTripFlightInfo baseFlight = new RoundTripFlightInfo();
			     List<FlightSegement> segs = new ArrayList<FlightSegement>();
			     FlightDetail flightDetail = new FlightDetail();
			     List<String> flightNoList = new ArrayList<String>();
			     String detail_html=getFlightDetail(html,array_flight[0],"");
			     String d_html=StringUtils.substringAfter(detail_html, "segment information") ;
			    	String[] results = d_html.split("segment");
                    for(int i=1;i<results.length;i++){ 
                    	FlightSegement seg = new FlightSegement();
			    	 //出发地
                    	String departure_div=StringUtils.substringBetween(results[i],"departure", " <br />");
                    	String departure=StringUtils.substringBetween(departure_div,"value=\"", "\"");
                    	seg.setDepairport(departure);
			    	 //到达地
                     	String arrival_div=StringUtils.substringBetween(results[i],"arrival", " <br />");
                    	String arrival=StringUtils.substringBetween(arrival_div,"value=\"", "\"");
                    	seg.setArrairport(arrival);
			    	 //出发时间
                    	String departure_date_div=StringUtils.substringBetween(results[i],"departure date", " <br />");
                    	String departure_date=StringUtils.substringBetween(departure_date_div,"value=\"", "\"");
                    	seg.setDepDate(departure_date.substring(0, 10));
                    	seg.setDeptime(departure_date.substring(11));
			    	 //到达时间
                    	String arrival_date_div=StringUtils.substringBetween(results[i],"arrival date", " <br />");
                    	String arrival_date=StringUtils.substringBetween(arrival_date_div,"value=\"", "\"");
                    	seg.setArrDate(arrival_date.substring(0, 10));
                    	seg.setArrtime(arrival_date.substring(11));
		
			    	 //航空公司编码
                    	String airline_Code_div=StringUtils.substringBetween(results[i],"airline Code", " <br />");
                    	String airline_Code=StringUtils.substringBetween(airline_Code_div,"value=\"", "\"");
                    	seg.setCompany(airline_Code);
           	    	 //航班号
                    	String flightNumber_div=StringUtils.substringBetween(results[i],"flightNumber", " <br />");
                    	String flightNumber=StringUtils.substringBetween(flightNumber_div,"value=\"", "\"");
                    	seg.setFlightno(airline_Code+flightNumber);
                    	flightNoList.add(airline_Code+flightNumber);
                    
                    	segs.add(seg);
                    }
    				flightDetail.setFlightno(flightNoList);
    				flightDetail.setMonetaryunit("EUR");
    				flightDetail.setPrice(Double.parseDouble(entry.getValue()));
    				flightDetail.setDepcity(param.getDep());
    				flightDetail.setArrcity(param.getArr());
    				flightDetail.setWrapperid(param.getWrapperid());
    				flightDetail.setDepdate(String2Date(param.getDepDate()));
    				
    			     String re_detail_html=getFlightDetail(html,array_flight[0],array_flight[1]);
    			     String re_d_html=StringUtils.substringAfter(re_detail_html, "segment information") ;
    			 	 String[] re_results = re_d_html.split("segment");
    			 	 List<FlightSegement> re_segs = new ArrayList<FlightSegement>();
    			 	 List<String> retflightno = new ArrayList<String>();
    		         for(int i=1;i<re_results.length;i++){ 
                       	FlightSegement re_seg = new FlightSegement();
   			    	 //出发地
                       	String departure_div=StringUtils.substringBetween(re_results[i],"departure", " <br />");
                       	String departure=StringUtils.substringBetween(departure_div,"value=\"", "\"");
                       	re_seg.setDepairport(departure);
   			    	 //到达地
                        	String arrival_div=StringUtils.substringBetween(re_results[i],"arrival", " <br />");
                       	String arrival=StringUtils.substringBetween(arrival_div,"value=\"", "\"");
                       	re_seg.setArrairport(arrival);
   			    	 //出发时间
                       	String departure_date_div=StringUtils.substringBetween(re_results[i],"departure date", " <br />");
                       	String departure_date=StringUtils.substringBetween(departure_date_div,"value=\"", "\"");
                       	re_seg.setDepDate(departure_date.substring(0, 10));
                       	re_seg.setDeptime(departure_date.substring(11));
   			    	 //到达时间
                       	String arrival_date_div=StringUtils.substringBetween(re_results[i],"arrival date", " <br />");
                       	String arrival_date=StringUtils.substringBetween(arrival_date_div,"value=\"", "\"");
                       	re_seg.setArrDate(arrival_date.substring(0, 10));
                       	re_seg.setArrtime(arrival_date.substring(11));
   		
   			    	 //航空公司编码
                       	String airline_Code_div=StringUtils.substringBetween(re_results[i],"airline Code", " <br />");
                       	String airline_Code=StringUtils.substringBetween(airline_Code_div,"value=\"", "\"");
                       	re_seg.setCompany(airline_Code);
              	    	 //航班号
                       	String flightNumber_div=StringUtils.substringBetween(re_results[i],"flightNumber", " <br />");
                       	String flightNumber=StringUtils.substringBetween(flightNumber_div,"value=\"", "\"");
                       	re_seg.setFlightno(airline_Code+flightNumber);
                       	re_segs.add(re_seg);
                       	retflightno.add(airline_Code+flightNumber);
                      }
    				
    				//baseFlight.setOutboundPrice(Math.round(Double.parseDouble(price.substring(1))));
					baseFlight.setRetinfo(re_segs);
					baseFlight.setRetdepdate(String2Date(param.getRetDate()));
					baseFlight.setRetflightno(retflightno);
				    //baseFlight.setReturnedPrice(Math.round(Double.parseDouble(returnedPrice.substring(1))));
    				baseFlight.setDetail(flightDetail);
    				baseFlight.setInfo(segs);
    				flightList.add(baseFlight);
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
	 * 遍历listRecos json对象 获取低价票的map(o_recosID|i_recosID,price)
	 * @param String strJson,String low_price
	 * @return Map<String,String>  
	 */
	public static  Map<String,String> getFlightMap(String strJson,String low_price){
		Map<String,String> map =new HashMap<String,String>();
		JSONObject object = JSON.parseObject(strJson);
		JSONObject listRecos = object.getJSONObject("listRecos");
		String[] recosIDs = object.getString("recosIDs").split("\\|");;
		//遍历listRecos json对象 获取低价票的map(recosID,price)
		for (String id : recosIDs) {
			if(!StringUtils.isBlank(id)){
				JSONObject obj = listRecos.getJSONObject(id);
				String price = obj.getJSONObject("price").getString("price");
				price=price.substring(6).replace(",", "");
				low_price=low_price.replace(",", "");
				if(low_price.contains(price)){
					String 	o_flightsId=obj.getString("outboundFlightIDs");
					String 	i_flightsId=obj.getString("inboundFlightIDs");
					if(!StringUtils.isBlank(o_flightsId)){
					String [] 	o_array_flight	=  o_flightsId.split("\\|");
					String [] 	i_array_flight	=  i_flightsId.split("\\|");
					for(String o_flight_id:o_array_flight){
						if(!StringUtils.isBlank(o_flight_id)){
						for(String i_flight_id:i_array_flight){
						if(!StringUtils.isBlank(i_flight_id)){
							//比较同一航班获取的价格 取最低价 map.put(往ID|去ID,price)
							
							if(map.keySet().toString().contains(o_flight_id+"|"+i_flight_id)){
						    	if(	Double.parseDouble(map.get(o_flight_id+"|"+i_flight_id).toString())>Double.parseDouble(price)){
							          map.put(o_flight_id+"|"+i_flight_id, price);
							    }
							}else{
						          map.put(o_flight_id+"|"+i_flight_id, price);
							}
					    }
						}	
					  }
					 }
					}
				}
			}
		}
		return map;
	}
	
	/**
	 * 根据json中的recosID：序号 获取航班detail info
	 * @param String html,String recosID
	 * @return String  
	 */
	public static String getFlightDetail(String html,String o_recosID,String i_recosID) {
		FlightSearchParam param =new FlightSearchParam();
		QFHttpClient httpClient = new QFHttpClient(param, false);
		StringBuffer detail_url=new StringBuffer("https://book.adria.si");
//https://book.adria.si/plnext/adriaNext/FlexPricerFlightDetailsPopUp.action;jsessionid=S6mVJ31-H0lBTHoYOl_sFykiVVPlQWdPnJh8C61UC4NSZ7_49sZx!-1204293131!-526982658?SITE=BAUQBAUQ&LANGUAGE=GB&PAGE_TICKET=0&TRIP_TYPE=R&PRICING_TYPE=I&DISPLAY_TYPE=1&ARRANGE_BY=N&FLIGHT_ID_1=&FLIGHT_ID_2=0
		String detail_from = StringUtils.substringBetween(html,
				 "name=\"FlightDetailsPopUpForm\"", "</form>").trim();
		String action= StringUtils.substringBetween(detail_from,"action=\"","method").trim();
		detail_url.append(action.substring(0, action.length()-1));
		detail_url.append("?");
		String params=StringUtils.substringAfter(detail_from,"class=\"transparentForm\">");
		String[] array_param=params.split("<");
        for(int i=1;i<array_param.length;i++){
        if(array_param[i].contains("value")){
        String name= StringUtils.substringBetween(array_param[i],"name=","value").trim();
        String value=StringUtils.substringBetween(array_param[i],"value=","/>").trim();
        detail_url.append(name.substring(1,name.length()-1)+"="+value.substring(1,value.length()-1)+"&");
        }else{
        	 String name= StringUtils.substringBetween(array_param[i],"name=","/>").trim();
        	 if(name.substring(1,name.length()-1).equals("FLIGHT_ID_1")){
        		 detail_url.append("FLIGHT_ID_1="+o_recosID+"&");
        	 }
        	 if(name.substring(1,name.length()-1).equals("FLIGHT_ID_2")){
        		 detail_url.append("FLIGHT_ID_2="+i_recosID+"&");
        	 } 
        }	
        }
        String getUrl=detail_url.toString().substring(0, detail_url.toString().length()-1);
        QFGetMethod  get1 = null;	
		try {	
			String cookie = StringUtils.join(httpClient.getState()
					.getCookies(), "; ");
			get1 = new QFGetMethod(getUrl);
			get1.setFollowRedirects(false);
			httpClient.getState().clearCookies();
			get1.addRequestHeader("Cookie", cookie);
			get1.addRequestHeader("X-Requested-With", "XMLHttpRequest");
			get1.addRequestHeader("Referer", getUrl);
			get1.addRequestHeader("Content-Type","text/html;charset=UTF-8"); 
		    int status = httpClient.executeMethod(get1);
		    return get1.getResponseBodyAsString();
		} catch (Exception e) {			
			e.printStackTrace();
		} finally{
			if (null != get1){
				get1.releaseConnection();
			}
		}	
		return "Exception";
	}
	
	
	/**
	 * String 转 Date
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
	/**
	 * EEE MMM dd HH:mm:ss zzz yyyy 转 "yyyy-MM-dd HH:MM
	 * @param strdate
	 * @return
	 */
         public static String  String2Date_yyyy_MM_dd(String str){
		Map map = new HashMap();
		map.put("Jan", "01");
		map.put("Feb", "02");
		map.put("Mar", "03");
		map.put("Apr", "04");
		map.put("May", "05");
		map.put("Jun", "06");
		map.put("Jul", "07");
		map.put("Aug", "08");
		map.put("Sept", "09");
		map.put("Oct", "10");
		map.put("Nov", "11");
		map.put("Dec", "12");
	    String [] str_array=str.split(" ");
	    String strDate=str_array[5]+"-"+map.get(str_array[1])+"-"+str_array[2]+" "+str_array[3].substring(0,5);
		return strDate;
	 }
}
