## Description of Eval Geolocation Android app

#### General description
Develop an app that consumes the API specified by [SERVER.md] and implemented in module `:ktor-server`.

#### Screens
1. Main Screen - Consists of a map (Google Maps Library, OpenStreetMaps) with restaurants obtained
through the server API displayed as "location pins" overlayed on top of the map and a list of
resumed information on the restaurants being displayed at the bottom, as a bottom sheet or carousel. 
User can freely move the map around and the app will refresh the displayed restaurants based on the
new location and zoom level the user is observing.

2. Details Screen - When the user taps on a restaurant displayed on the bottom sheet or carousel
the app will go to this screen showing detailed information about the selected restaurant.

#### Development Guidelines
* Android app can use the client API library implemented in `:ktor-client` module.
* App views and screens must follow Google Material 3 Design System.
* App must make use of Jetpack libraries for Navigation and Lifecycle (ViewModel).
* Can (and should) use libraries for already solved problems.
* App should survive rotation (configuration) changes.

#### Evaluation
Candidate will be evaluated on code cleanliness, separation of concerns, app architecture, knowledge
of Kotlin language, knowledge of Android framework, application lifecycle, etc..

Bonus points if: (In descending order of priority but in ascending order of difficulty)
* App supports light/dark mode.
* App views are implemented using Jetpack Compose.
* App handles screen size differences.
* App handles landscape orientation.
* App offers offline-first experience.
