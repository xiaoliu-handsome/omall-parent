package com.offcn.common.exception;

import com.offcn.common.result.ResultCodeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "自定义全局异常类")
public class OmallException extends RuntimeException{

    @ApiModelProperty("异常状态码")
    private Integer code;

    public OmallException(String message,Integer code) {
        super(message);
        this.code = code;
    }

    public OmallException(ResultCodeEnum resultCodeEnum){
        super(resultCodeEnum.getMessage());
        this.code=resultCodeEnum.getCode();
    }

    @Override
    public String toString() {
        return "OmallException{" +
                "code=" + code +
                ", message=" + this.getMessage() +
                '}';
    }



}
