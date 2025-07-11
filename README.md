# Word Mem: A personal, handy vocabulary building app

WordMem is an Android vocabulary learning and memorization app designed to help users build their vocabulary through word lookup, personal collections, and flashcard features. Sources of word explanations include vocabulary.com and dictionary api. This could also be used as a custom learning tool with your own words and explanations. Although I initially created this to learn words for the GRE, I've also used this app to learn words from languages I'm learning using the custom input feature.

## Installation for Users

### Direct APK Installation (Recommended)

**Quick Install:**
1. Download the APK: from `./app/release/app-release.apk`
2. Install the APK by tapping on the downloaded file
3. Enable unknown sources if prompted
4. Launch WordMem from app list!

**Requirements:**
- Android 5.0 (API level 21) or higher
- ~50MB storage space
- Internet connection for online word lookup (optional)

## Features

### Word Lookup
- **Inhouse Database**: Search using a pre-built SQLite database (`all_words.db` containing about 15k words). This was developed by using words from Magoosh and Princeton GRE word lists and scraping defintions from Vocabulary.com.
- **Online Sources**: 
  - DictionaryAPI.dev (primary online source, pretty reliable)
  - Vocabulary.com (fallback with web scraping, web scraping stopped working recently due to bot checks)
- **Source Filtering**: Filter searches by specific sources:
  - Any source (default. checks inhouse database first, fetched from dictionaryapi if not found)
  - Own definitions (custom input)
  - Vocabulary.com
  - DictionaryAPI

### Word Collection
- Words are saved with their specific source information and date added
- When multiple definitions exist, uses priority order:
  1. Own definitions
  2. Vocabulary.com
  3. DictionaryAPI
- Search within your personal collection

### Flashcard System
- **Weighted Random Selection**: Words with lower scores appear more frequently
- **Performance Based Scoring**: Scoring system (0.0 - 1.0) based on user performance
- **Scoring System**: Three-button system for immediate feedback:
  - "Don't Know" (-0.2 score adjustment)
  - "Neutral" (-0.05 adjustment)
  - "Know It" (+0.15 adjustment)
- **Filtering Options**:
  - All words
  - Needs Practice (score < 0.4)
  - Fair (0.4 ≤ score < 0.6)
  - Good (0.6 ≤ score < 0.8)
  - Excellent (score ≥ 0.8)

### Word Analysis
- Statistics on learning progress, individual scores for every word
- Could add a few more insights...

### User Interface
- Idk, I'm happy about the neat and simple UI. HCI nerds may disagree~

## Technical Deets

### Built With
- **Language**: Java (but used Python to scrape and build database)
- **Database**: SQLite
- **Network**: 
  - JSoup for HTML parsing (Vocabulary.com fallback)
  - HttpURLConnection for DictionaryAPI
  - Probably could be better, my knowledge is limited
- **UI Framework**: 
  - AndroidX libraries
  - EasyFlipView for flashcard animations
  - Fragment based views

## Installation for Developers

**You need:**
- Android Studio (latest)
- Android SDK with API level 21 or higher
- Java Development Kit (JDK) 8 or higher

**Steps:**
1. **Clone the repo**

2. **Open in Android Studio**

3. **Sync and Build:**
   - Android Studio will prompt to sync Gradle files
   - Click "Sync Now" and wait for completion

4. **Run the app:**
   - Connect an Android device (API 21+) with USB Debugging or start an emulator
   - Click the "Run" button

