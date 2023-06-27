package com.zyh.ce4j.strategy;

import com.zyh.ce4j.domain.Result;

public interface CheckStrategy {
	/**
	 * 根据最后一行输出，判定执行结果
	 * @param lastPrint
	 * @return
	 */
	public Result endCheck(String lastPrint);
	
	/**
	 * 策略:不检查输出结果
	 */
	public static CheckStrategy NONE_CHECK_STRATEGY_INSTANCE = new CheckStrategy() {
		@Override
		public Result endCheck(String lastPrint) {
			// TODO Auto-generated method stub
			return new Result(Result.Status.UNKNOWN, "未检查执行结果，执行结果未知。");
		}
	};
	
}
