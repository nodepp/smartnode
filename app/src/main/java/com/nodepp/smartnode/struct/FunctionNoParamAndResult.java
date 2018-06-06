package com.nodepp.smartnode.struct;

/**
 * Created by yuyue on 2017/8/15.
 */
public abstract class FunctionNoParamAndResult extends Function{
    public FunctionNoParamAndResult(String functionName) {
        super(functionName);
    }
    public abstract void function();
}
