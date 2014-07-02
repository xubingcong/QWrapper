
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

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.lang.StringUtils;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.qunar.qfwrapper.bean.booking.BookingInfo;
import com.qunar.qfwrapper.bean.booking.BookingResult;
import com.qunar.qfwrapper.bean.search.FlightDetail;
import com.qunar.qfwrapper.bean.search.FlightSearchParam;
import com.qunar.qfwrapper.bean.search.FlightSegement;
import com.qunar.qfwrapper.bean.search.ProcessResultInfo;
import com.qunar.qfwrapper.bean.search.RoundTripFlightInfo;
import com.qunar.qfwrapper.constants.Constants;
import com.qunar.qfwrapper.interfaces.QunarCrawler;
import com.qunar.qfwrapper.util.QFGetMethod;
import com.qunar.qfwrapper.util.QFHttpClient;
import com.qunar.qfwrapper.util.QFPostMethod;

/**
 * 往返请求
 * 中国东方航空，gjsairmu005
 * http://ca.ceair.com/
 * @author xubc
 *
 */ 
public class Wrapper_gjsairmu005 implements QunarCrawler{
	   private  QFHttpClient httpClient = null;
	public static void main(String[] args) {
		/*
		 *测试条件
		 */
				
		Wrapper_gjsairmu005 instance = new Wrapper_gjsairmu005();
		
		FlightSearchParam p =new FlightSearchParam();
		p.setWrapperid("gjsairmu005");
		p.setDep("PEK");
		p.setArr("GMP");
		p.setDepDate("2014-07-15");
		p.setRetDate("2014-08-27");
		p.setTimeOut("120000");
		String html=instance.getHtml(p);
	//	System.out.println(html);
		String page="";
		try {
			Files.write(html, new File("E:\\006.html"),Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		
	
		String bookingUrlPre = "http://ca.ceair.com/muovc/front/reservation/flight-search!doFlightSearch.shtml";
		BookingResult bookingResult = new BookingResult();
		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo.setAction(bookingUrlPre);
		bookingInfo.setMethod("get");
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("cond.tripType", "RT");
		map.put("cond.depCode", arg0.getDep());
		map.put("cond.arrCode", arg0.getArr());
		map.put("cond.routeType", "1");
		map.put("depDate", arg0.getDepDate());
		map.put("depRtDate", arg0.getRetDate());
		map.put("depRtDate", "");
	//	map.put("submit", "Book+Now");
		bookingInfo.setInputs(map);		
		bookingResult.setData(bookingInfo);
		bookingResult.setRet(true);
		return bookingResult;
	}

	@Override
	public String getHtml(FlightSearchParam param) {
		QFGetMethod get = null;	
		try {	
		 httpClient = new QFHttpClient(param, false);
			String getUrl = String
		                 //	http://ca.ceair.com/muovc/front/reservation/flight-search!doFlightSearch.shtml?cond.tripType=RT&cond.depCode=PEK&cond.arrCode=GMP&cond.routeType=3&depDate=2014-07-15&depRtDate=2014-08-27&submit=Book+Now
					.format("http://ca.ceair.com/muovc/front/reservation/flight-search!doFlightSearch.shtml?cond.tripType=RT&cond.depCode=%s&cond.arrCode=%s&cond.routeType=1&depDate=%s&depRtDate=%s&submit=Book+Now",param.getDep(), param.getArr(),param.getDepDate(),param.getRetDate());
			get = new QFGetMethod(getUrl);
			get.getParams().setContentCharset("utf-8");
			int status = httpClient.executeMethod(get);
			return get.getResponseBodyAsString();
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
		if(html.contains(" We apologize that there")){
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;		
		}
		try {
			List<RoundTripFlightInfo> flightList = new ArrayList<RoundTripFlightInfo>();

			//截取"往"的航班div
			String dep_div=StringUtils.substringBetween(html,"class=\"flight_table rt_go\">","class=\"flight_table rt_back\"");
			//截取"返"的航班div
			String ret_div=StringUtils.substringBetween(html, "class=\"flight_table rt_back\"","class=\"flight_table rt_back\"");
			
			Map monMap =getMonth();
			//	Map cityMap=getCity(Files.toString(new File("E:\\007.html"),Charsets.UTF_8));
			Map cityMap=getCity();
			
			String[] results =dep_div.split("<tbody>");
			String[] ret_results = ret_div.split("<tbody>");
			for (int j = 1; j < results.length; j++) {
				RoundTripFlightInfo baseFlight = new RoundTripFlightInfo();
				List<FlightSegement> segs = new ArrayList<FlightSegement>();
				FlightDetail flightDetail = new FlightDetail();
				List<String> flightNoList = new ArrayList<String>();
				 String[] FlightDiv = results[j].split("<tr class=\"booking\">");
				 String totalPrice="";
				 String reTotalPrice="";
				 for(int i=1;i<FlightDiv.length;i++){
		    		    String cityDiv=StringUtils.substringAfter(FlightDiv[i],"区分是飞机还是高铁的图标 -->");
			    		 String[] array_td = cityDiv.split("<td");
		    			// 票价
					    if(i==1)
					    totalPrice=StringUtils.substringBetween(array_td[4],"\"price\">","</span>").replace(",", "");;
						FlightSegement seg=new FlightSegement();
						// 获取起飞时间
						String year = param.getDepDate().substring(0, 4);
						String depDate = StringUtils.substringBetween(FlightDiv[i],
							"icon_time_blue.gif\" />", "</td>").trim();
						String depDay = depDate.substring(6);
						String[] depaArray = depDay.split("\\.");
						String depMon = monMap.get(depaArray[0]).toString();
						seg.setDepDate(year + "-" + depMon + "-" + depaArray[1]);
						seg.setDeptime(depDate.substring(0, 5));
					    // 获取到达时间
						String arrDate = StringUtils.substringBetween(FlightDiv[i],
							"/icon_time_red.gif\" />", "</td>").trim();
						String arrDay = depDate.substring(6);
						String[] arrArray = arrDay.split("\\.");
						String arrMon = monMap.get(arrArray[0]).toString();
						seg.setArrDate(year + "-" + arrMon + "-" + arrArray[1]);
						seg.setArrtime(arrDate.substring(0, 5));
						//获取航班号
						String flightNo=StringUtils.substringBetween(array_td[0],"/>","</td>").trim();
						seg.setFlightno(flightNo);
						flightNoList.add(flightNo);
					    //获取航空公司编号
						String company=flightNo.substring(0, 2);
					    seg.setCompany(company);
					    //获取起飞城市
					    String depairport=StringUtils.substringBetween(array_td[1],">","</td>").trim();
					    seg.setDepairport(cityMap.get(depairport).toString());
					    //获取到达城市
					    String arrairport=StringUtils.substringBetween(array_td[2],">","</td>").trim();
					    seg.setArrairport(cityMap.get(arrairport).toString());
					    segs.add(seg);
				 }
					flightDetail.setFlightno(flightNoList);
					flightDetail.setMonetaryunit("USD");
					flightDetail.setDepcity(param.getDep());
					flightDetail.setArrcity(param.getArr());
					flightDetail.setWrapperid(param.getWrapperid());
					flightDetail.setDepdate(String2Date(param.getDepDate()));
				
				for (int k = 1; k < ret_results.length; k++) {
				//截取返回航班信息
					List<FlightSegement> re_segs = new ArrayList<FlightSegement>();
					List<String> retflightno =new ArrayList<String>();
					 String[] reFlightDiv = ret_results[k].split("<tr class=\"booking\">");
					 for(int n=1;n<reFlightDiv.length;n++){
					  FlightSegement re_seg=new FlightSegement();
					  String cityDiv=StringUtils.substringAfter(reFlightDiv[n],"区分是飞机还是高铁的图标 -->");
			    		 String[] array_td = cityDiv.split("<td");
		    			// 票价
					    if(n==1)
					    reTotalPrice=StringUtils.substringBetween(array_td[4],"\"price\">","</span>").replace(",", "");;
						FlightSegement seg=new FlightSegement();
						// 获取起飞时间
						String year = param.getDepDate().substring(0, 4);
						String depDate = StringUtils.substringBetween(reFlightDiv[n],
							"icon_time_blue.gif\" />", "</td>").trim();
						String depDay = depDate.substring(6);
						String[] depaArray = depDay.split("\\.");
						String depMon = monMap.get(depaArray[0]).toString();
						seg.setDepDate(year + "-" + depMon + "-" + depaArray[1]);
						seg.setDeptime(depDate.substring(0, 5));
					    // 获取到达时间
						String arrDate = StringUtils.substringBetween(reFlightDiv[n],
							"/icon_time_red.gif\" />", "</td>").trim();
						String arrDay = depDate.substring(6);
						String[] arrArray = arrDay.split("\\.");
						String arrMon = monMap.get(arrArray[0]).toString();
						seg.setArrDate(year + "-" + arrMon + "-" + arrArray[1]);
						seg.setArrtime(arrDate.substring(0, 5));
						//获取航班号
						String flightNo=StringUtils.substringBetween(array_td[0],"/>","</td>").trim();
						seg.setFlightno(flightNo);
						retflightno.add(flightNo);
					    //获取航空公司编号
						String company=flightNo.substring(0, 2);
					    seg.setCompany(company);
					    //获取起飞城市
					    String depairport=StringUtils.substringBetween(array_td[1],">","</td>").trim();
					    seg.setDepairport(cityMap.get(depairport).toString());
					    //获取到达城市
					    String arrairport=StringUtils.substringBetween(array_td[2],">","</td>").trim();
					    seg.setArrairport(cityMap.get(arrairport).toString());
					    re_segs.add(seg);
					  
					 }
					baseFlight.setInfo(segs);
					flightDetail.setPrice(Double.parseDouble(totalPrice)+Double.parseDouble(reTotalPrice));
					baseFlight.setDetail(flightDetail);
					baseFlight.setOutboundPrice(Double.parseDouble(totalPrice));
					baseFlight.setRetinfo(re_segs);
					baseFlight.setRetdepdate(String2Date(param.getRetDate()));
					baseFlight.setRetflightno(retflightno);
				    baseFlight.setReturnedPrice(Double.parseDouble(reTotalPrice));
				    flightList.add(baseFlight);
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
	 * 二次请求获取机场三字码
	 * 
	 * @param radio_id
	 * @return
	 */
	public String resquestCity() {
		QFGetMethod get = null;	
		try {	
			String getUrl = String
					.format("http://ca.ceair.com/muovc/resource/en_CA/js/city.js");
			get = new QFGetMethod(getUrl);
			get.getParams().setContentCharset("utf-8");
			int status = httpClient.executeMethod(get);
			return get.getResponseBodyAsString();
		} catch (Exception e) {			
			e.printStackTrace();
		} finally{
			if (null != get){
				get.releaseConnection();
			}
		}
		return "Exception";
	}

	public Map getCity() {

		String html=resquestCity();
		Map map =new HashMap();
		String[] results_html =StringUtils.substringAfter(html,"var _cityData =").replace("+\n", "").split(";\"+");
		for(int i=0;i<results_html.length-1;i++){
		        String[] array_str=	results_html[i].trim().split("\\|");
		        String[] array=array_str[0].split(":");
				map.put(array[1], array[0].replace("\"", ""));
		}
		return map;
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
