package sz.future.test.test1;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sz.future.dao.FutureDao;
import sz.future.domain.MdDay;
import sz.future.util.CsvDataUtil;

public class Test1 {

	private static final String[] strs = { "AG", "AU", "CU", "FG", "J", "JM",
		"L", "M", "ME", "OI", "P", "RB", "RM", "RU", "SR", "TA", "V", "Y" };
	private static final String[] months = { "01", "02", "03", "04", "05",
			"06", "07", "08", "09", "10", "11", "12" };
	private static final String[] days = { "01", "02", "03", "04", "05", "06",
			"07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17",
			"18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28",
			"29", "30", "31" };
	private static final int year = 2013;
	public static final String test_instrument_id = "RM1401";
	public static String instrument_id = "";
	public static String full_path = "";
	private static Pattern pt = Pattern.compile(
			"([A-Z]+)([0-9]+)_([0-9]+).CSV", Pattern.DOTALL
					+ Pattern.CASE_INSENSITIVE);
	private static Matcher mt = null;
	private static double ds = 0;
	private static int count = 0;
	private static FutureDao dao = new FutureDao();

	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		List<MdDay> list = dao.loadDayData(test_instrument_id);
		
		queryMd();
//		for (int i = 100; i <= 900; i += 100) {
//			for (int j = 1; j <= 31; j++) {
//				try {
//					init("F:/baiduyundownload/20130" + (i + j) + "/M05_20130"
//							+ (i + j) + ".csv");
//				} catch (Exception e) {
//					continue;
//				}
//				strategy();
//				ds += Global.point;
//				count += Global.transactionCount;
////				Global.init();
//				Thread.sleep(500);
//			}
//
//			// init("F:/baiduyundownload/20131114/M05_20131114.csv");
//		}
		System.out.println(ds);
		System.out.println(count);
		// init("E:/NEW/Book1.csv");
	}

	private static void testTrading(String path) {
		// load csv
		List<String[]> csvList = CsvDataUtil.readeCsv(path);
		int size = csvList.size();
		Global.initArray(size);
		// fill to array
		for (int row = 0; row < csvList.size(); row++) {
			Global.lastPriceArray[row] = Double.parseDouble(csvList.get(row)[2]);
			Global.volumeArray[row] = Integer.parseInt(csvList.get(row)[3]);
			Global.volumeTotalArray[row] = Integer.parseInt(csvList.get(row)[4]);
			Global.priceS1Array[row] = Double.parseDouble(csvList.get(row)[6]);
			Global.priceB1Array[row] = Double.parseDouble(csvList.get(row)[8]);
			// System.out.println(Global.priceArray[row]);
		}
	}
	
	private static void queryMd() {
		String instrumentYear = "";
		for (int i = 0; i < months.length; i++) {
			for (int j = 0; j < days.length; j++) {
				StringBuffer sb = new StringBuffer("e:/BaiduYunDownload/");
				String dateDir = year + months[i] + days[j];
				sb.append(dateDir).append("/");
				for (int k = 0; k < strs.length; k++) {
					Map<Integer, String> map = new TreeMap<Integer, String>();
					for (int k2 = 0; k2 < months.length; k2++) {
						StringBuffer sb2 = new StringBuffer();
						sb2.append(strs[k]).append(months[k2]).append("_")
								.append(dateDir).append(".csv");
						String path = sb.toString() + sb2.toString();
						int count = CsvDataUtil.readCsvCount(path);
						if (count > 0) {
							map.put(CsvDataUtil.readCsvCount(path), path);
						}
					}
					if (map.size() == 0) {
						continue;
					}
					Iterator<Entry<Integer, String>> it = map.entrySet()
							.iterator();
					Entry<Integer, String> entry = null;
					while (it.hasNext()) {
						entry = it.next();
					}
					full_path = entry.getValue();
					System.out.println("行数：" + entry.getKey() + "路径：" + entry.getValue());
					// 保存数据
					mt = pt.matcher(entry.getValue());
					if (mt.find()) {
//						System.out.println(months[i] +" : "+mt.group(2));
						// 如果行情月份小于合约月份，年份为13
						if (Integer.parseInt(months[i]) < Integer.parseInt(mt
								.group(2))) {
							instrumentYear = "13";
						} else {
							instrumentYear = "14";
						}
						instrument_id = mt.group(1) + instrumentYear + mt.group(2);
//						System.out.println("合约名: "+mt.group(1) + instrumentYear
//								+ mt.group(2));
//						System.out.println("合约商品: " + mt.group(1));
//						System.out.println("合约月份: " + mt.group(2));
//						System.out.println("行情日期: " + mt.group(3));
					}
					//如果查找出的合约和要测试的合约相同就进行测试
					if(test_instrument_id.equals(instrument_id)){
						testTrading(full_path);
					}
				}
			}
		}

	}

	private static void strategy() {
		System.err.println("================================================");
		
		print();
	}

	/**
	 * 交易
	 * 
	 * @param priceB1 买一价
	 * @param priceS1 卖一价
	 * @param oc 开平标志 open:true close:false
	 * @param bs 买卖方向 buy:true sell:false
	 */
	private static void trader(double priceB1, double priceS1, boolean oc,
			boolean bs) {
		double profit = 0;
		if (bs) {
			if (oc) {// 买多开仓
				Global.bs = true;
				Global.openOrClose = true;
				Global.positionPrice = priceS1;
				Global.transactionCount++;
				Global.longCount++;
			} else {// 买多平仓
				profit = priceB1 - Global.positionPrice;
				Global.point = Global.point + profit;
				System.out.println("第" + Global.transactionCount + "次交易："
						+ profit);
				if (profit > 0) {
					Global.profitCount++;
				} else if (profit < 0) {
					Global.lossCount++;
				} else {
					Global.balanceCount++;
				}
				mark();
			}
		} else {
			if (oc) {// 卖空开仓
				Global.bs = false;
				Global.openOrClose = false;
				Global.positionPrice = priceB1;
				Global.transactionCount++;
				Global.shortCount++;
			} else {// 卖空平仓
				profit = Global.positionPrice - priceS1;
				Global.point = Global.point + profit;
				System.out.println("第" + Global.transactionCount + "次交易："
						+ profit);
				if (profit > 0) {
					Global.profitCount++;
				} else if (profit < 0) {
					Global.lossCount++;
				} else {
					Global.balanceCount++;
				}
				mark();
			}
		}
	}

	private static void mark() {
		Global.positionPrice = 0;
		Global.transactionCount++;
		Global.closeCount++;
	}

	private static void print() {
		System.out.println("总交易次数: " + Global.transactionCount);
		System.out.println("买多交易次数:" + Global.longCount);
		System.out.println("卖空交易次数:" + Global.closeCount);
		System.out.println("点数:" + Global.point);
		System.out.println("盈次数：" + Global.profitCount);
		System.out.println("亏次数：" + Global.lossCount);
		System.out.println("平次数：" + Global.balanceCount);
	}
}
