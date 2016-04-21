package cn.com.cig.adsense.utils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import cn.com.cig.adsense.vo.fix.Size;

public class Constant {
	
	public static String SERVER_ID = "11968080";
	
	public static String MQ_KEY = "kestrel.echo.impression.queue";
	public static final String DB_DRIVER = "com.mysql.jdbc.Driver";
	public static String DB_CONNECTION = "jdbc:mysql://115.28.240.244:3306/cig_adsense";//2014-12-30 搬到日航重新架设数据库
	public static String DB_USER = "root";
	public static String DB_PASSWORD = "yiche2015gogogo";
	public static String BITA_DB_CONNECTION = "jdbc:mysql://115.28.240.244:3306/cigdc_ad_sys";//2014-12-30 搬到日航重新架设数据库
	public static String BITA_DB_USER = "root";
	public static String BITA_DB_PASSWORD = "yiche2015gogogo";//2014-12-30 搬到日航重新架设数据库
	
	//易车购车页接口曝光检测
	public static String BITA_DB_STATISTICS_CONNECTION = "jdbc:mysql://192.168.1.211:3306/dad_engine_report";//2014-12-30 搬到日航重新架设数据库
	public static String BITA_DB_STATISTICS_USER = "v210_tas_cptg";
	public static String BITA_DB_STATISTICS_PASSWORD = "YeZG9Jr7Rz58W9pb";//2014-12-30 搬到日航重新架设数据库,2015-06-04 搬到日航重新架设数据库,
	
	
	public static String REQUEST_LOG_PATH_PREFIX = "/data/wwwlogs/echo.adsense.cig.com.cn/requests";
	public static String ACCESS_LOG_PATH_PREFIX = "/data/wwwlogs/echo.adsense.cig.com.cn/access";
	// 车型库
	public static String MODEL_PIC_DB_CONNECTION = "jdbc:mysql://115.28.240.244:3306/cigdc_yiche";//2014-12-30 搬到日航重新架设数据库
	public static String MODEL_PIC_DB_USER = "root";
	public static String MODEL_PIC_DB_PASSWORD = "yiche2015gogogo";//2014-12-30 搬到日航重新架设数据库
	
	public static String TOPN_MODELS_FOLDER = "/data/wwwroot/echo.adsense.cig.com.cn/topn_models";
	public static String COMPETITIVE_MODELS_FOLDER = "/data/wwwroot/echo.adsense.cig.com.cn/competitive_models";
	public static String YIPAI_REGION_CODE = "/data/wwwroot/echo.adsense.cig.com.cn/yipai_region_code/yipai_region_code_utf8.csv";
	
	// 清理Headers_REFERER中监测代码的正则表达式(防止加了代码的url无法匹配，无法进行内容投放)
	public static Pattern HEADERS_REFERER_CLEANER = Pattern.compile("\\.html\\?|\\?WT.mc_id=|\\?referrer=");
	
	public static List<String> CIG_LIST = new ArrayList<>();
	public static List<String> BITAUTO_LIST = new ArrayList<>();
	public static List<String> GROUPON_LIST = new ArrayList<>();//团购pid列表
	public static List<String> ACTIVITY_LIST = new ArrayList<>();//活动pid列表
	public static List<String> HUIMAICHE_ILIST = new ArrayList<>();//惠买车接口pid列表
	public static List<String> HUIMAICHE_DLIST = new ArrayList<>();//惠买车动态pid列表
	public static List<String> USEDCAR_LIST = new ArrayList<>();//二手车pid列表
	public static List<String> YX_ILIST = new ArrayList<>();//易鑫数据接口pid列表
	public static List<String> YX_DLIST = new ArrayList<>();//易鑫动态模版列表
	public static List<String> YCH_LIST = new ArrayList<>();//易车惠动态模版列表
	public static List<String> HMC_ONE_LIST = new ArrayList<>();//易车惠动态商品源1
	public static List<String> HMC_TWO_LIST = new ArrayList<>();//易车惠动态商品源2
	public static List<String> HMC_FOUR_LIST = new ArrayList<>();//易车惠动态商品源4
	public static final Map<String,List<String>> ADAID_AREAS=new LinkedHashMap<>();//广告位:地域
	
	// 3 years
	public static final int COOKIE_MAX_AGE = 3 * 365 * 24 * 60 * 60;
	
	public static final String BITAUTO_SPECIAL_POSITION_SEPARATOR = "&";
	public static final String USER_BEHAVIOR_COOKIE_ID = "CIGDCUB";
	public static final String COOKIE_ID = "CIGDCID";
	public static final String POSITION_ID = "pid";
	public static final String MATERIAL_ID = "mid";
	public static final String CITY_ID = "cityId";
	public static final String DEVICE_ID = "device";
	public static final String CALLBACK = "callback";
	public static final String LANDING_PAGE = "lp";
	public static final String MESSAGE_TYPE_IMPRESSION = "i";
	public static final String MESSAGE_TYPE_CLICK = "c";
	public static final String MESSAGE_TYPE_REGION_IMPRESSION = "ri";
	public static final String USER_BEHAVIOR_IMPRESSION = "impression";
	public static final String USER_BEHAVIOR_LAST_IMPRESSION = "lastImpression";
	public static final String USER_BEHAVIOR_CLICK = "click";
	public static final String REQUEST_CHARSET = "cs";
	public static final String DEFAULT_REQUEST_CHARSET = "UTF-8";
	// 同页面 同cookie 标识
	public static final String UK = "uk";
	
	public static final String ID_COOKIE_TEMPLATE = COOKIE_ID + "=%s; path=/; domain=.cig.com.cn; Max-Age=94608000; Expires=Tue, 01-Jul-2017 13:33:33 GMT";
	public static final String UB_COOKIE_TEMPLATE = USER_BEHAVIOR_COOKIE_ID + "=%s; path=/; domain=.cig.com.cn; Max-Age=94608000; Expires=Tue, 01-Jul-2017 13:33:33 GMT";
	public static final String P3P_COOKIE_KEY = "P3P";
	public static final String P3P_COOKIE_VALUE = "CP='CURa ADMa DEVa PSAo PSDo OUR BUS UNI PUR INT DEM STA PRE COM NAV OTC NOI DSP COR'";
	
	public static final String CIG = "cig";
	public static final String BITAUTO = "bita";
	public static final String DYNAMIC = "dynamic";
	public static final String SMARTAD = "smartAD";
	
	//TODO diamand 读取topx
	public static final int TOP_X = 1;
	public static final String POSITION_WIDTH = "width";
	public static final String POSITION_HEIGHT = "height";
	public static final String BITAUTO_MATERIAL_SIZE_SEPARATOR = "_";
	
	public static final int MATCHED_TAGS_NUM_LIMIT = 10;
	
	public static final int RECEIVE_DATA_LIMIT=100000;
	
	/*TODO */
	public static final int BITO = 0;
	public static final int GROUP_ON =2;
	public static final int ACTIVITY =3;
	public static final int HUIMAICHE =4;
	public static final int HUIMAICHE_FIX =5;
	public static final int USED_CAR =6;
	public static final int YIXIN=7;
	public static final int YIXIN_DYN=8;
	public static final int YICHEHUI_DYN=9;
	public static final int HUIMAICHE_DYN_ONE=10;
	public static final int HUIMAICHE_DYN_TWO=11;
	public static final int HUIMAICHE_DYN_FOUR=13;
	
	
	public static final Size SIZE_240_200=new Size(240,200);
	public static final Size SIZE_240_310=new Size(240,310);
	public static final Size SIZE_990_80=new Size(990,80);
	public static final Size SIZE_990_81=new Size(990,81);
	public static final Size SIZE_720_80=new Size(720,80);
	public static final Size SIZE_740_80=new Size(740,80);
	public static final Size SIZE_750_80=new Size(750,80);
	public static final Size SIZE_240_155=new Size(240,155);
	
	public static final String GROUP_INTERFACE_URL="http://api.hd.bitauto.com/tuanarea/?appid=9040e27b92b13&appkey=d58e3582afa99040e27b92b13c8f2280&hotmodel=0&token=8fc86790cd8a17f6ee4a4c467d3b5fd8";
	public static final String GROUPHOT_INTERFACE_URL="http://api.hd.bitauto.com/tuanarea/?appid=9040e27b92b13&appkey=d58e3582afa99040e27b92b13c8f2280&hotmodel=1&token=177f7676c8f27745b9cd6df00d1524ae";
	public static final String Activity_INTERFACE_URL="http://api.hd.bitauto.com/hdarea/?appid=4aa54310d848f&appkey=70b0ff696771ef4aa54310d848f854bd&token=62b6d0e1611d7d3f2ae2eb7ab5f79687";//需要活动给地址
	public static final String HUIMAICHE_INTERFACE_URL="http://www.huimaiche.com/api/Rest.ashx?method=getsalecarmodinfo";//需要惠买车活动接口
	public static final String CITY_INTERFACE_URL="http://api.admin.bitauto.com/api/common/cityvalueset.aspx?type=348city";//城市集接口URL
	public static final String HUIMAICHE_OPENCITY_URL="http://www.huimaiche.com/api/Rest.ashx?method=getopencitylist";//城市集接口URL
	public static final String HUIMAICHE_TIPCITY_URL="http://www.huimaiche.com/api/Rest.ashx?method=getipcityinfo";//城市对应的接口集
	public static final String USEDCAR_INTERFACE_URL="http://yicheapi.taoche.cn/cardata/carserialpricerange.ashx";//二手车数据接口
	public static final String CTR_URL="127.0.0.1";
	public static final int CTR_URL_PORT=8000;
	public static final String CTR_URL_PRIFIX="predict";
	public static final String CTR_URL_FORMAT="http://%s:%s/%s?%s";
	
	public static String PMP_ADVISTER_LIST="http://dad.autodmp.com/pmp_Today_Data/advertiser_has_place?key=b513596792f80a361614ee4e0a79015f";//pmp广告主——广告位——对应列表
	public static String PMP_ADVISTER_CAMPAIN_LIST="http://dad.autodmp.com/pmp_Today_Data/advertiser_campaign_audience_material?key=ce4877653bb9e3a14f37a64ea56f2064";//pmp广告主广—告计划—物料对照关系
	
	public final static String YICHEHUI_INTERFACE_URL="http://api.market.bitauto.com/MessageInterface/DynamicAds/GetProductBrandHandler.ashx";
	public final static String YICHEHUI_INTERFACE_KEY="key=cbb11ed87dc8a95d81400c7f33c7c171";
	
	public static final String YX_PADS_URL ="http://openapi.chedai.bitauto.com/YiChePrecisionAdvertise/API?method=%s";
	public static final String YX_PADS_API_ID = "200109";
	public static final String YX_PADS_API_KEY = "c5735ea5f892c76639d073ec";
	public static final String YX_PADS_STATUS = "alive";
	public static final String YX_PADS_DATA_TYPE = "json";//xml 
	
	public static final String HUIMAICHE_PRODUCT_INF="http://img0.ctags.net/pmpc/v1.0/products/5b188a338566331cb364dbf546a7904f/product_cfb94baf3ffa4e330abbc5b20d5fb09e.ptd";
	public static final String HUIMAICHE_PRODUCT_ZX="http://img0.ctags.net/pmpc/v1.0/products/5b188a338566331cb364dbf546a7904f/product_4c0fa229cbceed0264734ccc3fd374af.ptd";
	
	public static final double ONE_DAY=24*60*60*1000;//团购计算剩余天数
	
	public static final int GROUPON_RESULT_SIZE=6;//团购投送的素材数量
	public static final int ONE_DELIVERY_NUM = 9;//一次投放的素材数量
	
	public static final String PARAM = "key";
	public static final String KEY = "245205057e6d3fb4665197ebef083bd7";
	
	public static final String HUIMAICHE_DATA_PATH="/data/wwwroot/echo.adsense.cig.com.cn/json_data/";
	public static final String USEDCAR_DATA_PATH="/data/wwwroot/echo.adsense.cig.com.cn/json_data/"; //TODO记得要修改上线时候
	public static final String YX_DATA_PATH="/data/wwwroot/echo.adsense.cig.com.cn/json_data/"; //TODO记得要修改上线时候
	
	
	public final static String TAG_VERSION=":v2";
	public final static String MASTER="master";
	public final static String SLAVE="slave";
	public final static int TIME_DAY_MILLISECOND=86400000;

	public static int CTR_STOP=1;//CTR预估  0关闭 1开启
	public static int HEADERS_REFERER_SWITCH=1;//内容定向URL标签 1:开启，0关闭
	public static int VISITOR_FREQUENCY_SWITCH=1;//用户屏次监控  1:开启，0关闭
	public static String FREQUENCY_KEY="DADPCDayUid";//redis 查询用户浏览信息标识。
	
	
	public static final String ECHO_ROOT="striker/echo";
	public static final String ECHO_CONFIG_KEY=ECHO_ROOT+"/config";//上线改回来
	public static final String CASSANDRA_CONFIG_KEY=ECHO_ROOT+"/cassandra_cluster";
	public static final String REDISV3_CONFIG_KEY  =ECHO_ROOT+"/redis_v3";
	public static final String REDISV2_CONFIG_KEY  =ECHO_ROOT+"/redis-v2";
	public static final String TEMPLATE_CONFIG  ="striker/echo/dynamic/template";
}