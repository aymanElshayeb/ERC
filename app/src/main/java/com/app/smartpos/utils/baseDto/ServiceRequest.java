package com.app.smartpos.utils.baseDto;


public class ServiceRequest<T> {
    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static <T> ServiceRequest<T> constructServiceRequest(T data) {
        ServiceRequest<T> serviceRequest = new ServiceRequest<>();
        serviceRequest.setData(data);
        return serviceRequest;
    }
}
