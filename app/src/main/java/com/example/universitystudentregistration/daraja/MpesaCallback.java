package com.example.universitystudentregistration.daraja;

import com.google.gson.annotations.SerializedName;

public class MpesaCallback {
    @SerializedName("Body")
    private CallbackBody body;

    public CallbackBody getBody() {
        return body;
    }

    public void setBody(CallbackBody body) {
        this.body = body;
    }

    public static class CallbackBody {
        @SerializedName("stkCallback")
        private STKCallback stkCallback;

        public STKCallback getStkCallback() {
            return stkCallback;
        }

        public void setStkCallback(STKCallback stkCallback) {
            this.stkCallback = stkCallback;
        }
    }

    public static class STKCallback {
        @SerializedName("MerchantRequestID")
        private String merchantRequestID;

        @SerializedName("CheckoutRequestID")
        private String checkoutRequestID;

        @SerializedName("ResultCode")
        private int resultCode;

        @SerializedName("ResultDesc")
        private String resultDesc;

        @SerializedName("CallbackMetadata")
        private CallbackMetadata callbackMetadata;

        public String getMerchantRequestID() { return merchantRequestID; }
        public void setMerchantRequestID(String merchantRequestID) { this.merchantRequestID = merchantRequestID; }
        public String getCheckoutRequestID() { return checkoutRequestID; }
        public void setCheckoutRequestID(String checkoutRequestID) { this.checkoutRequestID = checkoutRequestID; }
        public int getResultCode() { return resultCode; }
        public void setResultCode(int resultCode) { this.resultCode = resultCode; }
        public String getResultDesc() { return resultDesc; }
        public void setResultDesc(String resultDesc) { this.resultDesc = resultDesc; }
        public CallbackMetadata getCallbackMetadata() { return callbackMetadata; }
        public void setCallbackMetadata(CallbackMetadata callbackMetadata) { this.callbackMetadata = callbackMetadata; }

        public boolean isSuccess() {
            return resultCode == 0;
        }
    }

    public static class CallbackMetadata {
        @SerializedName("Item")
        private java.util.List<CallbackItem> items;

        public java.util.List<CallbackItem> getItems() { return items; }
        public void setItems(java.util.List<CallbackItem> items) { this.items = items; }

        public String getValue(String name) {
            if (items != null) {
                for (CallbackItem item : items) {
                    if (name.equals(item.getName())) {
                        return item.getValue();
                    }
                }
            }
            return null;
        }
    }

    public static class CallbackItem {
        @SerializedName("Name")
        private String name;

        @SerializedName("Value")
        private String value;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getValue() { return value; }
        public void setValue(String value) { this.value = value; }
    }
}