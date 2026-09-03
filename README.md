# 🇱🇰 CeylonSteps (සීලෝන් ස්ටෙප්ස්)
### The Ultimate Sri Lankan Travel Companion & Social Explorer

[![Android](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](#)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-FFCA28?logo=firebase&logoColor=black)](#)
[![OpenStreetMap](https://img.shields.io/badge/Maps-OpenStreetMap-7EBC6F?logo=openstreetmap&logoColor=white)](#)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

**CeylonSteps** යනු ශ්‍රී ලංකාව පුරා සංචාරය කරන දේශීය සහ විදේශීය ගවේෂකයින් (Explorers & Backpackers) වෙනුවෙන්ම නිර්මාණය කරන ලද All-in-One Travel Diary සහ Community Social Network එකකි. 

සංචාරය කරන සෑම තැනක්ම සිතියමක සටහන් කරගැනීම, මතකයන් (Memories) සුරැකීම, සහ වෙනත් සංචාරකයින් සමඟ සජීවීව අදහස් හුවමාරු කරගැනීම එකම තැනකින් සිදුකළ හැක.

---

## 🚀 Key Features (ප්‍රධාන විශේෂාංග)

### 🗺️ 1. Interactive Footprint Tracker & Map
* **OpenStreetMap Integration:** දිවයිනේ ඕනෑම තැනක ඔබ ගිය සංචාර සිතියම මත පින් කර සටහන් තබාගැනීමේ හැකියාව.
* **Auto Province & District Detection:** පින් කරන ස්ථානය ශ්‍රී ලංකාවේ පළාත් 9 සහ දිස්ත්‍රික්ක 25 අතුරින් කුමකට අයත් දැයි ස්වයංක්‍රීයව හඳුනාගනී.
* **GPS Coordinate Snapping:** තනි ටැප් එකකින් ඔබ සිටින නිශ්චිත GPS Latitude/Longitude සටහන් කරගැනීම.

### 🚗 2. Multi-Stop Road Trip Planner
* එක් ගමනක නැවතුම් පොළවල් කිහිපයක් එක පෙළට සකසා සම්පූර්ණ Road Trip Route එකක් සිතියමේ සටහන් කිරීම (උදා: *Colombo ➔ Kandy ➔ Ella ➔ Mirissa*).
* දුර (Distance in km), ගතවන කාලය, සහ එක් එක් නැවතුමට අදාළ මතකයන් කළමනාකරණය.

### 📸 3. Travel Memories & Media
* සෑම ස්ථානයකටම අදාළ ඡායාරූප සහ විස්තර සුරැකීම.
* **Pinch-to-zoom Media Viewer:** උසස් තත්ත්වයෙන් සම්පූර්ණ තිරයේ ඡායාරූප නැරඹීමේ පහසුකම.
* **Offline-First Storage:** දුර බැහැර හෝ අන්තර්ජාල සංඥා නොමැති ප්‍රදේශවලදී ද දත්ත ඔබගේ උපාංගයේ සුරක්ෂිතව තබාගැනීම.

### 🌍 4. Ceylon Community Feed & Social Sharing
* **Public vs Private Journal:** ඔබේ සංචාරක සටහන් පෞද්ගලිකව තබාගැනීම හෝ Public Community Feed එකට එක් කිරීම.
* **Social Engagement:** Posts Share කිරීම, අන් අයගේ Stories වලට Like/Comment කිරීම සහ අනෙකුත් සංචාරකයින් Follow කිරීම.
* **Save to My Journal:** වෙනත් සංචාරකයෙකු බෙදාගත් සිත්ගත් ස්ථානයක් තනි ටැප් එකකින් ඔබගේ සංචාරක ලැයිස්තුවට එක් කරගැනීම.

### 🏆 5. Explorer Ranks & Level Engine
* දිස්ත්‍රික්ක සහ පළාත් ගවේෂණය කරන ප්‍රමාණය අනුව තරාතිරම (Rank) ඉහළ යාම.
* **Achievements:** *Novice Wanderer*, *Island Explorer*, *Ceylon Legend* වැනි ගෞරවනීය Badges අත්පත් කරගැනීම.

### ☁️ 6. Automated Cloud Sync & Backup
* **Firebase Firestore Realtime Sync:** ඩිවයිස් අතර සජීවීව Stories සහ Feed දත්ත හුවමාරු වීම.
* **Google Drive Cloud Backup:** Google Sign-In මඟින් ඔබගේ පුද්ගලික Google Drive එකට දත්ත Backup කර නැවත ලබාගැනීමේ (1-Tap Restore) පහසුකම.

---

## 🛠️ Tech Stack

* **Platform:** Android (Java/Kotlin & Modern WebView)
* **Frontend:** HTML5, CSS3, JavaScript (ES6+), Jetpack Compose / Android Native Architecture
* **Mapping Engine:** OpenStreetMap (Leaflet.js / OSM SDK)
* **Database & Sync:** 
  * Local: Room Database / LocalStorage (Offline Cache)
  * Cloud: Google Firebase Firestore (Realtime Sync)
* **Authentication & Backup:** Google OAuth 2.0 / Google Drive API

---

## ⚙️ Setup & Configuration

### Prerequisites
* Android Studio (Ladybug / Iguana or later)
* JDK 17+
* Firebase Account with an active project

### Firebase & Google Services Setup
1. Clone this repository:
   ```bash
   git clone [https://github.com/dinushlakmal/CeylonSteps.git](https://github.com/dinushlakmal/CeylonSteps.git)
