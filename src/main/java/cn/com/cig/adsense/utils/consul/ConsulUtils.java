package cn.com.cig.adsense.utils.consul;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Optional;
import com.orbitz.consul.model.kv.Value;

/**
 * @File: ConsulUtils.java
 * @Package cn.com.cig.adsense.utils
 * @Description: TODO
 * @author zhangguodong
 * @date 2016年2月26日 上午10:20:38
 * @version V1.0
 */
public class ConsulUtils {
	/**
	 * description: hash_key maps
	 * 
	 * @param newValues
	 * @return
	 */
	public static Map<Integer, String> cacheHashKeyMap(
			Map<String, Value> newValues) {
		Map<Integer, String> kv = new HashMap<Integer, String>();
		Set<String> ks = newValues.keySet();
		Iterator<String> it = ks.iterator();
		while (it.hasNext()) {
			String nextKey = it.next();
			Value nextValue = newValues.get(nextKey);
			String key = nextValue.getKey();
			Optional<String> opt = nextValue.getValue();
			if (opt.isPresent()) {
				String value = nextValue.getValueAsString().get();
				kv.put(key.hashCode(), value);
			}
		}
		return kv;
	}

	public static Map<String, String> cacheStringKVMap(
			Map<String, Value> newValues) {
		Map<String, String> kv = new HashMap<String, String>();
		Set<String> ks = newValues.keySet();
		Iterator<String> it = ks.iterator();
		while (it.hasNext()) {
			String nextKey = it.next();
			Value nextValue = newValues.get(nextKey);
			Optional<String> opt = nextValue.getValue();
			if (opt.isPresent()) {
				String key = nextValue.getKey();
				String[] split = key.split("/");
				if(split.length > 0){
					String value = nextValue.getValueAsString().get();
					kv.put(split[split.length-1], value);
				}
			}
		}
		return kv;
	}
}
