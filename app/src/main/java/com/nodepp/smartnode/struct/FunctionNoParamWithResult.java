package com.nodepp.smartnode.struct;

/**
 * Created by yuyue on 2017/8/15.
 */
public abstract class FunctionNoParamWithResult<Result> extends Function {
    public FunctionNoParamWithResult(String functionName) {
        super(functionName);
    }
    public abstract Result function();
}
