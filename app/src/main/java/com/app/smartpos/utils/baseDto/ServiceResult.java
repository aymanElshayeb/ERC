package com.app.smartpos.utils.baseDto;


import java.util.List;


public class ServiceResult<T> {

    private int code;
    private String status;
    private Data<T> data;
    private Fault fault;

    public Fault getFault() {
        return fault;
    }

    public void setFault(Fault fault) {
        this.fault = fault;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return this.status;
    }

    public void setData(Data<T> data) {
        this.data = data;
    }

    public Data<T> getData() {
        return this.data;
    }

    public static class Data<T> {
        private List<T> returnedObj;
        private Integer total;

        public Data() {
        }

        public void setReturnedObj(List<T> returnedObj) {
            this.returnedObj = returnedObj;
        }

        public void setTotal(Integer total) {
            this.total = total;
        }

        public List<T> getReturnedObj() {
            return this.returnedObj;
        }

        public Integer getTotal() {
            return this.total;
        }
    }


}
