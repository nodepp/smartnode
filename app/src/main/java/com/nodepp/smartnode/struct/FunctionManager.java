package com.nodepp.smartnode.struct;

import java.util.HashMap;

/**
 * Created by yuyue on 2017/8/15.
 */
public class FunctionManager {

    private static FunctionManager mFunctions;
    public HashMap<String, FunctionNoParamAndResult> mFunctionNoParamAndResultMap;
    public HashMap<String, FunctionNoParamWithResult> mFunctionNoParamWithResultMap;
    public HashMap<String, FunctionWithParamNoResult> mFunctionWithParamNoResultMap;
    public HashMap<String, FunctionWithParamAndResult> mFunctionWithParamAndResultMap;

    public static synchronized FunctionManager getInstance() {
        if (mFunctions == null) {
            synchronized (FunctionManager.class) {
                if (mFunctions == null) {
                    mFunctions = new FunctionManager();
                }
            }
        }
        return mFunctions;
    }

    public FunctionManager addFunction(FunctionNoParamAndResult function) {
        if (mFunctionNoParamAndResultMap == null) {
            mFunctionNoParamAndResultMap = new HashMap<String, FunctionNoParamAndResult>();
        }
        mFunctionNoParamAndResultMap.put(function.mFunctionName, function);
        return this;
    }

    public FunctionManager addFunction(FunctionNoParamWithResult function) {
        if (mFunctionNoParamWithResultMap == null) {
            mFunctionNoParamWithResultMap = new HashMap<String, FunctionNoParamWithResult>();
        }
        mFunctionNoParamWithResultMap.put(function.mFunctionName, function);
        return this;
    }

    public FunctionManager addFunction(FunctionWithParamNoResult function) {
        if (mFunctionWithParamNoResultMap == null) {
            mFunctionWithParamNoResultMap = new HashMap<String, FunctionWithParamNoResult>();
        }
        mFunctionWithParamNoResultMap.put(function.mFunctionName, function);
        return this;
    }

    public FunctionManager addFunction(FunctionWithParamAndResult function) {
        if (mFunctionWithParamAndResultMap == null) {
            mFunctionWithParamAndResultMap = new HashMap<String, FunctionWithParamAndResult>();
        }
        mFunctionWithParamAndResultMap.put(function.mFunctionName, function);
        return this;
    }

    public void invokeFunction(String functionName) {
        FunctionNoParamAndResult f = null;
        if (mFunctionNoParamAndResultMap != null) {
            f = mFunctionNoParamAndResultMap.get(functionName);
            if (f != null) {
                f.function();
            }
//            if (f == null) {
//                throw new FunctionException("has no Function " + functionName);
//            }
        }
    }

    public <Param> void invokeFunction(String functionName, Param param) throws FunctionException {
        FunctionWithParamNoResult f = null;
        if (mFunctionWithParamNoResultMap != null) {
            f = mFunctionWithParamNoResultMap.get(functionName);
            if (f != null) {
                f.function(param);
            }
            if (f == null) {
                throw new FunctionException("has no Function " + functionName);
            }
        }
    }

    public <Result> Result invokeFunction(String functionName, Class<Result> c) throws FunctionException {
        FunctionNoParamWithResult f = null;
        if (mFunctionNoParamWithResultMap != null) {
            f = mFunctionNoParamWithResultMap.get(functionName);
            if (f != null) {
//                return (Result) f.function();//不安全
                if (c != null) {
                    return c.cast(f.function());
                }
            }
            if (f == null) {
                throw new FunctionException("has no Function " + functionName);
            }
        }
        return null;
    }

    public <Result, Param> Result invokeFunction(String functionName, Class<Result> c, Param param) throws FunctionException {
        FunctionWithParamAndResult f = null;
        if (mFunctionWithParamAndResultMap != null) {
            f = mFunctionWithParamAndResultMap.get(functionName);
            if (f != null) {
                if (c != null) {
                    return c.cast(f.function(param));
                }
            }
            if (f == null) {
                throw new FunctionException("has no Function " + functionName);
            }
        }
        return null;
    }
    public void clear() {
       if (mFunctions != null) {
           mFunctions = null;
       }
    }
}
