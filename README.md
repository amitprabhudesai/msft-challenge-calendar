# msft-challenge-calendar
Calendar and Agenda Views of the Outlook App

Modified: July 20, 2017

## Getting Stuff Done

### Latest build 
You can get the latest application builds 
[here](https://github.com/amitprabhudesai/msft-challenge-calendar/tree/master/apk) 
to take the app for a spin. The application supports API level 16 and above.

### Building from source
To build the sources, you need
- Android SDK Tools 
- Android SDK Platform Tools
- Android SDK Build Tools
- Android SDK Platform API level 25
- Android Support Repository
- Google Repository
- Java Development Kit (JDK) and Java SE Runtime Environment (JRE) 1.8
- Gradle 2.10 or later

You will need to have the following environment variables set
- `JAVA_HOME`
- `ANDROID_HOME`
- `GRADLE_HOME`

You will need the Android Platform tools location added to your path. To do so, 
add the following to your `.bash_profile`
```
# Add Android platform-tools to the path 
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

## New in this version
First alpha-builds for the Calendar app. Please view the 
[Changelog](https://github.com/amitprabhudesai/msft-challenge-calendar/blob/master/ChangeLog.md) 
for a complete list of additions, fixes, enhancements and 
open issues in the latest builds.

## Open Issues
Please see the [Issues](https://github.com/amitprabhudesai/msft-challenge-calendar/issues) 
section for open issues. A quick summary of open issues follows:
- Show empty sections for days where there are no events
- Multi-day events show up only on the first day of the event

## License
The source is provided under the Apache 2.0 License.

## About Me
Hi there! Thanks for reviewing my submission. You can learn more about 
me [here](https://github.com/amitprabhudesai/msft-challenge-calendar/blob/master/AboutMe.md).
