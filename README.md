# eval-geolocation
INpulse internal project to help evaluate candidates

# The System to be developed

* A web server made in Kotlin on top of Ktor, used to list the nearby restaurants within a given geo-location.
* This is not a technical specification but a set of requirements. The developer's job is to decide how exactly this should be executed.

### Endpoints

* Get nearby restaurants:
  * obligatory input: latitude, longitude, number of results
  * optional inputs: maximum distance (km)
  * The restaurants found nearby
* Get restaurant details:
  * With the restaurant Id, get more info about the place;
* Add new restaurant

PS: non-happy path should return an error code and a message describing what went wrong

### Models

* Restaurant item:
  * Id
  * Name
  * Location
  * User Price Review (1-5)
  * User Quality Review (1-5)
* Restaurant detail
  * TODO





