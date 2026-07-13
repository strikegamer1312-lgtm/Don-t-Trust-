# Trap Devil — Native Android App (with AdMob ads)

## Sabse aasan tareeqa: bina Android Studio ke real APK banwana

Aapke paas Android Studio nahi hai to koi masla nahi — **GitHub ka free cloud
server khud APK bana dega**. Aapko sirf ye files ek GitHub repo mein upload
karni hain (drag & drop, koi coding nahi):

### Steps:

1. **github.com** par free account banayein (agar nahi hai)
2. Upar right mein **"+"** → **"New repository"** → koi bhi naam dein (e.g. `trap-devil`) → **Public** rakhein → **Create repository**
3. Naye repo page par **"uploading an existing file"** link par click karein
4. Is `TrapDevilNative` folder ke **andar ki saari files aur folders** (including hidden `.github` folder) drag-and-drop karke upload karein, phir **"Commit changes"** dabayein

   ⚠️ Zaroori: `.github/workflows/build-apk.yml` file bhi zaroor upload honi chahiye — yehi file APK banwati hai. Agar GitHub ki upload screen `.github` folder na dikhaye, to us file ko alag se "Add file → Create new file" se path `.github/workflows/build-apk.yml` type karke uska content paste kar dein.

5. Upload hote hi upar **"Actions"** tab par jayein — ek build automatically shuru ho jayegi (agar na ho to "Run workflow" button dabayein)
6. **3-5 minute wait karein** — jab tak green ✅ checkmark na aa jaye
7. Us completed run par click karein → sabse neeche **"Artifacts"** section mein **"TrapDevil-app"** dikhega → download karein
8. Downloaded zip ke andar `app-debug.apk` hogi — usi ko apne **Android phone** mein transfer/download karein aur tap karke install kar lein
   (Pehli baar "Install from unknown sources" allow karna pad sakta hai — Android khud poochega)

Bas — install hote hi icon tap karein, game seedha khul jayega, **ads bhi
chalenge** (banner hamesha neeche, interstitial boss ke baad, rewarded "🎁 Ad"
button se, app-open pehli baar khulte hi).

Ye poori tarah **free** hai aur har baar jab bhi aap code mein koi change
karke dobara upload karenge, GitHub khud nayi APK bana dega.

---

## Advanced option: Android Studio se khud build karna

Agar aapke paas Android Studio hai:

1. Android Studio kholein → **Open** → is `TrapDevilNative` folder ko select karein
2. Gradle sync hone dein (internet chahiye)
3. **Build → Generate Signed Bundle / APK** → APK ya AAB chunein
4. Signing key banayein (Android Studio khud guide karega) — **is key ko safe rakhein**
5. Build complete hone par `.apk`/`.aab` mil jayegi

Play Store par publish karna ho to **Android App Bundle (.aab)** wala format
chahiye hoga, GitHub Actions wala upar wala method sirf test-install ke liye
`.apk` deta hai (debug build, Play Store ke liye nahi).

## Ye app kya karta hai

- Game ek native WebView ke andar chalta hai (fast, fullscreen, landscape-locked)
- **Banner ad** — hamesha neeche dikhega
- **Interstitial ad** — har boss level clear karne ke baad
- **Rewarded ad** — HUD mein "🎁 Ad" button dabane par (1 death maaf)
- **App Open ad** — app pehli baar khulte hi

Sab 4 ad unit IDs already `MainActivity.java` mein daal di hain:
```
App ID:        ca-app-pub-5225365475775473~6183807537
Banner:        ca-app-pub-5225365475775473/1841916466
Interstitial:  ca-app-pub-5225365475775473/8211883699
Rewarded:      ca-app-pub-5225365475775473/7724798539
App Open:      ca-app-pub-5225365475775473/7765729353
```
(Agar koi ID galat placement pe hai, `MainActivity.java` ke top mein 4 lines
mein bas ID string badal dein — baaki code same rahega.)

## Testing (zaroori)

- Naya AdMob account/app kuch ghanton se din tak "under review" rehta hai, is dauran test/blank ads dikh sakte hain, ye normal hai
- Testing ke dauran chahein to apne device ko AdMob console mein **"Test Device"** add kar sakte hain taake real ad na chale

## Google Play Store par publish karna (baad mein, jab ready ho)

1. https://play.google.com/console par jaake **Developer account** banayein ($25 one-time fee)
2. **Create app** → app ka naam, category, etc. bharain
3. Android Studio se banayi `.aab` file **Internal Testing** ya **Production** mein upload karein
4. Store listing (screenshots, description, privacy policy link) bharke submit karein
5. Google review karega (kuch ghante se 1-2 din) — approve hone par app live ho jayegi

## Files kya kya hain

```
TrapDevilNative/
├── .github/workflows/build-apk.yml  ← GitHub cloud build ka setup (mat delete karein)
├── app/
│   ├── build.gradle              ← AdMob dependency yahan hai
│   ├── src/main/
│   │   ├── AndroidManifest.xml   ← AdMob App ID yahan hai
│   │   ├── java/.../MainActivity.java   ← saari ad logic
│   │   ├── res/layout/activity_main.xml ← WebView + banner ad layout
│   │   ├── res/mipmap-*/         ← app icon (sab sizes)
│   │   └── assets/www/index.html ← poora game (isi file ko edit karein agar game mein kuch change karna ho)
├── build.gradle
└── settings.gradle
```

Game mein koi bhi change karna ho (levels, difficulty, controls, etc.) to
sirf `app/src/main/assets/www/index.html` edit karein, phir GitHub par dobara
upload/commit karein — Actions khud nayi APK bana dega.

