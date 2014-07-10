
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
import java.util.Map;

import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.lang.StringUtils;

import com.alibaba.fastjson.JSON;
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
 * 往返请求
 * 赞比亚先锋航空
 * http://flyzambia.com/  gjdairpo001,gjsairpo001
 * @author xubc
 *
 */                  
public class Wrapper_gjsairpo001 implements QunarCrawler{
	public static void main(String[] args) {
		/*
		 * 测试条件 gjsairpo001
	     */
				
		Wrapper_gjsairpo001 instance = new Wrapper_gjsairpo001();
		
		FlightSearchParam p =new FlightSearchParam();
		p.setWrapperid("gjdairpo001");
		p.setDep("LUN");
		p.setArr("CIP");
		p.setDepDate("2014-07-18");
		p.setRetDate("2014-07-22");
		p.setTimeOut("60000");
		String html=instance.getHtml(p);
//		{
//			try {
//			
//				Files.write(html, new File("E:\\006.html"),Charsets.UTF_8);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		String page="";
//		try {
//			page = Files.toString(new File("E:\\006.html"),Charsets.UTF_8);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
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
	public BookingResult getBookingInfo(FlightSearchParam param) {
		
		String bookingUrlPre = "http://flyzambia.com/";
		BookingResult bookingResult = new BookingResult();
		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo.setAction(bookingUrlPre);
		bookingInfo.setMethod("get");
		bookingResult.setData(bookingInfo);
		bookingResult.setRet(true);
		return bookingResult;
	}

	@Override
	public String getHtml(FlightSearchParam arg0) {
		QFHttpClient httpClient = new QFHttpClient(arg0, false);
		QFGetMethod get = null;
		QFPostMethod post = null;
		QFPostMethod post1 = null;

		// 对于需要cookie的网站，请自己处理cookie（必须）
		httpClient.getParams().setCookiePolicy(
				CookiePolicy.BROWSER_COMPATIBILITY);
		String getUrl = String
				.format("http://customer3.videcom.com/ProflightZambia/VARS/Public/CustomerPanels/Requirements.aspx?currency=USD");
		try {
			//第一次请求 获取varsSessionID
			get = new QFGetMethod(getUrl);
			int status = httpClient.executeMethod(get);
			if (status >= 400) {
				return "StatusError" + status;
			}
			String cookies = StringUtils.join(httpClient.getState()
					.getCookies(), "; ");
			String firstResponse = get.getResponseBodyAsString();
			String varsSessionID = StringUtils.substringBetween(firstResponse,
					"name=\"VarsSessionID\" value=\"", "\" class=\"\" />")
					.trim();
			String secUrl = String
					.format("http://customer3.videcom.com/ProflightZambia/VARS/Public/WebServices/AvailabilityWS.asmx/GetFlightAvailability?VarsSessionID="
							+ varsSessionID);
			try {
				//第二次请求 提交json参数并获取下次请求的url
				post = new QFPostMethod(secUrl);
				JSONObject o = new JSONObject();
				JSONObject FormData = new JSONObject();
				JSONArray Origin = new JSONArray();
				Origin.add(arg0.getDep());
				JSONArray Destination = new JSONArray();
				Destination.add(arg0.getArr());
				FormData.put("VarsSessionID", varsSessionID);
				JSONArray DepartureDate = new JSONArray();
				DepartureDate.add(getStringDate(arg0.getDepDate()));
				FormData.put("Origin", Origin);
				FormData.put("Destination", Destination);
				FormData.put("DepartureDate", DepartureDate);
				JSONArray ReturnDate = new JSONArray();
			     ReturnDate.add(arg0.getRetDate());
			    FormData.put("ReturnDate", ReturnDate);
				FormData.put("Adults", "1");
				FormData.put("Children", "0");
				FormData.put("Seniors", "0");
				FormData.put("Students", "0");
				FormData.put("Infants", "0");
				FormData.put("Youths", "0");
				FormData.put("Teachers", "0");
				FormData.put("SeatedInfants", "0");
				FormData.put("EVoucher", "");
				FormData.put("SearchUser", "PUBLIC");
				o.put("FormData", FormData);
				o.put("IsMMBChangeFlightMode", false);
				System.out.println(o.toJSONString());
				post.setRequestBody(o.toJSONString());
				httpClient.getState().clearCookies();
				post.addRequestHeader("Cookie", cookies);
				post.addRequestHeader("X-Requested-With", "XMLHttpRequest");
				post.addRequestHeader("Referer", getUrl);
				post.addRequestHeader("Content-Type",
						"application/json; charset=utf-8");
				status = httpClient.executeMethod(post);
				if (status >= 400) {
					return "StatusError" + status;
				}
				String jsonString = post.getResponseBodyAsString();
				JSONObject object = JSON.parseObject(jsonString);
				JSONObject d = object.getJSONObject("d");
				String data = d.get("Data").toString();
				String NextURL = d.get("NextURL").toString();
				try {
					//第三次请求 获取html
					post1 = new QFPostMethod(NextURL);
					NameValuePair[] pairs = new NameValuePair[] { new NameValuePair(
							"VarsSessionID", data), };
					post1.setRequestBody(pairs);
					post1.addRequestHeader("Cookie", cookies);
					post1.addRequestHeader("Referer", getUrl);
					post1.addRequestHeader("Content-Type",
							"application/x-www-form-urlencoded");
					status = httpClient.executeMethod(post1);
					String html = post1.getResponseBodyAsString();
					return html;
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (null != post1) {
						post1.releaseConnection();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (null != post) {
					post.releaseConnection();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != get) {
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
		if(html.contains("No flights available on requested date")){
			result.setRet(false);
			result.setStatus(Constants.NO_RESULT);
			return result;		
		}
		try {
			List<RoundTripFlightInfo> flightList = new ArrayList<RoundTripFlightInfo>();
			String fiightTable=StringUtils.substringBetween(html,"<table class=\"FlightAvailabilityTable\">","</table");
			String Inbound_context=StringUtils.substringAfter(html, "Inbound:");
			String reFiightTable=StringUtils.substringBetween(Inbound_context,"<table class=\"FlightAvailabilityTable\">","</table");
			String[] results = fiightTable
					.split("<tr");
			String[] ret_results = reFiightTable
			.split("<tr");
			//从下来框中截取选中的币种编号
			String year = param.getDepDate().substring(0, 4);
			Map monMap =getMonth();
			for (int i = 1; i < results.length; i++) {
				if(results[i].contains("FltRequestedDate")){
				List<FlightSegement> segs = new ArrayList<FlightSegement>();
				List<String> flightNoList = new ArrayList<String>();
			    String [] arrayTd=  results[i].split("<td");
				FlightSegement seg = new FlightSegement();
			    seg.setDepairport(param.getDep());
				seg.setArrairport(param.getArr());
				//获取起飞日期和时间
				String depTime=StringUtils.substringBetween(arrayTd[2], "class=\"time\">", "</span>");
				String depDate=StringUtils.substringBetween(arrayTd[2], "class=\"flightDate\">", "</span>");
				seg.setDeptime(depTime);
				String[] depaArray = depDate.split(" ");
				String depMon = monMap.get(depaArray[1]).toString();
				seg.setDepDate(year + "-" + depMon + "-" + depaArray[0]);
				//获取到达日期和时间
				String arrTime=StringUtils.substringBetween(arrayTd[4], "class=\"time\">", "</span>");
				String arrDate=StringUtils.substringBetween(arrayTd[4], "class=\"flightDate\">", "</span>");
				String[] arrArray = arrDate.split(" ");
				String arrMon = monMap.get(arrArray[1]).toString();
				seg.setArrtime(arrTime);
				seg.setArrDate(year + "-" + arrMon + "-" + arrArray[0]);
				//获取航班号
				String flightNO=StringUtils.substringBetween(arrayTd[6], "class=\"FlightColumn\">", "<br").replace("\r\n","");
				seg.setCompany(flightNO.substring(0, 2));
				seg.setFlightno(flightNO);
				flightNoList.add(flightNO);
				segs.add(seg);

				String price ="";
				for(int k=7;k<arrayTd.length;k++){
					if(arrayTd[k].contains("$")){
						price=StringUtils.substringBetween(arrayTd[k], "$", "</span>");
						break;
					}
				}
				if("".endsWith(price)){
					continue;
				}

				
				for (int j = 1; j < ret_results.length; j++) {
					if(ret_results[j].contains("FltRequestedDate")){
				//截取返回航班信息
					List<FlightSegement> re_segs = new ArrayList<FlightSegement>();
					String [] re_arrayTd=  ret_results[i].split("<td");
					FlightSegement re_seg = new FlightSegement();
					re_seg.setDepairport(param.getArr());
					re_seg.setArrairport(param.getDep());
					//获取起飞日期和时间
					String re_depTime=StringUtils.substringBetween(re_arrayTd[2], "class=\"time\">", "</span>");
					String re_depDate=StringUtils.substringBetween(re_arrayTd[2], "class=\"flightDate\">", "</span>");
					re_seg.setDeptime(re_depTime);
					String[] re_depaArray = re_depDate.split(" ");
					String re_depMon = monMap.get(re_depaArray[1]).toString();
					re_seg.setDepDate(year + "-" + re_depMon + "-" + re_depaArray[0]);
					//获取到达日期和时间
					String re_arrTime=StringUtils.substringBetween(re_arrayTd[4], "class=\"time\">", "</span>");
					String re_arrDate=StringUtils.substringBetween(re_arrayTd[4], "class=\"flightDate\">", "</span>");
					String[] re_arrArray = re_arrDate.split(" ");
					String re_arrMon = monMap.get(re_arrArray[1]).toString();
					re_seg.setArrtime(re_arrTime);
					re_seg.setArrDate(year + "-" + re_arrMon + "-" + re_arrArray[0]);
					//获取航班号
					String re_flightNO=StringUtils.substringBetween(re_arrayTd[6], "class=\"FlightColumn\">", "<br").replace("\r\n","");
					re_seg.setCompany(re_flightNO.substring(0, 2));
					re_seg.setFlightno(re_flightNO);
					List<String> retflightno =new ArrayList<String>();
					retflightno.add(re_flightNO);
					re_segs.add(re_seg);
					String re_price="";
					for(int n=7;n<re_arrayTd.length;n++){
						if(re_arrayTd[n].contains("$")){
							re_price=StringUtils.substringBetween(re_arrayTd[n], "$", "</span>");
							break;
						}
					}
					if("".endsWith(re_price)){
						continue;
					}
					RoundTripFlightInfo baseFlight = new RoundTripFlightInfo();
					FlightDetail flightDetail = new FlightDetail();
					flightDetail.setFlightno(flightNoList);
					flightDetail.setMonetaryunit("USD");
					flightDetail.setDepcity(param.getDep());
					flightDetail.setArrcity(param.getArr());
					flightDetail.setWrapperid(param.getWrapperid());
					flightDetail.setDepdate(String2Date(param.getDepDate()));
					baseFlight.setInfo(segs);
					flightDetail.setPrice(Double.parseDouble(price)+Double.parseDouble(re_price));
					baseFlight.setDetail(flightDetail);
					baseFlight.setOutboundPrice(Double.parseDouble(price));
					baseFlight.setRetinfo(re_segs);
					baseFlight.setRetdepdate(String2Date(param.getRetDate()));
					baseFlight.setRetflightno(retflightno);
				    baseFlight.setReturnedPrice(Double.parseDouble(re_price));
				    flightList.add(baseFlight);
			       }
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
	
	public Map getMonth() {
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
		return map;
	}
	
	public String  getStringDate(String date){
		Map map = new HashMap();
		map.put( "01","Jan");
		map.put( "02","Feb");
		map.put( "03","Mar");
		map.put("04","Apr");
		map.put( "05","May");
		map.put( "06","Jun");
		map.put( "07","Jul");
		map.put("08","Aug");
		map.put( "09","Sept");
		map.put( "10","Oct");
		map.put( "11","Nov");
		map.put( "12","Dec");
		  String str1= date.substring(5, 7);
		  date=  date.replace(str1, map.get(str1).toString());
	      String [] array=date.split("-");
	      
		  String str=array[2]+"-"+array[1]+"-"+array[0];
		  return str;
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
}
