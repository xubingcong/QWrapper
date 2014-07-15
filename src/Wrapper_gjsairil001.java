import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.bean.search.OneWayFlightInfo;
import com.qunar.qfwrapper.bean.search.FlightDetail;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.RoundTripFlightInfo;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;
import com.qunar.qfwrapper.constants.Constants;

import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.NameValuePair;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Charsets;
import com.google.common.io.Files;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author http://montenegroairlines.com/en/home.html 
 * gjsairil001
 * LGW-DME 2014-7-18 2014-7-27
   DMG-COG 2014-7-22 2014-8-2
   FCO-BEG 2014-7-22 2014-8-3
 */
public class Wrapper_gjsairil001 implements QunarCrawler {

	private QFHttpClient httpClient = null;

	public static void main(String[] args) {

		FlightSearchParam searchParam = new FlightSearchParam();
		searchParam.setDep("LON");
		searchParam.setArr("DME");
		searchParam.setDepDate("2014-07-25");
		searchParam.setRetDate("2014-07-30");
		searchParam.setTimeOut("300000");
		searchParam.setToken("");

		String html = "";
		try {
			html = new Wrapper_gjsairil001().getHtml(searchParam);
			Files.write(html, new File("D:\\006.html"), Charsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		 String page="";
//		 try {
//		 page = Files.toString(new File("D:\\006.html"),Charsets.UTF_8);
//		 } catch (IOException e) {
//		 e.printStackTrace();
//		 }
		 ProcessResultInfo result = new ProcessResultInfo();
		 result = new Wrapper_gjsairil001().process(html,searchParam);
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

	public BookingResult getBookingInfo(FlightSearchParam arg0) {

		String bookingUrlPre = "http://montenegroairlines.com/en/home.html";
		BookingResult bookingResult = new BookingResult();
		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo.setAction(bookingUrlPre);
		bookingInfo.setMethod("get");
		bookingResult.setData(bookingInfo);
		bookingResult.setRet(true);
		return bookingResult;

	}

	public String getHtml(FlightSearchParam param) {
		QFPostMethod first_post = null;
		QFPostMethod second_post = null;
		QFPostMethod third_post = null;

		String url = "http://montenegroairlines.com/en/home/amadeus/booking.html";
		try {
			httpClient = new QFHttpClient(param, false);
			// 按照浏览器的模式来处理cookie
			httpClient.getParams().setCookiePolicy(
					CookiePolicy.BROWSER_COMPATIBILITY);
			String cookie = "";
			httpClient.getParams().setCookiePolicy(
					CookiePolicy.BROWSER_COMPATIBILITY);
			first_post = new QFPostMethod(url);
			// 设置第一次post提交表单数据
			NameValuePair[] pairs = new NameValuePair[] {
					new NameValuePair("todaysDay", todayDate()),
					new NameValuePair("fromCity", "LON"),
					new NameValuePair("startTimeMon", getDateFormart(param.getDepDate(),"yyyyMM")),
					new NameValuePair("startTimeDay", getDateFormart(param.getDepDate(),"dd")),
					new NameValuePair("startTimeCal", param.getDepDate()),
					new NameValuePair("toCity", "DME"),
					new NameValuePair("endTimeMon", getDateFormart(param.getRetDate(),"yyyyMM")),
					new NameValuePair("endTimeDay", getDateFormart(param.getRetDate(),"dd")),
					new NameValuePair("endTimeCal", param.getRetDate()),
					new NameValuePair("roundTrip", "on"),
					new NameValuePair("passengersADT", "1"),
					new NameValuePair("passengersYTH", "0"),
					new NameValuePair("passengersCHD", "0"),
					new NameValuePair("passengersINF", "0"),
					new NameValuePair("passengersYCD", "0"),
					new NameValuePair("amadeusPNR", ""),
					new NameValuePair("lastName", ""),
					new NameValuePair("currentLang", "en"),
					new NameValuePair("isBooking", "1"),
					new NameValuePair("isTimeTable", "0"),
					new NameValuePair("isCheckTrip", "0"),
					new NameValuePair("isHotels", "0"),
					new NameValuePair("isRentACar", "0"),
					new NameValuePair("bookNowTxt", "book flight"),
					new NameValuePair("timetableNowTxt", "timetable"),
					new NameValuePair("checktripNowTxt", "Trip Status"),
					new NameValuePair("lang", "en") };
			first_post.setRequestBody(pairs);
			// first_post.addRequestHeader("Cookie", cookie);
			int statuscode = httpClient.executeMethod(first_post);
			if (statuscode >= 400) {
				return "StatusError" + statuscode;
			}
			String postHtml = first_post.getResponseBodyAsString();
			String url_ = StringUtils.substringBetween(postHtml,
					"<form action=\"", "\" method").trim();
			String SITE = StringUtils.substringBetween(postHtml,
					"<textarea name=\"SITE\" style=\"display: none\" >",
					"</textarea>").trim();
			String LANGUAGE = StringUtils.substringBetween(postHtml,
					"<textarea name=\"LANGUAGE\" style=\"display: none\" >",
					"</textarea>");
			String TRANSACTION = StringUtils
					.substringBetween(
							postHtml,
							"<textarea name=\"EMBEDDED_TRANSACTION\" style=\"display: none\" >",
							"</textarea>").trim();
			String ENCT = StringUtils.substringBetween(postHtml,
					"<textarea name=\"ENCT\" style=\"display: none\" >",
					"</textarea>").trim();
			String ENC = StringUtils.substringBetween(postHtml,
					"<textarea name=\"ENC\" style=\"display: none\" >",
					"</textarea>").trim();

			try {

				second_post = new QFPostMethod(url_);
				// 设置第二次post提交表单数据
				NameValuePair[] pairs_ = new NameValuePair[] {
						new NameValuePair("SITE", SITE),
						new NameValuePair("LANGUAGE", LANGUAGE),
						new NameValuePair("EMBEDDED_TRANSACTION", TRANSACTION),
						new NameValuePair("ENCT", ENCT),
						new NameValuePair("ENC", ENC) };
				second_post.setRequestBody(pairs_);
				second_post.addRequestHeader("Host", "book.eu1.amadeus.com");
				cookie = StringUtils.join(httpClient.getState().getCookies(),
						"; ");
				// second_post.addRequestHeader("Cookie", cookie);
				second_post.addRequestHeader("Referer", url);
				int statusCode = httpClient.executeMethod(second_post);
				if (statusCode >= 400) {
					return "StatusError" + statusCode;
				}
				String html = second_post.getResponseBodyAsString();

				if(!html.contains("checked=\"checked\"")){
					Files.write(html, new File("D:\\006.html"), Charsets.UTF_8);
				    return 	"Today Flight is full";
				}
				
				StringBuffer detail_url = new StringBuffer(
						"http://book.eu1.amadeus.com");
				String detail_from = StringUtils.substringBetween(html,
						"form_select_far", "</form>").trim();
				String urlStr = StringUtils.substringBetween(detail_from,
						"action=\"", "\">");
				detail_url.append(urlStr);
				// http://book.eu1.amadeus.com/plnext/montenegroairlines/FlexPricerAvailabilityDispatcherPui.action;jsessionid=qrgy1fuxuRlxrMF9RMdOVyRpdfK5YS8PBmpWgq26NTniR9YKA8YI!-2116536595!1142692366
				String[] array_param = detail_from.split("<");

				List<String> nameArray = new ArrayList();
				List<String> valueArray = new ArrayList();
				for (int i = 1; i < array_param.length; i++) {
					if (array_param[i].contains("value")) {
						String name = StringUtils.substringBetween(
								array_param[i], "name=\"", "\"").trim();
						String value = StringUtils.substringBetween(
								array_param[i], "value=\"", "\"").trim();
						nameArray.add(name);
						valueArray.add(value);
					}
				}

				try {
					
					third_post = new QFPostMethod(detail_url.toString());
					// 设置第三次post提交表单数据
					NameValuePair[] third_pairs = new NameValuePair[] {
							new NameValuePair(nameArray.get(0),
									valueArray.get(0)),
							new NameValuePair(nameArray.get(1),
									valueArray.get(1)),
							new NameValuePair(nameArray.get(2),
									"0"),
							new NameValuePair(nameArray.get(3),
									"0"),
							new NameValuePair(nameArray.get(4),
									valueArray.get(4)),
							new NameValuePair(nameArray.get(5),
									valueArray.get(5)),
							new NameValuePair(nameArray.get(6),
									valueArray.get(6)),
							new NameValuePair(nameArray.get(7),
									valueArray.get(7)),
							new NameValuePair(nameArray.get(8),
									valueArray.get(8)),
							new NameValuePair(nameArray.get(9),
									valueArray.get(9)),
							new NameValuePair(nameArray.get(10),
									valueArray.get(10)),
							new NameValuePair(nameArray.get(11),
									valueArray.get(11)),
							new NameValuePair(nameArray.get(12),
									valueArray.get(12)),
							new NameValuePair(nameArray.get(13),
									valueArray.get(13)),
							new NameValuePair(nameArray.get(14),
									valueArray.get(14)),
							new NameValuePair(nameArray.get(15),
									valueArray.get(15)),
							new NameValuePair(nameArray.get(16),
									valueArray.get(16)),
							new NameValuePair(nameArray.get(17),
									valueArray.get(17)),
							new NameValuePair(nameArray.get(18),
									valueArray.get(18)),
							new NameValuePair(nameArray.get(19),
									valueArray.get(19)),
							new NameValuePair(nameArray.get(20),
									valueArray.get(20)),
							new NameValuePair(nameArray.get(21),
									valueArray.get(21)),
							new NameValuePair(nameArray.get(22),
									valueArray.get(22)),
							new NameValuePair(nameArray.get(23),
									valueArray.get(23)),
							new NameValuePair(nameArray.get(24),
									valueArray.get(24)),
							new NameValuePair(nameArray.get(25),
									valueArray.get(25)),
							new NameValuePair(nameArray.get(26),
									valueArray.get(26)),
							new NameValuePair(nameArray.get(27),
									valueArray.get(27)),
							new NameValuePair(nameArray.get(28),
									valueArray.get(28)),
							new NameValuePair(nameArray.get(29),
									valueArray.get(29)) };
					third_post.setRequestBody(third_pairs);
					cookie = StringUtils.join(httpClient.getState()
							.getCookies(), "; ");
					// cookie =
					// StringUtils.join(httpClient.getState().getCookies(),
					// "; ");
					//third_post.addRequestHeader("Cookie", cookie);
					third_post.addRequestHeader("Referer", url_);
					third_post.addRequestHeader("Content-Type",
							"application/x-www-form-urlencoded");
					third_post.addRequestHeader("Host", "book.eu1.amadeus.com");
//					HostConfiguration configuration = httpClient
//							.getHostConfiguration();
//					configuration.setProxy("127.0.0.1", 8888);
//					httpClient.setHostConfiguration(configuration);
					int status = httpClient.executeMethod(third_post);
					if (status >= 400) {
						return "StatusError" + status;
					}
					return third_post.getResponseBodyAsString();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (null != third_post) {
						third_post.releaseConnection();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (null != second_post) {
					second_post.releaseConnection();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != first_post) {
				first_post.releaseConnection();
			}
		}
		return "Exception";
	}

	public ProcessResultInfo process(String html, FlightSearchParam param) {


		/*
		 * ProcessResultInfo中，
		 * ret为true时，status可以为：SUCCESS(抓取到机票价格)|NO_RESULT(无结果，没有可卖的机票)
		 * ret为false时
		 * ，status可以为:CONNECTION_FAIL|INVALID_DATE|INVALID_AIRLINE|PARSING_FAIL
		 * |PARAM_ERROR
		 */
		ProcessResultInfo result = new ProcessResultInfo();
		if ("Exception".equals(html)) {
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;
		}
		// 需要有明显的提示语句，才能判断是否INVALID_DATE|INVALID_AIRLINE|NO_RESULT
		if (html.contains("Today Flight is full")) {
			result.setRet(false);
			result.setStatus(Constants.INVALID_DATE);
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
			    FlightDetail flightDetail = new FlightDetail();
				     List<FlightSegement> segs = new ArrayList<FlightSegement>();
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
	                    	String strDeparture_date=String2Date_yyyy_MM_dd(departure_date);
	                    	seg.setDepDate(strDeparture_date.substring(0, 10));
	                    	seg.setDeptime(strDeparture_date.substring(11));
				    	 //到达时间
	                      	String arrival_date_div=StringUtils.substringBetween(results[i],"arrival date", " <br />");
	                    	String arrival_date=StringUtils.substringBetween(arrival_date_div,"value=\"", "\"");
	                    	String strArrival_date=String2Date_yyyy_MM_dd(arrival_date);
	                    	seg.setArrDate(strArrival_date.substring(0, 10));
	                    	seg.setArrtime(strArrival_date.substring(11));

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
	    				
	    			     String re_detail_html=getFlightDetail(html,"",array_flight[1]);
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
	                       	String strDeparture_date1=String2Date_yyyy_MM_dd(departure_date);
	                       	re_seg.setDepDate(strDeparture_date1.substring(0, 10));
	                       	re_seg.setDeptime(strDeparture_date1.substring(11));
	   			    	 //到达时间
	                       	String arrival_date_div=StringUtils.substringBetween(re_results[i],"arrival date", " <br />");
	                       	String arrival_date=StringUtils.substringBetween(arrival_date_div,"value=\"", "\"");
	                       	String strArrival_date1=String2Date_yyyy_MM_dd(arrival_date);
	                       	re_seg.setArrDate(strArrival_date1.substring(0, 10));
	                       	re_seg.setArrtime(strArrival_date1.substring(11));
	   		
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
	 * 遍历listRecos json对象 获取低价票的map(recosID,price)
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
		StringBuffer detail_url=new StringBuffer("http://book.eu1.amadeus.com");
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
	 * 根据json中的recosID：序号 获取航班detail info
	 * @param String html,String recosID
	 * @return String  
	 */
	public static String getFlightDetail(String html,String recosID) {
		//http://book.eu1.amadeus.com/plnext/montenegroairlines/FlexPricerFlightDetailsPopUp.action;jsessionid=6TQzLY7NWSSoIjfQx65xt7OFiWpzra_32fw52JzpiuPedMMgK-Pq!1611192436!-850174526?SITE=BCZHBCZH&LANGUAGE=GB&PAGE_TICKET=1&TRIP_TYPE=O&PRICING_TYPE=I&DISPLAY_TYPE=1&ARRANGE_BY=&FLIGHT_ID_1=0&FLIGHT_ID_2=
		//http://book.eu1.amadeus.com/plnext/montenegroairlines/FlexPricerFlightDetailsPopUp.action;jsessionid=kfszmK2vPjHk6WNA7zFDih1NQft9At7IwieJ-E58a6wUVc8qb9Ko!-205717269!-2116536595?SITE=BCZHBCZH&LANGUAGE=GB&PAGE_TICKET=1&TRIP_TYPE=O&PRICING_TYPE=I&DISPLAY_TYPE=1&ARRANGE_BY=&FLIGHT_ID_1=0&FLIGHT_ID_2=
		FlightSearchParam param =new FlightSearchParam();
		QFHttpClient httpClient = new QFHttpClient(param, false);
		StringBuffer detail_url=new StringBuffer("http://book.eu1.amadeus.com");
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
        		 detail_url.append("FLIGHT_ID_1="+recosID+"&");
        	 }else{
        	 detail_url.append(name.substring(1,name.length()-1)+"="+"&");
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
		//	get1.addRequestHeader("Referer", getUrl);
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

	
	
	private static String getDateFormart(String str, String formart) {
		String[] array = str.split("-");
		String Year = array[0];
		String Month = array[1];
		String Day = array[2];
		char p = 'd';
		if (formart == "yyyy-MM") {
			p = 'b';
		} else if (formart == "yyyyMM") {
			p = 'c';
		} else if (formart == "dd") {
			p = 'd';
		} else if (formart == "yyyy-MM-dd") {
			p = 'a';
		}

		switch (p) {
		case 'a':
			return (Year + "-" + Month + "-" + Day);
		case 'b':
			return (Year + "-" + Month);
		case 'c':
			return (Year + Month);
		case 'd':
			return (Day);
		default:
			return (Year + "-" + Month + "-" + Day);
		}
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

	
	/**
	 * String 转 Date
	 * @param strdate
	 * @return
	 */
	
	private static String getDaysAfter(String str, int num) {
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date date;
		try {
			date = format.parse(str);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.DATE, num);

			return df.format(cal.getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
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
	
	public String   todayDate (){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		return df.format(new Date());
		}
}
