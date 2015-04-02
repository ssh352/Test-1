package sz.future.monitor;

import java.util.Iterator;
import java.util.Map;

import sz.future.dao.FutureDevDao;
import sz.future.domain.InverstorPosition;
import sz.future.test.test1.Global;
import sz.future.trader.comm.ServerParams;
import sz.future.trader.comm.Super;
import sz.future.util.StatisticsUtil;
import sz.future.util.TraderUtil;

/**
 * 行情监测线程
 */
public class TestMonitor extends Thread{
	private String[] instruments ;//,"pp1501","sr1501","jd1501","pta1501","fg1501","rm1501"
	private Map<String, double[]> tickData;
	private double[] lastTick;
	private FutureDevDao dao = new FutureDevDao();
	
	public TestMonitor(){
		//克隆合约数组
		instruments = new String[ServerParams.instruments2.length];
		for (int i = 0; i < ServerParams.instruments2.length; i++) {
//			System.out.println("ServerParams.instruments2[i]: "+ServerParams.instruments2[i]);
			instruments[i] = ServerParams.instruments2[i];
			//装载历史收盘价（一段时间内）
			Super.HISTORY_CLOSE_PRICE.put(instruments[i], dao.getHistoryClosePrice(Super.historyDateRange, instruments[i]));
		}
		while(true){
			if(init()){
				break;
			} else {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private boolean init(){
		boolean bool = false;
		if(TraderUtil.qryTradingAccount()==0){
			bool = true;
			System.out.println("查询资金成功");
		} else {
			bool =false;
			System.err.println("查询资金失败");
		}
		if(TraderUtil.qryPosition()==0){
			bool = true;
			System.out.println("查询持仓成功");
		} else {
			bool = false;
			System.err.println("查询持仓失败");
		}
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(Super.INVESTOR_POSITION.size() > 0){
			Iterator<String> it = Super.INVESTOR_POSITION.keySet().iterator();
			while(it.hasNext()){
				if(TraderUtil.qryPositionDetail(it.next())==0){
					bool = true;
					System.out.println("查询开仓价成功");
				} else {
					bool = false;
					System.err.println("查询开仓价失败");
				}
			}
		}
		return bool;
	}
	/**
	 * @param args
	 */
	public void run() {
		System.err.println("------------启动行情监测线程-------------");
		while(true){
			tickData = Super.TICK_DATA;
//			System.out.println(ServerParams.instruments[0]);
			
			for (int i = 0; i < instruments.length; i++) {
				lastTick = tickData.get(instruments[i]);//获取当前合约的最新行情
				if(lastTick == null){
//					System.err.println(instruments[i]+" TICK行情丢失1");
					continue;
				} else if (lastTick[0] ==0){
//					System.err.println(instruments[i]+" TICK行情丢失2");
					break;
				}
//				System.out.println("监测"+instruments[i] + " 价格：" +lastTick[0]);
				Double highestPpriceOfPeriod = dao.getLimitPrice(Global.period, instruments[i], 1);
				Double lowestPriceOfPeriod = dao.getLimitPrice(Global.period, instruments[i], 2);
				double preMA5 = StatisticsUtil.getClosePriceTotal(Super.HISTORY_CLOSE_PRICE.get(instruments[i]), 5)/5;//前一天的MA5
				double preMA10 = StatisticsUtil.getClosePriceTotal(Super.HISTORY_CLOSE_PRICE.get(instruments[i]), 10)/10;//前一天的MA10
				double currMA5 = StatisticsUtil.getClosePriceTotal(Super.HISTORY_CLOSE_PRICE.get(instruments[i]), 4)/5 + lastTick[0]/5;//当前的的MA5
				double currMA10 = StatisticsUtil.getClosePriceTotal(Super.HISTORY_CLOSE_PRICE.get(instruments[i]), 9)/10 + lastTick[0]/10;//当前的的MA10
				double [] historyClosePrices = Super.HISTORY_CLOSE_PRICE.get(instruments[i]);
				if(historyClosePrices.length < Super.historyDateRange){
					System.err.println(instruments[i]+"合约的历史数据不完整....");
				}
//				System.out.println("instrumentId: "+instruments[i]);
				if(Super.INVESTOR_POSITION.size() == 0 || Super.INVESTOR_POSITION.get(instruments[i]) == null){
					//没有持仓该合约
					if((lastTick[0] - highestPpriceOfPeriod) > Global.breakPoint && (currMA5 > currMA10)) {
							//买多
							TraderUtil.orderInsert(instruments[i], true, 5, "0", lastTick[5]);
							System.out.println(instruments[i] + "： 买多5手 "+lastTick[0]);
					} else if ((lowestPriceOfPeriod - lastTick[0]) > Global.breakPoint && (currMA10 > currMA5)) {
							//卖空
							TraderUtil.orderInsert(instruments[i], false, 5, "0", lastTick[6]);
							System.out.println(instruments[i] + "： 卖空5手 "+lastTick[0]);
					} 
				} else {
					//有持仓该合约
					boolean closeFlag1 = false ;//浮亏超过限定值Global.floatSpace
					boolean closeFlag2 = false ;//前一日MA5小于或大于MA10
//					boolean closeFlag3 = false ;//当前利润小于最高利润百分比
					InverstorPosition inverstorPostion = Super.INVESTOR_POSITION.get(instruments[i]);
					char c = inverstorPostion.getPosiDirectionType();
					if(c=='2'){//多仓 
						if(preMA5 < preMA10){
							closeFlag2 = true;
						}
						closeFlag1 = (Super.INVESTOR_POSITION_OPEN_PRICE.get(instruments[i]) - lastTick[0]) > ServerParams.floatSpace*lastTick[0];
						if(closeFlag1||closeFlag2){
							InverstorPosition ip = Super.INVESTOR_POSITION.get(instruments[i]);
							if(ip.getPosition() > 0){//平今仓
								TraderUtil.orderInsert(instruments[i], false, 5, "3", lastTick[6]);
								System.out.println(instruments[i] + "： 平今多仓5手 "+lastTick[0]);
							} else if (ip.getYdposition() > 0){//平昨仓
								TraderUtil.orderInsert(instruments[i], false, 5, "1", lastTick[6]);
								System.out.println(instruments[i] + "： 平昨多仓5手 "+lastTick[0]);
							}
						}
					} else if(c=='3'){//空仓
						if(preMA5 > preMA10){
							closeFlag2 = true;
						}
						System.out.println("instruments[i]:  "+instruments[i]);
						closeFlag1 = (lastTick[0] - Super.INVESTOR_POSITION_OPEN_PRICE.get(instruments[i])) > ServerParams.floatSpace*lastTick[0];
						if(closeFlag1||closeFlag2){
							InverstorPosition ip = Super.INVESTOR_POSITION.get(instruments[i]);
							if(ip.getPosition() > 0){//平今仓
								TraderUtil.orderInsert(instruments[i], true, 5, "3", lastTick[5]);
								//从持仓容器里移除
								Super.INVESTOR_POSITION_OPEN_PRICE.remove(instruments[i]);
								Super.INVESTOR_POSITION.remove(instruments[i]);
								System.out.println(instruments[i] + "： 平今空仓5手 "+lastTick[0]);
							} else if (ip.getYdposition() > 0){//平昨仓
								TraderUtil.orderInsert(instruments[i], true, 5, "1", lastTick[5]);
								//从持仓容器里移除
								Super.INVESTOR_POSITION_OPEN_PRICE.remove(instruments[i]);
								Super.INVESTOR_POSITION.remove(instruments[i]);
								System.out.println(instruments[i] + "： 平昨空仓5手 "+lastTick[0]);
							}
						}
					}
				}
//				System.err.println(instruments[i] + " : " + lastTick[0] + ":" + lastTick[1] + " : " + lastTick[2] + ":" + lastTick[3] + " : " + lastTick[4] + ":" + lastTick[5] + " : " + lastTick[6]);
			}
			
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
