package sz.future.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sz.future.conn.DBConnectionManager;
import sz.future.domain.MdDay;
import sz.future.domain.MdTick;
import sz.future.test.test1.Global;
import sz.future.util.ImportData;



public class FutureDao {
	protected static final Log log = LogFactory.getLog(FutureDao.class);
	private Connection conn;
	private ResultSet rs;
	private PreparedStatement pst;
	
	private SimpleDateFormat sfDate = new SimpleDateFormat("yyyy-MM-dd");
	private SimpleDateFormat sfTime = new SimpleDateFormat("HH:mm:ss");
	
	public FutureDao(){
		conn = null;
	    rs = null;
	    pst = null;
	}
	
	public void saveFutureHistory(List<String []> data) throws SQLException{
		conn = DBConnectionManager.getConnection();
		String sql = "INSERT INTO tb_qh_history_2013 (date, time, price, volume, volume_total, position_change, price_b1, volume_b1, price_s1, volume_s1, bs, name) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		Iterator<String []> it = data.iterator();
		while(it.hasNext()){
			String[] str = it.next();
			pst = (PreparedStatement) conn.prepareStatement(sql);
//			pstm.setDate(1, new Date().parse(str[0]));
		}
	}
	
	public void saveDayProfit(double profit) {
		conn = DBConnectionManager.getConnection();
		String sql = "INSERT INTO tb_day_profit (trading_date, profit, instrument_id) VALUES (?, ?, ?)";
		try {
			pst = (PreparedStatement) conn.prepareStatement(sql);
//			pst.setDate(1, new java.sql.Date(sz.future.test.test2.Global.tradingDay.getTime()));
			pst.setDate(1, new java.sql.Date(sz.future.test.test1.Global.tradingDay.getTime()));
			pst.setDouble(2, profit);
			pst.setString(3, Global.test_instrument_id);
			pst.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnectionManager.closePreparedStatement(pst);
			DBConnectionManager.closeConnection(conn);
		}
	}
	
	public void saveMdTick(List<MdTick> data){
		conn = DBConnectionManager.getConnection();
		int index = 0;
		StringBuffer insert = new StringBuffer("");
		for(int i=0; i < data.size(); i++){
			insert.append("(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?),");
		}
		String query = "INSERT INTO tb_md_day_2013 (instrument_id, trading_day, update_time, last_price, volume, property, bs, b1_price, s1_price, b1_volume, s1_volume, total_volume, highest_price, lowest_price) VALUES "+insert.toString().substring(0,insert.length()-1);
		
		try {
			conn.setAutoCommit(false);
			pst = conn.prepareStatement(query);
			pst.execute("SET FOREIGN_KEY_CHECKS=0");
			for (MdTick tick : data) {
				pst.setString(index+1, ImportData.instrument_id);
				try {
					System.err.println("......."+tick.getTradingDay()); 
					pst.setDate(index+2, new java.sql.Date(sfDate.parse(tick.getTradingDay()).getTime()));
					pst.setTime(index+3, new java.sql.Time(sfTime.parse(tick.getUpdateTime()).getTime()));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				pst.setDouble(index+4, tick.getLastPrice());
				
				pst.setInt(index+5, tick.getVolume());
				pst.setInt(index+6, tick.getProperty());
				pst.setString(index+7, tick.getBs());
				pst.setDouble(index+8, tick.getB1Price());
				pst.setDouble(index+9, tick.getS1Price());
				pst.setInt(index+10, tick.getB1Volume());
				pst.setInt(index+11, tick.getS1Volume());
				pst.setInt(index+12, tick.getTotalVolume());
				pst.setDouble(index+13, tick.getHighestPrice());
				pst.setDouble(index+14, tick.getLowestPrice());
				index = index+14;
			}
//			System.out.println(query);
			pst.executeUpdate();
			conn.commit();
//			System.err.println("LikesCount - Data has been saved.");
			System.err.println("MdTick - Saved: "+data.size());
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnectionManager.closePreparedStatement(pst);
			DBConnectionManager.closeConnection(conn);
		}
	}
	
	
	
	public List<MdDay> loadDayData(String instrumentId){
		List<MdDay> list = new ArrayList<MdDay>();
		conn = DBConnectionManager.getConnection();
		String query = "SELECT instrument_id,trading_day,last_price,total_volume FROM tb_md_day_2013 WHERE instrument_id = ? ORDER BY trading_day desc";
		try {
			pst = conn.prepareStatement(query);
			pst.setString(1, instrumentId);
			rs = pst.executeQuery();
			while (rs.next()) {
				MdDay md = new MdDay();
				md.setInstrumentID(rs.getString("instrument_id"));
				md.setTradingDay(rs.getDate("trading_day"));
				md.setLastPrice(rs.getDouble("last_price"));
				md.setTotalVolume(rs.getInt("total_volume"));
				list.add(md);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnectionManager.closeResultSet(rs);
			DBConnectionManager.closePreparedStatement(pst);
			DBConnectionManager.closeConnection(conn);
		}
		return list;
	}
	
	/**
	 * 最新价集合
	 * @param instrumentId
	 * @return
	 */
	public Map<Date, Double> loadDayData1(String instrumentId){
		Map<Date, Double> map = new LinkedHashMap<Date, Double>();
		conn = DBConnectionManager.getConnection();
		String query = "SELECT instrument_id,trading_day,last_price,total_volume FROM tb_md_day_2013 WHERE instrument_id = ? ORDER BY trading_day desc";
		try {
			pst = conn.prepareStatement(query);
			pst.setString(1, instrumentId);
			rs = pst.executeQuery();
			while (rs.next()) {
				map.put(rs.getDate("trading_day"), rs.getDouble("last_price"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnectionManager.closeResultSet(rs);
			DBConnectionManager.closePreparedStatement(pst);
			DBConnectionManager.closeConnection(conn);
		}
		return map;
	}
	
	/**
	 * 最高价集合
	 * @param instrumentId
	 * @return
	 */
	public Map<Date, Double> loadDayData2(String instrumentId){
		Map<Date, Double> map = new LinkedHashMap<Date, Double>();
		conn = DBConnectionManager.getConnection();
		String query = "SELECT trading_day,highest_price FROM tb_md_day_2013 WHERE instrument_id = ? ORDER BY trading_day desc";
		try {
			pst = conn.prepareStatement(query);
			pst.setString(1, instrumentId);
			rs = pst.executeQuery();
			while (rs.next()) {
				map.put(rs.getDate("trading_day"), rs.getDouble("highest_price"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnectionManager.closeResultSet(rs);
			DBConnectionManager.closePreparedStatement(pst);
			DBConnectionManager.closeConnection(conn);
		}
		return map;
	}
	
	/**
	 * 最低价集合
	 * @param instrumentId
	 * @return
	 */
	public Map<Date, Double> loadDayData3(String instrumentId){
		Map<Date, Double> map = new LinkedHashMap<Date, Double>();
		conn = DBConnectionManager.getConnection();
		String query = "SELECT trading_day,lowest_price FROM tb_md_day_2013 WHERE instrument_id = ? ORDER BY trading_day desc";
		try {
			pst = conn.prepareStatement(query);
			pst.setString(1, instrumentId);
			rs = pst.executeQuery();
			while (rs.next()) {
				map.put(rs.getDate("trading_day"), rs.getDouble("lowest_price"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnectionManager.closeResultSet(rs);
			DBConnectionManager.closePreparedStatement(pst);
			DBConnectionManager.closeConnection(conn);
		}
		return map;
	}
	/**
	 * @param days 获取几天前的价格
	 * @param instrumentId 当前合约名
	 * @param tradingDay 当前交易日
	 * @return
	 */
	public List<Double> getPriceArray(int days, String instrumentId, Date tradingDay, int type){
		List<Double> array = new ArrayList<Double>();
		conn = DBConnectionManager.getConnection();
		String query1 = "SELECT last_price FROM tb_md_day_2013 WHERE instrument_id = ? and trading_day = ?";
		String query2 = "";
//		switch (type){
//			case 1:query2 = "SELECT last_price FROM tb_md_day_2013 WHERE instrument_id = ? and trading_day < ? ORDER BY trading_day desc limit ?";
//			case 2:query2 = "SELECT highest_price FROM tb_md_day_2013 WHERE instrument_id = ? and trading_day < ? ORDER BY trading_day desc limit ?";
//			case 3:query2 = "SELECT lowest_price FROM tb_md_day_2013 WHERE instrument_id = ? and trading_day < ? ORDER BY trading_day desc limit ?";
//		}
		if(type == 1){
			query2 = "SELECT last_price FROM tb_md_day_2013 WHERE instrument_id = ? and trading_day < ? ORDER BY trading_day desc limit ?";
		} else if (type == 2) {
			query2 = "SELECT highest_price FROM tb_md_day_2013 WHERE instrument_id = ? and trading_day < ? ORDER BY trading_day desc limit ?";
		} else if (type == 3) {
			query2 = "SELECT lowest_price FROM tb_md_day_2013 WHERE instrument_id = ? and trading_day < ? ORDER BY trading_day desc limit ?";
		}
		try {
			pst = conn.prepareStatement(query1);
			pst.setString(1, instrumentId);
			pst.setDate(2, new java.sql.Date(tradingDay.getTime()));
			rs = pst.executeQuery();
			if(rs.next()){
				pst = conn.prepareStatement(query2);
				pst.setString(1, instrumentId);
				pst.setDate(2, new java.sql.Date(tradingDay.getTime()));
				pst.setInt(3, days);
				rs = pst.executeQuery();
				while (rs.next()) {
					if(type == 1){
						array.add(rs.getDouble("last_price"));
					} else if (type == 2) {
						array.add(rs.getDouble("highest_price"));
					} else if (type == 3) {
						array.add(rs.getDouble("lowest_price"));
					}
//					switch (type){
//						case 1:array.add(rs.getDouble("last_price"));
//						case 2:array.add(rs.getDouble("highest_price"));
//						case 3:array.add(rs.getDouble("lowest_price"));
//					}
				}
			} else {
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			DBConnectionManager.closeResultSet(rs);
			DBConnectionManager.closePreparedStatement(pst);
			DBConnectionManager.closeConnection(conn);
		}
		return array;
	}
	
	
}
