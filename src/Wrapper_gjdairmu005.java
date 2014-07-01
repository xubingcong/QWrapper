
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

/**
 * 单程请求
 * 中国东方航空，gjdairmu005
 * http://ca.ceair.com/
 * @author xubc
 *
 */                  
public class Wrapper_gjdairmu005 implements QunarCrawler{
     private  QFHttpClient httpClient = null;
  //   private  Map map=null;
 	
 	public static void main(String[] args) {
 		/*
 		 *   gjdairmu005、gjsairmu005
 		 *   测试条件 svo-tlv  lca-lon
 	     */
 				
 		Wrapper_gjdairmu005 instance = new Wrapper_gjdairmu005();
 		
 		
 		FlightSearchParam p =new FlightSearchParam();
 		p.setWrapperid("gjdairmu005");
 		p.setDep("PEK");
 		p.setArr("PVG");
 		p.setDepDate("2014-07-11");
 		p.setTimeOut("120000");
 		String html="";
 		try {
 			 html=instance.getHtml(p);
 			// Map cityHtml=instance.getCity();
 			Files.write(html, new File("E:\\007.html"),Charsets.UTF_8);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
// 		String page="";
// 		try {
// 			page = Files.toString(new File("E:\\006.html"),Charsets.UTF_8);
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
		
	
		String bookingUrlPre = "http://ca.ceair.com/muovc/front/reservation/flight-search!doFlightSearch.shtml";
		BookingResult bookingResult = new BookingResult();
		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo.setAction(bookingUrlPre);
		bookingInfo.setMethod("get");
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("cond.tripType", "OW");
		map.put("cond.depCode", arg0.getDep());
		map.put("cond.arrCode", arg0.getArr());
		map.put("cond.routeType", "1");
		map.put("depDate", arg0.getDepDate());
		map.put("depRtDate", "");
		map.put("submit", "Book+Now");
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
			//               http://ca.ceair.com/muovc/front/reservation/flight-search!doFlightSearch.shtml?cond.tripType=OW&cond.depCode=PEK&cond.arrCode=CDG&cond.routeType=1&depDate=2014-07-11&depRtDate=&submit=Book+Now  
					.format("http://ca.ceair.com/muovc/front/reservation/flight-search!doFlightSearch.shtml?cond.tripType=OW&cond.depCode=%s&cond.arrCode=%s&cond.routeType=1&depDate=%s&depRtDate=&submit=Book+Now", param.getDep(), param.getArr(),param.getDepDate());
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
		if(html.contains("No Flights Available")){
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;		
		}
		if(html.contains(" We apologize that there ")){
			result.setRet(false);
			result.setStatus(Constants.CONNECTION_FAIL);
			return result;		
		}
		try {
			List<OneWayFlightInfo> flightList = new ArrayList<OneWayFlightInfo>();
			Map monMap =getMonth();
			//	Map cityMap=getCity(Files.toString(new File("E:\\007.html"),Charsets.UTF_8));
			Map cityMap=getCity();
			String[] results = html.split("<tbody>");
			for(int j=1;j<results.length;j++){
				OneWayFlightInfo baseFlight = new OneWayFlightInfo();
				List<FlightSegement> segs = new ArrayList<FlightSegement>();
				FlightDetail flightDetail = new FlightDetail();
				List<String> flightNoList = new ArrayList<String>();
			     String[] FlightDiv = results[j].split("<tr class=\"booking\">");
			     String totalPrice="";
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
		    	flightDetail.setPrice(Double.parseDouble(totalPrice));
		    	flightDetail.setMonetaryunit("USD");
		    	flightDetail.setFlightno(flightNoList);
		    	flightDetail.setDepcity(param.getDep());
		    	flightDetail.setArrcity(param.getArr());
		    	flightDetail.setWrapperid(param.getWrapperid());
		    	flightDetail.setDepdate(String2Date(param.getDepDate()));
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
