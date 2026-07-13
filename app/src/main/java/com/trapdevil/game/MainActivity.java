package com.trapdevil.game;

import android.os.Bundle;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.appopen.AppOpenAd;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

/**
 * Trap Devil — native Android wrapper.
 *
 * Loads the HTML5 game (assets/www/index.html) inside a WebView and shows
 * AdMob ads around it:
 *   - Banner:       always docked at the bottom (see activity_main.xml)
 *   - Interstitial: shown after every boss level is cleared (triggered from JS)
 *   - Rewarded:     shown when the player taps the in-game "🎁 Ad" button
 *   - App Open:     shown once when the app is first launched
 *
 * ---- YOUR AD UNIT IDS ----
 * If any placement should use a different ad unit than assigned below,
 * just swap the ID strings — everything else stays the same.
 */
public class MainActivity extends AppCompatActivity {

    private static final String APP_ID          = "ca-app-pub-5225365475775473~6183807537";
    private static final String BANNER_ID       = "ca-app-pub-5225365475775473/1841916466";
    private static final String INTERSTITIAL_ID = "ca-app-pub-5225365475775473/8211883699";
    private static final String REWARDED_ID     = "ca-app-pub-5225365475775473/7724798539";
    private static final String APP_OPEN_ID     = "ca-app-pub-5225365475775473/7765729353";

    private WebView webView;
    private AdView bannerAdView;
    private InterstitialAd interstitialAd;
    private RewardedAd rewardedAd;
    private AppOpenAd appOpenAd;
    private boolean appOpenAdShownOnce = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        // Initialize the Mobile Ads SDK, then start loading full-screen ad formats
        MobileAds.initialize(this, initializationStatus -> {
            loadInterstitial();
            loadRewarded();
            loadAppOpenAd();
        });

        // ---- Banner ----
        bannerAdView = findViewById(R.id.bannerAdView);
        bannerAdView.loadAd(new AdRequest.Builder().build());

        // ---- WebView / game ----
        webView = findViewById(R.id.gameWebView);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);      // required — the game uses localStorage
        settings.setDatabaseEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new AdBridge(), "AndroidAds");
        webView.loadUrl("file:///android_asset/www/index.html");
    }

    @Override
    protected void onDestroy() {
        if (bannerAdView != null) bannerAdView.destroy();
        if (webView != null) webView.destroy();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        if (bannerAdView != null) bannerAdView.pause();
        if (webView != null) webView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bannerAdView != null) bannerAdView.resume();
        if (webView != null) webView.onResume();
    }

    // ==================== Interstitial ====================
    private void loadInterstitial() {
        InterstitialAd.load(this, INTERSTITIAL_ID, new AdRequest.Builder().build(),
            new InterstitialAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull InterstitialAd ad) {
                    interstitialAd = ad;
                    interstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                        @Override
                        public void onAdDismissedFullScreenContent() {
                            interstitialAd = null;
                            loadInterstitial(); // preload the next one
                        }
                        @Override
                        public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                            interstitialAd = null;
                            loadInterstitial();
                        }
                    });
                }
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    interstitialAd = null;
                }
            });
    }

    private void showInterstitial() {
        runOnUiThread(() -> {
            if (interstitialAd != null) {
                interstitialAd.show(MainActivity.this);
            }
            // If it isn't loaded yet, we simply skip showing it this time —
            // gameplay should never be blocked waiting for an ad to load.
        });
    }

    // ==================== Rewarded ====================
    private void loadRewarded() {
        RewardedAd.load(this, REWARDED_ID, new AdRequest.Builder().build(),
            new RewardedAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull RewardedAd ad) {
                    rewardedAd = ad;
                }
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    rewardedAd = null;
                }
            });
    }

    private void showRewarded() {
        runOnUiThread(() -> {
            if (rewardedAd == null) {
                Toast.makeText(MainActivity.this, "Ad not ready yet — try again in a moment", Toast.LENGTH_SHORT).show();
                return;
            }
            rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                @Override
                public void onAdDismissedFullScreenContent() {
                    rewardedAd = null;
                    loadRewarded(); // preload the next one
                }
                @Override
                public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                    rewardedAd = null;
                    loadRewarded();
                }
            });
            rewardedAd.show(MainActivity.this, rewardItem -> {
                // User watched the full ad and earned the reward — tell the web page
                webView.post(() -> webView.evaluateJavascript(
                    "window.onAdRewardEarned && window.onAdRewardEarned();", null));
            });
        });
    }

    // ==================== App Open ====================
    private void loadAppOpenAd() {
        AppOpenAd.load(this, APP_OPEN_ID, new AdRequest.Builder().build(),
            new AppOpenAd.AppOpenAdLoadCallback() {
                @Override
                public void onAdLoaded(@NonNull AppOpenAd ad) {
                    appOpenAd = ad;
                    if (!appOpenAdShownOnce) {
                        appOpenAdShownOnce = true;
                        appOpenAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
                                appOpenAd = null;
                            }
                            @Override
                            public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                                appOpenAd = null;
                            }
                        });
                        appOpenAd.show(MainActivity.this);
                    }
                }
                @Override
                public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                    appOpenAd = null;
                }
            });
    }

    // ==================== JS <-> Native bridge ====================
    // Exposed to the game's JavaScript as window.AndroidAds
    public class AdBridge {
        @JavascriptInterface
        public void showInterstitial() {
            MainActivity.this.showInterstitial();
        }
        @JavascriptInterface
        public void showRewarded() {
            MainActivity.this.showRewarded();
        }
    }
}
