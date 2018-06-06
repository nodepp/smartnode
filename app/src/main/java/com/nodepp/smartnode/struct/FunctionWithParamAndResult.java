package com.nodepp.smartnode.struct;

/**
 * Created by yuyue on 2017/8/15.
 */
public abstract class FunctionWithParamAndResult<Param,Result> extends Function {
    public FunctionWithParamAndResult(String functionName) {
        super(functionName);
    }
    public abstract Result function(Param param);
}
