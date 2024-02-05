package scan.reader.qrscanner.barcodescanner.ads;

public interface AdEventListener {

  void onAdLoaded(Object adObject);
    void onAdClosed();
    void onLoadError(String errorCode);
}
