package cn.com.cig.adsense.dao.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;

import cn.com.cig.adsense.dao.MaterialLibraryDao;
import cn.com.cig.adsense.utils.Constant;
import cn.com.cig.adsense.utils.http.AsyncHttpClientUtils;
import cn.com.cig.adsense.vo.PlatefromEnum;
import cn.com.cig.adsense.vo.fix.Advertiser;
import cn.com.cig.adsense.vo.fix.BitautoMaterial;
import cn.com.cig.adsense.vo.fix.Campaign;
import cn.com.cig.adsense.vo.fix.DockingEnum;
import cn.com.cig.adsense.vo.fix.Material;
import cn.com.cig.adsense.vo.fix.MaterialType;
import cn.com.cig.adsense.vo.fix.OsTypeEnum;
import cn.com.cig.adsense.vo.fix.Position;
import cn.com.cig.adsense.vo.fix.PositionTypeEnum;
import cn.com.cig.adsense.vo.fix.Size;
import static cn.com.cig.adsense.utils.Utils.httpURLConnRequest;

public class MaterialLibraryDaoImpl implements MaterialLibraryDao {

	private static Logger logger = LoggerFactory
			.getLogger(MaterialLibraryDaoImpl.class);

	private static final String GET_MATERIAL_SQL_STEP_1 = "SELECT "
			+ " pm.placeId AS positionId, pm.meterialId AS id, pm.is_default AS type, "
			+ " m.id, m.met_name AS name, m.met_type AS met_type, m.width AS width, m.height AS height, m.upload_file_url AS url, m.link_url AS landingPage, m.text AS text, "
			+ " ma.audienceId "
			+ " FROM place_meterial AS pm, meterial AS m, meterial_audience AS ma "
			+ " WHERE pm.status = 1 AND pm.meterialId = m.id AND pm.meterialId = ma.meterialId";

	private static final String GET_MATERIAL_SQL_STEP_2 = "SELECT ar.regionId, ar.audienceId FROM audience_region AS ar WHERE ar.audienceId IN (%s)";

	private static final String GET_MATERIAL_SQL_STEP_3 = "SELECT ass.styleId, ass.audienceId FROM audience_style AS ass WHERE ass.audienceId IN (%s)";

	private static final String GET_BITAUTO_MATERIAL_SQL = "SELECT "
			+ "material.*,campaign.id AS campaign_id,audience.style,audience.tags,audience.region,campaign.place,audience.auto_matche,audience_has_material.compete_models,audience.id as aid,advertiser.limit_visitor_display_day,campaign.expose_visit_day_limit,campaign.expose_visit_hour_limit,advertiser.id as advisterId"
			+ " FROM "
			+ " ((((advertiser right join campaign on campaign.advertiser_id=advertiser.id) inner join campaign_has_audience on campaign.id=campaign_has_audience.camp_id ) "
			+ " inner join audience on campaign_has_audience.audience_id=audience.id) "
			+ " inner join audience_has_material  on audience_has_material.audience_id=audience.id) "
			+ " left join material on audience_has_material.material_id=material.id "
			+ "WHERE "
			+ "advertiser.is_del=0 "
			+ "AND advertiser.display_limited_byday=0 "
			+ "AND advertiser.id != 1 " //排除测试广告主ID为1的广告主
			+ "AND campaign.is_del=0 "
			+ "AND campaign.status=2 "
			+ "AND campaign.is_pmp=0 "
			+ "AND material.is_del=0 "
			+ "AND material.`status`=1 "
			+ "AND audience.is_del=0 "
			+ "AND audience.`status`=1 "
			+ "AND material.is_sync=1 "
			+ "AND (advertiser.limit_display_day=0 OR advertiser.limit_display_day>advertiser.actual_limit_display_day) "
			+ "AND (advertiser.limit_click_day=0 OR advertiser.limit_click_day>advertiser.actual_limit_click_day) "
			+ "AND (campaign.expose_throw_total_limit=0 OR campaign.expose_throw_total_limit>campaign.expose_throw_total) "
			+ "AND (campaign.expose_throw_day_limit=0 OR campaign.expose_throw_day_limit>campaign.expose_throw_day) "
			+ "AND (campaign.expose_throw_total_click_limit=0 OR campaign.expose_throw_total_click_limit>campaign.expose_throw_total_click_limit_syn) "
			+ "AND (campaign.expose_throw_day_click_limit=0 OR campaign.expose_throw_day_click_limit>campaign.expose_throw_day_click_limit_syn) ";

	private static final String GET_POSITION_SIZE_BY_ID = "SELECT id, CONCAT(CAST(width AS CHAR), '"
			+ Constant.BITAUTO_MATERIAL_SIZE_SEPARATOR
			+ "', CAST(height AS CHAR)) AS size FROM place WHERE is_del = 0";

	private static final String GET_POSITION_WIDTH_WIGHT_BY_ID = "SELECT id,width,height FROM place WHERE is_del = 0";
	// cigdc_ad_sys
	private static final String NEED_AUTO_MATCH_MODELS_STEP1 = "SELECT ma.id, a.style FROM audience_has_material AS ma, audience AS a WHERE ma.audience_id = a.id AND a.auto_matche = 1 AND (a.style!=null OR a.style != '')";// 需要修改,已修改
	// cigdc_ad_sys
	private static final String NEED_AUTO_MATCH_MODELS_STEP2 = "UPDATE audience inner join audience_has_material on audience.id=audience_has_material.audience_id SET audience.auto_matche = 2, audience_has_material.compete_models = '%s' WHERE audience_has_material.id = %s";// 需要修改，表结构字段发生变化,已修改

	private static final String GET_ALL_POSITIONS_SQL = "SELECT p.id, p.name, p.type as ttype, p.title_min_num, p.title_max_num, p.attribute, m.id, m.name, m.type as mtype, m.platefrom, m.middle, m.docking, m.GUID, c.id, c.name FROM place AS p, media AS m, channel AS c WHERE p.media_id = m.id AND p.channel_id = c.id AND p.is_del = 0";

	@Override
	public List<Material> getCigAllMaterial() {
		return getAllMaterial(getCigDBConnection());
	}

	@Override
	public List<BitautoMaterial> getBitautoAllMaterial() {
		Connection connection = null;
		Statement statement = null;
		try {
			connection = getBitautoDBConnection();
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(GET_BITAUTO_MATERIAL_SQL);
			List<BitautoMaterial> tmp = new ArrayList<>();
			while (rs.next()) {
				BitautoMaterial mat = new BitautoMaterial();
				mat.setId(rs.getInt("id"));
				mat.setCampaignId(rs.getInt("campaign_id"));
				mat.setCreator(rs.getString("creator"));
				mat.setCreatetime(rs.getInt("createtime"));
				mat.setFileName(rs.getString("file_name"));
				mat.setCreative(rs.getString("creative"));
				mat.setUrl(rs.getString("url"));
				mat.setHeight(rs.getInt("height"));
				mat.setWidth(rs.getInt("width"));
				mat.setIsDel(rs.getInt("is_del"));
				mat.setIsSync(rs.getInt("is_sync"));
				mat.setLinkUrl(rs.getString("link_url"));
				mat.setName(rs.getString("mat_name"));
				int mat_type = rs.getInt("mat_type");
				// 0图片, 1文章, 2flash,3视频,4图文
				switch (mat_type) {
				case 0:
					mat.setMatType(MaterialType.IMAGE);
					break;
				case 1:
					mat.setMatType(MaterialType.TEXT);
					break;
				case 2:
					mat.setMatType(MaterialType.FLASH);
					break;
				case 3:
					mat.setMatType(MaterialType.VIDEO);
					break;
				case 4:
					mat.setMatType(MaterialType.TELETEXT);
					break;
				default:
					logger.warn("Illegal material type! mat_type:" + mat_type);
					break;
				}
				mat.setText(rs.getString("text"));
				mat.setRegions(toIntList(rs.getString("region")));
				mat.setModels(toIntList(rs.getString("style")));
				mat.setPlaces(toIntList(rs.getString("place")));
				mat.setAudienceId(rs.getInt("aid"));
				// 处理自动匹配车型的需求。
				// material_audience.auto_matche,material_audience.compete_models
				// 0:不需要匹配.1:需要匹配，未完成。2:匹配完成
				int auto_match = rs.getInt("auto_matche");
				if (auto_match == 2) {
					String compete_models = rs.getString("compete_models");
					mat.setCompetitiveModels(toIntList(compete_models));
				}
				// 非车型标签.
				List<Integer> tags = toIntList(rs.getString("tags"));
				if ((tags != null) && (tags.size() > 0)) {
					mat.getModels().addAll(tags);
				}
				mat.setWapLinkUrl(rs.getString("wap_link_url"));
				mat.setTitle(StringEscapeUtils.unescapeHtml4(rs.getString("title")));
				mat.setDescription(rs.getString("description"));
				
				mat.setAdvLimitVisitorDisplayDay(Long.valueOf(rs.getLong("limit_visitor_display_day")));
				mat.setCampExposeVisitDayLimit(Long.valueOf(rs.getLong("expose_visit_day_limit")));
				mat.setCampExposeVisitHourLimit(Long.valueOf(rs.getLong("expose_visit_hour_limit")));
				mat.setAdvisterId(rs.getInt("advisterId"));
				tmp.add(mat);
			}
			return tmp;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (statement != null)
				try {
					statement.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
		}
		return Collections.emptyList();
	}

	@Override
	public Map<String, String> getPositionSize() {
		Map<String, String> pidAndSize = new HashMap<>();
		Connection connection = null;
		Statement statement = null;
		try {
			connection = getBitautoDBConnection();
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(GET_POSITION_SIZE_BY_ID);
			while (rs.next()) {
				int pid = rs.getInt("id");
				String size = rs.getString("size");
				if ((pid > 0) && (size != null) && (size.length() > 0)) {
					pidAndSize.put(Integer.toString(pid), size);
				} else {
					logger.error("Get position size illegal! positionId:" + pid
							+ " size:" + size);
				}
			}
			return pidAndSize;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (statement != null)
				try {
					statement.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
		}
		return pidAndSize;
	}

	/**
	 * 获取尺寸
	 * 
	 * @return
	 */
	@Override
	public Map<String, Size> getPositionWidthAndHeight() {
		Map<String, Size> pidAndSize = new HashMap<>();
		Connection connection = null;
		Statement statement = null;
		try {
			connection = getBitautoDBConnection();
			statement = connection.createStatement();
			ResultSet rs = statement
					.executeQuery(GET_POSITION_WIDTH_WIGHT_BY_ID);
			while (rs.next()) {
				int pid = rs.getInt("id");
				int width = rs.getInt("width");
				int height = rs.getInt("height");

				if (width == 0 || height == 0) {
					logger.error("Get position size illegal! positionId:" + pid
							+ " size:" + width + "_" + height);
				} else {
					Size size = new Size();
					size.setWidth(width);
					size.setHeight(height);
					pidAndSize.put(Integer.toString(pid), size);
				}
			}
			return pidAndSize;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (statement != null)
				try {
					statement.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
		}
		return pidAndSize;
	}

	private List<Integer> toIntList(String s) {
		if (s == null || s.trim().length() == 0 || "NULL".equalsIgnoreCase(s))
			return new ArrayList<Integer>();
		return new ArrayList<Integer>(Collections2.transform(Splitter.on(',')
				.trimResults().omitEmptyStrings().splitToList(s.trim()),
				new Function<String, Integer>() {

					@Override
					public Integer apply(String input) {
						return Integer.parseInt(input);
					}
				}));

		/*
		 * return
		 * ImmutableList.copyOf(Collections2.transform(Splitter.on(',').trimResults
		 * ().omitEmptyStrings().splitToList(s.trim()), new Function<String,
		 * Integer>(){
		 * 
		 * @Override public Integer apply(String input) { return
		 * Integer.parseInt(input); }}));
		 */
	}

	private List<Material> getAllMaterial(Connection dbConnection) {
		// audienceId: Material
		Map<Integer, Material> as = new HashMap<>();
		PreparedStatement preparedStatement1 = null;
		PreparedStatement preparedStatement2 = null;
		PreparedStatement preparedStatement3 = null;
		try {
			List<Integer> audienceIds = new ArrayList<>();
			preparedStatement1 = dbConnection
					.prepareStatement(GET_MATERIAL_SQL_STEP_1);
			ResultSet rs = preparedStatement1.executeQuery();
			while (rs.next()) {
				int audienceId = rs.getInt("audienceId");
				if (as.containsKey(audienceId)) {
					Material temp = as.get(audienceId);
					// int regionId = rs.getInt("regionId");
					// int modelId = rs.getInt("modelId");
					// if((regionId > 0) &&
					// (!temp.getRegions().contains(regionId))){
					// temp.getRegions().add(regionId);
					// }
					// if((modelId > 0) &&
					// (!temp.getModels().contains(modelId))){
					// temp.getModels().add(modelId);
					// }
					if ((temp.getText() == null)
							|| (temp.getText().length() == 0)) {
						String text = rs.getString("text");
						temp.setText(text);
					}
				} else {
					Material temp = new Material();
					String id = rs.getString("id");
					temp.setId(id);
					String name = rs.getString("name");
					int type = rs.getInt("type");
					String positionId = rs.getString("positionId");
					String url = rs.getString("url");
					String landingPage = rs.getString("landingPage");
					// int regionId = rs.getInt("regionId");
					// int modelId = rs.getInt("modelId");
					String text = rs.getString("text");
					int met_type = rs.getInt("met_type");
					int width = rs.getInt("width");
					int height = rs.getInt("height");
					temp.setName(name);
					temp.setType(type);
					temp.setPositionId(positionId);
					temp.setAudienceId(audienceId);
					temp.setUrl(url);
					temp.setLandingPage(landingPage);
					// if(regionId > 0){
					// temp.setRegions(new
					// ArrayList<Integer>(Arrays.asList(regionId)));
					// } else {
					// temp.setRegions(new ArrayList<Integer>());
					// }
					// if(modelId > 0){
					// temp.setModels(new
					// ArrayList<Integer>(Arrays.asList(modelId)));
					// } else {
					// temp.setModels(new ArrayList<Integer>());
					// }
					temp.setText(text);
					temp.setMet_type(met_type);
					temp.setWidth(width);
					temp.setHeight(height);
					as.put(audienceId, temp);
					audienceIds.add(audienceId);
				}
			}

			if (audienceIds.size() > 0) {
				StringBuilder sb = new StringBuilder();
				for (int audienceId : audienceIds) {
					sb.append(audienceId).append(",");
				}
				sb.deleteCharAt(sb.length() - 1);

				preparedStatement2 = dbConnection.prepareStatement(String
						.format(GET_MATERIAL_SQL_STEP_2, sb.toString()));
				rs = preparedStatement2.executeQuery();
				while (rs.next()) {
					int regionId = rs.getInt("regionId");
					int audienceId = rs.getInt("audienceId");
					if (as.containsKey(audienceId)) {
						Material temp = as.get(audienceId);
						if ((temp.getRegions() == null)) {
							temp.setRegions(new ArrayList<Integer>(Arrays
									.asList(regionId)));
						} else {
							temp.getRegions().add(regionId);
						}
					} else {
						logger.warn("Unknown audienceId! audienceId:"
								+ audienceId);
					}
				}
				preparedStatement3 = dbConnection.prepareStatement(String
						.format(GET_MATERIAL_SQL_STEP_3, sb.toString()));
				rs = preparedStatement3.executeQuery();
				while (rs.next()) {
					int styleId = rs.getInt("styleId");
					int audienceId = rs.getInt("audienceId");
					if (as.containsKey(audienceId)) {
						Material temp = as.get(audienceId);
						if ((temp.getModels() == null)) {
							temp.setModels(new ArrayList<Integer>(Arrays
									.asList(styleId)));
						} else {
							temp.getModels().add(styleId);
						}
					} else {
						logger.warn("Unknown audienceId! audienceId:"
								+ audienceId);
					}
				}
			}

		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (preparedStatement1 != null) {
				try {
					preparedStatement1.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
			}
			if (preparedStatement2 != null) {
				try {
					preparedStatement2.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
			}
			if (preparedStatement3 != null) {
				try {
					preparedStatement3.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
			}
			if (dbConnection != null) {
				try {
					dbConnection.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}

		return ImmutableList.copyOf(as.values());
	}

	@Override
	public Map<Integer, List<Integer>> needAutoMatchModels() {
		Map<Integer, List<Integer>> result = new HashMap<>();
		Connection connection = null;
		Statement statement = null;
		try {
			connection = getBitautoDBConnection();
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(NEED_AUTO_MATCH_MODELS_STEP1);
			while (rs.next()) {
				int materialAudienceId = rs.getInt("id");
				List<Integer> models = toIntList(rs.getString("style"));
				result.put(materialAudienceId, models);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (statement != null)
				try {
					statement.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
		}
		return result;
	}

	@Override
	public void saveAutoMatchCompetitiveModels(
			Map<Integer, List<Integer>> competitiveModels) {

		if ((competitiveModels == null) || (competitiveModels.size() == 0)) {
			logger.warn("Competitive models is empty!");
		}

		Connection connection = null;
		Statement statement = null;
		try {
			connection = getBitautoDBConnection();
			statement = connection.createStatement();
			Iterator<Entry<Integer, List<Integer>>> iter = competitiveModels
					.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<Integer, List<Integer>> entry = iter.next();
				int materialAudienceId = entry.getKey();
				// 竞品
				List<Integer> models = entry.getValue();
				if ((models != null) && (models.size() > 0)) {
					Stream<Integer> stream = models.stream();
					String modelsStr = stream.map((model) -> model.toString())
							.collect(Collectors.joining(","));
					stream.close();
					String updateSql = String.format(
							NEED_AUTO_MATCH_MODELS_STEP2, modelsStr,
							materialAudienceId);
					statement.addBatch(updateSql);
				}

			}
			statement.executeBatch();
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (statement != null)
				try {
					statement.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
		}

	}

	@Override
	public List<Position> getAllPositions() {

		List<Position> result = new ArrayList<Position>();
		Connection connection = null;
		Statement statement = null;
		try {
			connection = getBitautoDBConnection();
			statement = connection.createStatement();
			ResultSet rs = statement.executeQuery(GET_ALL_POSITIONS_SQL);
			while (rs.next()) {
				Position position = new Position();
				
				//place_table
				String pid = rs.getString(1);   			//place.id   广告位ID
				String name = rs.getString(2);				//place.name 广告位名称
				int pType = rs.getInt(3);  					//place.type 广告位类型 0 图片 1 文章 2 flash 3 视频，4图片\\flash)
				int titleMinNum = rs.getInt(4);        		//place.title_min_num 文字标题最小值
				int titleMaxNum = rs.getInt(5);        		//place.title_max_num 文字标题字数最大值
				int pAttribute = rs.getInt(6);         		//place.attribute   属性字段 0 信息流，默认信息流  
				
				//media_table
				String mediaId = rs.getString(7);      		//media.id 
				String mediaName = rs.getString(8);    		//media.name
				int mType = rs.getInt(9);              		//media.type   1是pc端 2是移动端
				int osType = rs.getInt(10);              	//media.platefrom 0 IOS、1 Android
				String midList = rs.getString(11);
				List<String> middle=null;
				if(midList != null && !"".endsWith(midList)){
					middle = Arrays.asList(midList.split(","));//1 手机、2 平板。多选用逗号相隔如:1,2
				}
				int dock = rs.getInt(12);					//media.0 API、1 SDK
				String guid = rs.getString(13); 			//app guid全球唯一标示，pc/wap 默认0
				
				//channel_table
				String channelId = rs.getString(14);        //渠道ID
				String channelName = rs.getString(15);		//渠道名称
				
				//place_bean
				position.setId(pid);
				position.setName(name);
				switch (pType) {
					case 0:
						position.setType(PositionTypeEnum.IMAGE);
						break;
					case 1:
						position.setType(PositionTypeEnum.TEXT);
						break;
					case 2:
						position.setType(PositionTypeEnum.FLASH);
						break;
					case 3:
						position.setType(PositionTypeEnum.VIDEO);
						break;
					case 4:
						position.setType(PositionTypeEnum.IMAGEORFLASH);
						break;
					case 5:
						position.setType(PositionTypeEnum.ELETEXT);
						break;
					default:
						logger.warn("Illegal position type! type:" + pType+ " pid:" + pid);
						break;
				}
				
				position.setTitleMinNum(titleMinNum);
				position.setTitleMaxNum(titleMaxNum);
				position.setAttribute(pAttribute);
				
				//media_bean
				position.setMediaId(mediaId);
				position.setMediaName(mediaName);
				switch (mType) {
					case 0:
						position.setMediaType(PlatefromEnum.PC);
						break;
					case 1:
						position.setMediaType(PlatefromEnum.WAP);
						break;
					case 2:
						position.setMediaType(PlatefromEnum.APP);
						break;
					default:
						logger.warn("Illegal platefrom! platefrom:" + mType+ " pid:" + pid);
						break;
				}
				switch (osType) {
					case 0:
						position.setOsType(OsTypeEnum.IOS);
						break;
					case 1:
						position.setOsType(OsTypeEnum.ANDROID);
						break;
					default:
						logger.warn("Illegal OsType! OsType:" + osType+ " pid:" + pid);
						break;
				}
				position.setMiddle(middle);
				switch (dock) {
					case 0:
						position.setDocking(DockingEnum.API);
						break;
					case 1:
						position.setDocking(DockingEnum.SDK);
						break;
					default:
						break;
				}
				
				position.setMiddle(middle);
				position.setGuid(guid);
				position.setAttribute(pAttribute);    
				
				//channel_bean
				position.setChannelId(channelId);
				position.setChannelName(channelName);
				result.add(position);
			}
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (statement != null)
				try {
					statement.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
			if (connection != null)
				try {
					connection.close();
				} catch (SQLException e) {
					logger.error(e.getMessage(), e);
				}
		}
		return result;
	}

	private static Connection getCigDBConnection() {

		Connection dbConnection = null;
		try {
			Class.forName(Constant.DB_DRIVER);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
		}

		try {
			DriverManager.setLoginTimeout(10);
			dbConnection = DriverManager.getConnection(Constant.DB_CONNECTION,
					Constant.DB_USER, Constant.DB_PASSWORD);
			return dbConnection;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}

		return dbConnection;
	}

	private static Connection getBitautoDBConnection() {

		Connection dbConnection = null;
		try {
			Class.forName(Constant.DB_DRIVER);
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage(), e);
		}

		try {
			DriverManager.setLoginTimeout(10);
			dbConnection = DriverManager.getConnection(
					Constant.BITA_DB_CONNECTION, Constant.BITA_DB_USER,
					Constant.BITA_DB_PASSWORD);
			return dbConnection;
		} catch (SQLException e) {
			logger.error(e.getMessage(), e);
		}

		return dbConnection;
	}

	@Override
	public String getCTRMaterials(String url) {
		return AsyncHttpClientUtils.getInstance().asyncHttpUrl(url);
	}

	@Override
	public Map<Integer, Integer> getPMPAdvisters() {
		Map<Integer,Integer> map=new HashMap<>();
		String pmpAdsList=httpURLConnRequest(Constant.PMP_ADVISTER_LIST,"GET");
		if(pmpAdsList ==null || "".equals(pmpAdsList) || pmpAdsList.length() > 10000000000L){
			logger.error("request>>>>["+Constant.PMP_ADVISTER_LIST+"] is error");
			return map;
		}
		/*判断http接口返回数据是否是纯正JSONObject格式*/
		if(!(pmpAdsList.startsWith("{")) || !(pmpAdsList.endsWith("}"))){
			logger.error("request>>>>["+Constant.PMP_ADVISTER_LIST+"] is deficiency of data {}");
			return null;
		}
		JSONObject jarr=JSONObject.fromObject(pmpAdsList);
        int code = jarr.getInt("code");
        int status = jarr.getInt("status");
        if(code != 200 || status != 1){
        	logger.error("request>>>>:"+Constant.PMP_ADVISTER_LIST+" parse json error! not 200");
        	return null;
        }
        JSONArray jsonArray = jarr.getJSONArray("data");
        if(jsonArray ==null){
        	logger.error("request>>>>:"+Constant.PMP_ADVISTER_LIST+" parse json of data is null...");
        	return null;
        }
        for(int i=0;i<jsonArray.size();i++){
        	JSONObject j2 = jsonArray.getJSONObject(i);
        	int aid=j2.getInt("advisterId");
        	int pid = j2.getInt("pid");
        	if(!map.containsKey(pid)){
        		map.put(pid, aid);
        	}
        }
		return map;
	}

	@Override
	@SuppressWarnings("unchecked")
	public Map<Integer, Advertiser> getPMPAdvertisers() {
		Map<Integer,Advertiser> advisterMap=new HashMap<>();
		String data=httpURLConnRequest(Constant.PMP_ADVISTER_CAMPAIN_LIST,"GET");
		if(data ==null || "".equals(data) || data.length() >100000000000L){
			logger.error("request>>>>["+Constant.PMP_ADVISTER_CAMPAIN_LIST+"] is error");
			return null;
		}
		/*判断http接口返回数据是否是纯正JSONArray格式*/
		if(!(data.startsWith("[")) || !(data.endsWith("]"))){
			logger.error("request>>>>["+Constant.PMP_ADVISTER_CAMPAIN_LIST+"] is deficiency of data []");
			return null;
		}
        JSONArray advisters = JSONArray.fromObject(data);
        for(int i=0;i<advisters.size();i++){
        	JSONObject advister = advisters.getJSONObject(i);
        	if(advister == null) continue;//如果为空扔掉
        	Advertiser ad=new Advertiser();
        	int id = advister.getInt("id");//获取id
        	String name = advister.getString("name");//获取name
        	long limitVisitorDisplayDay = advister.getLong("limit_visitor_display_day");//广告计划设定的按天每访客的曝光限定值
        	ad.setId(id);
        	ad.setName(name);
        	ad.setLimitVisitorDisplayDay(limitVisitorDisplayDay);
        	
        	List<Campaign> direct=new ArrayList<>();  //定投广告计划
        	List<Campaign> unDirect=new ArrayList<>();//非定投PMP广告计划
        	List<Campaign> general=new ArrayList<>(); //非定投非PMP广告计划 *（这里区别与公投部分的广告计划）
        	
        	JSONArray campaigns = advister.getJSONArray("campaigns");
        	for(int j=0;j<campaigns.size();j++){
        		JSONObject campaign = campaigns.getJSONObject(j);
        		Campaign camp=new Campaign();
        		int campainId = campaign.getInt("id");
        		int isPmp = campaign.getInt("isPmp");
        		long exposeVisitHourLimit = campaign.getLong("expose_visit_hour_limit");
        		long exposeVisitDayLimit = campaign.getLong("expose_visit_day_limit");
        		List<Integer> places=(List<Integer>)JSONArray.toCollection(campaign.getJSONArray("places"), Integer.class);
				/*图片素材*/
				Map<String, List<BitautoMaterial>> accurateMaterial = new HashMap<>();           // 精准图片素材
				Map<String, List<BitautoMaterial>> unAccurateMaterial = new HashMap<>();         // 非精准图片素材
				/*图文素材*/
				List<BitautoMaterial> accurateEletextMaterial = new ArrayList<BitautoMaterial>();	// 精准图文素材
				List<BitautoMaterial> unAccurateEletextMaterial = new ArrayList<BitautoMaterial>(); // 非精准图文素材
				/*文本素材*/
				List<BitautoMaterial> accurateTextMaterial = new ArrayList<BitautoMaterial>();   // 精准文本素材
				List<BitautoMaterial> unAccurateTextMaterial = new ArrayList<BitautoMaterial>(); // 非精准文本素材
        		
        		JSONArray audiences = campaign.getJSONArray("audiences");
        		for(int k=0;k<audiences.size();k++){
        			JSONObject audience = audiences.getJSONObject(k);
        			int autoMatche = audience.getInt("autoMatche");
        			List<Integer> styles=(List<Integer>)JSONArray.toCollection(audience.getJSONArray("style"), Integer.class);
        			List<Integer> tags=(List<Integer>)JSONArray.toCollection(audience.getJSONArray("tags"), Integer.class);
        			List<Integer> regions=(List<Integer>)JSONArray.toCollection(audience.getJSONArray("regions"), Integer.class);
        			List<Integer> competeModels=(List<Integer>)JSONArray.toCollection(audience.getJSONArray("competeModels"), Integer.class);
        			
        			JSONArray materials = audience.getJSONArray("materials");
        			for(int l=0;l<materials.size();l++){
        				BitautoMaterial bitauto=new BitautoMaterial();
        				JSONObject material = materials.getJSONObject(l);
        				int mid = material.getInt("id");
        				String creator=material.getString("creator");
        				int createtime=material.getInt("createtime");
        				String file_name=material.getString("file_name");
        				String creative=material.getString("creative");
        				String url=material.getString("url");
        				int height = material.getInt("height");
        				int width = material.getInt("width");
        				int is_del = material.getInt("is_del");
        				int isSync = material.getInt("is_sync");
        				String link_url=material.getString("link_url");
        				String mat_name=material.getString("mat_name");
        				int mat_type = material.getInt("mat_type");
        				String text=material.getString("text");
        				String wap_link_url=material.getString("wap_link_url");
        				String title=material.getString("title");
        				String description=material.getString("description");
        				bitauto.setId(mid);
        				bitauto.setCampaignId(campainId);
        				bitauto.setCreator(creator);
        				bitauto.setCreatetime(createtime);
        				bitauto.setFileName(file_name);
        				bitauto.setCreative(creative);
        				bitauto.setUrl(url);
        				bitauto.setHeight(height);
        				bitauto.setWidth(width);
        				bitauto.setIsDel(is_del);
        				bitauto.setIsSync(isSync);
        				bitauto.setLinkUrl(link_url);
        				bitauto.setName(mat_name);
        				
        				switch (mat_type) {
        				case 0:
        					bitauto.setMatType(MaterialType.IMAGE);
        					break;
        				case 1:
        					bitauto.setMatType(MaterialType.TEXT);
        					break;
        				case 2:
        					bitauto.setMatType(MaterialType.FLASH);
        					break;
        				case 3:
        					bitauto.setMatType(MaterialType.VIDEO);
        					break;
        				case 4:
        					bitauto.setMatType(MaterialType.TELETEXT);
        					break;
        				default:
        					logger.warn("Illegal material type! mat_type:" + mat_type);
        					break;
        				}
        				bitauto.setText(text);
        				bitauto.setRegions(regions);
        				bitauto.setModels(styles);
        				bitauto.setPlaces(places);
        				bitauto.setAudienceId(id);
        				// 处理自动匹配车型的需求。
        				// material_audience.auto_matche,material_audience.compete_models
        				// 0:不需要匹配.1:需要匹配，未完成。2:匹配完成
        				if (autoMatche == 2) {
        					bitauto.setCompetitiveModels(competeModels);
        				}
        				// 非车型标签.
        				if ((tags != null) && (tags.size() > 0)) {
        					bitauto.getModels().addAll(tags);
        				}
        				bitauto.setWapLinkUrl(wap_link_url);
        				bitauto.setTitle(title);
        				bitauto.setDescription(description);
        				bitauto.setAdvisterId(id);
        				bitauto.setAdvLimitVisitorDisplayDay(limitVisitorDisplayDay);
        				bitauto.setCampExposeVisitDayLimit(exposeVisitDayLimit);
        				bitauto.setCampExposeVisitHourLimit(exposeVisitHourLimit);
        				
        				if (bitauto.getMatType() == MaterialType.TEXT || bitauto.getMatType() == MaterialType.TELETEXT) {
    						// 文本物料不再使用老莫生成的创意，直接用title。解决：title截断以后，创意不同但是title相同的问题。
        					bitauto.setCreative(bitauto.getTitle());
    						// 默认/非精准物料
    						if (((bitauto.getRegions() == null)
    								|| (bitauto.getRegions().size() == 0 || (bitauto.getRegions().size()==1 && bitauto.getRegions().contains(0))))
    								&& ((bitauto.getModels() == null)
    								|| (bitauto.getModels().size() == 0) || (bitauto.getModels().size()==1 && bitauto.getModels().contains(0)))) {
    							// 默认/非精准物料不受地域限制
    							bitauto.setRegions(new ArrayList<Integer>(Arrays.asList(0)));
    							unAccurateTextMaterial.add(bitauto);
    						 }else{
    							accurateTextMaterial.add(bitauto);
    						 }
    					}
        				
        				if(bitauto.getMatType() == MaterialType.TELETEXT){
    						if (((bitauto.getRegions() == null)
    								|| (bitauto.getRegions().size() == 0 || (bitauto.getRegions().size()==1 && bitauto.getRegions().contains(0))))
    								&& ((bitauto.getModels() == null)
    								|| (bitauto.getModels().size() == 0) || (bitauto.getModels().size()==1 && bitauto.getModels().contains(0)))) {
    							// 默认/非精准物料不受地域限制
    							bitauto.setRegions(new ArrayList<Integer>(Arrays.asList(0)));
    							unAccurateEletextMaterial.add(bitauto);
    						}else{
    							accurateEletextMaterial.add(bitauto);
    						}
    					}
        				
        				if(bitauto.getMatType() == MaterialType.IMAGE || bitauto.getMatType() == MaterialType.FLASH || bitauto.getMatType() == MaterialType.VIDEO){
    						// 默认/非精准物料放到一起，实现定投. 
    						if (((bitauto.getRegions() == null)
    								|| (bitauto.getRegions().size() == 0 || (bitauto.getRegions().size()==1 && bitauto.getRegions().contains(0))))
    								&& ((bitauto.getModels() == null)
    								|| (bitauto.getModels().size() == 0) || (bitauto.getModels().size()==1 && bitauto.getModels().contains(0)))) {
    							// 默认/非精准物料不受地域限制
    							bitauto.setRegions(new ArrayList<Integer>(Arrays.asList(0)));
    							String size = bitauto.getWidth()+ Constant.BITAUTO_MATERIAL_SIZE_SEPARATOR+ bitauto.getHeight();
    							if (unAccurateMaterial.containsKey(size)) {
    								unAccurateMaterial.get(size).add(bitauto);
    							} else {
    								unAccurateMaterial.put(size,new ArrayList<BitautoMaterial>(Arrays.asList(bitauto)));
    							}
    						}else{
        						String size = bitauto.getWidth()+ Constant.BITAUTO_MATERIAL_SIZE_SEPARATOR+ bitauto.getHeight();
        						if (accurateMaterial.containsKey(size)) {
        							accurateMaterial.get(size).add(bitauto);
        						} else {
        							accurateMaterial.put(size, new ArrayList<BitautoMaterial>(Arrays.asList(bitauto)));
        						}
    						}
    					}
        			}
        		}
        		
        		camp.setId(id);                                             //设置广告计划ID
        		camp.setIsPmp(isPmp);                                       //设置是否为开启了PMP的广告计划标识
        		camp.setPlaces(places);                                     //设置其定投广告位
        		camp.setExposeVisitDayLimit(exposeVisitDayLimit);           //设置其广告计划按天每日每访客的曝光数限定
        		camp.setExposeVisitHourLimit(exposeVisitHourLimit);         //设置其广告计划按天每小时每访客的曝光数限定
        		
        		camp.setBitautoMaterial(accurateMaterial);                  //设置图片的每PMP广告计划→精准素材
        		camp.setDefaultBitautoMaterial(unAccurateMaterial);         //设置图片的每PMP广告计划→默认素材
        		
        		camp.setBitautoEletextMaterial(accurateEletextMaterial);	//设置图文的每PMP广告计划→精准素材
        		camp.setDefaultEletextMaterial(unAccurateEletextMaterial);  //设置图文的每PMP广告计划→精准素材
        		
        		camp.setBitautoMaterialText(accurateTextMaterial);          //设置广计文本PMP→精准素材
        		camp.setDefaultBitautoMaterialText(unAccurateTextMaterial); //设置广计文本PMP→默认素材
        		
        		if(places == null || places.size() ==0){
        			if(isPmp == 1){
        				unDirect.add(camp);//非定投PMP广告计划
        			}else{
        				general.add(camp); //非定投非PMP广告计划
        			}
        		}else{
        			if(isPmp ==1){
        				direct.add(camp);  //定投广告计划
        			}else{                 
        				general.add(camp); //非定投非PMP广告计划
        			}
        		}
        	}
        	ad.setDirectional(direct);
        	ad.setUnDirectional(unDirect);
        	ad.setGeneral(general);
        	
        	if(!advisterMap.containsKey(id)){
        		advisterMap.put(id, ad);
        	}
        }
        return advisterMap;
	}
}
