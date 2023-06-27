package com.zyh.ce4j.strategy;

import com.zyh.ce4j.domain.Result;
import com.zyh.ce4j.util.StringUtils;

/**
 * 预实现的一个简单判定执行结果的策略：
 * 最后一条输出格式：0:XXXX,判定为执行成功
 * @author zhengyh
 *
 */
public class ColonEndStrategy implements CheckStrategy {

	@Override
	public Result endCheck(String lastPrint) {
    	if(StringUtils.isNotBlank(lastPrint) && lastPrint.contains(":")) {
    		String[] tags = lastPrint.split(":");
    		if(tags.length < 2 ) {
    			return new Result(Result.Status.FAILURE, lastPrint);
    		}
    		String codeStr = tags[0].trim();
    		//此处假定命令行执行结果,最后输出行： 0:xxxxxxx,作为成功
    		Result.Status status; 
    		if(StringUtils.isNumeric(codeStr) && Integer.parseInt(codeStr) == 0) {
    			status = Result.Status.SUCCESS;
    		} else {
    			status = Result.Status.FAILURE;
    		}
    		return new Result(status, tags[1]);
    	}
    	return new Result(Result.Status.FAILURE, lastPrint);
	}

}
