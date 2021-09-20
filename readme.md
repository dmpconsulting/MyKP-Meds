[![Build Status](https://jenkins.mobilepipeline.kp.org/job/TPMG/job/TPMG/job/MyKPMedsForAndroid/job/develop/badge/icon)](https://jenkins.mobilepipeline.kp.org/job/TPMG/job/TPMG/job/MyKPMedsForAndroid/job/develop/)
![Language Java](https://img.shields.io/badge/Language-java-blue.svg)

[[Sonar Qube](https://sonarqube-bluemix.kp.org/dashboard?id=org.kp.consumer%3AMyMedsAndroid)]

## Pipeline Gradle Plugin

https://confluence-aes.kp.org/display/MDP/Pipeline+Gradle+Plugin 

# My KP Meds

For members of Kaiser Permanente (KP) in Northern California, Colorado, Hawaii, Mid-Atlantic States (Maryland, Virginia, and Washington, D.C.), Oregon, and SW Washington.Make it easy and convenient for members to take medications at the right time.
My KP Meds android application automatically imports your list of KP medications and helps you stay on track. Create reminders that work with your schedule. And when it’s time to refill, order from the phone.

## Features
- View your current KP medication
- Create medication reminders
- Set refill reminders
- Get reminders without signing in 
- Order refills
- Track medication history
- View photos of your medications to avoid errors
- Explore features with the in-app guide

## Pre - requisites
- Android Studio v3.3.0 above
- Android Build Tools v28.0.3

## Requirements
- Minimum API: 23
- Target SDK: 28

## Libraries Used
- Cardview
- Recyclerview
- Guava
- Json Jackson
- Retrofit
- App Dynamics
- Robolectric
- SQLCipher
- Volley
- Google Services
- Firebase

#### Internal Libraries
- KpSecurity
- TPMG Common

## Installation and Product Flavors

This application uses the Gradle build system.

1) Download the project by cloning this repository.
2) Once the project is compiled successfully, open the gradle toolbar and select the required gradle task to install the apk on the device for the respective environment.

  - Select install AllLowerEnvDevRelease to install all Lower environment configured apk.
  - Select install PpBetaRelease to install Beta environment configured apk.
  - Select install ProdProductionRelease to install Production environment configured apk.


## Sonarqube

To run sonarqube follow the below mentioned steps:

        1) Ensure that the Project Key & Project name is as intended in sonarqube.gradle.
        2) Run the following command on the terminal
            chmod +x gradlew
            ./gradlew lintQi1QaRelease sonarqube --stacktrace --continue
            

