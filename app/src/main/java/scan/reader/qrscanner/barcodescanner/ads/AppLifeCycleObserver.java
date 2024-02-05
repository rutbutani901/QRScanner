package scan.reader.qrscanner.barcodescanner.ads;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

public class AppLifeCycleObserver implements LifecycleObserver {

   AppOpenLifeCyleChange appOpenLifeCyleChange;
   @OnLifecycleEvent(Lifecycle.Event.ON_START)
   public void  onEnterForeground(){
      appOpenLifeCyleChange.onForeground();
   }

   @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
   public void  onEnterBackground(){
      appOpenLifeCyleChange.onBackground();
   }

   public AppLifeCycleObserver(AppOpenLifeCyleChange appOpenLifeCyleChange){
      this.appOpenLifeCyleChange=appOpenLifeCyleChange;
   }




}


