package cn.com.cig.adsense.dao.cassandra;




import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;

/**
 * 
 * @author zgd
 * 
 */
public class UserDataDao {
	private static Logger logger = LoggerFactory.getLogger(UserDataDao.class);
	
	private static PreparedStatement statement;
	private static Session session;
	public UserDataDao() {
		session = CassandraFactoryDynamically.Instance().getSession();
		statement = session.prepare("select * from cigdmp.userdata where user_id=? ");
	}



	public UserData selectOne(String userId) {
		try {
			Statement bounds = statement.bind(userId);
			ResultSet result = session.execute(bounds);
			bounds.setConsistencyLevel(ConsistencyLevel.ONE);
			if (!result.isExhausted()) {
				UserData data = new UserData();
				data.setUser_id(userId);
				for (Row row : result) {
					data.setCity(row.getInt("city"));
					data.setProvince(row.getInt("province"));
					data.setCreated_time(row.getDate("created_time"));
					data.setLast_model(row.getInt("last_model"));
					data.setLast_visited(row.getDate("last_visited"));

					data.setEcheId(row.getString("echeId"));
					data.setUa(row.getInt("ua"));
					data.setStatus(row.getInt("status"));

					data.setMsc(row.getMap("msc", Integer.class, Integer.class));
					data.setOsc(row.getMap("osc", Integer.class, Integer.class));
					return data;
				}
			}
		} catch (Exception e) {
			logger.error("error redis:", e);
		}finally{
			//session.close();
		}
		return null;
	}
	
	public UserData selectOne(String userId, int timeout){
		try {
			Statement bounds = statement.bind(userId);
			bounds.setConsistencyLevel(ConsistencyLevel.ONE);
			ResultSetFuture result = session.executeAsync(bounds);
			for (Row row : result.get(timeout, TimeUnit.MILLISECONDS)) {
				UserData data = new UserData();
				data.setUser_id(userId);
				data.setCity(row.getInt("city"));
				data.setProvince(row.getInt("province"));
				data.setCreated_time(row.getDate("created_time"));
				data.setLast_model(row.getInt("last_model"));
				data.setLast_visited(row.getDate("last_visited"));
	
				data.setEcheId(row.getString("echeId"));
				data.setUa(row.getInt("ua"));
				data.setStatus(row.getInt("status"));
				data.setMsc(row.getMap("msc", Integer.class, Integer.class));
				data.setOsc(row.getMap("osc", Integer.class, Integer.class));
				return data;
			}
		} catch (Exception e) {
			logger.error("error cassandra timeout method:", e);
		}finally{
			//session.close();
		}
		return null;
		
	}
}
