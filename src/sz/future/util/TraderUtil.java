package sz.future.util;

import static org.hraink.futures.ctp.thostftdcuserapidatatype.ThostFtdcUserApiDataTypeLibrary.THOST_FTDC_CC_Immediately;
import static org.hraink.futures.ctp.thostftdcuserapidatatype.ThostFtdcUserApiDataTypeLibrary.THOST_FTDC_D_Buy;
import static org.hraink.futures.ctp.thostftdcuserapidatatype.ThostFtdcUserApiDataTypeLibrary.THOST_FTDC_D_Sell;
import static org.hraink.futures.ctp.thostftdcuserapidatatype.ThostFtdcUserApiDataTypeLibrary.THOST_FTDC_FCC_NotForceClose;
import static org.hraink.futures.ctp.thostftdcuserapidatatype.ThostFtdcUserApiDataTypeLibrary.THOST_FTDC_OPT_LimitPrice;
import static org.hraink.futures.ctp.thostftdcuserapidatatype.ThostFtdcUserApiDataTypeLibrary.THOST_FTDC_TC_GFD;
import static org.hraink.futures.ctp.thostftdcuserapidatatype.ThostFtdcUserApiDataTypeLibrary.THOST_FTDC_VC_AV;

import org.hraink.futures.ctp.thostftdcuserapistruct.CThostFtdcInputOrderActionField;
import org.hraink.futures.ctp.thostftdcuserapistruct.CThostFtdcInputOrderField;
import org.hraink.futures.ctp.thostftdcuserapistruct.CThostFtdcQryInvestorPositionDetailField;
import org.hraink.futures.ctp.thostftdcuserapistruct.CThostFtdcQryInvestorPositionField;
import org.hraink.futures.ctp.thostftdcuserapistruct.CThostFtdcQryOrderField;
import org.hraink.futures.ctp.thostftdcuserapistruct.CThostFtdcQryTradeField;
import org.hraink.futures.ctp.thostftdcuserapistruct.CThostFtdcQryTradingAccountField;

import sz.future.dao.FutureDevDao;
import sz.future.trader.comm.M;
import sz.future.trader.comm.ServerParams;
import sz.future.trader.comm.Super;
import sz.future.trader.console.TestTrader;

public class TraderUtil {
//	private static int requestID = 0;

	static FutureDevDao dao = new FutureDevDao();
	/**
	 * @param instrumentId 合约代码
	 * @param directionFlag 买卖方向 买：true  卖：fasle
	 * @param volumeTotal 申买/卖量
	 * @param offsetFlag 开平标志（开仓=0，平仓=1，强平=2，平今=3，平昨=4，强减=5，本地强平=6）
	 * @param limitPrice 价格 md[0]最新价 md[1]最高价 md[2]最低价 md[3]开盘价 md[4]前日收盘价 md[5]涨停价 md[6]跌停价 
	 * @return 
	 */
	public static int orderInsert(String instrumentId,  boolean directionFlag, int volumeTotal, String offsetFlag, double limitPrice){
		//下单操作
		CThostFtdcInputOrderField inputOrderField=new CThostFtdcInputOrderField();
		//期货公司代码
		inputOrderField.setBrokerID(ServerParams.BROKER_ID);
		//投资者代码
		inputOrderField.setInvestorID(ServerParams.USER_ID);
		// 合约代码
		inputOrderField.setInstrumentID(instrumentId);
		///报单引用
		inputOrderField.setOrderRef((++Super.currOrderRef)+"");
		System.err.println("OrderRef: " + (Super.currOrderRef));
		// 用户代码
		inputOrderField.setUserID(ServerParams.USER_ID);
		// 报单价格条件
		inputOrderField.setOrderPriceType(THOST_FTDC_OPT_LimitPrice);
		//买卖方向 买：true  卖：fasle
		if (directionFlag){
			inputOrderField.setDirection(THOST_FTDC_D_Buy);
		} else {
			inputOrderField.setDirection(THOST_FTDC_D_Sell);
		}
		// 组合开平标志
		inputOrderField.setCombOffsetFlag(offsetFlag);
		// 组合投机套保标志
		inputOrderField.setCombHedgeFlag("1");
		// 申买、申卖价格
		inputOrderField.setLimitPrice(limitPrice);
		// 手数量
		inputOrderField.setVolumeTotalOriginal(volumeTotal);
		// 有效期类型
		inputOrderField.setTimeCondition(THOST_FTDC_TC_GFD);
		// GTD日期
		inputOrderField.setGTDDate("");
		// 成交量类型
		inputOrderField.setVolumeCondition(THOST_FTDC_VC_AV);
		// 最小成交量
		inputOrderField.setMinVolume(0);
		// 触发条件
		inputOrderField.setContingentCondition(THOST_FTDC_CC_Immediately);
		// 止损价
		inputOrderField.setStopPrice(0);
		// 强平原因
		inputOrderField.setForceCloseReason(THOST_FTDC_FCC_NotForceClose);
		// 自动挂起标志
		inputOrderField.setIsAutoSuspend(0);
//		sleepThread();
		System.out.println("##输入报单## " + instrumentId); 
		int returnMsg = TestTrader.traderApi.reqOrderInsert(inputOrderField, ++Super.requestID);
		return returnMsg;
	}
	
	public static int orderAction(String instrumentId){
		CThostFtdcInputOrderActionField actionField = new CThostFtdcInputOrderActionField();
//		CThostFtdcOrderActionField actionField = new CThostFtdcOrderActionField();
		actionField.setBrokerID(ServerParams.BROKER_ID);
		actionField.setInvestorID(ServerParams.USER_ID);
		actionField.setInstrumentID(instrumentId);
		actionField.setActionFlag('0');//0删除 3修改
//		sleepThread();
		return TestTrader.traderApi.reqOrderAction(actionField, ++Super.requestID);
	}
	
	/**
	 * 查询持仓明细 可一次查全部持仓明细
	 * @return
	 */
	public static int qryPositionDetail(){
		CThostFtdcQryInvestorPositionDetailField positionField = new CThostFtdcQryInvestorPositionDetailField();
		positionField.setBrokerID(ServerParams.BROKER_ID);
//		positionField.setInstrumentID(instrumentId);
		positionField.setInvestorID(ServerParams.USER_ID);
		sleepThread();
		System.out.println("##查询全部持仓明细## ");
		int i = TestTrader.traderApi.reqQryInvestorPositionDetail(positionField, ++Super.requestID);
		sleepThread();
		return i;
	}
	
	/**
	 * 查询持仓 可一次查全部持仓
	 * @param instrumentId
	 * @return
	 */
	public static int qryPosition(){
		CThostFtdcQryInvestorPositionField positionField = new CThostFtdcQryInvestorPositionField();
		positionField.setBrokerID(ServerParams.BROKER_ID);
//		positionField.setInstrumentID("");
		positionField.setInvestorID(ServerParams.USER_ID);
//		Super.INVESTOR_POSITION.clear();//清理历史持仓情况
		sleepThread();
		System.out.println("##查询全部持仓##");
		return TestTrader.traderApi.reqQryInvestorPosition(positionField, ++Super.requestID);
	}
	
	/**
	 * 查询报单
	 * @return
	 */
	public static int qryOrder(String instrumentId){
		CThostFtdcQryOrderField orderField = new CThostFtdcQryOrderField();
		orderField.setBrokerID(ServerParams.BROKER_ID);
		orderField.setInstrumentID(instrumentId);
		orderField.setInvestorID(ServerParams.USER_ID);
		sleepThread();
		System.out.println("##查询报单## " + instrumentId);
		return TestTrader.traderApi.reqQryOrder(orderField, ++Super.requestID);
	}
	
	/**
	 * 查询资金明细
	 * @param instrumentId
	 * @return
	 */
	public static int qryTradingAccount(){
		CThostFtdcQryTradingAccountField accountField = new CThostFtdcQryTradingAccountField();
		accountField.setBrokerID(ServerParams.BROKER_ID);
		accountField.setInvestorID(ServerParams.USER_ID);
		sleepThread();
		System.out.println("##查询资金## ");
		return TestTrader.traderApi.reqQryTradingAccount(accountField, ++Super.requestID);
	}
	
	/**
	 * 查询成交单
	 * @return
	 */
	public static int qryTrade(){
		CThostFtdcQryTradeField tradeField = new CThostFtdcQryTradeField();
		tradeField.setBrokerID(ServerParams.BROKER_ID);
//		tradeField.setInstrumentID(instrumentId);
		tradeField.setInvestorID(ServerParams.USER_ID);
//		tradeField.setTradeID(M.instrumentId+M.currOrderRef);
		sleepThread();
		System.out.println("##查询成交## ");
		return TestTrader.traderApi.reqQryTrade(tradeField, ++Super.requestID);
	}
	
	private static void sleepThread(){
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
