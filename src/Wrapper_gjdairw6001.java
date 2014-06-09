
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
import org.apache.commons.lang.StringUtils;

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
 * @author xubc
 *
 */                  
public class Wrapper_gjdairw6001 implements QunarCrawler{
	public static void main(String[] args) {
		/*
		 * 测试条件
		 * FCO-TSR 2014-08-14 BGY-PRG 2014-08-22 VKO-BUD 2014-08-30 
	     */
				
		Wrapper_gjdairw6001 instance = new Wrapper_gjdairw6001();
		
		
		FlightSearchParam p =new FlightSearchParam();
		p.setWrapperid("gjdairw6001");
		p.setDep("VKO");
		p.setArr("BUD");
		p.setDepDate("2014-06-30");
		p.setTimeOut("60000");
		String html=instance.getHtml(p);
//		System.out.println(html);
//		String page="";
//		try {
//			page = Files.toString(new File("E:\\Noname5.html"),Charsets.UTF_8);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
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
	public BookingResult getBookingInfo(FlightSearchParam param) {
		
		String bookingUrlPre = "http://wizzair.com/en-GB/Search";
		BookingResult bookingResult = new BookingResult();
		BookingInfo bookingInfo = new BookingInfo();
		bookingInfo.setAction(bookingUrlPre);
		bookingInfo.setMethod("post");
		Map<String, String> map = new LinkedHashMap<String, String>();
		map.put("__EVENTTARGET", "ControlGroupRibbonAnonHomeView_AvailabilitySearchInputRibbonAnonHomeView_ButtonSubmit");
		map.put("__VIEWSTATE", "/wEPDwUBMGQYAQUeX19Db250cm9sc1JlcXVpcmVQb3N0QmFja0tleV9fFgEFWkNvbnRyb2xHcm91cFJpYmJvbkFub25Ib21lVmlldyRBdmFpbGFiaWxpdHlTZWFyY2hJbnB1dFJpYmJvbkFub25Ib21lVmlldyRTdHVkZXRTZW5pb3JHcm91cLcMW6Bfdi6XQ3jIOh46M/Uyyf+xnV2YpSj4opm7Zf8k");
		map.put("ControlGroupRibbonAnonHomeView$AvailabilitySearchInputRibbonAnonHomeView$OriginStation", param.getDep());
		map.put("ControlGroupRibbonAnonHomeView$AvailabilitySearchInputRibbonAnonHomeView$DestinationStation", param.getArr());
		map.put("ControlGroupRibbonAnonHomeView$AvailabilitySearchInputRibbonAnonHomeView$DepartureDate",param.getDepDate().replaceAll("(....)-(..)-(..)", "$3/$2/$1"));
		map.put("ControlGroupRibbonAnonHomeView$AvailabilitySearchInputRibbonAnonHomeView$PaxCountADT", "1"	);
		map.put("ControlGroupRibbonAnonHomeView$AvailabilitySearchInputRibbonAnonHomeView$PaxCountCHD", "0");
		map.put("ControlGroupRibbonAnonHomeView$AvailabilitySearchInputRibbonAnonHomeView$PaxCountINFANT", "0");
		map.put("ControlGroupRibbonAnonHomeView$AvailabilitySearchInputRibbonAnonHomeView$ButtonSubmit", "Search");
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
	     	post=new QFPostMethod("http://wizzair.com/en-GB/Search");
	     	NameValuePair[] pairs = new NameValuePair[]{
			new NameValuePair("__EVENTTARGET", "ControlGroupRibbonAnonHomeView_AvailabilitySearchInputRibbonAnonHomeView_ButtonSubmit"),
		     new NameValuePair("__VIEWSTATE", "/wEPDwUBMGQYAQUeX19Db250cm9sc1JlcXVpcmVQb3N0QmFja0tleV9fFgEFWkNvbnRyb2xHcm91cFJpYmJvbkFub25Ib21lVmlldyRBdmFpbGFiaWxpdHlTZWFyY2hJbnB1dFJpYmJvbkFub25Ib21lVmlldyRTdHVkZXRTZW5pb3JHcm91cLcMW6Bfdi6XQ3jIOh46M/Uyyf+xnV2YpSj4opm7Zf8k"),
		     new NameValuePair("ControlGroupRibbonAnonHomeView$AvailabilitySearchInputRibbonAnonHomeView$OriginStation", param.getDep()),
		     new NameValuePair("ControlGroupRibbonAnonHomeView$AvailabilitySearchInputRibbonAnonHomeView$DestinationStation", param.getArr()),
		     new NameValuePair("ControlGroupRibbonAnonHomeView$AvailabilitySearchInputRibbonAnonHomeView$DepartureDate",param.getDepDate().replaceAll("(....)-(..)-(..)", "$3/$2/$1")),
		     new NameValuePair("ControlGroupRibbonAnonHomeView$AvailabilitySearchInputRibbonAnonHomeView$PaxCountADT", "1"	),
		     new NameValuePair("ControlGroupRibbonAnonHomeView$AvailabilitySearchInputRibbonAnonHomeView$PaxCountCHD", "0"),
		     new NameValuePair("ControlGroupRibbonAnonHomeView$AvailabilitySearchInputRibbonAnonHomeView$PaxCountINFANT", "0"),
		     new NameValuePair("ControlGroupRibbonAnonHomeView$AvailabilitySearchInputRibbonAnonHomeView$ButtonSubmit", "Search"),  
		  };
		     post.setRequestBody(pairs);
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
				String cookie = StringUtils.join(httpClient.getState()
						.getCookies(), "; ");
				get = new QFGetMethod(url);
				get.setFollowRedirects(false);
				httpClient.getState().clearCookies();
				get.addRequestHeader("Cookie", cookie);
				get.addRequestHeader("X-Requested-With", "XMLHttpRequest");
				get.addRequestHeader("Referer", url);
				get.addRequestHeader("Content-Type","text/html;charset=UTF-8"); 
				httpClient.executeMethod(get);
				return get.getResponseBodyAsString();
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
			List<OneWayFlightInfo> flightList = new ArrayList<OneWayFlightInfo>();
			String[] results = html
					.split("<p class=\" flight label  selectFlightTooltip \">");
			//从下来框中截取选中的币种编号
			String span_Currency = StringUtils.substringBetween(html,
					"WizzSummaryDisplaySelectViewRibbonSelectView$PaymentCurrencySelector", "</select>").trim();
			String span_CurrencyCode = StringUtils.substringBetween(span_Currency,
					"selected", "</option>").trim();
			String currencyCode = span_CurrencyCode.substring(span_CurrencyCode.length()-3, span_CurrencyCode.length());
			for (int i = 1; i < results.length; i++) {
				OneWayFlightInfo baseFlight = new OneWayFlightInfo();
				List<FlightSegement> segs = new ArrayList<FlightSegement>();
				FlightDetail flightDetail = new FlightDetail();
				List<String> flightNoList = new ArrayList<String>();
				String span = StringUtils
						.substringBetween(
								results[i],
								"name=\"ControlGroupRibbonSelectView$AvailabilityInputRibbonSelectView$Market1\"",
								"requiredError").trim();
				// 截取出航班信息
				String[] array = span.split("~");
				String code = array[7]+" "+ array[8];
				String depDate = array[12];
				String arrDate = array[14];
				// 截取出票价信息
				String span_price = StringUtils.substringBetween(results[i],
						"<span class=\"flight-fare-nowizzclub\">", "</span>")
						.trim();
				String price = StringUtils.substringAfter(span_price,
						"<span class=\"price\">").trim();

				FlightSegement seg = new FlightSegement();
				flightNoList.add(code.substring(2));
				seg.setFlightno(code.substring(2));
				seg.setDepDate(depDate.substring(0, 10).replaceAll("(..)/(..)/(....)", "$3-$1-$2"));
			    seg.setArrDate(arrDate.substring(0, 10).replaceAll("(..)/(..)/(....)", "$3-$1-$2"));
				seg.setCompany(array[7]);
			    seg.setDepairport(array[11]);
				seg.setArrairport(array[13]);
				seg.setDeptime(depDate.substring(11));
				seg.setArrtime(arrDate.substring(11));
				segs.add(seg);
				flightDetail.setFlightno(flightNoList);
				flightDetail.setMonetaryunit(currencyCode);
				flightDetail.setPrice(Math.round(Double.parseDouble(price.substring(1))));
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
