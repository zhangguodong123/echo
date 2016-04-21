package cn.com.cig.adsense.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.com.cig.adsense.utils.date.SystemTimer;
import cn.com.cig.adsense.utils.job.ScheduledJob;
import cn.com.cig.adsense.vo.fix.BitautoMaterial;

public class MaterialUtils {

	private static final Logger logger = LoggerFactory.getLogger(MaterialUtils.class);
	private static final Random random = new Random();
	// milliseconds
	private static final long CLEANUP_TASK_PERIOD = 5000L;
	private static final ConcurrentHashMap<String, ExpireObject> ukCache = new ConcurrentHashMap<>(10_000);
	private static final ConcurrentHashMap<String, CookieExpireObject> cookieCache = new ConcurrentHashMap<>(10_000);
	private static Map<Integer, Integer[]> allRandomNums = new HashMap<Integer, Integer[]>();
	
	public String generateNotDuplicatedCookieId(String uk, String cookieId){
		if((uk==null) || ("".equals(uk))){
			logger.warn("Uk is empty!");
			return null;
		}
		CookieExpireObject cookieExpireObject = new CookieExpireObject(cookieId);
		CookieExpireObject putIfAbsent = cookieCache.putIfAbsent(uk, cookieExpireObject);
		if(putIfAbsent == null){
			return cookieExpireObject.getCookie();
		}
		return putIfAbsent.getCookie();
	}
	
	/*
	 * 去重的前提是，同一ip的请求发到同一台服务器上；不考虑同一ip的请求发到多台机器的情况。
	 */
	public Collection<BitautoMaterial> removeDuplicate(String uk, Collection<BitautoMaterial> source){
		
		if((uk==null) || ("".equals(uk))){
			logger.warn("Uk is empty!");
			return null;
		}
		
		if((source==null) || (source.size()==0)){
			logger.warn("Source material is empty!");
			return null;
		}
		
		// 按照Collection<BitautoMaterial>的顺序去重，只投放排在前面的物料，后面的物料就投不出来了（5s内刷新会投出来后面的物料）。
//		Map<Integer, List<BitautoMaterial>> creativeMap = new LinkedHashMap<Integer, List<BitautoMaterial>>();
		// 为了使后面物料有曝光机会，符合要求的物料随机投一个(和returnMaterial一致)，并且不能重复。
		
		Integer[] randomNums = null;
		int size = source.size();
		if(allRandomNums.containsKey(size)){
			randomNums = allRandomNums.get(size);
		}else{
			randomNums = randomIntegerArray(source.size());
			logger.warn("allRandomNums have not randomNums, generated(randomIntegerArray)! size:" + size);
		}
		BitautoMaterial[] randomSource = new BitautoMaterial[source.size()];
		int f = 0;
		BitautoMaterial firstMaterial = null;//记录第一个素材
		for(BitautoMaterial m : source){
			int index = randomNums[f];
			randomSource[index] = m;
			if(index == 0){
				firstMaterial = m;
			}
			f++;
			// 不加重复的创意Id，减小后面原子操作(merge)的执行时间。
//			int creativeId = m.getCreative();
//			if(creativeMap.containsKey(creativeId)){
//				creativeMap.get(creativeId).add(m);
//			}else{
//				List<BitautoMaterial> materials = new ArrayList<BitautoMaterial>();
//				materials.add(m);
//				creativeMap.put(creativeId, materials);
//			}
		}
		
//		BitautoMaterial firstMaterial = source.iterator().next();
		List<String> firstCreatives  = new ArrayList<String>();
		firstCreatives.add(firstMaterial.getCreative());
		ExpireObject firstCreative = new ExpireObject(firstCreatives);
//		final Integer[] targetCreativeArr = {-1};
		final BitautoMaterial[] targetMaterial = new BitautoMaterial[1];
		// uk是否存在已投过的创意
		final Integer[] ukHaveCreative ={-1}; 
		
//		V oldValue = map.get(key);
//		 V newValue = (oldValue == null) ? value :
//		              remappingFunction.apply(oldValue, value);
//		 if (newValue == null)
//		     map.remove(key);
//		 else
//		     map.put(key, newValue);
		try{
			ukCache.merge(uk, firstCreative, (oldValue, value) -> {
				ukHaveCreative[0] = 1;
				List<String> temp = oldValue.getCreatives();
				
//				for(Integer i : creativeMap.keySet()){
//					if(!temp.contains(i.intValue())){ 
//						temp.add(i);
//						targetCreativeArr[0] = i;
//						break;
//					}
//				}
				
				for(BitautoMaterial m : randomSource){
					String creative = m.getCreative();
					if(!temp.contains(creative)){ 
						temp.add(creative);
//						targetCreativeArr[0] = creative;
						targetMaterial[0] = m;
						break;
					}
				}
				return oldValue;
			});
		}catch(Exception e){
			logger.error("Merge error!", e);
			return null;
		}
		
		List<BitautoMaterial> result = null;
		if(ukHaveCreative[0] == -1){
			// 不存在已投过的创意，直接使用第一个物料的创意。
			result = Arrays.asList(firstMaterial);;
		} else {
			if(targetMaterial[0] != null){
				// 存在已投过的创意，同时找到不重复的创意
				result = Arrays.asList(targetMaterial[0]);
			}else{
				// 存在已投过的创意，同时都是重复的创意（找不到不重复的创意），返回null，投非精准物料或者从重复的非精准物料里随机投一个。
			}
		}
		
		return result;
	}
	
	private Map<Integer, Integer[]> generateRandomNums(){
		Map<Integer, Integer[]> tempRandomNums = new HashMap<Integer, Integer[]>(200);
		for(int i=1;i<=100;i++){
			tempRandomNums.put(i, randomIntegerArray(i));
		}
		return tempRandomNums;
	}
	
	/*
	 * 产生m个n范围内的不重复随机数（m<=n)
	 */
	private Integer[] randomIntegerArray(int range){
		
		if(range <= 0){
			logger.warn("range <= 0! range:" + range);
			return null;
		}
		
		Integer[] nums = new Integer[range];
		for(int i = 0; i<range; i++){
			nums[i] = i;
		}
		for(int i = 0; i<range; i++){
			int randomNum = random.nextInt(range - i) + i;
			int temp = nums[i];
			nums[i] = nums[randomNum];
			nums[randomNum] = temp;
		}
		return nums;
	}
	
	private Runnable cleanupTask = new Runnable() {

		@Override
		public void run() {
			logger.info("Cleanup task start...");
			try{
				allRandomNums = generateRandomNums();
			} catch(Exception e){
				logger.error("Cleanup generateRandomNums failed!", e);
			}
			try{
				int size = ukCache.size();
				long currentTime = SystemTimer.currentTimeMillis();
				Iterator<Entry<String, ExpireObject>> iter = ukCache.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<String, ExpireObject> entry = iter.next();
					if (currentTime - entry.getValue().getCreateTime() >= CLEANUP_TASK_PERIOD) {
						ukCache.remove(entry.getKey());
					}
				}
				logger.info("ukCache size:before:" + size + " after:" + ukCache.size());
			} catch(Exception e){
				logger.error("Cleanup Task failed! ukCache size:" + ukCache.size(), e);
			}
			try{
				int size = cookieCache.size();
				long currentTime = SystemTimer.currentTimeMillis();
				Iterator<Entry<String, CookieExpireObject>> iter = cookieCache.entrySet().iterator();
				while (iter.hasNext()) {
					Entry<String, CookieExpireObject> entry = iter.next();
					if (currentTime - entry.getValue().getCreateTime() >= CLEANUP_TASK_PERIOD) {
						cookieCache.remove(entry.getKey());
					}
				}
				logger.info("cookieCache size:before:" + size + " after:" + cookieCache.size());
			} catch(Exception e){
				logger.error("Cleanup Task failed! cookieCache size:" + cookieCache.size(), e);
			}
			logger.info("Cleanup task end.");
		}
	};
	
	private MaterialUtils(){
		ScheduledJob.getInstance().scheduleAtFixedRate(cleanupTask, 1000L, CLEANUP_TASK_PERIOD,TimeUnit.MILLISECONDS);
	}
	
	// 单例
	private static class MaterialUtilsHolder {
		private static MaterialUtils materialUtils = new MaterialUtils();
	}

	public static MaterialUtils getInstance() {
		return MaterialUtilsHolder.materialUtils;
	}
	
	private static class ExpireObject {

		public ExpireObject(List<String> creatives) {
			this.createTime = SystemTimer.currentTimeMillis();
			this.creatives = creatives;
		}

		private long createTime;
		private List<String> creatives = new ArrayList<String>();
		
		public long getCreateTime() {
			return createTime;
		}

		public List<String> getCreatives() {
			return creatives;
		}

		@Override
		public String toString() {
			return "createTime:" + createTime + " creatives:" + creatives;
		}

	}
	
	private class CookieExpireObject {

		public CookieExpireObject(String cookie) {
			this.createTime = SystemTimer.currentTimeMillis();
			this.cookie = cookie;
		}

		private long createTime;
		private String cookie;
		
		public long getCreateTime() {
			return createTime;
		}

		public String getCookie() {
			return cookie;
		}

		@Override
		public String toString() {
			return "createTime:" + createTime + " cookie:" + cookie;
		}

	}
	
}
