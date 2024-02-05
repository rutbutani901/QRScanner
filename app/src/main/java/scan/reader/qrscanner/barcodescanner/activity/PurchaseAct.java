package scan.reader.qrscanner.barcodescanner.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.yandex.metrica.YandexMetrica;

import scan.reader.qrscanner.barcodescanner.R;

import scan.reader.qrscanner.barcodescanner.databinding.ActivityInAppPurchaseBinding;
import scan.reader.qrscanner.barcodescanner.util.Constant;
import scan.reader.qrscanner.barcodescanner.util.MySharedPrefrence;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PurchaseAct extends AppCompatActivity {

    private ActivityInAppPurchaseBinding binding;
    ArrayList<ProductDetails> productDetailsArrayList= new ArrayList<>();

//    "android.test.purchased"
//    "qremoveads_99"
    private  List<String> skusList= Arrays.asList(
            ""
    );
    String product_id="";
    private BillingClient billingClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        binding=ActivityInAppPurchaseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        FirebaseAnalytics firebaseAnalytics= FirebaseAnalytics.getInstance(this);
        Bundle params = new Bundle();
        params.putString("ScreenName", "screenName");
        firebaseAnalytics.logEvent("PurchaseActivity", params);

        String eventParameters = "{\"screenName\":\"screenName\"}";
        YandexMetrica.reportEvent("PurchaseActivity", eventParameters);


        binding.crossMark.setOnClickListener(View->{
           onBackPressed();

        });

        binding.upGradeNow.setOnClickListener(View->{
            checkPurchase();
        });

        billingClient= BillingClient.newBuilder(this)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();

        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingServiceDisconnected() {
            }

            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {

                if(billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK){
                    startPurchase();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if(MySharedPrefrence.Companion.newInstance(PurchaseAct.this).getLanguageCode().isEmpty()){
            startActivity(new Intent(PurchaseAct.this, LanguageAct.class));
            finish();
        }else {
            super.onBackPressed();
            finish();
        }

    }

    private PurchasesUpdatedListener purchasesUpdatedListener=(billingResult, purchases) -> {
        Constant.Companion.setSplashScreen(false);

        if(billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK & purchases!=null){
            for(Purchase purchase:purchases){
                managePurchase(purchase);
            }
        }
        else if(billingResult.getResponseCode()== BillingClient.BillingResponseCode.USER_CANCELED){
            Toast.makeText(getApplicationContext(),getString(R.string.purchaseCanceled),Toast.LENGTH_SHORT).show();
        }
        else if(billingResult.getResponseCode()== BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){}
        else {
            Toast.makeText(getApplicationContext(),getString(R.string.error)+ billingResult.getDebugMessage(),Toast.LENGTH_SHORT).show();
        }
    };

    void managePurchase(Purchase purchase1){
        if(product_id.equals(purchase1.getProducts().get(0))&& purchase1.getPurchaseState()== Purchase.PurchaseState.PURCHASED){
            if(!purchase1.isAcknowledged()){
                Log.d("adsBilling","managePurchaseIsAcknowledge");
                AcknowledgePurchaseParams acknowledgePurchaseParams=
                        AcknowledgePurchaseParams.newBuilder().
                                setPurchaseToken(purchase1.getPurchaseToken())
                                .build();

                billingClient.acknowledgePurchase(acknowledgePurchaseParams,acknowledgePurchaseResponseListener);
            }else {
                Log.d("adsBilling","managePurchase");
                setPurchased();
            }
        }
        else  if(product_id.equals(purchase1.getProducts().get(0))&& purchase1.getPurchaseState()== Purchase.PurchaseState.PENDING){
            Toast.makeText(getApplicationContext(),getString(R.string.purchasePendingCompleteTransaction),Toast.LENGTH_SHORT).show();
        }
        else if(product_id.equals(purchase1.getProducts().get(0))&& purchase1.getPurchaseState()== Purchase.PurchaseState.UNSPECIFIED_STATE){
            Toast.makeText(getApplicationContext(),getString(R.string.failedToPurchase),Toast.LENGTH_SHORT).show();
        }
    }
    AcknowledgePurchaseResponseListener acknowledgePurchaseResponseListener=new AcknowledgePurchaseResponseListener() {
        @Override
        public void onAcknowledgePurchaseResponse(@NonNull BillingResult billingResult) {
            if(billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK){
                runOnUiThread(()->{
                    Toast.makeText(PurchaseAct.this,getString(R.string.purchaseSuccess),Toast.LENGTH_SHORT).show();
                    setPurchased();
                });
            }
        }
    };


    private  void checkPurchase(){
        if(productDetailsArrayList!=null && productDetailsArrayList.size()!=0){
            ProductDetails productDetails= getProductDetailsObjectFromSubscriptionType(skusList.get(0));

            if(productDetails!=null){

                Constant.Companion.setSplashScreen(true);
                product_id= productDetails.getProductId();
                BillingFlowParams.ProductDetailsParams build= BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build();

                List<BillingFlowParams.ProductDetailsParams> productDetailsParamsList= new ArrayList<>();
                productDetailsParamsList.add(build);
                BillingFlowParams billingFlowParams= BillingFlowParams.newBuilder()
                        .setProductDetailsParamsList(productDetailsParamsList)
                        .build();

                BillingResult billingResult= billingClient.launchBillingFlow(PurchaseAct.this, billingFlowParams);
            }
        }else {

            Toast.makeText(PurchaseAct.this,getString(R.string.noPlans),Toast.LENGTH_SHORT).show();
        }
    }


    private void startPurchase(){

        QueryProductDetailsParams.Product inAppProduct= QueryProductDetailsParams.Product.newBuilder()
                .setProductId(skusList.get(0))
                .setProductType(BillingClient.ProductType.INAPP)
                .build();


        List<QueryProductDetailsParams.Product> productList= new ArrayList<>();
        productList.add(inAppProduct);

        QueryProductDetailsParams params= QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build();

        billingClient.queryProductDetailsAsync(
                params,
                (billingResult, productDetailsList) -> {
                    if(billingResult.getResponseCode()== BillingClient.BillingResponseCode.OK){
                        if(productDetailsList.size()>0){
                            PurchaseAct.this.productDetailsArrayList.addAll(productDetailsList);
                            runOnUiThread(this::setProductDetailsList);
                        }else {
                            runOnUiThread(()->{
                                Toast.makeText(getApplicationContext(),getString(R.string.purchaseNotFound),Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                }
        );
    }

    private void setProductDetailsList(){

        if(productDetailsArrayList!=null && !productDetailsArrayList.isEmpty()){
            ProductDetails productDetailsmonth= getProductDetailsObjectFromSubscriptionType(skusList.get(0));
            if(productDetailsmonth!=null){
                binding.priceValue.setText(Objects.requireNonNull(productDetailsmonth.getOneTimePurchaseOfferDetails()).getFormattedPrice());
            }
        }
    }


    private ProductDetails getProductDetailsObjectFromSubscriptionType(String subscriptionType){
        for(int i=0; i< productDetailsArrayList.size();i++){
            if(productDetailsArrayList.get(i).getProductId().equalsIgnoreCase(subscriptionType)){
                return productDetailsArrayList.get(i);
            }
        }
        return null;
    }

    private void setPurchased(){
        setResult(RESULT_OK);
        finish();
    }



}