package scan.reader.qrscanner.barcodescanner.ads;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import scan.reader.qrscanner.barcodescanner.R;
import scan.reader.qrscanner.barcodescanner.util.MySharedPrefrence;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.VideoController;
import com.google.android.gms.ads.VideoOptions;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.nativead.MediaView;
import com.google.android.gms.ads.nativead.NativeAd;
import com.google.android.gms.ads.nativead.NativeAdOptions;
import com.google.android.gms.ads.nativead.NativeAdView;

import java.util.Random;

public class AdHandler {
   public InterstitialAd interstitialAd;
   public boolean isAdLoaded=false;
   public boolean isAdLoadFailed=false;
   public boolean isAdLoadProcessing=false;
   private static AdHandler singelton;
   public NativeAd nativeAd;
   public boolean isNativeAdLoad=false;
   public boolean isNativeAdLoadFailed=false;
   private ProgressDialog progressDialog;
   AdEventListener adEventListener;

   public static AdHandler getInstance(Context context){
      if(singelton==null) {
         singelton=new AdHandler();
      }
      return singelton;
   }
   public boolean isNetworkAvailable(@NonNull Context context){
      ConnectivityManager manager= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
      NetworkInfo networkInfo= manager.getActiveNetworkInfo();
      boolean networkAvailable=false;
      if(networkInfo!=null && networkInfo.isConnected()){
         networkAvailable=true;
      }
      return networkAvailable;
   }

   private void adProgress(Context context){

      progressDialog=new ProgressDialog(context);
      progressDialog.setMessage("Ad showing..");
      progressDialog.setCanceledOnTouchOutside(false);
      progressDialog.setCancelable(true);
   }

   public void showProgress(Activity activity){
      activity.runOnUiThread(()->{
         try {
            adProgress(activity);
            if(progressDialog!=null && !progressDialog.isShowing()){
               progressDialog.show();
            }
         }
         catch (Exception e){
            e.printStackTrace();
         }
      });
   }
   public  void dismissProgress(Activity activity){
      activity.runOnUiThread(()->{
         try {
            if(progressDialog!=null && progressDialog.isShowing()){
               progressDialog.dismiss();
            }
         }catch (Exception e){
            e.printStackTrace();
         }
      });
   }

   public  void loadNativeAd(final Context context,String nativeAdId, final AdEventListener adEventListener){

      if(! MySharedPrefrence.Companion.newInstance(context).isPremiumPurchased()){
         if(isNetworkAvailable(context)){
            AdLoader.Builder builder= new AdLoader.Builder(context,nativeAdId);
            builder.forNativeAd(unifiedNativeAd->{
               nativeAd=unifiedNativeAd;
               isNativeAdLoad=true;
               if(adEventListener!=null){
                  adEventListener.onAdLoaded(unifiedNativeAd);
               }else if(this.adEventListener!=null){
                  this.adEventListener.onAdLoaded(unifiedNativeAd);
               }
            }).withAdListener(new AdListener() {

               @Override
               public void onAdLoaded() {
                  super.onAdLoaded();
               }
               @Override
               public void onAdOpened() {
                  super.onAdOpened();
                  nativeAd=null;
                  loadNativeAd(context,nativeAdId,adEventListener);
               }
               @Override
               public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                  super.onAdFailedToLoad(loadAdError);
                  if(adEventListener!=null){
                     adEventListener.onLoadError(loadAdError.getMessage());//check this
                  }
                  isNativeAdLoadFailed=true;
               }


               @Override
               public void onAdClosed() {
                  super.onAdClosed();
               }
               @Override
               public void onAdClicked() {
                  super.onAdClicked();
               }

            });
            VideoOptions videoOptions=new VideoOptions.Builder()
                    .setStartMuted(true)
                    .build();
            NativeAdOptions adOptions= new NativeAdOptions.Builder()
                    .setVideoOptions(videoOptions).build();
            builder.withNativeAdOptions(adOptions);
            AdLoader adLoader= builder.build();
            adLoader.loadAd(new AdRequest.Builder().build());
         }
      }
   }

   public void showNativeAd(Context context, FrameLayout frameLayout,NativeAd nativeAd,
                                           boolean isShowMedia){
      if(!MySharedPrefrence.Companion.newInstance(context).isPremiumPurchased()){

         if (isNetworkAvailable(context)) {

            LayoutInflater inflater=LayoutInflater.from(context);
            NativeAdView adView;
            //                    inflater.inflate(R.layout.big_native_ad_new,null):
            adView=(NativeAdView) (isShowMedia ?
                    inflater.inflate(R.layout.big_native_three,null):
                    inflater.inflate(R.layout.small_native_two,null));
            if(frameLayout!=null){
               frameLayout.removeAllViews();
               frameLayout.addView(adView);
               frameLayout.setVisibility(View.VISIBLE);
            }
            try{
               MediaView mediaView=adView.findViewById(R.id.mediaView);
               if(isShowMedia){
                  mediaView.setMediaContent(nativeAd.getMediaContent());
                  mediaView.setOnHierarchyChangeListener(new ViewGroup.OnHierarchyChangeListener(){


                     @Override
                     public void onChildViewRemoved(View parent, View child) {}

                     @Override
                     public void onChildViewAdded(View parent, View child) {
                        if(child instanceof ImageView){
                           ImageView imageView=(ImageView) child;
                           imageView.setAdjustViewBounds(true);
                           imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                        }
                     }

                  });
               }

               adView.setMediaView(mediaView);
               adView.setIconView(adView.findViewById(R.id.adIcon));
               adView.setHeadlineView(adView.findViewById(R.id.adTitle));
               adView.setBodyView(adView.findViewById(R.id.adDescription));
               adView.setAdvertiserView(adView.findViewById(R.id.adAdvertiser));
               adView.setCallToActionView(adView.findViewById(R.id.adInstallButton));

               ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

               if(nativeAd.getIcon()==null){
                  adView.getIconView().setVisibility(View.GONE);
               }
               else {

                  adView.getIconView().setVisibility(View.VISIBLE);
                  ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
               }

               if(nativeAd.getBody()==null){
                  adView.getBodyView().setVisibility(View.INVISIBLE);
               }
               else {
                  adView.getBodyView().setVisibility(View.VISIBLE);
                  ((TextView) adView.getBodyView()).setText(nativeAd.getBody());

               }
               if(nativeAd.getCallToAction()==null){
                  adView.getCallToActionView().setVisibility(View.INVISIBLE);
               }
               else {

                  adView.getCallToActionView().setVisibility(View.VISIBLE);
                  ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
               }

               adView.getAdvertiserView().setVisibility(View.INVISIBLE);

               adView.setNativeAd(nativeAd);
               if(isShowMedia){

                  adView.getMediaView().setVisibility(View.VISIBLE);
               }
               else {
                  adView.getMediaView().setVisibility(View.GONE);
               }

               VideoController videoController=nativeAd.getMediaContent().getVideoController();
               videoController.mute(true);
               if(videoController.hasVideoContent()){
                  videoController.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                     @Override
                     public void onVideoEnd() {
                        super.onVideoEnd();
                     }
                  });
               }
               adView.setNativeAd(nativeAd);



            }catch (Exception e){
               e.printStackTrace();
            }
         }

      }


   }

   public void loadInterstitialAd(Context context,String interId){
      if(!MySharedPrefrence.Companion.newInstance(context).isPremiumPurchased()){
         if(interstitialAd==null && !isAdLoadProcessing){
            isAdLoadProcessing=true;
            AdRequest adRequest=new AdRequest.Builder().build();
            InterstitialAd.load(context, interId, adRequest,
                    new InterstitialAdLoadCallback() {
                       @Override
                       public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                          super.onAdFailedToLoad(loadAdError);
                          isAdLoadFailed=true;
                          isAdLoadProcessing=false;
                          isAdLoaded=false;
                       }

                       @Override
                       public void onAdLoaded(@NonNull InterstitialAd interAd) {
                          super.onAdLoaded(interAd);
                          isAdLoaded=true;
                          isAdLoadFailed=false;
                          isAdLoadProcessing=false;
                          interstitialAd=interAd;
                       }

                    });
         }
      }

   }

   public void loadInterstitialAdWithRandomNumber(Activity context, String interId, int number, OnAdClosedListener onAdClosedListener){

      if(!MySharedPrefrence.Companion.newInstance(context).isPremiumPurchased()){

         if(!AppOpenHandler.Companion.isShowingAd()){

            Random random=new Random();
            int r= random.nextInt(number);
            if(number==1 || r==1){
               if(interstitialAd!=null){

                  if(!isAdLoadFailed&& isAdLoaded  && !isAdLoadProcessing){
                     interstitialAd.setFullScreenContentCallback(
                             new FullScreenContentCallback() {

                                @Override
                                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                   super.onAdFailedToShowFullScreenContent(adError);
                                   
                                   if(progressDialog!=null && progressDialog.isShowing()){
                                      progressDialog.dismiss();
                                   }
                                   isAdLoaded=false;
                                   interstitialAd=null;
                                   isAdLoadFailed=false;
                                   isAdLoadProcessing=false;
                                   onAdClosedListener.onAdClosed();


                                }

                                @Override
                                public void onAdDismissedFullScreenContent() {
                                   super.onAdDismissedFullScreenContent();

                                   if(progressDialog!=null && progressDialog.isShowing()){
                                      progressDialog.dismiss();
                                   }
                                   AppOpenHandler.Companion.setShowingAd(false);
                                   isAdLoadProcessing=false;
                                   isAdLoaded=false;
                                   interstitialAd=null;
                                   isAdLoadFailed=false;
                                   loadInterstitialAd(context,interId);
                                   onAdClosedListener.onAdClosed();

                                }



                                @Override
                                public void onAdShowedFullScreenContent() {
                                   super.onAdShowedFullScreenContent();

                                   AppOpenHandler.Companion.setShowingAd(true);




                                }
                             }
                     );
                     interstitialAd.show(context);
                  }
                  else {
                     onAdClosedListener.onAdClosed();
                  }
               }
               else {
                  if(!TextUtils.isEmpty(interId)){
                     loadInterstitialAd(context,interId);
                  }
                  onAdClosedListener.onAdClosed();
               }
            }else {
               onAdClosedListener.onAdClosed();
            }
         }else {
            onAdClosedListener.onAdClosed();
         }
      }

   }

   public void loadSplashInter(Activity context,String interId, OnAdClosedListener onAdClosedListener){
      if(!MySharedPrefrence.Companion.newInstance(context).isPremiumPurchased()){

         if(interstitialAd==null){
            isAdLoadProcessing=true;
            AdRequest adRequest=new AdRequest.Builder().build();
            InterstitialAd.load(context, interId, adRequest,
                    new InterstitialAdLoadCallback() {
                       @Override
                       public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                          super.onAdFailedToLoad(loadAdError);
                          isAdLoadFailed=true;
                          isAdLoadProcessing=false;
                          isAdLoaded=false;
                          AdHandler.getInstance(context).dismissProgress(context);
                          onAdClosedListener.onAdClosed();


                       }

                       @Override
                       public void onAdLoaded(@NonNull InterstitialAd interAd) {
                          super.onAdLoaded(interAd);

                          isAdLoaded=true;
                          isAdLoadFailed=false;
                          isAdLoadProcessing=false;
                          interstitialAd=interAd;
                          AdHandler.getInstance(context).dismissProgress(context);
                          loadInterstitialAdWithRandomNumber(context,interId,1,onAdClosedListener);


                       }
                    });
         }
      }
   }

   public void loadAdaptiveBanner(Context context, FrameLayout adContainerView, String bannerAdID, final AdEventListener adEventListener) {

      try {
         if (!MySharedPrefrence.Companion.newInstance(context).isPremiumPurchased()) {

            AdView adView = new AdView(context);
            adView.setAdUnitId(bannerAdID);
            adContainerView.removeAllViews();
            adContainerView.addView(adView);

            final AdSize adSize = getAdSize(context, adContainerView);
            adView.setAdSize(adSize);

            AdRequest adRequest =
                    new AdRequest.Builder().build();

            adView.setAdListener(new AdListener() {
               @Override
               public void onAdLoaded() {
                  super.onAdLoaded();
                  if (adEventListener != null) {
                     adEventListener.onAdLoaded(null);
                  }
               }

               @Override
               public void onAdClosed() {
                  super.onAdClosed();

                  if (adEventListener != null) {
                     adEventListener.onAdClosed();
                  }
               }

               @Override
               public void onAdFailedToLoad(LoadAdError loadAdError) {
                  super.onAdFailedToLoad(loadAdError);

                  if (adEventListener != null) {
                     adEventListener.onLoadError(loadAdError.getMessage());
                  }

               }
            });
            // Start loading the ad in the background.
            adView.loadAd(adRequest);

         }
      } catch (Exception e) {
         e.printStackTrace();

      }
   }

   public AdSize getAdSize(Context context, FrameLayout adContainerView) {
      // Determine the screen width (less decorations) to use for the ad width.
      Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
      DisplayMetrics outMetrics = new DisplayMetrics();
      display.getMetrics(outMetrics);

      float density = outMetrics.density;

      float adWidthPixels = adContainerView.getWidth();

      // If the ad hasn't been laid out, default to the full screen width.
      if (adWidthPixels == 0) {
         adWidthPixels = outMetrics.widthPixels;
      }

      int adWidth = (int) (adWidthPixels / density);

      return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth);
   }


   public void showNativeAdBannerType(Context context, FrameLayout frameLayout,NativeAd nativeAds){

      if (!MySharedPrefrence.Companion.newInstance(context).isPremiumPurchased()) {

         LayoutInflater inflater=LayoutInflater.from(context);
         NativeAdView adView;
         adView=(NativeAdView) inflater.inflate(R.layout.small_banner_type_native_ad,null);

         if(frameLayout!=null){
            frameLayout.removeAllViews();
            frameLayout.addView(adView);
            frameLayout.setVisibility(View.VISIBLE);
         }
         try{
            MediaView mediaView=adView.findViewById(R.id.mediaView);
            adView.setMediaView(mediaView);
            adView.setIconView(adView.findViewById(R.id.adIcon));
            adView.setHeadlineView(adView.findViewById(R.id.adTitle));
            adView.setBodyView(adView.findViewById(R.id.adDescription));
            adView.setAdvertiserView(adView.findViewById(R.id.adAdvertiser));
            adView.setCallToActionView(adView.findViewById(R.id.adInstallButton));

            ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

            if(nativeAd.getIcon()==null){
               adView.getIconView().setVisibility(View.GONE);
            }
            else {
               adView.getIconView().setVisibility(View.VISIBLE);
               ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
            }

            if(nativeAd.getBody()==null){
               adView.getBodyView().setVisibility(View.INVISIBLE);
            }
            else {
               adView.getBodyView().setVisibility(View.VISIBLE);
               ((TextView) adView.getBodyView()).setText(nativeAd.getBody());

            }
            if(nativeAd.getCallToAction()==null){
               adView.getCallToActionView().setVisibility(View.INVISIBLE);
            }
            else {

               adView.getCallToActionView().setVisibility(View.VISIBLE);
               ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
            }


            adView.getAdvertiserView().setVisibility(View.INVISIBLE);

            adView.setNativeAd(nativeAd);

            adView.getMediaView().setVisibility(View.GONE);
            VideoController videoController=nativeAd.getMediaContent().getVideoController();
            videoController.mute(true);
            if(videoController.hasVideoContent()){
               videoController.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                  @Override
                  public void onVideoEnd() {
                     super.onVideoEnd();
                  }
               });
            }
            adView.setNativeAd(nativeAd);



         }catch (Exception e){
            e.printStackTrace();
         }
      }
   }

   public void showNativeAdInSupportedCodesAdapter(Context context, FrameLayout frameLayout,NativeAd nativeAds){

      if (!MySharedPrefrence.Companion.newInstance(context).isPremiumPurchased()) {

         LayoutInflater inflater=LayoutInflater.from(context);
         NativeAdView adView;
         adView=(NativeAdView) inflater.inflate(R.layout.supported_codes_native_ad_layout,null);

         if(frameLayout!=null){
            frameLayout.removeAllViews();
            frameLayout.addView(adView);
            frameLayout.setVisibility(View.VISIBLE);
         }
         try{
            MediaView mediaView=adView.findViewById(R.id.mediaView);
            adView.setMediaView(mediaView);
            adView.setIconView(adView.findViewById(R.id.adIcon));
            adView.setHeadlineView(adView.findViewById(R.id.adTitle));
            adView.setBodyView(adView.findViewById(R.id.adDescription));
            adView.setAdvertiserView(adView.findViewById(R.id.adAdvertiser));
            adView.setCallToActionView(adView.findViewById(R.id.adInstallButton));

            ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());

            if(nativeAd.getIcon()==null){
               adView.getIconView().setVisibility(View.GONE);
            }
            else {
               adView.getIconView().setVisibility(View.VISIBLE);
               ((ImageView) adView.getIconView()).setImageDrawable(nativeAd.getIcon().getDrawable());
            }

            if(nativeAd.getBody()==null){
               adView.getBodyView().setVisibility(View.INVISIBLE);
            }
            else {
               adView.getBodyView().setVisibility(View.VISIBLE);
               ((TextView) adView.getBodyView()).setText(nativeAd.getBody());

            }
            if(nativeAd.getCallToAction()==null){
               adView.getCallToActionView().setVisibility(View.INVISIBLE);
            }
            else {

               adView.getCallToActionView().setVisibility(View.VISIBLE);
               ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());
            }


            adView.getAdvertiserView().setVisibility(View.INVISIBLE);

            adView.setNativeAd(nativeAd);

            adView.getMediaView().setVisibility(View.GONE);
            VideoController videoController=nativeAd.getMediaContent().getVideoController();
            videoController.mute(true);
            if(videoController.hasVideoContent()){
               videoController.setVideoLifecycleCallbacks(new VideoController.VideoLifecycleCallbacks() {
                  @Override
                  public void onVideoEnd() {
                     super.onVideoEnd();
                  }
               });
            }
            adView.setNativeAd(nativeAd);



         }catch (Exception e){
            e.printStackTrace();
         }
      }
   }
}
