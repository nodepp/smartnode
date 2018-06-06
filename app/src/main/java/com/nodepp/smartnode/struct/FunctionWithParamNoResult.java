package com.nodepp.smartnode.struct;

/**
 * Created by yuyue on 2017/8/15.
 */
public abstract class FunctionWithParamNoResult<Param> extends Function {
    public FunctionWithParamNoResult(String functionName) {
        super(functionName);
    }
    public abstract void function(Param param);
}
