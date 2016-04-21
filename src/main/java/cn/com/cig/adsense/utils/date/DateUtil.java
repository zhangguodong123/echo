package cn.com.cig.adsense.utils.date;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.vo.DateEnum;

public class DateUtil {
	private static Logger logger = LoggerFactory.getLogger(DateUtil.class);
	
	public static long getFormatDateMillis(DateEnum type,Date date){
		Date millisDate = textFormatDate(type,dateFormatText(type,date));
		return millisDate.getTime();
	}
	
	/**
	 * descrption:String→Date
	 * @param type
	 * @param date
	 * @return
	 */
	public static Date textFormatDate(DateEnum type,String date){
		SimpleDateFormat sft=new SimpleDateFormat(type.value());
		try {
			return sft.parse(date);
		} catch (ParseException e) {
			logger.error("error:"+e.getMessage(),e);
		}
		return null;
	}
	/**
	 * descrption:Date→String
	 * @param type
	 * @return
	 */
	public static String dateFormatText(DateEnum type,Date date){
		SimpleDateFormat sft=new SimpleDateFormat(type.value());
		return sft.format(date);
	}
	
	/**
	 * date 必须符合【yyyy-MM-dd HH:mm】
	 * @param date
	 * @return
	 */
	public static String[] parseTextDateToArray(String date){
		StringTokenizer stk=new StringTokenizer(date," ");
		String day = stk.nextToken();
		String[] hm = stk.nextToken().split(":");
		String hour=hm[0];
		String minute=hm[1];
		return new String[]{day,hour,minute};
	}
	
	/**
	 * 获取一天当中的小时时间
	 * @return
	 */
	public static int getHourseOfDay(){
		SimpleDateFormat simp=new SimpleDateFormat(DateEnum.FORMAT_DATE_2.value());
		String format = simp.format(new Date());
		String hourse = format.split(" ")[1].split(":")[0];
		return Integer.parseInt(hourse);
	}
}
