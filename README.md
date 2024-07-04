# Reverse Geocoding App

|                        |                            |
|------------------------|----------------------------|
| ![](images/ios_ss.png) | ![](images/android_ss.png) |

[日本語のREADME](README_ja.md)

This open-source application provides fast and efficient reverse geocoding using administrative division data published by the Geospatial Information Authority of Japan (GSI). This app operates on both iOS and Android platforms using Kotlin Multiplatform and Room.

## Features

- **Integration with Kotlin Multiplatform and Room**: Unified database handling for both iOS and Android.
- **Optimized GSI Data**: Conversion of GSI's administrative division data into a format suitable for smartphone operations, enabling fast reverse geocoding.
- **Open Source**: The application's source code is available for free use within the bounds of the license.

## Project Structure

This project consists of multiple modules managed by Gradle. The roles of each module are as follows:

### javan_administrative_divisions
This module contains the database file to be embedded in the application when executed on a smartphone. Due to the large size of the DB file, it is provided via Play Asset Delivery for Android and On-Demand Resources for iOS.

### gsi_feature_preparation
A Java project that converts administrative division data downloaded from GSI into a format usable within the app, generating the DB file.

### gsi_feature_database
A Kotlin multiplatform library module that defines the schema and DAO for the Room Database referenced by both Android and iOS.

### iOS App
The application for iOS.

### app
The application for Android.

## Build Instructions

### 1. Prepare Administrative Division Data

Download the latest administrative division data from the [National Land Numerical Information download site](https://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-N03-2024.html) and extract it. As of July 2024, the latest file name for nationwide data is `N03-20240101_GML.zip`.

Download the JCC/JCG number information provided by JARL from 7K2ABV's [Heisei All Municipalities Communication Award](https://www7a.biglobe.ne.jp/~mss7k4/ZSKTS-AWD.mcsv).

Place the downloaded files in an appropriate location on your PC and create/edit the `gsi_feature_preparation/src/main/resources/config.properties` file.

```properties
inputFilePath = /path/to/N03-20240101.geojson # Path to the geojson file included in the downloaded zip
awardFilePath = /path/to/ZSKTS-AWD.mcsv.txt # Path to the downloaded ZSKTS-AWD.mcsv file
outputDirectoryPath = /path/to/ReverseGeocoder/japan_administrative_divisions/src/main/assets # Output directory path. Absolute path to japan_administrative_divisions/src/main/assets
```

### 2. Data Conversion

Open the same directory level where the cloned directory is located in Android Studio.

Build the project and run gsi_feature_preparation/src/main/java/com/covelline/gsi_feature_preparation/Main.kt. The data will be automatically converted and the DB file will be created.

### 3. Running the App (Android)

Set the Build Variants of the app module to ossDebug and run the app.

### 4. Running the App (iOS)

Open iosApp/iosApp.xcodeproj in Xcode, set the Target to oss, and build and run the project.

# License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
