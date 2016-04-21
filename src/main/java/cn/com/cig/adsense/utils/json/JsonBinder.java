/**
 * 
 */
package cn.com.cig.adsense.utils.json;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;





import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class JsonBinder implements Serializable{  
	private static Logger log = LoggerFactory.getLogger(JsonBinder.class);
  
    /**
	 * 
	 */
	private static final long serialVersionUID = 4737612970649212149L;
	private ObjectJsonMapper mapper;  
  
    @SuppressWarnings("deprecation")
	public JsonBinder(Inclusion inclusion) {  
        mapper = new ObjectJsonMapper();  
        //设置输出包含的属性  
        mapper.getSerializationConfig().setSerializationInclusion(inclusion);  
        //设置输入时忽略JSON字符串中存在而Java对象实际没有的属性  
        mapper.getDeserializationConfig().set(  
                org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);  
    }  
  
    /** 
     * 创建输出全部属性到Json字符串的Binder. 
     */  
    public static JsonBinder buildNormalBinder() {  
        return new JsonBinder(Inclusion.ALWAYS);  
    }  
  
    /** 
     * 创建只输出非空属性到Json字符串的Binder. 
     */  
    public static JsonBinder buildNonNullBinder() {  
        return new JsonBinder(Inclusion.NON_NULL);  
    }  
  
    /** 
     * 创建只输出初始值被改变的属性到Json字符串的Binder. 
     */  
    public static JsonBinder buildNonDefaultBinder() {  
        return new JsonBinder(Inclusion.NON_DEFAULT);  
    }  
  
    /** 
     * 如果JSON字符串为Null或"null"字符串,返回Null. 
     * 如果JSON字符串为"[]",返回空集合. 
     *  
     * 如需读取集合如List/Map,且不是List<String>这种简单类型时使用如下语句: 
     * List<MyBean> beanList = binder.getMapper().readValue(listString, new TypeReference<List<MyBean>>() {}); 
     */  
    public <T> T fromJson(String jsonString, Class<T> clazz) {  
        if (StringUtils.isEmpty(jsonString)) {  
            return null;  
        }  
  
        try {  
            return mapper.readValue(jsonString, clazz);  
        } catch (IOException e) {  
            log.info("parse json string error:" + jsonString);  
            return null;  
        }  
    }  
  
    /** 
     * 如果对象为Null,返回"null". 
     * 如果集合为空集合,返回"[]". 
     */  
    public String toJson(Object object) {  
  
        try {  
            return mapper.writeValueAsString(object);  
        } catch (IOException e) {  
            log.info("write to json string error:" + object);  
            return null;  
        }  
    }  
  
    /** 
     * 设置转换日期类型的format pattern,如果不设置默认打印Timestamp毫秒数. 
     */  
    @SuppressWarnings("deprecation")
	public void setDateFormat(String pattern) {  
        if (StringUtils.isNotBlank(pattern)) {  
            DateFormat df = new SimpleDateFormat(pattern);  
            mapper.getSerializationConfig().setDateFormat(df);  
            mapper.getDeserializationConfig().setDateFormat(df);  
        }  
    }  
  
    /** 
     * 取出Mapper做进一步的设置或使用其他序列化API. 
     */  
    public ObjectMapper getMapper() {  
        return mapper;  
    }  
    class ObjectJsonMapper extends ObjectMapper implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 2386847468174931628L;
    	
    }
}  